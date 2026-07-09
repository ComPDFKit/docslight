package com.compdf.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author ComPDFKit-WPH 2025/6/23 星期一
 */
@Data
public class IdpFileInfoDTO {

    private String fileId;

    private Integer status;

    private String fileDownUrl;

    private String fileName;

    private Integer pageCount;

    private LocalDateTime uploadTime;

    private Integer reviewStatus;

    private String resultDownUrl;

    private String failureReason;

    private String failureCode;

    private FileScheduleDTO fileSchedule;

    private String groupTemplateId;

    private String templateName;

}
