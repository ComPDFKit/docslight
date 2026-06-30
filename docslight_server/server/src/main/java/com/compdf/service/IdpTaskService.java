package com.compdf.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.compdf.entity.IdpTask;
import com.compdf.enums.TaskStatusEnum;
import com.compdf.pojo.FileParameterDTO;

import java.util.List;

/**
 * @author ComPDFKit-WPH 2024-09-11
 */
public interface IdpTaskService extends IService<IdpTask> {
    void updateStatusById(String taskId, TaskStatusEnum taskStatus);

    IdpTask selectById(String taskId);

    void insert(IdpTask task);

    List<IdpTask> selectByStatus(TaskStatusEnum processing);

    /**
     * 创建批量执行任务
     * @param taskType 任务类型 [抽取|解析]，[Extract | RESOLVE]
     * @param userId 用户ID
     * @return 任务
     */
    IdpTask createTask(String taskType, String userId);

    /**
     * 批量执行任务
     * @param taskId 任务ID
     * @param fileParameter 文件参数
     * @return 任务ID
     */
    String taskStart(String taskId, FileParameterDTO fileParameter);

    IPage<IdpTask> getTaskList(String userId, int pageNum, int pageSize);

//    IdpTask selectById(String taskId);
}