package com.compdf.enums;

/**
 * @author ComPDFKit-WPH 2025/2/28 0028
 */
public enum ServiceStatusEnum {
    RUNNING(1),
    STOP(2),
    BEING_USED(3)
    ;
    private final Integer value;

    ServiceStatusEnum(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    public static ServiceStatusEnum getEnumByType(Integer type) {
        for (ServiceStatusEnum typeEnum : ServiceStatusEnum.values()) {
            if (typeEnum.getValue().equals(type)) {
                return typeEnum;
            }
        }
        return ServiceStatusEnum.RUNNING;
    }

}
