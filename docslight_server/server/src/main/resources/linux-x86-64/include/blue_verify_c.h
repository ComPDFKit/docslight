#ifndef BLUE_BLUE_VERIFY_C_H
#define BLUE_BLUE_VERIFY_C_H

#include "blue_utils_c.h"

#if __ANDROID__
#include "jni.h"
#endif

#ifdef __cplusplus
extern "C" {
#endif

typedef enum _ExecutionMode{
    BLUE_EXE_DEVELOPMENT,
    BLUE_EXE_PRODUCTION,
    BLUE_EXE_DEFAULT
} BlueExecutionMode;

/** \brief Set the license
  */
BLUE_API BlueErrorCode BlueLicenseInitialized(const char *license,
                                              int isFile,
                                              BlueDeviceFigureFP df_callback,
                                              BlueApplicationIDFP idCallback,
                                              BlueExecutionMode executionMode);

/** \brief Release all license resources.
  */
BLUE_API BlueErrorCode BlueLicenseDestroy();

/** \brief Check the functional permissions of the license.
  */
BLUE_API BlueErrorCode BlueCheckPermission(BluePermission);

/** \brief Detect the functional permissions that the License has, and return any custom value when it does not have permissions
  */
#define BLUE_MICRO_CHECK(x,xx) BlueErrorCode code = BlueCheckPermission(xx); \
if (code < 0) { \
return x; \
}

#ifdef __cplusplus
};
#endif

#endif //BLUE_BLUE_VERIFY_C_H
