package com.compdf.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author ComPDFKit-WPH 2025/6/6 星期五
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class StructuredTextblock extends Structured{
    private List<Integer> content;
    private String text;
    private Integer outline_level;
    private String sub_type;

}
