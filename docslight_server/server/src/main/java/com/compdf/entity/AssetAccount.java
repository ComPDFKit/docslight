package com.compdf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.compdf.config.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 租户资产账户，用于统一管理账户类型和有效期
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("asset_account")
public class AssetAccount extends BaseEntity {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 租户/企业主账号 ID
     */
    private String leaderId;

    /**
     * TRIAL / FORMAL
     */
    private String accountType;

    /**
     * 统一有效期，null 表示无到期时间
     */
    private LocalDateTime expireTime;

    /**
     * 0 未启用，1 启用
     */
    private Integer status;
}

