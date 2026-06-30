package com.compdf.client;

import com.alibaba.druid.support.json.JSONUtils;
import com.compdf.config.ActuatorServiceManage;
import com.compdf.entity.DocSlightSettings;
import com.compdf.enums.ErrorInfoEnum;
import com.compdf.exception.ComPDFKitException;
import com.compdf.pojo.ConvertParamDTO;
import com.compdf.pojo.ParseOptionsDTO;
import com.compdf.properties.ComPDFKitProperties;
import com.compdf.service.DocSlightSettingsService;
import com.compdf.utils.FileUtils;
import com.compdf.utils.JsonLayoutConverter;
import com.compdf.utils.JsonUtils;
import com.compdf.utils.ZipUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * @author ComPDFKit-WPH 2025/3/3 0003
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ActuatorClient {

    private final static RestTemplate restTemplate = new RestTemplate();
    private static final String HTTP_PREFIX = "http://";
    private final ComPDFKitProperties properties;

    /**
     * 根据服务器ID获取执行器服务的URL
     * <p>
     * 此方法旨在简化获取执行器服务URL的过程它通过调用ActuatorServiceManage类中的静态方法
     * 来获取API的URL，然后在前面添加HTTP前缀以形成完整的URL
     *
     * @return 返回格式化的执行器服务URL，包括HTTP前缀和从ActuatorServiceManage获取的URL
     */
    public String getActuatorServiceUrl(String taskId) {
        // 从ActuatorServiceManage类获取API执行器服务的URL
        String url;
        if (StringUtils.isEmpty(taskId)) {
            url = ActuatorServiceManage.getApiActuatorServiceUrl();
        } else {
            url = ActuatorServiceManage.getTaskActuatorServiceUrl(taskId);
        }
        // 在获取的URL前添加HTTP前缀，构造完整的URL并返回
        return HTTP_PREFIX + url;
    }

    /**
     * 本地文件转换
     *
     * @param file         文件
     * @param convertParam 文件处理参数
     * @return 结果文件
     */
    public File convertLocalFile(File file, ConvertParamDTO convertParam, String taskId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        LinkedMultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("conversion_config", JsonUtils.getJsonString(convertParam));
        formData.add("file_path", file.getPath());
        String result = restTemplate.postForObject(getActuatorServiceUrl(taskId) + "/conversion/sync_local_file", new HttpEntity<>(formData, headers), String.class);
        Map<?, ?> map = JsonUtils.jsonStringToBean(result, Map.class);
        if (map.get("code").equals("200")) {
            return new File(((Map<?, ?>) map.get("data")).get("result").toString());
        } else {
            throw new ComPDFKitException(map.get("code").toString(), map.get("message").toString());
        }
    }

    /**
     * PDF文件拆分
     *
     * @param rustFsId     RustFs文件ID
     * @param convertParam 转换参数（包含拆分参数和模式）
     * @param taskId       任务ID
     * @return 拆分后的ZIP文件
     * @throws ComPDFKitException 当参数无效、服务调用失败或文件写入失败时抛出
     */
    public File pdfFileSplit(String rustFsId, ConvertParamDTO convertParam, String taskId) {
        // 参数校验
        if (!StringUtils.hasText(rustFsId)) {
            throw new ComPDFKitException(ErrorInfoEnum.PARAM_VALIDATE_ERROR);
        }
        if (convertParam == null) {
            throw new ComPDFKitException(ErrorInfoEnum.ERROR_FILE_PARAMETER);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        LinkedMultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("rustfs_path", rustFsId);
        formData.add("arg", convertParam.getSplitArg());
        formData.add("mode", convertParam.getSplitMode());
        formData.add("label", convertParam.getSplitLabel());
        formData.add("separator", convertParam.getSplitSeparator());
        formData.add("original_name_first", convertParam.getSplitSeparator());

        byte[] responseData;
        try {
            responseData = restTemplate.postForObject(
                    getActuatorServiceUrl(taskId)
                            /*"http://192.168.20.11:8005"*/ + "/pdf/split",
                    new HttpEntity<>(formData, headers),
                    byte[].class
            );
        } catch (RestClientException e) {
            log.error(e.getMessage(), e);
            throw new ComPDFKitException(ErrorInfoEnum.ERROR_INNER);
        }

        // 响应数据校验
        if (responseData == null || responseData.length == 0) {
            throw new ComPDFKitException(ErrorInfoEnum.SPLIT_ERROR);
        }

        // 使用配置的临时目录创建文件
        String outputDir = properties.getTmpPath();
        Path outputPath;
        if (StringUtils.hasText(outputDir)) {
            outputPath = Paths.get(outputDir, FileUtils.getRandomFileName("split_") + ".zip");
        } else {
            outputPath = Paths.get(FileUtils.getFileRandomName() + ".zip");
        }
        try {
            // 确保父目录存在
            Files.createDirectories(outputPath.getParent());
            Files.write(outputPath, responseData, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new ComPDFKitException(ErrorInfoEnum.SPLIT_ERROR);
        }

        return outputPath.toFile();
    }

    public File convertToPDF(File file) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        LinkedMultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("file", new FileSystemResource(file));

        byte[] responseData;
        try {
            responseData = restTemplate.postForObject(
                    getActuatorServiceUrl(null) + "/conversion/anything2pdf",
                    new HttpEntity<>(formData, headers),
                    byte[].class
            );
        } catch (RestClientException e) {
            throw new ComPDFKitException(ErrorInfoEnum.ERROR_INNER);
        }

        // 响应数据校验
        if (responseData == null || responseData.length == 0) {
            throw new ComPDFKitException(ErrorInfoEnum.SPLIT_ERROR);
        }

        // 使用配置的临时目录创建文件
        String outputDir = properties.getTmpPath();
        Path outputPath;
        if (StringUtils.hasText(outputDir)) {
            outputPath = Paths.get(outputDir, FileUtils.getRandomFileName("2pdf_") + ".pdf");
        } else {
            outputPath = Paths.get(FileUtils.getFileRandomName() + ".pdf");
        }
        try {
            // 确保父目录存在
            Files.createDirectories(outputPath.getParent());
            Files.write(outputPath, responseData, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new ComPDFKitException(ErrorInfoEnum.SPLIT_ERROR);
        }

        return outputPath.toFile();
    }


    /**
     * 文件转换 - 文件流方式
     *
     * @param file         文件
     * @param convertParam 文件处理参数
     * @return 结果文件
     */
    public File convertFileStream(File file, ConvertParamDTO convertParam, String taskId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        LinkedMultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("conversion_config ", JSONUtils.toJSONString(convertParam));
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            InputStreamResource inputStreamResource = new InputStreamResource(fileInputStream) {
                @Override
                public long contentLength() throws IOException {
                    return fileInputStream.available();
                }

                @Override
                public String getFilename() {
                    return file.getName();
                }
            };
            formData.add("file", inputStreamResource);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        String outputFilePath = null;
        try {
            ResponseEntity<byte[]> responseEntity = restTemplate.postForEntity(getActuatorServiceUrl(taskId) + "conversion/sync_upload/", new HttpEntity<>(formData, headers), byte[].class);
            outputFilePath = properties.getTmpPath() + "/" + getFileName(responseEntity.getHeaders());
            Files.write(Paths.get(outputFilePath), Objects.requireNonNull(responseEntity.getBody()));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return new File(outputFilePath);
    }

    private static String getFileName(HttpHeaders headers) {
        String disposition = headers.getFirst(HttpHeaders.CONTENT_DISPOSITION);
        if (disposition != null && disposition.contains("attachment")) {
            String[] tokens = disposition.split(";");
            for (String token : tokens) {
                if (token.trim().startsWith("filename")) {
                    return token.substring(token.indexOf('=') + 1).trim().replace("\"", "");
                }
            }
        }
        return FileUtils.getFileRandomName();
    }


    public File convertLocalFileLayout(String rustFsId) {
        return convertLocalFileLayout(rustFsId, null);
    }

    private final RustFsClient rustFsClient;
    private final DocSlightSettingsService docSlightSettingsService;
    public File convertLocalFileLayout(String rustFsId, ParseOptionsDTO parseOptions) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        LinkedMultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        DocSlightSettings settings = docSlightSettingsService.getSettings();
        Path path = Paths.get(properties.getTmpPath() + "/" + UUID.randomUUID() + "_extract_template.pdf");
        try(InputStream inputStream = rustFsClient.downloadFile(rustFsId)){
            Files.write(path,
                    IOUtils.toByteArray(inputStream),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }catch (IOException e){
            throw new ComPDFKitException(ErrorInfoEnum.PARSING_ERROR);
        }

        formData.add("file", new FileSystemResource(path));
        formData.add("api_key", settings.getApikey());
        formData.add("mode", settings.getModel().toLowerCase());
        formData.add("base_url", "https://api-server.compdf.com");
        formData.add("local_llm_provider", settings.getLocalLlmProvider());
        formData.add("local_llm_model", settings.getLocalLlmModel());
        formData.add("local_llm_base_url", settings.getLocalLlmBaseUrl());
        formData.add("local_llm_api_key", settings.getLocalLlmApiKey());
        String url = properties.getDocSlightHost()+"/api/parse";


        try {

            ResponseEntity<byte[]> resp = restTemplate.postForEntity(
                    url,
                    new HttpEntity<>(formData, headers),
                    byte[].class
            );

            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null || resp.getBody().length == 0) {
                throw new ComPDFKitException(ErrorInfoEnum.PARSING_ERROR);
            }

            Path parent = Paths.get(properties.getTmpPath() + "/layout/" + UUID.randomUUID());
            Files.createDirectories(parent);
            String outName = FileUtils.getRandomFileName("layout") + ".zip";
            Path outPath = parent.resolve(outName);

            Files.write(outPath, resp.getBody(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            String unZipOut = outPath.toString().replace(".zip", "");
            ZipUtil.unZip(outPath.toString(), unZipOut);
            File[] files = new File(unZipOut).listFiles(f -> f.getName().endsWith(".md"));
            if (files != null && files.length > 0) {
                File mdFile = files[0];
                Path target = Paths.get(mdFile.getParentFile().getPath().concat("/result_md.md"));
                Files.move(mdFile.toPath(), target);
                mdFile = target.toFile();
                JsonLayoutConverter.markdownToTxt(new String(Files.readAllBytes(mdFile.toPath())), mdFile.getPath().replace("_md.md", "_txt.txt"));
            }
            FileUtils.deleteFile(outPath);
            ZipUtil.zipFolder(unZipOut, outPath.toString());
            return outPath.toFile();
        } catch (RestClientException | IOException e) {
            throw new ComPDFKitException(ErrorInfoEnum.PARSING_ERROR);
        }
    }

    public File convertLocalFileParseByFileStream(File pdf) {
        return convertLocalFileParseByFileStream(pdf, null);
    }

    public File convertLocalFileParseByFileStream(File pdf, ParseOptionsDTO parseOptions) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        LinkedMultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("file", new FileSystemResource(pdf));
        if (parseOptions != null) {
            formData.add("options_json", JsonUtils.getJsonString(parseOptions));
        }

        try {

            ResponseEntity<byte[]> resp = restTemplate.postForEntity(
                    getActuatorServiceUrl(null)
                            + "/llm_parser?imageType=url",
                    new HttpEntity<>(formData, headers),
                    byte[].class
            );

            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null || resp.getBody().length == 0) {
                throw new ComPDFKitException(ErrorInfoEnum.PARSING_ERROR);
            }

            Path parent = Paths.get(properties.getTmpPath() + "/layout/" + UUID.randomUUID());
            Files.createDirectories(parent);
            String outName = FileUtils.getRandomFileName("layout") + ".zip";
            Path outPath = parent.resolve(outName);

            Files.write(outPath, resp.getBody(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            String unZipOut = outPath.toString().replace(".zip", "");
            ZipUtil.unZip(outPath.toString(), unZipOut);
            File[] files = new File(unZipOut).listFiles(f -> f.getName().endsWith(".md"));
            if (files != null && files.length > 0) {
                File mdFile = files[0];
                Path target = Paths.get(mdFile.getParentFile().getPath().concat("/result_md.md"));
                Files.move(mdFile.toPath(), target);
                mdFile = target.toFile();
                JsonLayoutConverter.markdownToTxt(new String(Files.readAllBytes(mdFile.toPath())), mdFile.getPath().replace("_md.md", "_txt.txt"));
            }
            FileUtils.deleteFile(outPath);
            ZipUtil.zipFolder(unZipOut, outPath.toString());
            return outPath.toFile();
        } catch (RestClientException | IOException e) {
            throw new ComPDFKitException(ErrorInfoEnum.PARSING_ERROR);
        }
    }


    public List<File> teamDownFile(String jsonString) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // /v1/team_space/file/download
        try {
            ResponseEntity<byte[]> resp = restTemplate.postForEntity(
                    getActuatorServiceUrl(null)
                            /*"http://192.168.20.11:8001"*/ + "/v1/team_space/file/download",
                    new HttpEntity<>(jsonString, headers),
                    byte[].class
            );
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null || resp.getBody().length == 0) {
                throw new ComPDFKitException(ErrorInfoEnum.ERROR_FILE_DOWNLOAD);
            }
            Path parent = Paths.get(properties.getTmpPath() + "/team_down/" + UUID.randomUUID());
            Files.createDirectories(parent);
            String outName = FileUtils.getRandomFileName("team_down") + ".zip";
            Path outPath = parent.resolve(outName);

            Files.write(outPath, resp.getBody(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            String unZipOut = outPath.toString().replace(".zip", "");
            ZipUtil.unZip(outPath.toString(), unZipOut);
            File[] files = new File(unZipOut).listFiles();
            if (files != null && files.length > 0) {
                return Arrays.asList(files);
            } else
                throw new ComPDFKitException(ErrorInfoEnum.ERROR_FILE_DOWNLOAD);
        } catch (Exception e) {
            throw new ComPDFKitException(ErrorInfoEnum.ERROR_FILE_DOWNLOAD);
        }
    }

    public List<File> dmsDownFile(String jsonString) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // /v1/team_space/file/download
        try {
            ResponseEntity<byte[]> resp = restTemplate.postForEntity(
                    getActuatorServiceUrl(null)
                            /*"http://192.168.20.11:8001"*/ + "/v1/dms/files/download",
                    new HttpEntity<>(jsonString, headers),
                    byte[].class
            );
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null || resp.getBody().length == 0) {
                throw new ComPDFKitException(ErrorInfoEnum.ERROR_FILE_DOWNLOAD);
            }
            Path parent = Paths.get(properties.getTmpPath() + "/team_down/" + UUID.randomUUID());
            Files.createDirectories(parent);
            String outName = FileUtils.getRandomFileName("team_down") + ".zip";
            Path outPath = parent.resolve(outName);

            Files.write(outPath, resp.getBody(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            String unZipOut = outPath.toString().replace(".zip", "");
            ZipUtil.unZip(outPath.toString(), unZipOut);
            File[] files = new File(unZipOut).listFiles();
            if (files != null && files.length > 0) {
                return Arrays.asList(files);
            } else
                throw new ComPDFKitException(ErrorInfoEnum.ERROR_FILE_DOWNLOAD);
        } catch (Exception e) {
            throw new ComPDFKitException(ErrorInfoEnum.ERROR_FILE_DOWNLOAD);
        }
    }
}
