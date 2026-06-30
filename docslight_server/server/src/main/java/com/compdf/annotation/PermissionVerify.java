package com.compdf.annotation;

import com.compdf.enums.PermissionEnum;

import java.lang.annotation.*;

/**
 * 权限校验注解
 * 用于标注需要进行权限校验的方法或类
 * <p>
 * // 需要文件上传权限
 *
 * @author ComPDFKit-WPH 2023/8/17
 * @PermissionVerify(PermissionEnum.FILE_UPLOAD)
 * <p>
 * // 需要任一权限即可
 * @PermissionVerify({PermissionEnum.FILE_UPLOAD, PermissionEnum.FILE_DOWNLOAD})
 * <p>
 * // 需要同时满足所有权限
 * @PermissionVerify(value = {PermissionEnum.FILE_UPLOAD, PermissionEnum.SYSTEM_ADMIN}, requireAll = true)
 * <p>
 * // 仅校验是否登录
 * @PermissionVerify
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PermissionVerify {

    /**
     * 需要校验的权限列表
     * 默认为空数组，表示不校验具体权限，只校验是否登录
     */
    PermissionEnum[] value() default {};

    /**
     * 权限校验逻辑：true-需要满足所有权限，false-满足任一权限即可
     * 默认为false，即满足任一权限即可通过
     */
    boolean requireAll() default false;

}
