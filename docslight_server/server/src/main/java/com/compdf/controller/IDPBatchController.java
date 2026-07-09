package com.compdf.controller;

import com.compdf.config.base.R;
import com.compdf.service.ComIDPBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author ComPDFKit-WPH 2025/2/24 0024
 */
@RestController
@RequestMapping("idp")
@RequiredArgsConstructor
public class IDPBatchController {



    private final ComIDPBatchService batchService;

    @PostMapping("batch/create")
    public R<String> createTask(@RequestParam("folderPath") String folderPath,
                                @RequestParam("serviceIds") List<String> serviceIds,
                                @RequestParam("params") String params,
                                @RequestParam("type") String type) {
        return R.ok(batchService.createTask(folderPath, serviceIds, params,type));
    }

    @PostMapping("batch/execute")
    public R<String> executeTask(String taskId) {
        return R.ok(batchService.executeTask(taskId));
    }

    @PostMapping("batch/restart")
    public R<Void> restartTask(String taskId) {
        batchService.taskRestart(taskId);
        return R.ok();
    }

    @PostMapping("batch/stop")
    public void stopTask(String taskId) {
        batchService.taskStop(taskId);
    }

}
