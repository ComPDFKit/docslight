package com.compdf.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.compdf.config.ActuatorServiceManage;
import com.compdf.entity.IdpService;
import com.compdf.enums.ServiceStatusEnum;
import com.compdf.enums.ServiceTypeEnum;
import com.compdf.mapper.IdpServiceMapper;
import com.compdf.properties.ComPDFKitProperties;
import com.compdf.service.IdpServiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ComPDFKit-WPH 2024-09-11
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IdpServiceServiceImpl extends ServiceImpl<IdpServiceMapper, IdpService> implements IdpServiceService {

    @Value("${server.port}")
    private String port;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUserName;

    @Value("${spring.datasource.password}")
    private String dbUserPwd;

    private final ComPDFKitProperties properties;



    public static String getDBUrl(String url) {
        String regex = "jdbc:mysql://([^/]+/[^?]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            System.out.println("No match found");
            return null;
        }
    }

    @Override
    public IPage<IdpService> page(IdpService query) {
        return this.page(new Page<>(query.getPage(), query.getSize()), Wrappers.query(query));
    }

//    @Override
//    public String createConvertContainer(ConvertContainerInfoDTO containerInfoDTO) {
//        IdpService documentAIService = this.baseMapper.selectOne(new LambdaQueryWrapper<IdpService>()
//                .eq(IdpService::getServerId, containerInfoDTO.getServerId())
//                .eq(IdpService::getType, 1) // TODO 定义枚举值
//                .eq(IdpService::getStatus, 1));
//        // 查询服务器6+
//        IdpServer server = serverService.selectById(containerInfoDTO.getServerId());
//        SshUtil sshClient = SshUtil.getRemoteClient(new SshUtil.SshHost(server.getIp(), server.getUserName(), server.getUserPwd(), server.getPort()));
//
//        if (Objects.isNull(documentAIService)) {
//            sshClient.exceCommond("docker network create kdancn");
//            // 插入数据
//            IdpService documentAIServiceNew = new IdpService();
//            documentAIServiceNew.setConfig(JSONUtil.toJsonStr(containerInfoDTO));
//            documentAIServiceNew.setServerId(containerInfoDTO.getServerId());
//            documentAIServiceNew.setName("compdfkit-documentai");
//            documentAIServiceNew.setType(1);
//            documentAIServiceNew.setStatus(0);
//            this.save(documentAIServiceNew);
//            // 创建DocumentAI容器
//            StringBuilder GPU_ID = new StringBuilder();
//            for (int i = 0; i < containerInfoDTO.getGpuID().size(); i++) {
//                if (i < containerInfoDTO.getGpuID().size() - 1) {
//                    GPU_ID.append(containerInfoDTO.getGpuID().get(i)).append(",");
//                }else
//                    GPU_ID.append(containerInfoDTO.getGpuID().get(i));
//            }
//            StringBuilder documentAICreateCmd = new StringBuilder();
//            documentAICreateCmd.append("docker run ").append(containerInfoDTO.getIsGPU() ? "--gpus all " : "")
//                    .append("--restart=always --network=kdancn --name compdfkit-documentai")
//                    .append(" -p ").append("7100:7100")
//                    .append(" -e USE_GPU=").append("\"").append(containerInfoDTO.getIsGPU()).append("\"")
//                    .append(" -e GPU_ID=").append(GPU_ID)
//                    .append(" -e MODEL_COUNT=").append(containerInfoDTO.getModelCount());
//            for (String workDirectory : containerInfoDTO.getWorkingDirectoryList()) {
//                documentAICreateCmd.append(" -v ").append(workDirectory).append(":").append(workDirectory);
//            }
//            documentAICreateCmd.append(" -d ").append(properties.getDocumentAIImage());
//            log.info("DocumentAI创建命令:{}", documentAICreateCmd.toString());
//            log.info("DocumentAI创建结果:{}", sshClient.exceCommond(documentAICreateCmd.toString()));
//            documentAIServiceNew.setStatus(1);
//            this.updateById(documentAIServiceNew);
//        }
//        IdpService convertService = new IdpService();
//        convertService.setConfig(JSONUtil.toJsonStr(containerInfoDTO));
//        convertService.setServerId(containerInfoDTO.getServerId());
//        convertService.setType(0);
//        convertService.setStatus(0);
//        convertService.setPort(containerInfoDTO.getPort());
//        this.save(convertService);
//
//        String convertName = "compdfkit-convert" + convertService.getId();
//        convertService.setName(convertName);
//        StringBuilder convertCreateCmd = new StringBuilder();
//        // --restart=always
//        convertCreateCmd.append("docker run --network=kdancn --name ").append(convertName)
//                .append(" -p ").append(containerInfoDTO.getPort()).append(":7000")
//                .append(" -e LICENSE=").append(license)
//                .append(" -e LICENSE_KEY=").append(licenseKey)
//                .append(" -e DB_URL=").append(getDBUrl(dbUrl))
//                .append(" -e DB_USERNAME=").append(dbUserName)
//                .append(" -e DB_PASSWORD=").append(dbUserPwd)
//                .append(" -e SERVICE_ID=").append(convertService.getId())
//                .append(" -e TMP_PATH=").append(containerInfoDTO.getWorkingDirectoryList().get(0))
//                .append(" -e DOCUMENT_AI_HOST=").append("http://compdfkit-documentai:7100")
//                .append(" -e CONVERT_TIMEOUT=").append(containerInfoDTO.getConvertTimeOut());
//        for (String workDirectory : containerInfoDTO.getWorkingDirectoryList()) {
//            convertCreateCmd.append(" -v ").append(workDirectory).append(":").append(workDirectory);
//        }
//        convertCreateCmd.append(" -d ").append(properties.getConvertImage());
//        log.info("convert创建命令:{}", convertCreateCmd.toString());
//        List<String> exceCommond = sshClient.exceCommond(convertCreateCmd.toString());
//        exceCommond.forEach(log::info);
//        exceCommond.forEach(cmd -> {
//            if (cmd.toLowerCase().contains("error") || cmd.toLowerCase().contains("ero")) {
//                throw new RuntimeException("创建失败," + cmd);
//            }
//        });
//
//        convertService.setStatus(1);
//        this.updateById(convertService);
//        return convertService.getId();
//    }

    @Override
    public List<IdpService> selectByIds(List<String> serviceIds) {
        return this.baseMapper.selectList(new LambdaQueryWrapper<IdpService>()
                .eq(IdpService::getStatus, 1)
                .in(IdpService::getId, serviceIds));
//        return null;
    }

    @Override
    public void restart(IdpService service, String taskId) {
//        String name = service.getName();
//        IdpServer server = serverService.selectById(service.getServerId());
//        SshUtil sshClient = SshUtil.getRemoteClient(new SshUtil.SshHost(server.getIp(), server.getUserName(), server.getUserPwd(), server.getPort()));
//        sshClient.exceCommond("docker restart " + name).forEach(log::info);
//        // 测试是否启动成功
//
//        // 重新分配工作任务
//        taskService.taskRestart(taskId ,service.getId());
    }

    @Override
    public void initService(String serverId) {
        IdpService idpService = this.selectByServiceIdAndTypeAndPort(serverId, ServiceTypeEnum.JAVA.getValue(), Integer.valueOf(port));
        if (Objects.isNull(idpService)) {
            IdpService javaService =  new IdpService();
            javaService.setServerId(serverId);
            javaService.setPort(Integer.valueOf(port));
            javaService.setConfig(JSONUtil.toJsonStr(properties));
            javaService.setType(ServiceTypeEnum.JAVA.getValue());
            javaService.setStatus(ServiceStatusEnum.RUNNING.getValue());
            javaService.setName(UUID.randomUUID().toString());
            this.save(javaService);
        } else {
            switch (ServiceStatusEnum.getEnumByType(idpService.getStatus())) {
                case RUNNING:
//                    log.info("java服务已经启动");
                    idpService.setConfig(JSONUtil.toJsonStr(properties));
                    this.updateById(idpService);
                    break;
                case STOP:
                    idpService.setStatus(ServiceStatusEnum.RUNNING.getValue());
                    idpService.setConfig(JSONUtil.toJsonStr(properties));
                    this.updateById(idpService);
                    break;
                case BEING_USED:
                    // 不处理
                    break;
            }
        }
        List<String> lowLevelEngine = properties.getLowLevelEngine();
        for (String levelEngine : lowLevelEngine) {
            String[] split = levelEngine.split(":");
            IdpService actuatorService = new IdpService();
            actuatorService.setServerId(serverId);
            actuatorService.setPort(Integer.valueOf(split[1]));
            actuatorService.setType(ServiceTypeEnum.RD.getValue());
            actuatorService.setStatus(ServiceStatusEnum.RUNNING.getValue());
            actuatorService.setName(split[0]);
            actuatorService.setUrl(levelEngine);
            this.registerActuatorService(actuatorService);
        }
        ActuatorServiceManage.initApiActuatorServiceList(lowLevelEngine);
    }

    @Override
    public IdpService selectByServiceIdAndTypeAndPort(String serverId, Integer type, Integer port) {
        return this.baseMapper.selectOne(new LambdaQueryWrapper<IdpService>()
                .eq(IdpService::getServerId, serverId)
                .eq(IdpService::getType, type).eq(IdpService::getPort, port));
    }

    /**
     * 注册执行器服务
     *
     * @param service 执行器服务
     */
    public void registerActuatorService(IdpService service) {
        IdpService actuatorService = this.baseMapper.selectOne(new LambdaQueryWrapper<IdpService>()
                .eq(IdpService::getServerId, service.getServerId())
                .eq(IdpService::getType, service.getType())
                .eq(IdpService::getPort, service.getPort())
                .eq(IdpService::getUrl, service.getUrl())
                .eq(IdpService::getName, service.getName()));
        if (Objects.isNull(actuatorService)) {
            this.baseMapper.insert(service);
        } else {
            // 如果不是使用中，则更新为运行中，使用中状态用于处理批量任务监管器重新启动分配
            if (!actuatorService.getStatus().equals(ServiceStatusEnum.BEING_USED.getValue())){
                actuatorService.setStatus(ServiceStatusEnum.RUNNING.getValue());
                this.baseMapper.updateById(actuatorService);
            }
        }
    }

}
