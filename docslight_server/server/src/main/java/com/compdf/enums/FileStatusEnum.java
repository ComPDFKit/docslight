package com.compdf.enums;

/**
 * @author ComPDFKit-WPH 2024/9/13 0013
 */
public enum FileStatusEnum {
    // 通用/解析复用状态
    CREATED(0),
    PROCESSING(1),
    SUCCESS(2),
    FAIL(3),
    DELETE(4),
    PAUSE(5),
    // 文档抽取新增状态（v2.6.1 状态机细化）
    PENDING_CLASSIFICATION(6),
    CLASSIFYING(7),
    CLASSIFICATION_FAILED(8),
    PENDING_EXTRACTION(9),
    EXTRACTING(10),
    EXTRACTION_SUCCESS(11),
    EXTRACTION_FAILED(12);

    private final Integer value;

    FileStatusEnum(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    public static FileStatusEnum getEnumByType(Integer type) {
        for (FileStatusEnum taskTypeEnum : FileStatusEnum.values()) {
            if (taskTypeEnum.getValue().equals(type)) {
                return taskTypeEnum;
            }
        }
        return FileStatusEnum.CREATED;
    }

    /**
     * 判断是否为成功终态（兼容旧 SUCCESS 与新增 EXTRACTION_SUCCESS）
     */
    public static boolean isSuccessStatus(Integer status) {
        return SUCCESS.value.equals(status) || EXTRACTION_SUCCESS.value.equals(status);
    }

    /**
     * 判断是否为失败终态（兼容旧 FAIL 与新增 EXTRACTION_FAILED / CLASSIFICATION_FAILED）
     */
    public static boolean isFailedStatus(Integer status) {
        return FAIL.value.equals(status) || EXTRACTION_FAILED.value.equals(status) || CLASSIFICATION_FAILED.value.equals(status);
    }

    /**
     * 判断是否为抽取类处理中状态
     */
    public static boolean isExtractProcessingStatus(Integer status) {
        return EXTRACTING.value.equals(status) || CLASSIFYING.value.equals(status);
    }

}
