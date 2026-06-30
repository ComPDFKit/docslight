package com.compdf.client;

import com.compdf.constant.RedisConstant;
import com.compdf.enums.PermissionEnum;
import com.compdf.service.UserService;
import com.compdf.utils.JsonUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author ComPDFKit-WPH 2025/6/20 星期五
 */
@Component
@RequiredArgsConstructor
public class LoginClient {

    @Value("${comidp.config.rag_engine}")
    private String ragLoginUrl;

    private final StringRedisTemplate redisTemplate;
    private final UserService userService;

    private static final RestTemplate restTemplate = new RestTemplate();

    public UserInfo getUserInfo(String authorization) {
        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorization);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                ragLoginUrl,
                HttpMethod.GET,
                entity,
                String.class
        );
        Map<?,?> userInfoResult = JsonUtils.jsonStringToBean(response.getBody(), Map.class);
        UserInfo data = JsonUtils.jsonStringToBean(JsonUtils.getJsonString(userInfoResult.get("data")), UserInfo.class);

        // 解析permissions字符串为Set<PermissionEnum>
        String permissions = JsonUtils.getJsonString(data.getPermissions());
        Set<PermissionEnum> userPermissions = parsePermissions(permissions);
        data.setUserPermissions(userPermissions);

        return data;
    }

    /**
     * 解析permissions JSON字符串为Set<PermissionEnum>
     * 递归解析父级和children中的code
     *
     * @param permissions JSON格式的权限字符串
     * @return 权限枚举集合
     */
    private Set<PermissionEnum> parsePermissions(String permissions) {
        Set<PermissionEnum> result = new HashSet<>();
        if (permissions == null || permissions.isEmpty()) {
            return result;
        }

        try {
            List<Map<String, Object>> permissionList = JsonUtils.jsonStringToBean(permissions, List.class);
            if (permissionList != null) {
                extractPermissionCodes(permissionList, result);
            }
        } catch (Exception e) {
            // 解析失败返回空集合
            return result;
        }

        return result;
    }

    /**
     * 递归提取权限code
     *
     * @param permissionList 权限列表
     * @param result         结果集合
     */
    @SuppressWarnings("unchecked")
    private void extractPermissionCodes(List<Map<String, Object>> permissionList, Set<PermissionEnum> result) {
        if (permissionList == null) {
            return;
        }

        for (Map<String, Object> item : permissionList) {
            // 提取当前节点的code
            Object codeObj = item.get("code");
            if (codeObj != null) {
                String code = codeObj.toString();
                PermissionEnum permissionEnum = PermissionEnum.getByCode(code);
                if (permissionEnum != null) {
                    result.add(permissionEnum);
                }
            }

            // 递归处理children
            Object children = item.get("children");
            if (children instanceof List) {
                extractPermissionCodes((List<Map<String, Object>>) children, result);
            }
        }
    }

    @Data
    public static class UserInfo {
        private String email;
        private String id;
        private String nickname;
        private String leader_id;
        private String role;
        private List<Object> permissions;
        private Set<PermissionEnum> userPermissions;
    }

    public String getUserId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return request.getAttribute("USER_ID").toString();
        }
        return null;
    }

    public UserInfo getUserInfo() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            Object userInfoObj = request.getAttribute("USER_INFO");
            if (userInfoObj != null) {
                UserInfo userInfo = JsonUtils.jsonStringToBean(userInfoObj.toString(), UserInfo.class);
                if (userInfo != null && userInfo.getUserPermissions() == null) {
                    // 如果userPermissions为空，尝试从permissions解析
                    String permissions = JsonUtils.getJsonString(userInfo.getPermissions());
                    userInfo.setUserPermissions(parsePermissions(permissions));
                }
                return userInfo;
            }
        }
        return null;
    }

    public String getRole() {
        UserInfo userInfo = getUserInfo();
        return userInfo != null ? userInfo.getRole() : null;
    }

    public String getLeaderId() {
        UserInfo userInfo = getUserInfo();
        if (Objects.equals(userInfo.getRole(), "user")) {
            return userInfo.getLeader_id();
        } else  {
            return userInfo.getId();
        }
    }

    public List<String> getTeamUserIds() {
        String leaderId = getLeaderId();
        return userService.selectTeamUserIds(leaderId);
    }

    public Boolean isFirstExtract() {
        String userId = getUserId();
        String key = RedisConstant.FIRST_EXTRACT + userId;
        if (redisTemplate.hasKey(key)) {
            return false;
        } else {
            redisTemplate.opsForValue().setIfAbsent(key, "1");
            return true;
        }
    }

}
