package com.compdf.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author ComPDFKit-WPH 2025/6/6 星期五
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class StructuredFooter extends Structured{
    private List<Blocks> blocks;

}
