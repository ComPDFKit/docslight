package com.compdf.utils;

import com.compdf.client.LoginClient;
import com.compdf.enums.ErrorInfoEnum;
import com.compdf.enums.PermissionEnum;
import com.compdf.exception.ComPDFKitException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限校验切面
 * 拦截带有 @PermissionVerify 注解的方法，进行权限校验
 *
 * @author ComPDFKit-WPH 2026/1/27
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionVerifyUtils {

    public static void checkPermission(PermissionEnum requiredPermission , LoginClient loginClient) {
        checkPermission(new PermissionEnum[]{requiredPermission}, false, loginClient);
    }
    /**
     * 在方法执行前进行权限校验
     */
    public static void checkPermission(PermissionEnum[] requiredPermissions , boolean requireAll, LoginClient loginClient) {
        // 如果没有指定权限，只校验是否登录
        if (requiredPermissions.length == 0) {
            return;
        }
        // 获取当前用户的权限列表
        Set<PermissionEnum> userPermissions = getCurrentUserPermissions(loginClient);
        // 进行权限校验
        boolean hasPermission;
        if (requireAll) {
            // 需要满足所有权限
            hasPermission = Arrays.stream(requiredPermissions)
                    .allMatch(userPermissions::contains);
        } else {
            // 满足任一权限即可
            hasPermission = Arrays.stream(requiredPermissions)
                    .anyMatch(userPermissions::contains);
        }
        if (!hasPermission) {
            log.warn("Permission verification failed, required permissions: {}, user permissions: {}",
                    Arrays.stream(requiredPermissions).map(PermissionEnum::getCode).collect(Collectors.toList()),
                    userPermissions);
            throw new ComPDFKitException(ErrorInfoEnum.PERMISSION_ERROR);
        }
    }
    /**
     * 获取当前用户的权限列表
     */
    private static Set<PermissionEnum> getCurrentUserPermissions(LoginClient loginClient) {
        LoginClient.UserInfo userInfo = loginClient.getUserInfo();
        if (userInfo == null || userInfo.getUserPermissions() == null) {
            return new HashSet<>();
        }
        return userInfo.getUserPermissions();
    }
}
