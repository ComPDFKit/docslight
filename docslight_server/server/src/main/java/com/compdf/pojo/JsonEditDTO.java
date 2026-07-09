package com.compdf.pojo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author ComPDFKit-WPH 2025/8/8 星期五
 */
@Data
@ToString
public class JsonEditDTO {

    private String fileId;

    private String language;

    private List<EditDTO> edits;

}
