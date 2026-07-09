package com.compdf.license.enums;

import com.compdf.exception.ComPDFKitException;
import lombok.Getter;

/**
 * @author ComPDFKit-WPH 2023/8/29
 */
public enum ErrorCodeEnum {

    E_LICENSE_SUCCESS((short) 0, "0", "Success", "Success"),
    E_LICENSE_INVALID((short) -100, "08100", "The license is invalid", "许可证无效"),
    E_LICENSE_EXPIRE((short) -101, "08101", "The license has expired", "许可证已过期"),
    E_LICENSE_UNSUPPORTED_PLATFORM((short) -102, "08102", "The license does not support the current platform", "该许可证不支持当前平台"),
    E_LICENSE_UNSUPPORTED_ID((short) -103, "08103", "The license does not support the application id", "许可证不支持应用程序 ID"),
    E_LICENSE_UNSUPPORTED_DEVICE((short) -104, "08104", "The license does not support the application id", "许可证不支持应用程序 ID"),
    E_LICENSE_PERMISSION_DENY((short) -105, "08105", "The license does not have the function permission", "License没有该功能权限"),
    E_LICENSE_UNINITIALIZED((short) -106, "08106", "License has not been initialized", "许可证尚未初始化"),
    E_LICENSE_ILLEGAL_ACCESS((short) -1000, "081000", "Illegal access to the API interface", "非法访问API接口"),
    E_LICENSE_FILE_READ_FAILED((short) -1001, "081001", "Failed to read license file", "读取许可证文件失败"),

    E_LICENSE_UNKNOWN_MISTAKE((short) 999, "08999", "License unknown mistake", "许可证未知错误"),
    ;
    private final short errorCode;
    @Getter
    private final String code;
    @Getter
    private final String usMsg;
    @Getter
    private final String zhMsg;

    ErrorCodeEnum(short errorCode, String code, String usMsg, String zhMsg) {
        this.errorCode = errorCode;
        this.code = code;
        this.usMsg = usMsg;
        this.zhMsg = zhMsg;
    }

    public static void errorCodeVerify(short errorCode) {
        if (E_LICENSE_SUCCESS.errorCode != errorCode) {
            for (ErrorCodeEnum errorCodeEnum : ErrorCodeEnum.values()) {
                if (errorCodeEnum.errorCode == errorCode) {
                    throw new ComPDFKitException(errorCodeEnum);
                }
            }
            throw new ComPDFKitException(E_LICENSE_UNKNOWN_MISTAKE);
        }
    }

    public static void errorCodeVerify(short errorCode,String language) {
        if (E_LICENSE_SUCCESS.errorCode != errorCode) {
            for (ErrorCodeEnum errorCodeEnum : ErrorCodeEnum.values()) {
                if (errorCodeEnum.errorCode == errorCode) {
                    throw new ComPDFKitException(errorCodeEnum,language);
                }
            }
            throw new ComPDFKitException(E_LICENSE_UNKNOWN_MISTAKE,language);
        }
    }

}
