package com.compdf.pojo;

import lombok.Data;

import java.util.List;

/**
 * @author ComPDFKit-WPH 2025/6/3 星期二
 */
@Data
public class ContentData {
    private String base64;
//    private String path;
    private List<Integer> region;
}
