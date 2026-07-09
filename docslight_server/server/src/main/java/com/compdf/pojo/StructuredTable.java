package com.compdf.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author ComPDFKit-WPH 2025/6/6 星期五
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class StructuredTable extends Structured{
    private Integer cols;
    private Object caption_id;
    private Integer rows;
    private Integer outline_level;
    private List<Integer> columns_width;
    private String parse_type;
    private String text;
    private String sub_type;
    private List<Integer> rows_height;

}
