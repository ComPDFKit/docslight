package com.compdf.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.compdf.entity.User;
import com.compdf.enums.PermissionEnum;

import java.util.List;

/**
 * @author ComPDFKit-WPH 2025/10/23 星期四
 */
public interface UserService extends IService<User> {


    List<String> selectTeamUserIds(String leaderId);

    String selectLeaderIdByUserId(String userId);

    User selectByUserId(String userId);

    void verifyPermission(User user, PermissionEnum permissionEnum);
}
