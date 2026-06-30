package com.compdf.enums;

import lombok.Getter;

/**
 * 模板状态：0 未启用，1 启用，2 删除
 */
@Getter
public enum TemplateStatusEnum {

    DISABLED(0),
    ENABLED(1),
    DELETED(2);

    private final int value;

    TemplateStatusEnum(int value) {
        this.value = value;
    }
}
