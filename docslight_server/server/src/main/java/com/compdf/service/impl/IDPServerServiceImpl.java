package com.compdf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.compdf.entity.IdpServer;
import com.compdf.exception.ComPDFKitException;
import com.compdf.mapper.IdpServerMapper;
import com.compdf.service.IDPServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

/**
 * @author ComPDFKit-WPH 2024-09-11
 */
@Service
@Slf4j
public class IDPServerServiceImpl extends ServiceImpl<IdpServerMapper, IdpServer> implements IDPServerService {


    @Override
    public IPage<IdpServer> page(IdpServer query) {
        return this.page(new Page<>(query.getPage(), query.getSize()), Wrappers.query(query));
    }

    /**
     * 保存
     */
    @Override
    public boolean save(IdpServer idpServer) {
        // TODO 限制字段条件

        if (Objects.isNull(idpServer.getName())) {
            idpServer.setName(idpServer.getIp());
        }
        return this.baseMapper.insert(idpServer) > 0;
    }

    /**
     * 修改
     */
    @Override
    public boolean update(IdpServer idpServer) {

        return this.baseMapper.updateById(idpServer) > 0;
    }

    /**
     * 删除
     */
    @Override
    public boolean remove(String id) {
        return this.baseMapper.deleteById(id) > 0;
    }

//    @Override
//    public String test(String id){
//        IdpServer idpServer = this.baseMapper.selectById(id);
//        List<String> results = SshUtil.getRemoteClient(new SshUtil.
//                SshHost(idpServer.getIp(), idpServer.getUserName(), idpServer.getUserPwd(), idpServer.getPort()))
//                .exceCommond("hostname");
//        if (CollectionUtils.isEmpty(results) || "".equals(results.get(0))){
//            // TODO 报错
//            return null;
//        }
//        return results.get(0);
//    }

    @Override
    public IdpServer selectById(String serverId) {
        return this.baseMapper.selectById(serverId);
    }

    @Override
    public String initServer() {
        try {
            // 获取本地主机对象
            InetAddress localHost = InetAddress.getLocalHost();
            // 获取主机名
            String hostName = localHost.getHostName();
            // 获取IP地址
            String hostAddress = localHost.getHostAddress();
            IdpServer idpServer = new IdpServer();
            idpServer.setIp(hostAddress);
            idpServer.setName(hostName);
            IdpServer idpServerQuery = this.baseMapper.selectOne(new LambdaQueryWrapper<IdpServer>()
                    .eq(IdpServer::getIp, hostAddress)
                    .eq(IdpServer::getName, hostName));
            if (idpServerQuery == null) {
                this.baseMapper.insert(idpServer);
                return idpServer.getId();
            } else {
                log.info("This server has been initialized");
                return idpServerQuery.getId();
            }
        } catch (UnknownHostException e) {
            log.error("Unable to obtain the local IP address and hostname: " + e.getMessage());
            throw new ComPDFKitException("Unable to obtain the local IP address and hostname");
        }
    }


}
