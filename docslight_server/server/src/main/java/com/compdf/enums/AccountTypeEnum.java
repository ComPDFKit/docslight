package com.compdf.enums;

import lombok.Getter;

/**
 * 资产账户类型
 */
public enum AccountTypeEnum {
    TRIAL("TRIAL", "试用"),
    FORMAL("FORMAL", "正式"),
    UNKNOWN("UNKNOWN", "未知");

    @Getter
    private final String value;
    @Getter
    private final String name;

    AccountTypeEnum(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public static AccountTypeEnum of(String value) {
        if (value == null) {
            return UNKNOWN;
        }
        for (AccountTypeEnum accountTypeEnum : values()) {
            if (accountTypeEnum.getValue().equalsIgnoreCase(value)) {
                return accountTypeEnum;
            }
        }
        return UNKNOWN;
    }
}


