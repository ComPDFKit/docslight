package com.compdf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.compdf.entity.License;
import com.compdf.mapper.LicenseMapper;
import com.compdf.service.LicenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author ComPDFKit-WPH 2024-09-11
 */
@Service
@RequiredArgsConstructor
public class LicenseServiceImpl extends ServiceImpl<LicenseMapper, License> implements LicenseService {


    @Override
    public License selectByLicenseKey(String license) {
        return this.baseMapper.selectOne(new LambdaQueryWrapper<License>()
                .eq(License::getLicenseKey, license)
                .eq(License::getStatus, 1));
    }
}
