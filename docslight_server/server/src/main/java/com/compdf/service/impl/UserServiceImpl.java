package com.compdf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.compdf.entity.User;
import com.compdf.enums.ErrorInfoEnum;
import com.compdf.enums.PermissionEnum;
import com.compdf.exception.ComPDFKitException;
import com.compdf.mapper.UserMapper;
import com.compdf.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author ComPDFKit-WPH 2025/10/23 星期四
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    @Override
    public List<String> selectTeamUserIds(String leaderId) {
        List<User> users = this.baseMapper.selectList(new LambdaQueryWrapper<User>().select(User::getId).eq(User::getStatus, "1").eq(User::getLeaderId, leaderId));
        if (CollectionUtils.isEmpty(users)) {
            List<String> ids = new ArrayList<>();
            ids.add(leaderId);
            return ids;
        }
        List<String> userIds = users.stream().map(User::getId).collect(Collectors.toList());
        userIds.add(leaderId);
        return userIds;
    }

    @Override
    public String selectLeaderIdByUserId(String userId) {
        User user = this.baseMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getStatus, "1")
                .eq(User::getId, userId));
        if (Objects.equals(user.getRole(), "user")) {
            return user.getLeaderId();
        } else  {
            return user.getId();
        }
    }

    @Override
    public User selectByUserId(String userId) {
        return this.baseMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getId, userId).eq(User::getStatus, "1"));
    }

    @Override
    public void verifyPermission(User user, PermissionEnum permissionEnum) {
        int count = this.baseMapper.selectPermissionByUserIdAndPermission(user.getId(), permissionEnum.getCode());
        if (count < 1) {
            throw new ComPDFKitException(ErrorInfoEnum.PERMISSION_ERROR);
        }
    }
}
