package com.compdf.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "comidp.config")
public class ComPDFKitProperties {
    /**
     * licenseKey
     */
    private String licenseKey;
    /**
     * 在线认证license
     */
    private String license;
    /**
     * 参考认证库
     */
    private String licenseDeviceId;
    /**
     * 参考认证库
     */
    private String licenseBoundId;
    /**
     * 临时文件路径
     */
    private String tmpPath;
    /**
     * 语种
     */
    private String language;
    /**
     * 转换超时时间
     */
    private Long convertTimeOut;
    /**
     * 临时文件是否删除
     */
    private Boolean tmpFileIsClear;
    /**
     * model 开发模式和产品模式，以及默认
     */
    private String executionMode;

    /**
     * 是否使用GPU
     */
    private Boolean useGPU;
    /**
     * 使用卡情况
     */
//    private List<Integer> gpuID;
    /**
     * 模型数量
     */
    private Integer modelCount;
    /**
     * qwenAPIKey
     */
    private String qwenAPIKey;
    /**
     * qwenAPIModel
     */
    private String qwenAPIModel;

    private List<String> lowLevelEngine;

    private String docSlightHost;
}
