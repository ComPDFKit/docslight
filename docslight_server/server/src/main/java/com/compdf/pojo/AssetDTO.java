package com.compdf.pojo;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author ComPDFKit-WPH 2025/3/21 0021
 */
@Data
@ToString
public class AssetDTO {

    /**
     * 兼容旧接口字段：当前可用额度
     */
    private Integer asset;

    /**
     * 兼容旧接口字段：套餐/来源类型名称
     */
    private String assetTypeName;

    /**
     * 兼容旧接口字段：过期时间
     */
    private LocalDateTime overageTime;

    /**
     * 兼容旧接口字段：总额度
     */
    private Integer assetTotal;

    private AssetUserDTO user;

    private String accountType;

    private String accountTypeName;

    private LocalDateTime expireTime;

    private Boolean showExpireTime;

    private Boolean showAssetEntry;

    private List<AssetProductDTO> products;

    @Data
    @ToString
    public static class AssetUserDTO {
        private String avatar;
        private String username;
        private String email;
    }

    @Data
    @ToString
    public static class AssetProductDTO {
        private String productType;
        private String productName;
        private String unit;
        private String unitName;
        private Integer used;
        private Integer withholding;
        private Integer remaining;
        private Integer total;
        private Integer progress;
        private Integer fileLimit;
        private Integer singleFileSizeLimitMB;
    }
}
