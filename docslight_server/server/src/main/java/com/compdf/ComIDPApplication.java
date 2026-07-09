package com.compdf;

import com.compdf.config.YmlPropertiesConfig;
import com.compdf.license.LicenseNative;
import com.compdf.license.enums.ExecutionMode;
import com.compdf.license.utils.LicenseUtils;
import com.compdf.properties.ComPDFKitProperties;
import com.compdf.service.IDPServerService;
import com.compdf.service.IdpServiceService;
import com.compdf.service.impl.TemplateServiceImpl;
import com.compdf.utils.FileUtils;
import com.compdfkit.auth.AuthHttpClient;
import com.compdfkit.auth.DeviceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * @author ComPDFKit-WPH 2025/2/19 0019
 */
@SpringBootApplication
@EnableConfigurationProperties({ComPDFKitProperties.class})
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
@MapperScan("com.compdf.mapper")
public class ComIDPApplication implements ApplicationRunner {

    private final IDPServerService idpServerService;
    private final IdpServiceService idpserviceService;
    private final ComPDFKitProperties comPDFKitProperties;
    private final TemplateServiceImpl templateService;
    @Value("${isWebsite}")
    private Boolean isWebsite;

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(ComIDPApplication.class, args);
        YmlPropertiesConfig.setApplicationContext(applicationContext);
        log.info("Running ComPDF AI. port(s) {} (http)", applicationContext.getEnvironment().getProperty("server.port"));
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        String serverID = idpServerService.initServer();
        idpserviceService.initService(serverID);
        Files.createDirectories(Paths.get(comPDFKitProperties.getTmpPath()));

//        if (!isWebsite) {
//            ExecutionMode executionMode = ExecutionMode.getByModel(comPDFKitProperties.getExecutionMode());
//            log.info("The current mode is: {}", executionMode.getModeString());
//            String licenseKey = AuthHttpClient.getInstance(comPDFKitProperties.getLicense()).getAuthLicense();
//            if (!StringUtils.isEmpty(licenseKey)) {
//                try {
//                    new LicenseNative().initBlueLibrary(licenseKey, DeviceUtils.getDeviceID(),
//                            comPDFKitProperties.getLicenseBoundId(), executionMode, comPDFKitProperties.getLanguage());
//                } catch (Exception e) {
//                    // 报错重试一次
//                    LicenseUtils.refreshLicense(comPDFKitProperties);
//                }
//            } else {
//                new LicenseNative().initBlueLibrary(comPDFKitProperties.getLicenseKey(), DeviceUtils.getDeviceID(), comPDFKitProperties.getLicenseBoundId(), executionMode, comPDFKitProperties.getLanguage());
//            }
//        }

        // 读取resources/templateFile下所有文件（最终从jar包中获取），上传到rustfs中
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] templateResources = resolver.getResources("classpath:templateFile/*");
        Path tmpDir = Paths.get(comPDFKitProperties.getTmpPath(), "templateFile");
        Files.createDirectories(tmpDir);
        for (Resource resource : templateResources) {
            String filename = resource.getFilename();
            if (filename == null) {
                continue;
            }
            Path tmpFile = tmpDir.resolve(filename);
            try (InputStream is = resource.getInputStream()) {
                Files.copy(is, tmpFile, StandardCopyOption.REPLACE_EXISTING);
            }

            templateService.addDefulotTemplateFile(tmpFile.toFile(), FileUtils.getFileName(filename), 1);
            Files.deleteIfExists(tmpFile);
        }
//        Files.deleteIfExists(tmpDir);
        log.info("Template file upload completed, total: {}", templateResources.length);
    }

}
