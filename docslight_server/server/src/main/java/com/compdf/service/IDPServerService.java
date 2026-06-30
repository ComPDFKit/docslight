package com.compdf.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.compdf.entity.IdpServer;

/**
 * @author ComPDFKit-WPH 2024-09-11
 */
public interface IDPServerService extends IService<IdpServer> {

    /**
     * 列表查询
     * @param query 查询参数
     * @return 列表数据
     */
    IPage<IdpServer> page(IdpServer query);

    boolean update(IdpServer idpServer);

    boolean remove(String id);

//    String test(String id);

    IdpServer selectById(String serverId);

    String initServer();

}