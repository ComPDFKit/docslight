package com.compdf.license;

import com.compdf.license.enums.ErrorCodeEnum;
import com.compdf.license.enums.ExecutionMode;
import com.compdf.license.impl.BlueApplicationIDFPImpl;
import com.compdf.license.impl.BlueDeviceFigureFPImpl;
import com.sun.jna.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Objects;

/**
 * @author ComPDFKit-WPH 2023/8/28
 */
@Slf4j
public class LicenseNative {

    private static final String BLUE_LIBRARY_PATH = "/usr/lib/libblue.so";

    static {
        if (Platform.isLinux()) {
            File file = new File(BLUE_LIBRARY_PATH);
            if (file.exists()) {
                boolean isDelete = file.delete();
            }
            try (InputStream inputStreamLib = new BufferedInputStream(Objects.requireNonNull(LicenseNative.class.getClassLoader().getResourceAsStream("linux-x86-64/libblue.so")));
                 OutputStream outputStreamLib = new BufferedOutputStream(new FileOutputStream(BLUE_LIBRARY_PATH))) {
                IOUtils.copy(inputStreamLib, outputStreamLib);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public interface BlueDeviceFigureFP extends Callback {
        String device_callback();
    }

    public interface BlueApplicationIDFP extends Callback {
        String id_callback();
    }

    public interface BluePermission extends NativeMapped {
        int getValue();
    }

    private static BlueVerify blueVerify;

    public interface BlueVerify extends Library {

        String BlueGetVersion();

        /**
         * brief Set the license
         */
        short BlueLicenseInitialized(String license, int isFile, BlueDeviceFigureFP df_callback, BlueApplicationIDFP idCallback, int executionMode);

        short BlueLicenseDestroy();

        short BlueCheckPermission(int permission);
    }

    public BlueVerify getBlueVerify() {
        if (blueVerify == null) {
            try {
                blueVerify = (BlueVerify) Native.loadLibrary("/usr/lib/libblue.so", BlueVerify.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return blueVerify;
    }

    /**
     * init
     *
     * @param licenseKey
     * @param licenseDeviceId
     * @param licenseBoundId
     * @param language
     */
    public void initBlueLibrary(String licenseKey, String licenseDeviceId, String licenseBoundId, ExecutionMode executionMode, String language){
        BlueVerify blueVerify = this.getBlueVerify();
        ErrorCodeEnum.errorCodeVerify(blueVerify.BlueLicenseInitialized(licenseKey, 0,
                new BlueDeviceFigureFPImpl(licenseDeviceId), new BlueApplicationIDFPImpl(licenseBoundId),executionMode.getValue()),language);
    }

    /**
     * 注销License
     */
    public void destroyBlueLibrary(){
        BlueVerify blueVerify = this.getBlueVerify();
        ErrorCodeEnum.errorCodeVerify(blueVerify.BlueLicenseDestroy());
    }
}
