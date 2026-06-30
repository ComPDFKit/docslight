#ifndef BLUE_BLUE_UTILS_C_H
#define BLUE_BLUE_UTILS_C_H

#include "stdint.h"

#if defined(_MSC_VER)
#define BLUE_API __declspec(dllexport)
#else
#define BLUE_API __attribute__((visibility("default")))
#endif  // _WIN32

#ifdef __cplusplus
extern "C" {
#endif

/** \brief
  */
typedef const char* (*BlueDeviceFigureFP)();
typedef const char* (*BlueApplicationIDFP)();

typedef int16_t BlueErrorCode;
typedef uint16_t BluePlatformType;
typedef uint32_t BluePermission;

/// Success
#define     E_LICENSE_SUCCESS                (0)
/// The license is invalid
#define     E_LICENSE_INVALID                (-100)
/// The license has expired
#define     E_LICENSE_EXPIRE                 (-101)
/// The license does not support the current platform
#define     E_LICENSE_UNSUPPORTED_PLATFORM   (-102)
/// The license does not support the application id
#define     E_LICENSE_UNSUPPORTED_ID         (-103)
/// The license does not support the application id
#define     E_LICENSE_UNSUPPORTED_DEVICE     (-104)
/// The license does not have the function permission
#define     E_LICENSE_PERMISSION_DENY        (-105)
/// License has not been initialized
#define     E_LICENSE_UNINITIALIZED          (-106)
/// Illegal access to the API interface
#define     E_LICENSE_ILLEGAL_ACCESS         (-1000)
/// Failed to read license file
#define     E_LICENSE_FILE_READ_FAILED       (-1001)

#define     MODULE_OFFSET                    (24)
#define     MODULE_SUB_TYPE_OFFSET           (16)
#define     MODULE_PDF                       (0x01)
#define     MODULE_CONVERSION                (0x02)

/** \brief Platform type definition, this type has been marked in the SDK and cannot be changed.
  */
static const uint16_t PLATFORM_WINDOWS                  =              1 << 0;
static const uint16_t PLATFORM_IOS                      =              1 << 1;
static const uint16_t PLATFORM_MAC                      =              1 << 2;
static const uint16_t PLATFORM_ANDROID                  =              1 << 3;
static const uint16_t PLATFORM_LINUX                    =              1 << 4;
static const uint16_t PLATFORM_ALL                      =              PLATFORM_WINDOWS | PLATFORM_IOS | PLATFORM_MAC | PLATFORM_ANDROID | PLATFORM_LINUX;

///////PDF Module About//////////
static const uint32_t MODULE_PDF_FLAG                   =              MODULE_PDF << MODULE_OFFSET;
static const uint32_t MODULE_PDF_VIEWER_TYPE            =              0x01 << MODULE_SUB_TYPE_OFFSET;
static const uint32_t MODULE_PDF_ANNOT_TYPE             =              0x02 << MODULE_SUB_TYPE_OFFSET;
static const uint32_t MODULE_PDF_FORM_TYPE              =              0x03 << MODULE_SUB_TYPE_OFFSET;
static const uint32_t MODULE_PDF_EDITOR_TYPE            =              0x04 << MODULE_SUB_TYPE_OFFSET;
static const uint32_t MODULE_PDF_SECURITY_TYPE          =              0x05 << MODULE_SUB_TYPE_OFFSET;
static const uint32_t MODULE_PDF_EDIT_TYPE              =              0x06 << MODULE_SUB_TYPE_OFFSET;
static const uint32_t MODULE_PDF_CONVERSION_PDFA_TYPE   =              0x07 << MODULE_SUB_TYPE_OFFSET;
static const uint32_t MODULE_PDF_COMPARE_TYPE           =              0x08 << MODULE_SUB_TYPE_OFFSET;
static const uint32_t MODULE_PDF_SIGNATURE_TYPE         =              0x09 << MODULE_SUB_TYPE_OFFSET;

///////Permissions related to the PDF SDK//////////
static const uint32_t PDF_VIEWER_OUTLINE           = 1 << 0 | MODULE_PDF_FLAG | MODULE_PDF_VIEWER_TYPE;
static const uint32_t PDF_VIEWER_BOOKMARK          = 1 << 1 | MODULE_PDF_FLAG | MODULE_PDF_VIEWER_TYPE;
static const uint32_t PDF_VIEWER_RENDER            = 1 << 2 | MODULE_PDF_FLAG | MODULE_PDF_VIEWER_TYPE;
static const uint32_t PDF_VIEWER_SEARCH            = 1 << 3 | MODULE_PDF_FLAG | MODULE_PDF_VIEWER_TYPE;


static const uint32_t PDF_ANNOT_NOTE               = 1 << 0 | MODULE_PDF_FLAG | MODULE_PDF_ANNOT_TYPE;
static const uint32_t PDF_ANNOT_LINK               = 1 << 1 | MODULE_PDF_FLAG | MODULE_PDF_ANNOT_TYPE;
static const uint32_t PDF_ANNOT_FREETEXT           = 1 << 2 | MODULE_PDF_FLAG | MODULE_PDF_ANNOT_TYPE;
static const uint32_t PDF_ANNOT_SHAPE              = 1 << 3 | MODULE_PDF_FLAG | MODULE_PDF_ANNOT_TYPE;
static const uint32_t PDF_ANNOT_MARKUP             = 1 << 4 | MODULE_PDF_FLAG | MODULE_PDF_ANNOT_TYPE;
static const uint32_t PDF_ANNOT_STAMPS             = 1 << 5 | MODULE_PDF_FLAG | MODULE_PDF_ANNOT_TYPE;
static const uint32_t PDF_ANNOT_STAMPC             = 1 << 6 | MODULE_PDF_FLAG | MODULE_PDF_ANNOT_TYPE;
static const uint32_t PDF_ANNOT_INK                = 1 << 7 | MODULE_PDF_FLAG | MODULE_PDF_ANNOT_TYPE;
static const uint32_t PDF_ANNOT_SOUND              = 1 << 8 | MODULE_PDF_FLAG | MODULE_PDF_ANNOT_TYPE;
static const uint32_t PDF_ANNOT_DELETE             = 1 << 9 | MODULE_PDF_FLAG | MODULE_PDF_ANNOT_TYPE;
static const uint32_t PDF_ANNOT_FLATTEN            = 1 << 10 | MODULE_PDF_FLAG | MODULE_PDF_ANNOT_TYPE;
static const uint32_t PDF_ANNOT_XFDF               = 1 << 11 | MODULE_PDF_FLAG | MODULE_PDF_ANNOT_TYPE;

static const uint32_t PDF_FORM                     = 1 << 0 | MODULE_PDF_FLAG | MODULE_PDF_FORM_TYPE;
static const uint32_t PDF_FORM_FILL                = 1 << 1 | MODULE_PDF_FLAG | MODULE_PDF_FORM_TYPE;

static const uint32_t PDF_EDITOR_PAGE              = 1 << 0 | MODULE_PDF_FLAG | MODULE_PDF_EDITOR_TYPE;
static const uint32_t PDF_EDITOR_EXTRACT           = 1 << 1 | MODULE_PDF_FLAG | MODULE_PDF_EDITOR_TYPE;
static const uint32_t PDF_EDITOR_INFO              = 1 << 2 | MODULE_PDF_FLAG | MODULE_PDF_EDITOR_TYPE;
static const uint32_t PDF_EDITOR_CONVERT           = 1 << 3 | MODULE_PDF_FLAG | MODULE_PDF_EDITOR_TYPE;

static const uint32_t PDF_SECURITY_ENCRYPT         = 1 << 0 | MODULE_PDF_FLAG | MODULE_PDF_SECURITY_TYPE;
static const uint32_t PDF_SECURITY_DECRYPT         = 1 << 1 | MODULE_PDF_FLAG | MODULE_PDF_SECURITY_TYPE;
static const uint32_t PDF_SECURITY_WATERMARK       = 1 << 2 | MODULE_PDF_FLAG | MODULE_PDF_SECURITY_TYPE;
static const uint32_t PDF_SECURITY_REDACTION       = 1 << 3 | MODULE_PDF_FLAG | MODULE_PDF_SECURITY_TYPE;
static const uint32_t PDF_SECURITY_HEADER_FOOTER   = 1 << 4 | MODULE_PDF_FLAG | MODULE_PDF_SECURITY_TYPE;
static const uint32_t PDF_SECURITY_BATES           = 1 << 5 | MODULE_PDF_FLAG | MODULE_PDF_SECURITY_TYPE;
static const uint32_t PDF_SECURITY_BACKGROUND      = 1 << 6 | MODULE_PDF_FLAG | MODULE_PDF_SECURITY_TYPE;

static const uint32_t PDF_EDIT_TEXT                = 1 << 0 | MODULE_PDF_FLAG | MODULE_PDF_EDIT_TYPE;
static const uint32_t PDF_EDIT_IMAGE               = 1 << 1 | MODULE_PDF_FLAG | MODULE_PDF_EDIT_TYPE;

static const uint32_t PDF_CONVERSION_PDFA          = 1 << 0 | MODULE_PDF_FLAG | MODULE_PDF_CONVERSION_PDFA_TYPE;

static const uint32_t PDF_COMPARE                  = 1 << 0 | MODULE_PDF_FLAG | MODULE_PDF_COMPARE_TYPE;

static const uint32_t PDF_SIGNATURE                = 1 << 0 | MODULE_PDF_FLAG | MODULE_PDF_SIGNATURE_TYPE;


///////Conversion Module About//////////
static const uint32_t MODULE_CONVERSION_FLAG       =             MODULE_CONVERSION << MODULE_OFFSET;
static const uint32_t MODULE_CONVERSION_TYPE       =             0x01 << MODULE_SUB_TYPE_OFFSET;
///////Permissions related to the Conversion SDK//////////
static const uint32_t CONVERSION_WORD              =             1 << 0 | MODULE_CONVERSION_FLAG | MODULE_CONVERSION_TYPE;
static const uint32_t CONVERSION_PPT               =             1 << 1 | MODULE_CONVERSION_FLAG | MODULE_CONVERSION_TYPE;
static const uint32_t CONVERSION_EXCEL             =             1 << 2 | MODULE_CONVERSION_FLAG | MODULE_CONVERSION_TYPE;
static const uint32_t CONVERSION_TXT               =             1 << 3 | MODULE_CONVERSION_FLAG | MODULE_CONVERSION_TYPE;
static const uint32_t CONVERSION_TABLE             =             1 << 4 | MODULE_CONVERSION_FLAG | MODULE_CONVERSION_TYPE;
static const uint32_t CONVERSION_CSV               =             1 << 5 | MODULE_CONVERSION_FLAG | MODULE_CONVERSION_TYPE;
static const uint32_t CONVERSION_IMAGE             =             1 << 6 | MODULE_CONVERSION_FLAG | MODULE_CONVERSION_TYPE;
static const uint32_t CONVERSION_RTF               =             1 << 7 | MODULE_CONVERSION_FLAG | MODULE_CONVERSION_TYPE;
static const uint32_t CONVERSION_HTML              =             1 << 8 | MODULE_CONVERSION_FLAG | MODULE_CONVERSION_TYPE;
static const uint32_t CONVERSION_OCR               =             1 << 9 | MODULE_CONVERSION_FLAG | MODULE_CONVERSION_TYPE;
static const uint32_t CONVERSION_EXTRACT           =             1 << 10 | MODULE_CONVERSION_FLAG | MODULE_CONVERSION_TYPE;


///////DocumentAI Module About//////////
///////Permissions related to the DocumentAI SDK//////////


#ifdef __cplusplus
};
#endif

#endif //BLUE_BLUE_UTILS_C_H
