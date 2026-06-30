package com.compdf.entity;

import com.compdf.pojo.ExtractTemplateDTO;
import com.compdf.pojo.ExtractTemplateV2DTO;
import lombok.*;

import java.io.File;
import java.io.InputStream;

/**
 * @author ComPDFKit-WPH 2025/9/18 星期四
 */

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataExtractPojo {

    private String rustFsId;
    private ExtractTemplateDTO extractTemplateDTO;
    private ExtractTemplateV2DTO extractTemplateV2DTO;
    private String taskId;
    private File file;
    private Boolean isV2;

    private InputStream inputStream;

}
