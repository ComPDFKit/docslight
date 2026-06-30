package com.compdf.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author ComPDFKit-WPH 2026/6/24
 */
@Data
public class DocSlightSettings {

    private String apikey;

    /**
     * local | cloud
     */
    private String model;

    @JSONField(name = "local_llm_provider")
    private String localLlmProvider;

    @JSONField(name = "local_llm_model")
    private String localLlmModel;

    @JSONField(name = "local_llm_base_url")
    private String localLlmBaseUrl;

    @JSONField(name = "local_llm_api_key")
    private String localLlmApiKey;

    /**
     * vlm | integrate
     */
    @JSONField(name = "cloud_extract_mode")
    private String cloudExtractMode;

}
