package com.compdf.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "comidp.resonac")
@Configuration
public class ResonacProperties {
    /**
     * leaderId
     */
    private String leaderId;

    private List<String> templateList;
}
