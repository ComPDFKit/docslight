package com.compdf.license.enums;

import org.springframework.util.ObjectUtils;

/**
 * @author ComPDFKit-WPH 2023/11/7
 */
public enum ExecutionMode {
    // development
    BLUE_EXE_DEVELOPMENT(0,"DEVELOPMENT"),
    // production
    BLUE_EXE_PRODUCTION(1,"PRODUCTION"),
    // default
    BLUE_EXE_DEFAULT(2,"DEFAULT");

    private final int value;

    private final String modeString;

    ExecutionMode(int value,String model){
        this.value = value;
        this.modeString = model;
    }

    public int getValue() {
        return value;
    }

    public String getModeString() {
        return modeString;
    }

    /**
     * get By ModelString
     *
     * @param model ModelString
     * @return ExecutionMode
     */
    public static ExecutionMode getByModel(String model){
        for (ExecutionMode executionMode : ExecutionMode.values()) {
            if (ObjectUtils.nullSafeEquals(executionMode.modeString,model)) {
                return executionMode;
            }
        }
        return ExecutionMode.BLUE_EXE_DEFAULT;
    }
}
