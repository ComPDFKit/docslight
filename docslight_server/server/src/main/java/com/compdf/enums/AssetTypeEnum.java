package com.compdf.enums;

import lombok.Getter;

/**
 * @author ComPDFKit-WPH 2025/3/21 0021
 */
public enum AssetTypeEnum {
    NULL(-1, "null"),
    SUBSCRIPTION(1,"subscription"),
    PACKAGE(2,"package"),
    FREE(3,"free"),

    ;
    @Getter
    private final String typeName;
    @Getter
    private final Integer type;

    AssetTypeEnum(Integer type, String typeName) {
        this.typeName = typeName;
        this.type = type;
    }

    public static AssetTypeEnum getAssetTypeEnum(Integer type) {
        for (AssetTypeEnum assetTypeEnum : AssetTypeEnum.values()) {
            if (assetTypeEnum.getType().equals(type)) {
                return assetTypeEnum;
            }
        }
        return NULL;
    }
}
