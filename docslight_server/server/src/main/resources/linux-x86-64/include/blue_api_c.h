#ifndef BLUE_BLUE_API_C_H
#define BLUE_BLUE_API_C_H

#include "blue_utils_c.h"

#ifdef __cplusplus
extern "C" {
#endif

BLUE_API const char* BlueGetVersion();
BLUE_API const char* BlueGetBuildTag();

#ifdef __cplusplus
};
#endif

#endif //BLUE_BLUE_API_C_H
