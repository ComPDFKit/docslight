package com.compdf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.compdf.entity.IdpTask;
import com.compdf.enums.ErrorInfoEnum;
import com.compdf.enums.TaskStatusEnum;
import com.compdf.enums.TaskTypeEnum;
import com.compdf.exception.ComPDFKitException;
import com.compdf.mapper.IdpTaskMapper;
import com.compdf.pojo.FileParameterDTO;
import com.compdf.service.IdpFileService;
import com.compdf.service.IdpTaskService;
import com.compdf.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author ComPDFKit-WPH 2024-09-11
 */
@Service
@RequiredArgsConstructor
public class IdpTaskServiceImpl extends ServiceImpl<IdpTaskMapper, IdpTask> implements IdpTaskService {

//    @Autowired
//    @Lazy
    private IdpFileService idpFileService;
    @Autowired
    public void setIdpFileService(@Lazy IdpFileService idpFileService) {
        this.idpFileService = idpFileService;
    }
    @Override
    public void updateStatusById(String taskId, TaskStatusEnum taskStatus) {
        IdpTask task = new IdpTask();
        task.setId(taskId);
        task.setStatus(taskStatus.getValue());
        this.baseMapper.updateById(task);
    }

    @Override
    public IdpTask selectById(String taskId) {
        return this.baseMapper.selectById(taskId);
    }

    @Override
    public void insert(IdpTask task) {
        this.baseMapper.insert(task);
    }

    @Override
    public List<IdpTask> selectByStatus(TaskStatusEnum processing) {
        return this.baseMapper.selectList(new LambdaQueryWrapper<IdpTask>().eq(IdpTask::getStatus, processing.getValue()));
    }

    @Override
    public IdpTask createTask(String taskType, String userId) {
        IdpTask task = new IdpTask();
        if (Objects.isNull(TaskTypeEnum.getEnumByType(taskType))) {
            throw new ComPDFKitException(ErrorInfoEnum.TASK_TYPE_ERROR);
        }
        task.setStatus(TaskStatusEnum.CREATED.getValue());
        task.setUserId(userId);
        task.setType(taskType);
        this.baseMapper.insert(task);
        return task;
    }

    @Override
    public String taskStart(String taskId, FileParameterDTO fileParameter) {
        IdpTask idpTask = this.selectById(taskId);
        if (Objects.isNull(idpTask)) {
            throw new ComPDFKitException(ErrorInfoEnum.TASK_NOT_EXIST);
        }
        // 校验任务状态
        if (!Objects.equals(idpTask.getStatus(), TaskStatusEnum.CREATED.getValue())) {
            throw new ComPDFKitException(ErrorInfoEnum.TASK_STATUS_ERROR);
        }
        // 校验参数是否合理
        // 修改任务和任务中所有文件中的状态和文件参数
        idpTask.setParams(JsonUtils.getJsonString(fileParameter));
//        idpTask.setStatus(TaskStatusEnum.PROCESSING.getValue());
        this.updateById(idpTask);
        TaskTypeEnum enumByType = TaskTypeEnum.getEnumByType(idpTask.getType());
        idpFileService.startFile(taskId, fileParameter, enumByType);
        return taskId;
    }

    @Override
    public IPage<IdpTask> getTaskList(String userId, int pageNum, int pageSize) {
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        return this.baseMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<IdpTask>().eq(IdpTask::getUserId, userId).orderByDesc(IdpTask::getCreateDate));
    }

}
