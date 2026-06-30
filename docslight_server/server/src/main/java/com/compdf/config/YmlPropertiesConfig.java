package com.compdf.config;

import org.springframework.context.ApplicationContext;

/**
 * @author ComPDFKit-WPH 2023/8/23
 */
public class YmlPropertiesConfig {
    /**
     * springboot 应用上下文
     */
    private static ApplicationContext applicationContext;

    public static void setApplicationContext(ApplicationContext context){
        applicationContext = context;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
