package com.compdf.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.compdf.entity.Log;
import com.compdf.mapper.LogMapper;
import com.compdf.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author ComPDFKit-WPH 2024-09-11
 */
@Service
@RequiredArgsConstructor
public class LogServiceImpl extends ServiceImpl<LogMapper, Log> implements LogService {

    @Override
    public void insertLog(Log log) {
        this.baseMapper.insert(log);
    }
}
