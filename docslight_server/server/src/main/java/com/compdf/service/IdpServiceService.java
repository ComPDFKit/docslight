package com.compdf.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.compdf.entity.IdpService;

import java.util.List;

/**
 * @author ComPDFKit-WPH 2024-09-11
 */
public interface IdpServiceService extends IService<IdpService> {

    /**
     * 列表查询
     * @param query 查询参数
     * @return 列表数据
     */
    IPage<IdpService> page(IdpService query);

    List<IdpService> selectByIds(List<String> serviceIds);

    void restart(IdpService service, String taskId);

    void initService(String serverId);

    IdpService selectByServiceIdAndTypeAndPort(String serverId, Integer type, Integer port);
}