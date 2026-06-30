package com.compdf.enums;

/**
 * @author ComPDFKit-WPH 2024/9/13 0013
 */
public enum TaskStatusEnum {
    CREATED(0),
    PROCESSING(1),
    SUCCESS(2),
    FAIL(3),
    CANCEL(4),

    ;
    private final Integer value;

    TaskStatusEnum(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    public static TaskStatusEnum getEnumByType(Integer type) {
        for (TaskStatusEnum taskTypeEnum : TaskStatusEnum.values()) {
            if (taskTypeEnum.getValue().equals(type)) {
                return taskTypeEnum;
            }
        }
        return TaskStatusEnum.CREATED;
    }

}
