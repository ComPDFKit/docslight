package com.compdf.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author ZhouQiang 2022/7/13
 */
@ConfigurationProperties(prefix = "compdf.oss")
@Configuration
@Data
public class OssProperties {

    private String bucketName;

    private String endpoint;

    private String accessKeyId;

    private String accessKeySecret;

    private String filePrefix;
}
