package com.compdf.aspect;

import com.compdf.client.LoginClient;
import com.compdf.entity.License;
import com.compdf.enums.ErrorInfoEnum;
import com.compdf.exception.ComPDFKitException;
import com.compdf.properties.ComPDFKitProperties;
import com.compdf.service.LicenseService;
import com.compdf.utils.JsonUtils;
import com.compdfkit.auth.AuthHttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author ComPDFKit-WPH 2023/8/17
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class LicenseAuthAspect {

    private final StringRedisTemplate redisTemplate;
    private final LicenseService licenseService;
    private final LoginClient loginClient;
    @Value("${isWebsite}")
    private Boolean isWebsite;
    private final ComPDFKitProperties properties;

    @Pointcut("execution (* com.compdf.controller.*.*(..))")
    public void point() {
    }

    @Before("point() || @annotation(com.compdf.annotation.LicenseAuth)")
    public void apiKeyRequest() {
        if (isWebsite){
            // 获取本次请求请求头API_KEY
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String API_KEY = request.getHeader("API_KEY");
                if (StringUtils.isEmpty(API_KEY)){
                    throw new ComPDFKitException(ErrorInfoEnum.API_KEY_ERROR);
                }
                String license = AuthHttpClient.getInstance(API_KEY).getAuthLicense();
                if(!redisTemplate.hasKey(API_KEY)) {
                    License dbLicense = licenseService.selectByLicenseKey(API_KEY);
                    if (Objects.isNull(dbLicense)){
                        throw new ComPDFKitException(ErrorInfoEnum.API_KEY_ERROR);
                    }
                    redisTemplate.opsForValue().setIfAbsent(API_KEY, license, 1, TimeUnit.DAYS);
                }
            }
        }
    }

    @Before("point() || @annotation(com.compdf.annotation.LicenseAuth)")
    public void loginRequest() {
        if (!isWebsite){
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                if (request.getRequestURI().endsWith("get-file")) {
                    return;
                }
                if (request.getRequestURI().endsWith("parse") || request.getRequestURI().endsWith("extractOnlineAPIUrlMode") || request.getRequestURI().endsWith("parseOnlineAPIUrlMode") || request.getRequestURI().endsWith("convertToPdf") || request.getRequestURI().endsWith("parse-api")|| request.getRequestURI().endsWith("extract-api")) {
                    return;
                }
                if (request.getRequestURI().endsWith("data-extract-demo")) {
                    request.setAttribute("USER_ID", "demo_user");
                    return;
                }
                if (request.getRequestURI().endsWith("data-extract-api") || request.getRequestURI().endsWith("api-file-resolve-api")) {
                    String authorization = request.getHeader("api_key");
                    if(!Objects.equals(authorization, properties.getLicenseKey())) {
                        throw new ComPDFKitException(ErrorInfoEnum.LOGIN_401);
                    }
                    request.setAttribute("USER_ID", "api_user");
                    return;
                }
//                String AUTHORIZATION = request.getHeader("Authorization");
//                if (StringUtils.isEmpty(AUTHORIZATION)) {
//                    throw new ComPDFKitException(ErrorInfoEnum.LOGIN_401);
//                }
                String userId = "187a8e06f80211f09ce810ffe0d13cd8";
                LoginClient.UserInfo userInfo;
                try {
                    userInfo = JsonUtils.jsonStringToBean(USER_INFO_JSON, LoginClient.UserInfo.class);
                    userId = userInfo.getId();
                } catch (Exception e) {
                    log.error(e.getMessage(),e);
                    throw new ComPDFKitException(ErrorInfoEnum.LOGIN_401);
                }
                request.setAttribute("USER_ID", userId);
                request.setAttribute("USER_INFO", JsonUtils.getJsonString(userInfo));
            }
        }
    }

    private static final String USER_INFO_JSON = "{\n" +
            "  \"email\" : \"admin@admin.com\",\n" +
            "  \"id\" : \"187a8e06f80211f09ce810ffe0d13cd8\",\n" +
            "  \"nickname\" : \"admin\",\n" +
            "  \"role\" : \"admin\",\n" +
            "  \"permissions\" : [ {\n" +
            "    \"action_type\" : 0,\n" +
            "    \"children\" : [ {\n" +
            "      \"action_type\" : 2,\n" +
            "      \"code\" : \"extract:upload\",\n" +
            "      \"id\" : \"01010000\",\n" +
            "      \"level\" : 2,\n" +
            "      \"module_id\" : \"1\",\n" +
            "      \"name\" : \"upload files\",\n" +
            "      \"parent_id\" : \"01000000\",\n" +
            "      \"status\" : \"1\"\n" +
            "    }, {\n" +
            "      \"action_type\" : 2,\n" +
            "      \"code\" : \"extract:check\",\n" +
            "      \"id\" : \"01020000\",\n" +
            "      \"level\" : 2,\n" +
            "      \"module_id\" : \"1\",\n" +
            "      \"name\" : \"check result\",\n" +
            "      \"parent_id\" : \"01000000\",\n" +
            "      \"status\" : \"1\"\n" +
            "    }, {\n" +
            "      \"action_type\" : 2,\n" +
            "      \"code\" : \"extract:delete\",\n" +
            "      \"id\" : \"01030000\",\n" +
            "      \"level\" : 2,\n" +
            "      \"module_id\" : \"1\",\n" +
            "      \"name\" : \"delete files\",\n" +
            "      \"parent_id\" : \"01000000\",\n" +
            "      \"status\" : \"1\"\n" +
            "    }, {\n" +
            "      \"action_type\" : 1,\n" +
            "      \"children\" : [ {\n" +
            "        \"action_type\" : 2,\n" +
            "        \"code\" : \"extract:template:create\",\n" +
            "        \"id\" : \"01040100\",\n" +
            "        \"level\" : 3,\n" +
            "        \"module_id\" : \"1\",\n" +
            "        \"name\" : \"create templates\",\n" +
            "        \"parent_id\" : \"01040000\",\n" +
            "        \"status\" : \"1\"\n" +
            "      }, {\n" +
            "        \"action_type\" : 2,\n" +
            "        \"code\" : \"extract:template:delete\",\n" +
            "        \"id\" : \"01040200\",\n" +
            "        \"level\" : 3,\n" +
            "        \"module_id\" : \"1\",\n" +
            "        \"name\" : \"delete templates\",\n" +
            "        \"parent_id\" : \"01040000\",\n" +
            "        \"status\" : \"1\"\n" +
            "      }, {\n" +
            "        \"action_type\" : 2,\n" +
            "        \"code\" : \"extract:template:modify\",\n" +
            "        \"id\" : \"01040300\",\n" +
            "        \"level\" : 3,\n" +
            "        \"module_id\" : \"1\",\n" +
            "        \"name\" : \"modify templates\",\n" +
            "        \"parent_id\" : \"01040000\",\n" +
            "        \"status\" : \"1\"\n" +
            "      } ],\n" +
            "      \"code\" : \"extract:template\",\n" +
            "      \"id\" : \"01040000\",\n" +
            "      \"level\" : 2,\n" +
            "      \"module_id\" : \"1\",\n" +
            "      \"name\" : \"template management\",\n" +
            "      \"parent_id\" : \"01000000\",\n" +
            "      \"status\" : \"1\"\n" +
            "    }, {\n" +
            "      \"action_type\" : 2,\n" +
            "      \"code\" : \"extract:export\",\n" +
            "      \"id\" : \"01050000\",\n" +
            "      \"level\" : 2,\n" +
            "      \"module_id\" : \"1\",\n" +
            "      \"name\" : \"export result\",\n" +
            "      \"parent_id\" : \"01000000\",\n" +
            "      \"status\" : \"1\"\n" +
            "    } ],\n" +
            "    \"code\" : \"extract\",\n" +
            "    \"id\" : \"01000000\",\n" +
            "    \"level\" : 1,\n" +
            "    \"module_id\" : \"1\",\n" +
            "    \"name\" : \"extraction page\",\n" +
            "    \"status\" : \"1\"\n" +
            "  }, {\n" +
            "    \"action_type\" : 0,\n" +
            "    \"children\" : [ {\n" +
            "      \"action_type\" : 2,\n" +
            "      \"code\" : \"parse:upload\",\n" +
            "      \"id\" : \"02010000\",\n" +
            "      \"level\" : 2,\n" +
            "      \"module_id\" : \"2\",\n" +
            "      \"name\" : \"upload files\",\n" +
            "      \"parent_id\" : \"02000000\",\n" +
            "      \"status\" : \"1\"\n" +
            "    }, {\n" +
            "      \"action_type\" : 2,\n" +
            "      \"code\" : \"parse:check\",\n" +
            "      \"id\" : \"02020000\",\n" +
            "      \"level\" : 2,\n" +
            "      \"module_id\" : \"2\",\n" +
            "      \"name\" : \"check result\",\n" +
            "      \"parent_id\" : \"02000000\",\n" +
            "      \"status\" : \"1\"\n" +
            "    }, {\n" +
            "      \"action_type\" : 2,\n" +
            "      \"code\" : \"parse:delete\",\n" +
            "      \"id\" : \"02030000\",\n" +
            "      \"level\" : 2,\n" +
            "      \"module_id\" : \"2\",\n" +
            "      \"name\" : \"delete files\",\n" +
            "      \"parent_id\" : \"02000000\",\n" +
            "      \"status\" : \"1\"\n" +
            "    }, {\n" +
            "      \"action_type\" : 2,\n" +
            "      \"code\" : \"parse:export\",\n" +
            "      \"id\" : \"02040000\",\n" +
            "      \"level\" : 2,\n" +
            "      \"module_id\" : \"2\",\n" +
            "      \"name\" : \"export files\",\n" +
            "      \"parent_id\" : \"02000000\",\n" +
            "      \"status\" : \"1\"\n" +
            "    } ],\n" +
            "    \"code\" : \"parse\",\n" +
            "    \"id\" : \"02000000\",\n" +
            "    \"level\" : 1,\n" +
            "    \"module_id\" : \"2\",\n" +
            "    \"name\" : \"parse page\",\n" +
            "    \"status\" : \"1\"\n" +
            "  }, {\n" +
            "    \"action_type\" : 0,\n" +
            "    \"children\" : [ {\n" +
            "      \"action_type\" : 2,\n" +
            "      \"code\" : \"split:upload\",\n" +
            "      \"id\" : \"03010000\",\n" +
            "      \"level\" : 2,\n" +
            "      \"module_id\" : \"3\",\n" +
            "      \"name\" : \"upload files\",\n" +
            "      \"parent_id\" : \"03000000\",\n" +
            "      \"status\" : \"1\"\n" +
            "    }, {\n" +
            "      \"action_type\" : 2,\n" +
            "      \"code\" : \"split:split\",\n" +
            "      \"id\" : \"03020000\",\n" +
            "      \"level\" : 2,\n" +
            "      \"module_id\" : \"3\",\n" +
            "      \"name\" : \"split files\",\n" +
            "      \"parent_id\" : \"03000000\",\n" +
            "      \"status\" : \"1\"\n" +
            "    }, {\n" +
            "      \"action_type\" : 2,\n" +
            "      \"code\" : \"split:delete\",\n" +
            "      \"id\" : \"03030000\",\n" +
            "      \"level\" : 2,\n" +
            "      \"module_id\" : \"3\",\n" +
            "      \"name\" : \"delete files\",\n" +
            "      \"parent_id\" : \"03000000\",\n" +
            "      \"status\" : \"1\"\n" +
            "    }, {\n" +
            "      \"action_type\" : 2,\n" +
            "      \"code\" : \"split:export\",\n" +
            "      \"id\" : \"03040000\",\n" +
            "      \"level\" : 2,\n" +
            "      \"module_id\" : \"2\",\n" +
            "      \"name\" : \"export result\",\n" +
            "      \"parent_id\" : \"03000000\",\n" +
            "      \"status\" : \"1\"\n" +
            "    } ],\n" +
            "    \"code\" : \"split\",\n" +
            "    \"id\" : \"03000000\",\n" +
            "    \"level\" : 1,\n" +
            "    \"module_id\" : \"3\",\n" +
            "    \"name\" : \"split page\",\n" +
            "    \"status\" : \"1\"\n" +
            "  }, {\n" +
            "    \"action_type\" : 0,\n" +
            "    \"children\" : [ {\n" +
            "      \"action_type\" : 1,\n" +
            "      \"code\" : \"kb:create\",\n" +
            "      \"id\" : \"04010000\",\n" +
            "      \"level\" : 2,\n" +
            "      \"module_id\" : \"4\",\n" +
            "      \"name\" : \"create a knowledgebase\",\n" +
            "      \"parent_id\" : \"04000000\",\n" +
            "      \"status\" : \"1\"\n" +
            "    }, {\n" +
            "      \"action_type\" : 1,\n" +
            "      \"code\" : \"kb:qa\",\n" +
            "      \"id\" : \"04020000\",\n" +
            "      \"level\" : 2,\n" +
            "      \"module_id\" : \"4\",\n" +
            "      \"name\" : \"knowledgebase question and answer\",\n" +
            "      \"parent_id\" : \"04000000\",\n" +
            "      \"status\" : \"1\"\n" +
            "    } ],\n" +
            "    \"code\" : \"kb\",\n" +
            "    \"id\" : \"04000000\",\n" +
            "    \"level\" : 1,\n" +
            "    \"module_id\" : \"4\",\n" +
            "    \"name\" : \"knowledgebase\",\n" +
            "    \"status\" : \"1\"\n" +
            "  }, {\n" +
            "    \"action_type\" : 0,\n" +
            "    \"children\" : [ {\n" +
            "      \"action_type\" : 2,\n" +
            "      \"code\" : \"user:create\",\n" +
            "      \"id\" : \"05010000\",\n" +
            "      \"level\" : 2,\n" +
            "      \"module_id\" : \"5\",\n" +
            "      \"name\" : \"create new users\",\n" +
            "      \"parent_id\" : \"05000000\",\n" +
            "      \"status\" : \"1\"\n" +
            "    }, {\n" +
            "      \"action_type\" : 2,\n" +
            "      \"code\" : \"user:modify\",\n" +
            "      \"id\" : \"05020000\",\n" +
            "      \"level\" : 2,\n" +
            "      \"module_id\" : \"5\",\n" +
            "      \"name\" : \"modify a user\",\n" +
            "      \"parent_id\" : \"05000000\",\n" +
            "      \"status\" : \"1\"\n" +
            "    }, {\n" +
            "      \"action_type\" : 2,\n" +
            "      \"code\" : \"user:reset\",\n" +
            "      \"id\" : \"05030000\",\n" +
            "      \"level\" : 2,\n" +
            "      \"module_id\" : \"5\",\n" +
            "      \"name\" : \"reset the password of a user\",\n" +
            "      \"parent_id\" : \"05000000\",\n" +
            "      \"status\" : \"1\"\n" +
            "    }, {\n" +
            "      \"action_type\" : 2,\n" +
            "      \"code\" : \"user:delete\",\n" +
            "      \"id\" : \"05040000\",\n" +
            "      \"level\" : 2,\n" +
            "      \"module_id\" : \"5\",\n" +
            "      \"name\" : \"delete a user\",\n" +
            "      \"parent_id\" : \"05000000\",\n" +
            "      \"status\" : \"1\"\n" +
            "    }, {\n" +
            "      \"action_type\" : 1,\n" +
            "      \"children\" : [ {\n" +
            "        \"action_type\" : 2,\n" +
            "        \"code\" : \"user:charactor:create\",\n" +
            "        \"id\" : \"05050100\",\n" +
            "        \"level\" : 3,\n" +
            "        \"module_id\" : \"5\",\n" +
            "        \"name\" : \"create a charactor\",\n" +
            "        \"parent_id\" : \"05050000\",\n" +
            "        \"status\" : \"1\"\n" +
            "      }, {\n" +
            "        \"action_type\" : 2,\n" +
            "        \"code\" : \"user:charactor:permission\",\n" +
            "        \"id\" : \"05050200\",\n" +
            "        \"level\" : 3,\n" +
            "        \"module_id\" : \"5\",\n" +
            "        \"name\" : \"charactor permission configration\",\n" +
            "        \"parent_id\" : \"05050000\",\n" +
            "        \"status\" : \"1\"\n" +
            "      }, {\n" +
            "        \"action_type\" : 2,\n" +
            "        \"code\" : \"user:charactor:modify\",\n" +
            "        \"id\" : \"05050300\",\n" +
            "        \"level\" : 3,\n" +
            "        \"module_id\" : \"5\",\n" +
            "        \"name\" : \"modify a charactor\",\n" +
            "        \"parent_id\" : \"05050000\",\n" +
            "        \"status\" : \"1\"\n" +
            "      }, {\n" +
            "        \"action_type\" : 2,\n" +
            "        \"code\" : \"user:charactor:delete\",\n" +
            "        \"id\" : \"05050400\",\n" +
            "        \"level\" : 3,\n" +
            "        \"module_id\" : \"5\",\n" +
            "        \"name\" : \"delete a charactor\",\n" +
            "        \"parent_id\" : \"05050000\",\n" +
            "        \"status\" : \"1\"\n" +
            "      } ],\n" +
            "      \"code\" : \"user:charactor\",\n" +
            "      \"id\" : \"05050000\",\n" +
            "      \"level\" : 2,\n" +
            "      \"module_id\" : \"5\",\n" +
            "      \"name\" : \"charactor management page\",\n" +
            "      \"parent_id\" : \"05000000\",\n" +
            "      \"status\" : \"1\"\n" +
            "    }, {\n" +
            "      \"action_type\" : 2,\n" +
            "      \"code\" : \"user:search\",\n" +
            "      \"id\" : \"05050600\",\n" +
            "      \"level\" : 2,\n" +
            "      \"module_id\" : \"5\",\n" +
            "      \"name\" : \"search users\",\n" +
            "      \"parent_id\" : \"05000000\",\n" +
            "      \"status\" : \"1\"\n" +
            "    } ],\n" +
            "    \"code\" : \"user\",\n" +
            "    \"id\" : \"05000000\",\n" +
            "    \"level\" : 1,\n" +
            "    \"module_id\" : \"5\",\n" +
            "    \"name\" : \"user management page\",\n" +
            "    \"status\" : \"1\"\n" +
            "  } ],\n" +
            "  \"userPermissions\" : [ \"EXTRACT_CHECK\", \"USER_CHARACTOR_PERMISSION\", \"USER_CHARACTOR_DELETE\", \"SPLIT\", \"KB\", \"EXTRACT_DELETE\", \"SPLIT_SPLIT\", \"KB_QA\", \"EXTRACT_TEMPLATE_DELETE\", \"PARSE_CHECK\", \"EXTRACT_TEMPLATE_CREATE\", \"USER_RESET\", \"EXTRACT\", \"USER_CHARACTOR_MODIFY\", \"USER_DELETE\", \"EXTRACT_TEMPLATE_MODIFY\", \"USER_CREATE\", \"PARSE_UPLOAD\", \"USER_CHARACTOR\", \"KB_CREATE\", \"EXTRACT_TEMPLATE\", \"SPLIT_UPLOAD\", \"PARSE\", \"PARSE_DELETE\", \"USER\", \"USER_CHARACTOR_CREATE\", \"EXTRACT_UPLOAD\", \"SPLIT_DELETE\", \"USER_MODIFY\" ]\n" +
            "}";

}
