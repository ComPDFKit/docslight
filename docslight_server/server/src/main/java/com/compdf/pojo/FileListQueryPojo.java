package com.compdf.pojo;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author ComPDFKit-WPH 2026/1/26
 */
@Data
public class FileListQueryPojo {

    private String taskType;

    private Integer page;

    private Integer pageSize;

    private String fileName;

    private String groupTemplateId;
    private String groupId;

    private List<Integer> status;

    private List<Integer> reviewStatus;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;

}
