package com.compdf.pojo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author ComPDFKit-WPH 2026/1/28
 */
@Data
@ToString
public class FileExportDTO {

    private List<String> fileIds;

    /**
     * 导出格式：JSON、MD、TXT、DOCX、PDF、EXCEL、CSV
     */
    private String exportFormat;

    private Boolean isCompress = false;
}
