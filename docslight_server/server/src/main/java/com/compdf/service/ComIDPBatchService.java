package com.compdf.service;

import java.util.List;

/**
 * @author ComPDFKit-WPH 2025/3/7 0007
 */
public interface ComIDPBatchService {
    String createTask(String folderPath, List<String> serviceIds, String params,String type);

    String executeTask(String taskId);

    void initTaskFiles(String taskId);

    void taskStart(String taskId);

    void taskRestart(String taskId);

    void taskStop(String taskId);
}
