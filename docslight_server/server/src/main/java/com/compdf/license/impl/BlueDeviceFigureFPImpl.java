package com.compdf.license.impl;

import com.compdf.license.LicenseNative;

/**
 * @author ComPDFKit-WPH 2023/8/29
 */
public class BlueDeviceFigureFPImpl implements LicenseNative.BlueDeviceFigureFP {

    private String value;

    public BlueDeviceFigureFPImpl(String value){
        this.value = value;
    }

    @Override
    public String device_callback() {
        return value;
    }
}
