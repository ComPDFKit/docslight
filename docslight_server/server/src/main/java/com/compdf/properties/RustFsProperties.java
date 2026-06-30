package com.compdf.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "comidp.rustfs")
@Configuration
public class RustFsProperties {
    /**
     * RustFs host:port，suchAs:<a href="">http://127.0.0.1:9000</a>
     */
    private String baseUrl;

    /**
     * RustFs key
     */
    private String accessKeyId;

    /**
     * RustFs secret
     */
    private String secretAccessKey;
}
