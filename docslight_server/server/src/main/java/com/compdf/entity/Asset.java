package com.compdf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.compdf.config.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author ComPDFKit-WPH 2024-09-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("asset")
public class Asset extends BaseEntity {

    /**
     *
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * leaderId
     */
    private String leaderId;

    /**
     * 资产类型,1:订阅，2:Package，3:Free
     */
    private Integer assetType;

    /**
     * 产品资产类型：EXTRACT/PARSE/KNOWLEDGE_BASE
     */
    private String productType;

    /**
     * 额度单位：PAGE/FILE/MB
     */
    private String unit;

    /**
     * 资产总额
     */
    private Integer assetTotal;

    /**
     * 资产剩余
     */
    private Integer asset;

    /**
     * 预扣费资产
     */
    private Integer withholdingAsset;

    /**
     * 资产过期时间
     */
    private LocalDateTime assetOverTime;

    /**
     * 0 未启用，1启用
     */
    private Integer status;

}
