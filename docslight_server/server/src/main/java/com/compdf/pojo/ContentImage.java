package com.compdf.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author ComPDFKit-WPH 2025/6/6 星期五
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ContentImage extends Content {

    private List<Integer> size;
    private ContentData data;

}
