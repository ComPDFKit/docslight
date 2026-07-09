package com.compdf.service.impl;

import com.compdf.client.LoginClient;
import com.compdf.constant.RabbitMqConstant;
import com.compdf.entity.Asset;
import com.compdf.entity.IdpFile;
import com.compdf.enums.AssetTypeEnum;
import com.compdf.enums.ErrorInfoEnum;
import com.compdf.enums.FileStatusEnum;
import com.compdf.enums.TaskTypeEnum;
import com.compdf.exception.ComPDFKitException;
import com.compdf.pojo.*;
import com.compdf.properties.ComPDFKitProperties;
import com.compdf.service.*;
import com.compdf.utils.FileUtils;
import com.compdf.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author ComPDFKit-WPH 2025/3/19 0019
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OnlineAPIServiceImpl implements OnlineAPIService {

    private final ComIDPService comIDPService;
    private final AssetService assetService;
    private final ComPDFKitProperties properties;
    private final IdpFileService fileService;
    private final RabbitTemplate rabbitTemplate;
    private final LicenseService licenseService;
    private final LoginClient loginClient;
    @Value("${isWebsite}")
    private Boolean isWebsite;

    @Override
    @Transactional
    public DataExtractDTO onlineAPIDataExtract(MultipartFile file,
                                               FileParameterDTO fileParameter,
                                               HttpServletRequest request) {
//        List<Integer> pages = fileParameter.getPages();
//        pages.sort(Integer::compareTo);
//        for (int i = 0; i < pages.size(); i++) {
//            if (pages.get(i) < 1) {
//                pages.remove(i);
//            }
//        }
//        fileParameter.setPages(pages);
//        DataExtractDTO dataExtractDTO = comIDPService.intelligentDocumentExtractionAPI(file, fileParameter, request);
//        if (isWebsite) {
//            long asset = 1L;
//            // 扣除资产
//            String licenseId = licenseService.selectByLicenseKey(request.getHeader("API_KEY")).getId();
//            assetService.deductAsset(asset, licenseId);
//            // 增加资产使用流水记录
//            AssetStream assetStream = assetStreamService.getBaseMapper().selectOne(new LambdaQueryWrapper<AssetStream>()
//                    .eq(AssetStream::getUserId, licenseId)
//                    .orderByDesc(AssetStream::getCreatedAt).last("LIMIT 1"));
//            if (!Objects.isNull(assetStream)) {
//                assetStream.setId(null);
//                assetStream.setChangeAsset(Math.toIntExact(asset));
//                assetStream.setNowAsset(assetStream.getFinalAsset());
//                assetStream.setFinalAsset(assetStream.getFinalAsset() - Math.toIntExact(asset));
//                assetStream.setHandleUser("kdan");
//                assetStream.setType(1);
//                assetStreamService.save(assetStream);
//            }
//        }
        return null;
    }


    @Override
    public FileResultDTO apiFileResolve(MultipartFile file,
                                        FileParameterDTO fileParameter,
                                        HttpServletRequest request,
                                        HttpServletResponse response) {
        File localFile = FileUtils.multipartFileToFile(file, properties.getTmpPath(), null);
        Integer pageCount = FileUtils.getPageCount(localFile.getPath(), fileParameter.getPdfPwd());
        // 数据库添加数据记录
        IdpFile idpFile = new IdpFile();
        idpFile.setFileName(file.getOriginalFilename());
        idpFile.setFilePath(localFile.getPath());
        idpFile.setTaskType(TaskTypeEnum.LAYOUT.name());
        idpFile.setType(TaskTypeEnum.LAYOUT.name());
        idpFile.setStatus(FileStatusEnum.CREATED.getValue());
        idpFile.setPageCount(pageCount);
        idpFile.setParameter(JsonUtils.getJsonString(fileParameter));
        if (!isWebsite) {
            idpFile.setUserId(loginClient.getUserId());
        }

        fileService.save(idpFile);
        //        if (isWebsite) {
//            Long asset = Long.valueOf(idpFile.getPageCount());
//            // 扣除资产
//            assetService.deductAsset(asset, licenseService.selectByLicenseKey(request.getHeader("API_KEY")).getId());
//        }
        return comIDPService.fileResolve(idpFile, fileParameter, request);
    }

    @Override
    public AssetDTO getAsset(HttpServletRequest request) {
        return assetService.getAssetPanel(loginClient.getLeaderId(), loginClient.getUserId());
    }

    @Override
    public String asyncFileCreateDataExtract(MultipartFile file, List<String> keys, List<String> tableHandles, List<Integer> pages) {
        File localFile = FileUtils.multipartFileToFile(file, properties.getTmpPath(), null);
        // 数据库添加数据记录
        IdpFile idpFile = new IdpFile();
        idpFile.setFileName(localFile.getName());
        idpFile.setFilePath(localFile.getPath());
        idpFile.setTaskType(TaskTypeEnum.EXTRACTION.name());
        idpFile.setStatus(FileStatusEnum.PENDING_EXTRACTION.getValue());
        Map<String, String> parameters = new HashMap<String, String>() {{
            put("keys", JsonUtils.getJsonString(keys));
            put("tableHandles", JsonUtils.getJsonString(tableHandles));
            put("pages", JsonUtils.getJsonString(pages));
        }};
        idpFile.setParameter(JsonUtils.getJsonString(parameters));
        fileService.save(idpFile);
        // 发送消息
        rabbitTemplate.convertAndSend(RabbitMqConstant.API_FILE_HANDLE_EXCHANGE, RabbitMqConstant.API_FILE_HANDLE_ROUTING_KEY, idpFile.getId());
        return idpFile.getId();
    }

    //    private static final Logger LOGGER = Logger.getLogger(TextinOcrTest.class.getName());
    private static final String API_URL = "https://api.textin.com/ai/service/v1/pdf_to_markdown?apply_document_tree=1&markdown_details=1&catalog_detail=1&page_details=1&char_details=0&table_flavor=html&get_image=objects&parse_mode=scan&paratext_mode=annotation&apply_merge=1";
    private static final String APP_ID = "";
    private static final String APP_SECRET = "";

    public static String convertPdfToMarkdown(byte[] content, Boolean isBase64) {
        try {
            String httpUrl = API_URL;
            // 创建HTTP连接
            if (isBase64) {
                httpUrl = API_URL + "&image_output_type=base64str&get_image=both";
            }
            URL url = new URL(httpUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            // 设置请求头
            connection.setRequestProperty("x-ti-app-id", APP_ID);
            connection.setRequestProperty("x-ti-secret-code", APP_SECRET);
            connection.setRequestProperty("Content-Type", "application/octet-stream");

            // 写入请求体
            try (OutputStream os = connection.getOutputStream()) {
                os.write(content);
            }

            // 获取响应
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 读取响应体
                try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8.name())) {
                    scanner.useDelimiter("\\A");
                    return scanner.hasNext() ? scanner.next() : "";
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取 MultipartFile 格式的 PDF 总页数
     *
     * @param file Spring 上传文件对象
     * @return 总页数
     */
    public static int getPageCount(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream();
             PDDocument document = PDDocument.load(is)) {
            return document.getNumberOfPages();
        }
    }

    public static void main(String[] args) {
        try {
            // 获取PDF文件页数
            File f = new File("C:\\Users\\00\\Desktop\\work\\合合\\2023年永續報告書(中)台泥1101\\2023年永續報告書(中)台泥1101.pdf");
            byte[] pdfBytes = Files.readAllBytes(f.toPath());
            log.info("pdf2markdown start");
            String jsonResponse = convertPdfToMarkdown(pdfBytes, false);
            log.info("pdf2markdown jsonResponse result get");
            if (jsonResponse == null) {
                log.error("pdf2markdown jsonResponse is null");
                throw new ComPDFKitException("Processing failure, Please contact us!");
            }
            TextInPDF2MarkdownResult markdownResult = JsonUtils.jsonStringToBean(jsonResponse, TextInPDF2MarkdownResult.class);
            if (markdownResult.getCode() != 200) {
                throw new ComPDFKitException("Processing failure, Please contact us!");
            }
//            System.out.println();
//            System.out.println(markdownResult.getResult().getMarkdown());
            String s = downloadAndReplaceImages(markdownResult.getResult().getMarkdown());
            System.out.println(s);
            Files.write(Paths.get("C:\\Users\\00\\Desktop\\work\\合合\\2023年永續報告書(中)台泥1101\\2023年永續報告書(中)台泥1101.md"), s.getBytes());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ComPDFKitException("Processing failure, Please contact us!");
        }
    }

    public static String downloadAndReplaceImages(String input) throws IOException {
        // 创建图片保存目录
        Path imageDir = Paths.get("images");
        if (!Files.exists(imageDir)) {
            Files.createDirectories(imageDir);
        }

        // 使用原子计数器确保文件名唯一
        AtomicInteger counter = new AtomicInteger(1);

        // 先处理Markdown格式的图片
        input = processPattern(input, "!\\[\\]\\(([^)]+)\\)", imageDir, counter,
                (url, newPath) -> "![](" + newPath + ")");

        // 再处理HTML img标签
        input = processPattern(input, "<img\\s+src=\"([^\"]+)\"", imageDir, counter,
                (url, newPath) -> "<img src=\"" + newPath + "\"");

        return input;
    }

    private static String processPattern(String input, String regex,
                                         Path imageDir, AtomicInteger counter,
                                         UrlReplacer replacer) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String imageUrl = matcher.group(1);
            System.out.println("发现图片链接: " + imageUrl);

            try {
                // 从URL提取文件名
                String fileName = extractFileName(imageUrl);
                // 生成唯一文件名（防止冲突）
                String uniqueName = counter.getAndIncrement() + "_" + fileName;
                Path outputPath = imageDir.resolve(uniqueName);
                String relativePath = imageDir + "/" + uniqueName;

                // 下载图片
                downloadImage(imageUrl, outputPath);
                System.out.println("已下载: " + outputPath);

                // 替换为相对路径
                String replacement = replacer.replace(imageUrl, relativePath);
                matcher.appendReplacement(result, replacement);
            } catch (Exception e) {
                System.err.println("处理失败: " + imageUrl);
                // 失败时保留原始匹配
                matcher.appendReplacement(result, matcher.group());
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    @FunctionalInterface
    interface UrlReplacer {
        String replace(String originalUrl, String newPath);
    }

    private static String extractFileName(String url) {
        // 从URL获取最后部分作为文件名
        int lastSlash = url.lastIndexOf('/');
        if (lastSlash != -1 && lastSlash < url.length() - 1) {
            return url.substring(lastSlash + 1);
        }
        // 如果无法解析则使用默认名
        return "image.jpg";
    }

    private static void downloadImage(String url, Path outputPath) throws IOException {
        try (InputStream is = new URL(url).openStream()) {
            Files.copy(is, outputPath);
        }
    }


    @Override
    public Result pdf2markdown(MultipartFile file, Boolean isBase64, HttpServletRequest request) {
        try {
            // 获取PDF文件页数
            int pageCount = getPageCount(file);
            if (pageCount >= 1000) {
                throw new ComPDFKitException("The number of pages cannot exceed 1000.");
            }
            // 扣除资产
//            assetService.deductAsset((long) pageCount, licenseService.selectByLicenseKey(request.getHeader("API_KEY")).getId());

            byte[] pdfBytes = file.getBytes();
            log.info("pdf2markdown start");
            String jsonResponse = convertPdfToMarkdown(pdfBytes, isBase64);
            log.info("pdf2markdown jsonResponse result get");
            if (jsonResponse == null) {
                log.error("pdf2markdown jsonResponse is null");
                throw new ComPDFKitException("Processing failure, Please contact us!");
            }
            TextInPDF2MarkdownResult markdownResult = JsonUtils.jsonStringToBean(jsonResponse, TextInPDF2MarkdownResult.class);
            if (markdownResult.getCode() != 200) {
                throw new ComPDFKitException("Processing failure, Please contact us!");
            }
            return markdownResult.getResult();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ComPDFKitException("Processing failure, Please contact us!");
        }
    }


    @Override
    public FileResultDTO apiFileEditResolve(JsonEditDTO editDTO, HttpServletRequest request) {
        return comIDPService.apiFileEditResolve(editDTO, request);
    }

    @Override
    public FileResultDTO apiFileResolveApi(MultipartFile file, FileParameterDTO fileParameter, HttpServletRequest request, HttpServletResponse response) {
        File localFile = FileUtils.multipartFileToFile(file, properties.getTmpPath(),FileUtils.getFileName(Objects.requireNonNull(file.getOriginalFilename()))+"_comidp_batch_parse"+FileUtils.getFileExtension(file.getOriginalFilename()), null);
        Integer pageCount = FileUtils.getPageCount(localFile.getPath(), fileParameter.getPdfPwd());
        // 数据库添加数据记录
        IdpFile idpFile = new IdpFile();
        idpFile.setFileName(file.getOriginalFilename());
        idpFile.setFilePath(localFile.getPath());
        idpFile.setTaskType(TaskTypeEnum.LAYOUT.name());
        idpFile.setType(TaskTypeEnum.LAYOUT.name());
        idpFile.setStatus(FileStatusEnum.CREATED.getValue());
        idpFile.setPageCount(pageCount);
        idpFile.setParameter(JsonUtils.getJsonString(fileParameter));
        if (!isWebsite) {
            idpFile.setUserId(loginClient.getUserId());
        }

        fileService.save(idpFile);
        //        if (isWebsite) {
//            Long asset = Long.valueOf(idpFile.getPageCount());
//            // 扣除资产
//            assetService.deductAsset(asset, licenseService.selectByLicenseKey(request.getHeader("API_KEY")).getId());
//        }
        return comIDPService.fileResolveApi(idpFile, fileParameter, request);
    }

}
