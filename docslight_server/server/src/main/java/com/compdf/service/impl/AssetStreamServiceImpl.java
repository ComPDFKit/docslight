package com.compdf.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.compdf.entity.AssetStream;
import com.compdf.mapper.AssetStreamMapper;
import com.compdf.service.AssetStreamService;
import org.springframework.stereotype.Service;

/**
 * @author ComPDFKit-WPH 2025-07-11
 */
@Service
public class AssetStreamServiceImpl extends ServiceImpl<AssetStreamMapper, AssetStream> implements AssetStreamService {

    @Override
    public IPage<AssetStream> page(AssetStream query) {
        return this.page(new Page<>(query.getPage(), query.getSize()), Wrappers.query(query));
    }

}
