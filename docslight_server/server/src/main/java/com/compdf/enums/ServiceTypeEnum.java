package com.compdf.enums;

/**
 * @author ComPDFKit-WPH 2025/2/28 0028
 */
public enum ServiceTypeEnum {
    JAVA(1),
    RD(2),
    ;
    private final Integer value;

    ServiceTypeEnum(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    public static ServiceTypeEnum getEnumByType(Integer type) {
        for (ServiceTypeEnum typeEnum : ServiceTypeEnum.values()) {
            if (typeEnum.getValue().equals(type)) {
                return typeEnum;
            }
        }
        return ServiceTypeEnum.JAVA;
    }

}
