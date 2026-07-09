package com.compdf.enums;

import lombok.Getter;

/**
 * @author ComPDFKit-WPH 2026/2/5
 */
@Getter
public enum LogTypeEnum {

    LAYOUT("PARSE", "启动解析任务"),
    EXTRACT("EXTRACT", "启动抽取任务"),
    CONFIRM("CONFIRM", "确认抽取结果"),

    ;

    private final String value;
    private final String description;

    LogTypeEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

}
