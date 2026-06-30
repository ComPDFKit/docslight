package com.compdf.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ComPDFKit-WPH 2025/6/20 星期五
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FileParameterDTO extends ParseOptionsDTO{
    private String taskId;
    private List<String> keys = new ArrayList<>();
    private List<String> keysDescribe = new ArrayList<>();
    private List<String> tableHandles = new ArrayList<>();
    private List<String> tableHandlesDescribe = new ArrayList<>();
    private List<Integer> pages = new ArrayList<>();
    /**
     * 解析类型
     * all, text, image, table
     */
    private String resolveType;

    /**
     * 提取图片类型，页面PNG 和 页面内图片对象 和 前两者一起 <br>
     * page_png, page_image_object, all
     */
    private String imageType;

    /**
     * 输出文本类型
     * md, txt, json, excel, csv, isOnlyImage
     */
    private String outType;

    private Boolean enableOCR = false;

    private String ocrLanguage;

    private String pdfPwd;

    private Boolean isBilk = false;

    private String user_info;
}
