package com.compdf.license.utils;

import com.compdfkit.auth.AuthHttpClient;
import com.compdfkit.auth.DeviceUtils;
import com.compdf.license.LicenseNative;
import com.compdf.license.enums.ConversionModule;
import com.compdf.license.enums.ErrorCodeEnum;
import com.compdf.license.enums.ExecutionMode;
import com.compdf.properties.ComPDFKitProperties;

import java.util.Objects;

/**
 * @author ComPDFKit-WPH 2023/8/29
 */
public class LicenseUtils {

    private static final LicenseNative.BlueVerify blueVerify;

    static {
        blueVerify = new LicenseNative().getBlueVerify();
    }

    /**
     * 权限校验
     *
     * @param conversionModule conversionModule
     */
    public static void permissionCheck(ConversionModule conversionModule) {
        if (!Objects.isNull(conversionModule)){
            ErrorCodeEnum.errorCodeVerify(blueVerify.BlueCheckPermission(conversionModule.getValue()));
        }
    }

    /**
     * 刷新License
     *
     * @param comPDFKitProperties comPDFKitProperties
     */
    public static void refreshLicense(ComPDFKitProperties comPDFKitProperties) {
        // 报错重试一次
        String licenseKey = AuthHttpClient.getInstance(comPDFKitProperties.getLicense()).refreshLicenseKey();
        new LicenseNative().destroyBlueLibrary();
        ExecutionMode executionMode = ExecutionMode.getByModel(comPDFKitProperties.getExecutionMode());
        new LicenseNative().initBlueLibrary(licenseKey, DeviceUtils.getDeviceID(),
                comPDFKitProperties.getLicenseBoundId(), executionMode,comPDFKitProperties.getLanguage());
    }


}
