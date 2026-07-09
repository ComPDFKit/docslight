package com.compdf.enums;

import lombok.Getter;

/**
 * 产品资产类型
 */
public enum AssetProductTypeEnum {
    EXTRACT("EXTRACT", "文档抽取", 50),
    PARSE("PARSE", "文档解析", 100),
    KNOWLEDGE_BASE("KNOWLEDGE_BASE", "知识库", 100);

    @Getter
    private final String value;
    @Getter
    private final String name;
    @Getter
    private final Integer trialDefaultTotal;

    AssetProductTypeEnum(String value, String name, Integer trialDefaultTotal) {
        this.value = value;
        this.name = name;
        this.trialDefaultTotal = trialDefaultTotal;
    }

    public static AssetProductTypeEnum of(String value) {
        if (value == null) {
            return EXTRACT;
        }
        for (AssetProductTypeEnum productTypeEnum : values()) {
            if (productTypeEnum.getValue().equalsIgnoreCase(value)) {
                return productTypeEnum;
            }
        }
        return EXTRACT;
    }
}


