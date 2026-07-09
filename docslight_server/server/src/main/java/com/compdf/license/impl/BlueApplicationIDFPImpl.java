package com.compdf.license.impl;


import com.compdf.license.LicenseNative;

/**
 * @author ComPDFKit-WPH 2023/8/29
 */
public class BlueApplicationIDFPImpl implements LicenseNative.BlueApplicationIDFP {

    private String value;

    public BlueApplicationIDFPImpl(String value){
        this.value = value;
    }

    @Override
    public String id_callback() {
        return value;
    }
}
