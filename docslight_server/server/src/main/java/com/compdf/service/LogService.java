package com.compdf.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.compdf.entity.Asset;
import com.compdf.entity.Log;

import java.util.List;

/**
 * @author ComPDFKit-WPH 2024-09-11
 */
public interface LogService extends IService<Log> {
    void insertLog(Log log);
}