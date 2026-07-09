package com.compdf.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.compdf.entity.ConvertFile;


public interface ConvertFileService extends IService<ConvertFile> {

    ConvertFile getById(String fileId);
}
