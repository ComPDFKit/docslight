package com.compdf.service.impl;

import com.compdf.config.ActuatorServiceManage;
import com.compdf.config.ThreadPoolConfig;
import com.compdf.entity.IdpFile;
import com.compdf.entity.IdpServer;
import com.compdf.entity.IdpService;
import com.compdf.entity.IdpTask;
import com.compdf.enums.*;
import com.compdf.exception.ComPDFKitException;
import com.compdf.pojo.TaskParamVO;
import com.compdf.service.*;
import com.compdf.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author ComPDFKit-WPH 2025/3/7 0007
 */
@Service
@Slf4j
public class ComIDPBatchServiceImpl implements ComIDPBatchService {
    private final ComIDPService idpService;
    private final IdpServiceService serviceService;
    private final IdpFileService fileService;
    private final IdpTaskService taskService;
    private static final Map<String, Boolean> taskFlag = new HashMap<>();
    private static final Map<String, ThreadPoolExecutor> taskThreadMap = new HashMap<>();
    private final JavaMailSender javaMailSender;
    @Value("${spring.mail.username}")
    private String fromEmail;
    @Value("${spring.mail.toEmail}")
    private String toEmail;

    public ComIDPBatchServiceImpl(ComIDPService idpService, IdpServiceService serviceService, IdpFileService fileService, IdpTaskService taskService, JavaMailSender javaMailSender) {
        this.idpService = idpService;
        this.serviceService = serviceService;
        this.fileService = fileService;
        this.taskService = taskService;
        this.javaMailSender = javaMailSender;
    }

    /**
     * 创建一个任务
     *
     * @param folderPath folderPath
     * @param serviceIds serviceIds
     * @return taskId
     */
    @Override
    public String createTask(String folderPath, List<String> serviceIds, String params, String type) {
        // 检查serviceIds是否没有任务执行
        List<IdpService> idpServices = serviceService.selectByIds(serviceIds);
        if (CollectionUtils.isEmpty(idpServices)) {
            throw new ComPDFKitException(ErrorInfoEnum.THE_ACTUATOR_DOES_NOT_EXIST);
        }
        List<IdpService> collect = idpServices.stream().filter(s -> (!s.getStatus().equals(ServiceStatusEnum.RUNNING.getValue()))).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(collect)) {
            throw new ComPDFKitException(ErrorInfoEnum.THE_ACTUATOR_DOES_NOT_EXIST);
        }
        // 创建任务
        IdpTask task = new IdpTask();
        task.setFolderPath(folderPath);
        task.setServiceIds(JsonUtils.getJsonString(serviceIds));
        task.setStatus(TaskStatusEnum.CREATED.getValue());
        task.setParams(JsonUtils.getJsonString(new TaskParamVO(type, params)));
        taskService.insert(task);
        return task.getId();
    }

    @Override
    public String executeTask(String taskId) {
        IdpTask task = taskService.selectById(taskId);
        if (Objects.isNull(task)) {
            throw new ComPDFKitException(ErrorInfoEnum.TASK_NOT_EXIST);
        }
        if (task.getStatus().equals(TaskStatusEnum.CREATED.getValue())) {
            task.setStatus(TaskStatusEnum.PROCESSING.getValue());
            taskService.updateById(task);
        } else {
            throw new ComPDFKitException(ErrorInfoEnum.TASK_IS_RUNNING);
        }
        // 线程池获取线程处理文件夹文件初始化操作
        ThreadPoolConfig.TASK_INIT_POOL.execute(() -> this.initTaskFiles(taskId));
        return task.getId();
    }

    @Override
    public void initTaskFiles(String taskId) {
        IdpTask task = taskService.selectById(taskId);
        String folderPath = task.getFolderPath();
        TaskParamVO taskParamVO = JsonUtils.jsonStringToBean(task.getParams(), TaskParamVO.class);
        List<Path> filePaths = new ArrayList<>();
        try(Stream<Path> walk = Files.walk(Paths.get(folderPath))) {
            walk.filter(Files::isRegularFile).forEach(filePaths::add);
        } catch (IOException e) {
            log.error(e.getMessage(),e);
        }
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        List<List<Path>> batches = new ArrayList<>();
        for (int i = 0; i < filePaths.size(); i += 5000) {
            batches.add(filePaths.subList(i, Math.min(i + 5000, filePaths.size())));
        }

        for (List<Path> batch : batches) {
            executorService.submit(() -> {
                List<IdpFile> idpFiles = batch.stream().map(file -> {
                    IdpFile idpFile = new IdpFile();
                    idpFile.setTaskId(task.getId());
                    idpFile.setFileName(file.toFile().getName());
                    idpFile.setStatus(TaskTypeEnum.EXTRACTION.name().equals(taskParamVO.getType()) ? FileStatusEnum.PENDING_EXTRACTION.getValue() : FileStatusEnum.CREATED.getValue());
                    idpFile.setFilePath(file.toString());
                    idpFile.setTaskType(taskParamVO.getType());
                    idpFile.setParameter(taskParamVO.getParams());
                    // 设置idpFile属性...
                    return idpFile;
                }).collect(Collectors.toList());
                fileService.saveBatch(idpFiles, 5000);
            });
        }

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.MINUTES)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // 初始化任务执行器列表
        List<IdpService> serviceList = serviceService.selectByIds(JsonUtils.jsonStringToList(task.getServiceIds(), String.class));
        List<String> urls = serviceList.stream().map(IdpService::getUrl).collect(Collectors.toList());
        ActuatorServiceManage.initTaskActuatorServiceMap(new HashMap<String, List<String>>() {{
            put(taskId, urls);
        }});

        taskStart(taskId);
    }

    @Override
    public void taskStart(String taskId) {
        taskFlag.put(taskId, true);
        taskThreadMap.put(taskId, createTaskThread(taskId));
//        taskThreadExecute(taskId);
    }

    @Override
    public void taskRestart(String taskId) {
        IdpTask task = taskService.selectById(taskId);
        if (Objects.isNull(task)) {
            throw new ComPDFKitException(ErrorInfoEnum.TASK_NOT_EXIST);
        }
        taskFlag.put(taskId, true);
        ThreadPoolExecutor taskThread = taskThreadMap.get(taskId);
        if (Objects.isNull(taskThread)) {
            taskThread = createTaskThread(taskId);
            taskThreadMap.put(taskId, taskThread);
        }
//        taskThreadExecute(taskId);
        task.setStatus(TaskStatusEnum.PROCESSING.getValue());
        taskService.updateById(task);
    }



    @Override
    public void taskStop(String taskId) {
        IdpTask task = taskService.selectById(taskId);
        if (Objects.isNull(task)) {
            throw new ComPDFKitException(ErrorInfoEnum.TASK_NOT_EXIST);
        }
        taskFlag.put(taskId, false);
        taskThreadMap.get(taskId).shutdown();
        taskThreadMap.remove(taskId);
        taskFlag.remove(taskId);
        task.setStatus(TaskStatusEnum.CANCEL.getValue());
        taskService.updateById(task);
    }

    private ThreadPoolExecutor createTaskThread(String taskId) {
        IdpTask task = taskService.getById(taskId);
        int size = JsonUtils.jsonStringToList(task.getServiceIds(), String.class).size();
        return new ThreadPoolExecutor(
                size,
                size,
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
//        return new Thread(() -> {
//            while (true)
//                if (taskFlag.get(taskId)) {
//                    log.info("开始执行任务ID：{}", taskId);
//                    try {
//                        this.taskHandle(taskId);
//                    } catch (Exception e) {
//                        log.error("任务执行失败", e);
//                    }
//                } else {
//                    log.info("停止执行任务ID：{}", taskId);
//                    LockSupport.park();
//                }
//        });
    }


    @Scheduled(cron = "0 0/20 * * * ?")
    public void taskHealthMonitor() {
        // 查询当前是否有执行中任务
        List<IdpTask> records = taskService.selectByStatus(TaskStatusEnum.PROCESSING);
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        for (IdpTask task : records) {
            Long successCount = fileService.selectCountByTaskIdAndStatus(task.getId(), FileStatusEnum.SUCCESS, 20)
                    + fileService.selectCountByTaskIdAndStatus(task.getId(), FileStatusEnum.EXTRACTION_SUCCESS, 20);
            Long FailCount = fileService.selectCountByTaskIdAndStatus(task.getId(), FileStatusEnum.FAIL, 20)
                    + fileService.selectCountByTaskIdAndStatus(task.getId(), FileStatusEnum.EXTRACTION_FAILED, 20);
            if (successCount + FailCount < 1) {
                // 服务没有执行当前任务，需要重启服务
                this.taskRestart(task.getId());
                // 报警通知
//                sendErrorEmail(service, server);
            }
        }

    }

    private void sendErrorEmail(IdpService service, IdpServer server) {
        try {
            // 报警通知
            MimeMessage message = javaMailSender.createMimeMessage();
            System.getProperties().setProperty("mail.mime.splitlongparameters", "false");
            //第2个参数:是否允许添加多部件
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Server Warning");
            helper.setText(String.format("An exception occurred in %S in current server:%S", server.getName(), service.getName()));
            javaMailSender.send(message);
        } catch (MessagingException e) {
            log.error("email send Error,send content: " + String.format("An exception occurred in %S in current server:%S", server.getName(), service.getName()), e);
        }
    }

}
