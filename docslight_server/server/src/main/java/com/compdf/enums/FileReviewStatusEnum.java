package com.compdf.enums;

import lombok.Getter;

/**
 * @author ComPDFKit-WPH 2024/9/13 0013
 */
@Getter
public enum FileReviewStatusEnum {
    NOT_CONFIRMED(0),
    CONFIRMED(1),
    ;

    private final Integer value;

    FileReviewStatusEnum(Integer value) {
        this.value = value;
    }

    public static FileReviewStatusEnum getEnumByType(Integer type) {
        for (FileReviewStatusEnum taskTypeEnum : FileReviewStatusEnum.values()) {
            if (taskTypeEnum.getValue().equals(type)) {
                return taskTypeEnum;
            }
        }
        return FileReviewStatusEnum.NOT_CONFIRMED;
    }

}
