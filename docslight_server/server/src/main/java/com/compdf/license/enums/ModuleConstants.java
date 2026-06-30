package com.compdf.license.enums;

/**
 * @author ComPDFKit-WPH 2023/8/28
 */
public enum ModuleConstants {
    MODULE_OFFSET(24),
    MODULE_SUB_TYPE_OFFSET(16),
    MODULE_PDF(0x01),
    MODULE_CONVERSION(0x02);

    private final int value;

    ModuleConstants(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
