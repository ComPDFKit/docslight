package com.compdf.pojo;

import com.compdf.enums.TaskTypeEnum;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @author ComPDFKit-WPH 2026/1/26
 */
@Data
public class FileUploadPojo {

    private MultipartFile file;
    //    private String taskId;
    private Integer order;
    private TaskTypeEnum taskType;

    private String groupTemplateId;

    private String groupId;

    /**
     * 来源
     */
    private String source;

    private Map<?, ?> fileInfo;

}
