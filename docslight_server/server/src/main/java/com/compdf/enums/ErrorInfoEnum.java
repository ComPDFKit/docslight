package com.compdf.enums;

import lombok.Getter;

@Getter
public enum ErrorInfoEnum {
    ERROR_INNER("01001", "系统内部异常", "System internal error"),
    ERROR_CONVERT_FILE_UPLOAD("01002", "未成功上传转换后的文件到服务器", "Failed to upload processed files to the server"),
    ERROR_FILE_UPLOAD("01003", "文件上传异常", "Failed to upload files"),
    ERROR_FILE_DOWNLOAD("01004", "文件下载异常", "Failed to download files"),
    ERROR_FILE_NOT_NULL("01005", "文件不能为空", "The file cannot be empty"),
    ERROR_FILE_PARAMETER("01006", "文件参数异常：请设置正确的文件参数", "Abnormal file parameters: Please set valid file parameters"),
    ERROR_TASK_ID_NOT_NULL("01007", "任务id不能为空", "Task id cannot be empty"),

    ERROR_MALLOC_FAILED("01201", "系统内存空间不足", "System memory problem"),
    ERROR_UNKNOWN_ERROR("01202", "未知错误", "Unknown error"),
    ERROR_FILE_NOT_FOUND_OR_NOT_BE_OPENED("01203", "文件找不到或者不能打开", "Can't find or open the file"),
    ERROR_UNSUPPORTED_SECURITY_SCHEME("01204", "不支持的安全机制", "Unsupported security mechanism"),
//    ERROR_NONE("01205", "未知错误", "none"),
    ERROR_CSV_TABLE("01206", "CSV表格不存在", "The CSV file does not exist"),
    ERROR_DOCUMENT_AI_API("01207", "DocumentAI接口调用异常", "Failed to call DocumentAI APIs"),
    ERROR_DOCUMENT_AI_WRITER("01208", "DocumentAI识别并返回的数据，写入到文件失败", "Failed to write the recognized and returned data to the file"),
    e_ErrLicensePermissionDeny("01225", "许可证没有功能权限", "Permission deny"),

    ERROR_FILE_FORMAT("02001", "文件格式错误", "File format error"),
    ERROR_FILE_CONVERT_FORMAT("02002", "不支持转换该格式的文件", "Unsupported file format for conversion"),
    ERROR_IMAGE_FORMAT("02003", "不支持的图片格式", "Unsupported image format"),
    ERROR_FILE_IS_ENCRYPTED("02201", "文件存在加密", "The file is encrypted"),
    ERROR_PERMISSION_DENIED("02202", "认证失败: 错误的密匙", "Failed authentication: Incorrect secret key"),
    ERROR_PDF_ERROR("02203", "PDF文件异常", "Abnormal PDF file"),
    ERROR_FILE_NOT_IN_PDF_FORMAT_CORRUPTED("02204", "文件格式错误,非PDF", "Unsupported file format (Only PDF is supported)"),
    ERROR_PAGE_NOT_FOUNT_OR_CONTENT("02206", "文件内容错误或者页面丢失", "Invalid file content or pages don't exist"),
    ERROR_FILE_ERROR_OR_PASSWORD_ERROR("02207", "文件打开失败：文件类型不支持或存在加密", "Failed to open the file: Unsupported file format or encrypted file"),
    ERROR_INIT_EXCELXML("02208", "初始化Excel Xml失败", "Failed to initialize ExcelXml"),
    ERROR_OVERTIME("02209", "转档超时，请不要上传太大的文件", "Conversion timeout, please do not upload too large files"),
    ERROR_CONVERT_OFFICE_PDT_ERROR("02210","文件转换失败","File conversion failed"),
    ERROR_CONVERT_SIZE_ERROR("02211","文件转换失败：转档后的文件大小异常","Conversion Failed: Converted and generated a file with an abnormal file size"),
    ERROR_TABLE_NOT_FOUND("02212","表格不存在","Table does not exist"),


    PARAM_VALIDATE_ERROR("03000", "参数校验异常", "Parameter validation error"),
    DPI_VALIDATE_ERROR("03001", "正确的DPI设置范围：72-1500", "DPI range should be set from 72 to 1499"),

    FILE_KEY_NOT_HAVE_ERROR("04001","fileKey不存在", "The fileKey does not exist"),
    FILE_SIZE_ZONE_ERROR("04002","文件大小为0", "The file size is zero. There is no content in your file."),
    FILE_NOT_EXIST_ERROR("04003","文件不存在或打不开", "Your file does not exist or cannot be opened"),

    CONVERT_TYPE_NOT_SUPPORT_ERROR("07001","当前功能系统暂不支持", "Unsupported feature"),
    FAILED_TO_CREATE_DIGITAL_CERTIFICATE("07002","创建数字证书失败", "Failed to create digital certificate"),
    WRITE_SIGNATURE_ERROR("07003","创建签名失败", "Failed to create signature."),
    OVER_LIMIT("08001","额度不足，请升级套餐", "Insufficient quota. Please upgrade your plan."),

    THE_ACTUATOR_DOES_NOT_EXIST("08002","执行器不存在或无法使用", "The actuator does not exist or cannot be used."),
    TASK_NOT_EXIST("08003", "任务不存在", "Task does not exist."),
    TASK_IS_RUNNING("08004", "任务正在运行中", "Task is running."),
    FILE_INFORMATION_EXTRACTION_FAILED("08005", "关键信息提取失败。", "File information extraction failed."),
    API_KEY_ERROR("08006", "API_KEY 无效。", "API_KEY invalid."),
    LOGIN_401("401", "401: Unauthorized", "<Unauthorized '401: Unauthorized'>"),
    TASK_STATUS_ERROR("08007", "任务当前状态无效", "The current status of the task is invalid."),
    TASK_TYPE_ERROR("08008", "任务类型不支持", "The task type is not supported."),
    PARSING_ERROR("08009", "解析文件失败", "Failed to parse the file."),
    PARSING_IMG_NULL_ERROR("08010", "内容为空", "empty content"),

    TEMPLATE_NAME_TOO_LONG("08011", "模板名称过长", "Template name is too long"),
    TEMPLATE_NAME_EXIST("08012", "模板名称已存在", "Template name already exists"),
    TEMPLATE_NUM_LIMIT("08013", "模板数量已达上限", "The number of templates has reached the limit"),
    TEMPLATE_NOT_EXIST("08014", "模板不存在", "Template does not exist"),
//    TEMPLATE_NOT_SUPPORT("08015", "模板不支持", "Template not supported"),
    SPLIT_ERROR("08015", "拆分文件失败", "Failed to split the file."),
    PERMISSION_ERROR("08016", "权限不足", "Permission denied."),
    TEMPLATE_KEY_TOO_MANY("08017", "模板关键字过多", "Too many template keywords"),
    EXTRACT_ERROR("08018", "抽取文件失败", "Failed to extract the file."),
    TEMPLATE_IN_FILE_EXISTS("08019", "模板下存在文件", "There are files under the template"),
    DEFAULT_TEMPLATE_STATUS_NOT_MODIFY("08020", "默认模板状态不可修改", "Default template status cannot be modified"),
    ERROR_DOCSLIGHT_SETTINGS_NOT_FOUND("08021", "You haven't configured an API Key yet. Add one to get started.", "You haven't configured an API Key yet. Add one to get started.");
    /**
     * 01开头 系统异常
     * 02开头 文件异常
     * 03     参数校验异常
     */
    private final String code;
    private final String zhMsg;
    private final String usMsg;

    ErrorInfoEnum(String code, String zhMsg, String usMsg) {
        this.code = code;
        this.zhMsg = zhMsg;
        this.usMsg = usMsg;
    }

    public static ErrorInfoEnum getByCode(String code) {
        for (ErrorInfoEnum value : ErrorInfoEnum.values()) {
            if (value.getCode().equals(code)){
                return value;
            }
        }
        return null;
    }

}
