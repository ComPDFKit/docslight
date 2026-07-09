package com.compdf.enums;

/**
 * @author ComPDFKit-WPH 2024/9/13 0013
 * [EXTRACT | RESOLVE]
 */
public enum TaskTypeEnum {
    SPLIT,
    EXTRACTION,
    TEST_EXTRACTION,
    LAYOUT;

    public static TaskTypeEnum getEnumByType(String type) {
        for (TaskTypeEnum value : values()) {
            if (value.name().equals(type)) {
                return value;
            }
        }
        return EXTRACTION;
    }
}
