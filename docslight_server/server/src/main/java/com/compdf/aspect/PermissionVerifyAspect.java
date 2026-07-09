package com.compdf.aspect;

import com.compdf.annotation.PermissionVerify;
import com.compdf.client.LoginClient;
import com.compdf.enums.ErrorInfoEnum;
import com.compdf.enums.PermissionEnum;
import com.compdf.exception.ComPDFKitException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
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
public class PermissionVerifyAspect {

    private final LoginClient loginClient;

    /**
     * 在方法执行前进行权限校验
     */
    @Before("@annotation(com.compdf.annotation.PermissionVerify)")
    public void checkPermission(JoinPoint joinPoint) {
        // 获取方法上的注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        PermissionVerify permissionVerify = method.getAnnotation(PermissionVerify.class);

        // 如果方法上没有注解，尝试从类上获取
        if (permissionVerify == null) {
            permissionVerify = joinPoint.getTarget().getClass().getAnnotation(PermissionVerify.class);
        }

        if (permissionVerify == null) {
            return;
        }
        // 获取需要校验的权限
        PermissionEnum[] requiredPermissions = permissionVerify.value();
        // 如果没有指定权限，只校验是否登录
        if (requiredPermissions.length == 0) {
            return;
        }
        // 获取当前用户的权限列表
        Set<PermissionEnum> userPermissions = getCurrentUserPermissions();
        // 进行权限校验
        boolean hasPermission;
        if (permissionVerify.requireAll()) {
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

        log.debug("Permission verification passed, method: {}", method.getName());
    }
    /**
     * 获取当前用户的权限列表
     */
    private Set<PermissionEnum> getCurrentUserPermissions() {
        LoginClient.UserInfo userInfo = loginClient.getUserInfo();
        if (userInfo == null || userInfo.getUserPermissions() == null) {
            return new HashSet<>();
        }
        return userInfo.getUserPermissions();
    }
}
