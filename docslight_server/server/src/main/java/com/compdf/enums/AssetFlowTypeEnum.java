package com.compdf.enums;

import lombok.Getter;

/**
 * 资产流水类型
 */
public enum AssetFlowTypeEnum {
    RESERVE("RESERVE"),
    COMMIT("COMMIT"),
    RELEASE("RELEASE"),
    DEDUCT("DEDUCT"),
    ALLOCATE("ALLOCATE"),
    ADJUST("ADJUST");

    @Getter
    private final String value;

    AssetFlowTypeEnum(String value) {
        this.value = value;
    }
}

