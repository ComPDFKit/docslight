package com.compdf.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.compdf.entity.ConvertFile;
import com.compdf.mapper.ConvertFileMapper;
import com.compdf.service.ConvertFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConvertFileServiceImpl extends ServiceImpl<ConvertFileMapper, ConvertFile> implements ConvertFileService {

    private final ConvertFileMapper convertFileMapper;

    @Override
    public ConvertFile getById(String fileId) {
        return convertFileMapper.selectById(fileId);
    }

}
