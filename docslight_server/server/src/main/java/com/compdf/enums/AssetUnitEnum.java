package com.compdf.enums;

import lombok.Getter;

/**
 * 资产额度单位
 */
public enum AssetUnitEnum {
    PAGE("PAGE", "页"),
    FILE("FILE", "个"),
    MB("MB", "MB");

    @Getter
    private final String value;
    @Getter
    private final String name;

    AssetUnitEnum(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public static AssetUnitEnum of(String value) {
        if (value == null) {
            return PAGE;
        }
        for (AssetUnitEnum unitEnum : values()) {
            if (unitEnum.getValue().equalsIgnoreCase(value)) {
                return unitEnum;
            }
        }
        return PAGE;
    }
}


