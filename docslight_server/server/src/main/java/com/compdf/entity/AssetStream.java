package com.compdf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.compdf.config.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author ComPDFKit-WPH 2025-07-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("asset_stream")
public class AssetStream extends BaseEntity {

    /**
     * 
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    /**
     * 
     */
    private String userId;
    /**
     * 
     */
    private Integer nowAsset;
    /**
     * 
     */
    private Integer changeAsset;
    /**
     * 
     */
    private Integer type;
    /**
     * 
     */
    private  Integer finalAsset;
    /**
     * 
     */
    private String handleUser;
    /**
     * 
     */
    private Integer status;
    /**
     * 
     */
    private String remark;

}
