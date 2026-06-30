package com.compdf.service.impl;

import com.compdf.entity.DocSlightSettings;
import com.compdf.service.DocSlightSettingsService;
import com.compdf.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;

/**
 * @author ComPDFKit-WPH 2024-09-11
 */
@Service
@RequiredArgsConstructor
public class DocSlightSettingsServiceImpl implements DocSlightSettingsService {

    private static final String DocSlightSettingsRedisKey = "DocSlight:Settings:RedisKey";
    private static final String DocSlightSettingsFilePath = "C:\\Users\\ddfme\\Downloads\\DocSlightSettings.json";
    private final StringRedisTemplate redisTemplate;

    @Override
    public DocSlightSettings getSettings() {
        // 获取缓存内容，没有获取本地文件内容，都没有返回空
        try {
            String docSlightSettings = redisTemplate.opsForValue().get(DocSlightSettingsRedisKey);
            if (StringUtils.isEmpty(docSlightSettings)) {
                // 读取本地文件
                if (Files.exists(Paths.get(DocSlightSettingsFilePath))) {
                    docSlightSettings = new String(Files.readAllBytes(Paths.get(DocSlightSettingsFilePath)));
                    if (StringUtils.isEmpty(docSlightSettings)) {
                        return null;
                    }
                } else {
                    return null;
                }
                redisTemplate.opsForValue().set(DocSlightSettingsRedisKey, docSlightSettings, Duration.ofDays(7));
            }
            return JsonUtils.jsonStringToBean(docSlightSettings, DocSlightSettings.class);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DocSlightSettings updateSettings(DocSlightSettings docSlightSettings) {
        // 写入DocSlightSettingsFilePath，更新缓存，返回结果
        try {
            String docSlightSettingsStr = JsonUtils.getJsonString(docSlightSettings);
            Files.write(Paths.get(DocSlightSettingsFilePath), docSlightSettingsStr.getBytes()
                    , StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            redisTemplate.opsForValue().set(DocSlightSettingsRedisKey, docSlightSettingsStr, Duration.ofDays(7));
            return docSlightSettings;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}