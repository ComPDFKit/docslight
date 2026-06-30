package com.compdf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.compdf.config.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author ComPDFKit-WPH 2024-09-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("idp_service")
public class IdpService extends BaseEntity {

    /**
     *
     */
    private String config;
    /**
     * 
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    /**
     * 
     */
    private String name;
    /**
     * 
     */
    private String serverId;
    /**
     * 
     */
    private Integer type;

    private Integer status;

    private Integer port;

    private String url;

}
