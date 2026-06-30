package com.compdf.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ComPDFKit-WPH 2025/10/24 星期五
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class TemplateBodyDTO extends FileParameterDTO{

    private String name;
    private String templateId;
//    private List<String> keys = new ArrayList<>();
//    private List<String> keysDescribe = new ArrayList<>();
//    private List<String> tableHandles = new ArrayList<>();
//    private List<String> tableHandlesDescribe = new ArrayList<>();

}
