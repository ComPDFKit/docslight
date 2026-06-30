package com.compdf.annotation;

import com.compdf.enums.LogTypeEnum;
import com.compdf.enums.PermissionEnum;

import java.lang.annotation.*;

/**
 * @author ComPDFKit-WPH 2026/2/5
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ComPDFLog {

    String leaderId() default "";
    String userId() default "";
    PermissionEnum logModule();
    String log_content();
    String logRequest() default "";

}
