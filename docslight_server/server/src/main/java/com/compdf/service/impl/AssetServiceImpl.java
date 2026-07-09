package com.compdf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.compdf.client.LoginClient;
import com.compdf.entity.Asset;
import com.compdf.entity.AssetAccount;
import com.compdf.enums.AccountTypeEnum;
import com.compdf.enums.AssetFlowTypeEnum;
import com.compdf.enums.AssetProductTypeEnum;
import com.compdf.enums.AssetTypeEnum;
import com.compdf.enums.AssetUnitEnum;
import com.compdf.enums.ErrorInfoEnum;
import com.compdf.exception.ComPDFKitException;
import com.compdf.mapper.AssetAccountMapper;
import com.compdf.mapper.AssetMapper;
import com.compdf.pojo.AssetDTO;
import com.compdf.pojo.AssetReservationDTO;
import com.compdf.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * @author ComPDFKit-WPH 2024-09-11
 */
@Service
@RequiredArgsConstructor
public class AssetServiceImpl extends ServiceImpl<AssetMapper, Asset> implements AssetService {

    private final AssetAccountMapper assetAccountMapper;
    private final LoginClient loginClient;

    @Override
    @Transactional
    public void deductAsset(Integer asset, String leaderId, String userId) {
        deductAsset(asset, leaderId, userId, AssetProductTypeEnum.EXTRACT);
    }

    @Override
    @Transactional
    public void deductAsset(Integer asset, String leaderId, String userId, AssetProductTypeEnum productType) {
        int amount = normalizeAmount(asset);
        checkAccountUsable(leaderId);
        Asset assetInfo = getProductAsset(leaderId, productType);
        int beforeAsset = nvl(assetInfo.getAsset());
        int beforeWithholding = nvl(assetInfo.getWithholdingAsset());
        int row = this.baseMapper.updateAssetByLeaderIdAndProductType(amount, leaderId, productType.getValue());
        if (row == 0) {
            throw new ComPDFKitException(ErrorInfoEnum.OVER_LIMIT);
        }
        insertFlow(UUID.randomUUID().toString(), assetInfo.getId(), leaderId, productType.getValue(), null,
                beforeAsset, amount, beforeAsset - amount, AssetFlowTypeEnum.DEDUCT.getValue(),
                beforeWithholding, beforeWithholding, userId, "direct deduct");
    }

    @Override
    public AssetDTO getAssetPanel(String leaderId, String userId) {
        AssetAccount account = selectActiveAccount(leaderId);
        Asset legacyAsset = null;
        if (Objects.isNull(account)) {
            legacyAsset = selectFirstActiveAsset(leaderId);
        }

        AccountTypeEnum accountType = resolveAccountType(account, legacyAsset);
        LocalDateTime expireTime = Objects.nonNull(account) ? account.getExpireTime() : (Objects.nonNull(legacyAsset) ? legacyAsset.getAssetOverTime() : null);

        AssetDTO assetDTO = new AssetDTO();
        assetDTO.setUser(buildAssetUser());
        assetDTO.setAccountType(accountType.getValue());
        assetDTO.setAccountTypeName(accountType.getName());
        assetDTO.setExpireTime(expireTime);
        assetDTO.setShowExpireTime(Objects.nonNull(expireTime));
        assetDTO.setShowAssetEntry(AccountTypeEnum.TRIAL.equals(accountType));

        List<AssetDTO.AssetProductDTO> products = new ArrayList<>();
        for (AssetProductTypeEnum productType : AssetProductTypeEnum.values()) {
            Asset productAsset = selectProductAssetQuietly(leaderId, productType);
            products.add(buildProductDTO(productType, productAsset));
        }
        assetDTO.setProducts(products);

        Asset extractAsset = selectProductAssetQuietly(leaderId, AssetProductTypeEnum.EXTRACT);
        if (Objects.nonNull(extractAsset)) {
            assetDTO.setAsset(extractAsset.getAsset());
            assetDTO.setAssetTotal(extractAsset.getAssetTotal());
            assetDTO.setOverageTime(expireTime);
            assetDTO.setAssetTypeName(AssetTypeEnum.getAssetTypeEnum(extractAsset.getAssetType()).getTypeName());
        }
        return assetDTO;
    }

    @Override
    public void checkAccountUsable(String leaderId) {
        AssetAccount account = selectActiveAccount(leaderId);
        if (Objects.nonNull(account)) {
            checkExpire(account.getExpireTime());
            return;
        }
        Asset legacyAsset = selectFirstActiveAsset(leaderId);
        if (Objects.isNull(legacyAsset)) {
            throw new ComPDFKitException(ErrorInfoEnum.OVER_LIMIT);
        }
        checkExpire(legacyAsset.getAssetOverTime());
    }

    @Override
    public Asset getProductAsset(String leaderId, AssetProductTypeEnum productType) {
        Asset asset = selectProductAssetQuietly(leaderId, productType);
        if (Objects.isNull(asset)) {
            throw new ComPDFKitException(ErrorInfoEnum.OVER_LIMIT);
        }
        return asset;
    }

    @Override
    @Transactional
    public AssetReservationDTO reserveAsset(String leaderId, AssetProductTypeEnum productType, Integer estimateAmount, String bizId, String operatorId) {
        int amount = normalizeAmount(estimateAmount);
        checkAccountUsable(leaderId);
        Asset assetInfo = getProductAsset(leaderId, productType);
        int beforeAsset = nvl(assetInfo.getAsset());
        int beforeWithholding = nvl(assetInfo.getWithholdingAsset());
        int row = this.baseMapper.reserveAsset(amount, leaderId, productType.getValue());
        if (row == 0) {
            throw new ComPDFKitException(ErrorInfoEnum.OVER_LIMIT);
        }
        String reservationId = UUID.randomUUID().toString();
        insertFlow(reservationId, assetInfo.getId(), leaderId, productType.getValue(), bizId,
                beforeAsset, amount, beforeAsset - amount, AssetFlowTypeEnum.RESERVE.getValue(),
                beforeWithholding, beforeWithholding + amount, operatorId, "reserve asset");
        return AssetReservationDTO.builder()
                .reservationId(reservationId)
                .assetId(assetInfo.getId())
                .leaderId(leaderId)
                .productType(productType.getValue())
                .reservedAmount(amount)
                .bizId(bizId)
                .build();
    }

    @Override
    @Transactional
    public void commitReservation(String reservationId, Integer actualAmount) {
        Map<String, Object> reservation = this.baseMapper.selectReservationFlow(reservationId);
        if (reservation == null) {
            throw new ComPDFKitException(ErrorInfoEnum.OVER_LIMIT);
        }
        int actual = normalizeAmount(actualAmount);
        int reserved = mapInt(reservation, "changedAssets", "changed_assets");
        String assetId = mapString(reservation, "assetId", "asset_id");
        String leaderId = mapString(reservation, "leaderId", "leader_id");
        String productType = mapString(reservation, "productType", "product_type");
        String bizId = mapString(reservation, "bizId", "biz_id");

        Asset assetInfo = this.baseMapper.selectById(assetId);
        if (assetInfo == null) {
            throw new ComPDFKitException(ErrorInfoEnum.OVER_LIMIT);
        }
        int extraAmount = Math.max(actual - reserved, 0);
        int releaseAmount = Math.max(reserved - actual, 0);
        int beforeAsset = nvl(assetInfo.getAsset());
        int beforeWithholding = nvl(assetInfo.getWithholdingAsset());
        int row = this.baseMapper.commitReservedAsset(assetId, reserved, actual, extraAmount, releaseAmount);
        if (row == 0) {
            releaseReservation(reservationId, "commit failed, release reserved asset");
            throw new ComPDFKitException(ErrorInfoEnum.OVER_LIMIT);
        }
        this.baseMapper.closeReservationFlow(reservationId);
        insertFlow(UUID.randomUUID().toString(), assetId, leaderId, productType, bizId,
                beforeAsset, actual, beforeAsset - extraAmount + releaseAmount, AssetFlowTypeEnum.COMMIT.getValue(),
                beforeWithholding, beforeWithholding - reserved, leaderId, "commit reserved asset");
    }

    @Override
    @Transactional
    public void releaseReservation(String reservationId, String reason) {
        Map<String, Object> reservation = this.baseMapper.selectReservationFlow(reservationId);
        if (reservation == null) {
            return;
        }
        int reserved = mapInt(reservation, "changedAssets", "changed_assets");
        String assetId = mapString(reservation, "assetId", "asset_id");
        String leaderId = mapString(reservation, "leaderId", "leader_id");
        String productType = mapString(reservation, "productType", "product_type");
        String bizId = mapString(reservation, "bizId", "biz_id");

        Asset assetInfo = this.baseMapper.selectById(assetId);
        if (assetInfo == null) {
            return;
        }
        int beforeAsset = nvl(assetInfo.getAsset());
        int beforeWithholding = nvl(assetInfo.getWithholdingAsset());
        int row = this.baseMapper.releaseReservedAsset(assetId, reserved);
        if (row == 0) {
            return;
        }
        this.baseMapper.closeReservationFlow(reservationId);
        insertFlow(UUID.randomUUID().toString(), assetId, leaderId, productType, bizId,
                beforeAsset, reserved, beforeAsset + reserved, AssetFlowTypeEnum.RELEASE.getValue(),
                beforeWithholding, beforeWithholding - reserved, leaderId, reason);
    }

    @Override
    public List<Asset> selectByLicenseId(String licenseId) {
        return this.baseMapper.selectList(new LambdaQueryWrapper<Asset>()
                .eq(Asset::getLeaderId, licenseId).eq(Asset::getStatus, 1));
    }

    @Override
    public Asset selectByLeaderId(String LeaderId) {
        Asset asset = selectProductAssetQuietly(LeaderId, AssetProductTypeEnum.EXTRACT);
        if (Objects.isNull(asset)) {
            throw new ComPDFKitException(ErrorInfoEnum.OVER_LIMIT);
        }
        return asset;
    }

    private AssetAccount selectActiveAccount(String leaderId) {
        return assetAccountMapper.selectOne(new LambdaQueryWrapper<AssetAccount>()
                .eq(AssetAccount::getLeaderId, leaderId)
                .eq(AssetAccount::getStatus, 1)
                .last("LIMIT 1"));
    }

    private Asset selectProductAssetQuietly(String leaderId, AssetProductTypeEnum productType) {
        Asset asset = this.baseMapper.selectOne(new LambdaQueryWrapper<Asset>()
                .eq(Asset::getLeaderId, leaderId)
                .eq(Asset::getProductType, productType.getValue())
                .eq(Asset::getStatus, 1)
                .last("LIMIT 1"));
        if (asset == null && AssetProductTypeEnum.EXTRACT.equals(productType)) {
            asset = this.baseMapper.selectOne(new LambdaQueryWrapper<Asset>()
                    .eq(Asset::getLeaderId, leaderId)
                    .and(wrapper -> wrapper.isNull(Asset::getProductType).or().eq(Asset::getProductType, ""))
                    .eq(Asset::getStatus, 1)
                    .last("LIMIT 1"));
        }
        return asset;
    }

    private Asset selectFirstActiveAsset(String leaderId) {
        return this.baseMapper.selectOne(new LambdaQueryWrapper<Asset>()
                .eq(Asset::getLeaderId, leaderId)
                .eq(Asset::getStatus, 1)
                .last("LIMIT 1"));
    }

    private void checkExpire(LocalDateTime expireTime) {
        if (expireTime != null && expireTime.isBefore(LocalDateTime.now())) {
            throw new ComPDFKitException(ErrorInfoEnum.OVER_LIMIT);
        }
    }

    private AccountTypeEnum resolveAccountType(AssetAccount account, Asset legacyAsset) {
        if (Objects.nonNull(account)) {
            return AccountTypeEnum.of(account.getAccountType());
        }
        if (Objects.nonNull(legacyAsset) && Objects.equals(legacyAsset.getAssetType(), AssetTypeEnum.FREE.getType())) {
            return AccountTypeEnum.TRIAL;
        }
        return AccountTypeEnum.FORMAL;
    }

    private AssetDTO.AssetUserDTO buildAssetUser() {
        AssetDTO.AssetUserDTO userDTO = new AssetDTO.AssetUserDTO();
        LoginClient.UserInfo userInfo = loginClient.getUserInfo();
        if (userInfo != null) {
            userDTO.setUsername(userInfo.getNickname());
            userDTO.setEmail(userInfo.getEmail());
        }
        return userDTO;
    }

    private AssetDTO.AssetProductDTO buildProductDTO(AssetProductTypeEnum productType, Asset asset) {
        AssetDTO.AssetProductDTO productDTO = new AssetDTO.AssetProductDTO();
        productDTO.setProductType(productType.getValue());
        productDTO.setProductName(productType.getName());
        String unit = Objects.nonNull(asset) && asset.getUnit() != null ? asset.getUnit() : AssetUnitEnum.PAGE.getValue();
        AssetUnitEnum unitEnum = AssetUnitEnum.of(unit);
        int total = Objects.nonNull(asset) ? nvl(asset.getAssetTotal()) : 0;
        int remaining = Objects.nonNull(asset) ? nvl(asset.getAsset()) : 0;
        int withholding = Objects.nonNull(asset) ? nvl(asset.getWithholdingAsset()) : 0;
        int used = Math.max(total - remaining - withholding, 0);
        productDTO.setUnit(unitEnum.getValue());
        productDTO.setUnitName(unitEnum.getName());
        productDTO.setTotal(total);
        productDTO.setRemaining(remaining);
        productDTO.setWithholding(withholding);
        productDTO.setUsed(used);
        productDTO.setProgress(total <= 0 ? 0 : Math.min(100, used * 100 / total));
        if (AssetProductTypeEnum.KNOWLEDGE_BASE.equals(productType)) {
            productDTO.setFileLimit(10);
            productDTO.setSingleFileSizeLimitMB(50);
        }
        return productDTO;
    }

    private int normalizeAmount(Integer amount) {
        if (amount == null || amount <= 0) {
            return 1;
        }
        return amount;
    }

    private int nvl(Integer value) {
        return value == null ? 0 : value;
    }

    private void insertFlow(String id, String assetId, String leaderId, String productType, String bizId,
                            Integer currentAssets, Integer changedAssets, Integer updatedAssets,
                            String flowType, Integer currentWithholding, Integer updatedWithholding,
                            String operatorId, String remark) {
        this.baseMapper.insertAssetFlowWithProduct(id, assetId, leaderId, productType, bizId,
                currentAssets, changedAssets, updatedAssets, flowType,
                currentWithholding, updatedWithholding, operatorId, remark);
    }

    private String mapString(Map<String, Object> map, String camelKey, String underlineKey) {
        Object value = map.get(camelKey);
        if (value == null) {
            value = map.get(underlineKey);
        }
        return value == null ? null : value.toString();
    }

    private int mapInt(Map<String, Object> map, String camelKey, String underlineKey) {
        Object value = map.get(camelKey);
        if (value == null) {
            value = map.get(underlineKey);
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return value == null ? 0 : Integer.parseInt(value.toString());
    }

}
