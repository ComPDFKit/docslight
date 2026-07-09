package com.compdf.scheduled;

import com.compdf.config.OssFileClient;
import com.compdf.properties.ComPDFKitProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * @author ComPDFKit-WPH 2025/10/13 星期一
 */
@Component
@Slf4j
public class ComIDPJob {

    private final OssFileClient ossFileClient;
    private final ComPDFKitProperties properties;

    public ComIDPJob(OssFileClient ossFileClient, ComPDFKitProperties properties) {
        this.ossFileClient = ossFileClient;
        this.properties = properties;
    }

    /**
     * 定时任务
     * <p>
     * 抽取文件上传
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void extractFileUpload() throws IOException {
        File[] files = new File(properties.getTmpPath()).listFiles(file -> file.getName().endsWith(".pdf")
                || file.getName().endsWith(".png")
                || file.getName().endsWith(".jpg")
                || file.getName().endsWith(".jpeg"));
        for (File file : files) {
            ossFileClient.upload(file);
            file.delete();
        }
    }

}
