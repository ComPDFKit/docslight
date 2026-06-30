package com.compdf.enums;

/**
 * @author Bob 2023/7/12
 */
public enum ImageTypeEnum {

    PNG("png"),
    JPG("jpg"),
    JPEG("jpeg")
    ;

    public String value() {
        return value;
    }

    private final String value;

    ImageTypeEnum(String value) {
        this.value = value;
    }

    public static ImageTypeEnum getInstance(String value) {
        for (ImageTypeEnum imageTypeEnum : values()) {
            if (imageTypeEnum.value.equals(value)) {
                return imageTypeEnum;
            }
        }
        return null;
    }


}
