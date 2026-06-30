package com.compdf.enums;

import lombok.Getter;

/**
 * 权限枚举类
 *extract
 * extract:upload
 * extract:check
 * extract:delete
 * extract:template
 * extract:template:create
 * extract:template:delete
 * extract:template:modify
 * parse
 * parse:upload
 * parse:check
 * parse:delete
 * split
 * split:upload
 * split:split
 * split:delete
 * kb
 * kb:create
 * kb:qa
 * user
 * user:create
 * user:modify
 * user:reset
 * user:delete
 * user:charactor
 * user:charactor:create
 * user:charactor:permission
 * user:charactor:modify
 * user:charactor:delete
 * @author ComPDFKit-WPH 2026/1/27
 */
@Getter
public enum PermissionEnum {

    EXTRACT("extract", "权限：文档抽取"),
    EXTRACT_UPLOAD("extract:upload", "权限：上传抽取文档"),
    EXTRACT_CHECK("extract:check", "权限：查看抽取结果"),
    EXTRACT_DELETE("extract:delete", "权限：删除抽取文档"),
    EXTRACT_TEMPLATE("extract:template", "权限：抽取模板管理"),
    EXTRACT_TEMPLATE_CREATE("extract:template:create", "权限：创建抽取模板"),
    EXTRACT_TEMPLATE_DELETE("extract:template:delete", "权限：删除抽取模板"),
    EXTRACT_TEMPLATE_MODIFY("extract:template:modify", "权限：修改抽取模板"),
    PARSE("parse", "权限：文档解析"),
    PARSE_UPLOAD("parse:upload", "权限：上传解析文档"),
    PARSE_CHECK("parse:check", "权限：查看解析结果"),
    PARSE_DELETE("parse:delete", "权限：删除解析文档"),
    SPLIT("split", "权限：文档拆分"),
    SPLIT_UPLOAD("split:upload", "权限：上传拆分文档"),
    SPLIT_SPLIT("split:split", "权限：拆分文档"),
    SPLIT_DELETE("split:delete", "权限：删除拆分文档"),
    KB("kb", "权限：知识库管理"),
    KB_CREATE("kb:create", "权限：创建知识库"),
    KB_QA("kb:qa", "权限：知识库问答"),
    USER("user", "权限：用户管理"),
    USER_CREATE("user:create", "权限：创建用户"),
    USER_MODIFY("user:modify", "权限：修改用户信息"),
    USER_RESET("user:reset", "权限：重置用户密码"),
    USER_DELETE("user:delete", "权限：删除用户"),
    USER_CHARACTOR("user:charactor", "权限：角色管理"),
    USER_CHARACTOR_CREATE("user:charactor:create", "权限：创建角色"),
    USER_CHARACTOR_PERMISSION("user:charactor:permission", "权限：分配角色权限"),
    USER_CHARACTOR_MODIFY("user:charactor:modify", "权限：修改角色"),
    USER_CHARACTOR_DELETE("user:charactor:delete", "权限：删除角色")

    ;

    private final String code;
    private final String description;

    PermissionEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取对应的枚举值
     *
     * @param code 权限code
     * @return 对应的枚举值，如果不存在则返回null
     */
    public static PermissionEnum getByCode(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        for (PermissionEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }

}
