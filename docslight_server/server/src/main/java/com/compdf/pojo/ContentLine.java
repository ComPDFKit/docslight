package com.compdf.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author ComPDFKit-WPH 2025/6/6 星期五
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ContentLine extends Content {

    private String text;
    private double score;

}
