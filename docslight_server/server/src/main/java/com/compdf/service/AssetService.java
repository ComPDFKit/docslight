package com.compdf.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.compdf.entity.Asset;
import com.compdf.enums.AssetProductTypeEnum;
import com.compdf.pojo.AssetDTO;
import com.compdf.pojo.AssetReservationDTO;

import java.util.List;

/**
 * @author ComPDFKit-WPH 2024-09-11
 */
public interface AssetService extends IService<Asset> {

    /**
     * 扣除资产
     *
     * @param asset 扣除量
     * @param leaderId leaderId
     */
    void deductAsset(Integer asset, String leaderId, String userId);

    /**
     * 按产品类型扣除资产
     *
     * @param asset 扣除量
     * @param leaderId leaderId
     * @param userId 操作人
     * @param productType 产品资产类型
     */
    void deductAsset(Integer asset, String leaderId, String userId, AssetProductTypeEnum productType);

    /**
     * 查询资产面板
     */
    AssetDTO getAssetPanel(String leaderId, String userId);

    /**
     * 统一校验资产账户可用性和有效期
     */
    void checkAccountUsable(String leaderId);

    /**
     * 查询指定产品资产
     */
    Asset getProductAsset(String leaderId, AssetProductTypeEnum productType);

    /**
     * 预锁定资产
     */
    AssetReservationDTO reserveAsset(String leaderId, AssetProductTypeEnum productType, Integer estimateAmount, String bizId, String operatorId);

    /**
     * 按实际用量提交预锁定资产
     */
    void commitReservation(String reservationId, Integer actualAmount);

    /**
     * 释放预锁定资产
     */
    void releaseReservation(String reservationId, String reason);

    List<Asset> selectByLicenseId(String licenseId);

    Asset selectByLeaderId(String LeaderId);
}