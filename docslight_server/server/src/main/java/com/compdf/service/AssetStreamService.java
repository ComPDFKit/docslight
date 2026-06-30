package com.compdf.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.compdf.entity.AssetStream;

/**
 * @author ComPDFKit-WPH 2025-07-11
 */
public interface AssetStreamService extends IService<AssetStream> {

    /**
     * 列表查询
     * @param query 查询参数
     * @return 列表数据
     */
    IPage<AssetStream> page(AssetStream query);

}