package com.compdf.config;

import com.compdf.client.RustFsClient;
import com.compdf.properties.RustFsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RustFs Config
 *
 * @author ComPDFKit-WPH
 */
@Configuration
public class RustFsConfig {

    @Bean
    public RustFsClient rustFsClient(RustFsProperties rustFsProperties) {
        return new RustFsClient(rustFsProperties);
    }
}
