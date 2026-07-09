package com.compdf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.compdf.entity.Asset;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Map;

/**
 * @author ComPDFKit-WPH 2024-09-11
 */
public interface AssetMapper extends BaseMapper<Asset> {

    int updateAssetByLeaderId(@Param("asset") Integer asset,
                              @Param("licenseId")String licenseId);

    @Update("update asset set asset = asset - #{asset} where leader_id = #{leaderId} and product_type = #{productType} and status = 1 and asset >= #{asset}")
    int updateAssetByLeaderIdAndProductType(@Param("asset") Integer asset,
                                            @Param("leaderId") String leaderId,
                                            @Param("productType") String productType);

    @Update("update asset set asset = asset - #{asset}, withholding_asset = ifnull(withholding_asset, 0) + #{asset} where leader_id = #{leaderId} and product_type = #{productType} and status = 1 and asset >= #{asset}")
    int reserveAsset(@Param("asset") Integer asset,
                     @Param("leaderId") String leaderId,
                     @Param("productType") String productType);

    @Update("update asset set asset = asset - #{extraAmount} + #{releaseAmount}, withholding_asset = ifnull(withholding_asset, 0) - #{reservedAmount} where id = #{assetId} and status = 1 and ifnull(withholding_asset, 0) >= #{reservedAmount} and asset >= #{extraAmount}")
    int commitReservedAsset(@Param("assetId") String assetId,
                            @Param("reservedAmount") Integer reservedAmount,
                            @Param("actualAmount") Integer actualAmount,
                            @Param("extraAmount") Integer extraAmount,
                            @Param("releaseAmount") Integer releaseAmount);

    @Update("update asset set asset = asset + #{reservedAmount}, withholding_asset = ifnull(withholding_asset, 0) - #{reservedAmount} where id = #{assetId} and status = 1 and ifnull(withholding_asset, 0) >= #{reservedAmount}")
    int releaseReservedAsset(@Param("assetId") String assetId,
                             @Param("reservedAmount") Integer reservedAmount);

    void insertAssetFlow(String id, Integer asset, String assetId, Integer assetChange, String userId);

    @Insert("insert into asset_flow (id, asset_id, leader_id, product_type, biz_id, current_assets, changed_assets, type, flow_type, updated_assets, current_withholding, updated_withholding, operator_id, remark, status) values (#{id}, #{assetId}, #{leaderId}, #{productType}, #{bizId}, #{currentAssets}, #{changedAssets}, #{flowType}, #{flowType}, #{updatedAssets}, #{currentWithholding}, #{updatedWithholding}, #{operatorId}, #{remark}, 1)")
    void insertAssetFlowWithProduct(@Param("id") String id,
                                    @Param("assetId") String assetId,
                                    @Param("leaderId") String leaderId,
                                    @Param("productType") String productType,
                                    @Param("bizId") String bizId,
                                    @Param("currentAssets") Integer currentAssets,
                                    @Param("changedAssets") Integer changedAssets,
                                    @Param("updatedAssets") Integer updatedAssets,
                                    @Param("flowType") String flowType,
                                    @Param("currentWithholding") Integer currentWithholding,
                                    @Param("updatedWithholding") Integer updatedWithholding,
                                    @Param("operatorId") String operatorId,
                                    @Param("remark") String remark);

    @Select("select id, asset_id as assetId, leader_id as leaderId, product_type as productType, biz_id as bizId, changed_assets as changedAssets, flow_type as flowType, status from asset_flow where id = #{reservationId} and flow_type = 'RESERVE' and status = 1 limit 1")
    Map<String, Object> selectReservationFlow(@Param("reservationId") String reservationId);

    @Update("update asset_flow set status = 0 where id = #{reservationId} and flow_type = 'RESERVE' and status = 1")
    int closeReservationFlow(@Param("reservationId") String reservationId);
}
