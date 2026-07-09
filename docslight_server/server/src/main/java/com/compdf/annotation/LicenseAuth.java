package com.compdf.annotation;

import java.lang.annotation.*;

/**
 * @author ComPDFKit-WPH 2023/8/17
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LicenseAuth {
}
