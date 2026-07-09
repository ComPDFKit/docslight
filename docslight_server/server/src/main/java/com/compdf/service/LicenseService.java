package com.compdf.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.compdf.entity.IdpFile;
import com.compdf.entity.License;
import com.compdf.enums.FileStatusEnum;

/**
 * @author ComPDFKit-WPH 2024-09-11
 */
public interface LicenseService extends IService<License> {

    License selectByLicenseKey(String license);
}