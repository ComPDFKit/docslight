package com.compdf.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 资产预锁定结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetReservationDTO {

    private String reservationId;

    private String assetId;

    private String leaderId;

    private String productType;

    private Integer reservedAmount;

    private String bizId;
}

