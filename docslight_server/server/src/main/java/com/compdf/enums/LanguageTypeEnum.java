package com.compdf.enums;

import com.compdf.license.enums.ErrorCodeEnum;

import java.util.Objects;

/**
 * @author ComPDFKit-Bob 2023/7/24
 *
 * 接口错误提示语言（1、English，2、中文）
 */
public enum LanguageTypeEnum {
    ENGLISH(1),
    CHINESE(2),
    ;

    private final int value;

    LanguageTypeEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static LanguageTypeEnum getByValue(Integer value) {
        for (LanguageTypeEnum languageTypeEnum : LanguageTypeEnum.values()) {
            if (Objects.equals(languageTypeEnum.value,value)){
                return languageTypeEnum;
            }
        }
        return ENGLISH;
    }

    public static String filterMsg(ErrorInfoEnum errorInfoEnum, Integer language,String systemLanguage) {
        if (Objects.isNull(language)) {
            return Objects.equals(systemLanguage,"zh_cn") ? errorInfoEnum.getZhMsg() : errorInfoEnum.getUsMsg();
        }
        if (Objects.equals(getByValue(language), LanguageTypeEnum.CHINESE)) {
            return errorInfoEnum.getZhMsg();
        }
        return errorInfoEnum.getUsMsg();
    }

    public static String filterMsg(ErrorCodeEnum errorInfoEnum, Integer language, String systemLanguage) {
        if (Objects.isNull(language)) {
            return Objects.equals(systemLanguage,"zh_cn") ? errorInfoEnum.getZhMsg() : errorInfoEnum.getUsMsg();
        }
        if (Objects.equals(getByValue(language), LanguageTypeEnum.CHINESE)) {
            return errorInfoEnum.getZhMsg();
        }
        return errorInfoEnum.getUsMsg();
    }
}
