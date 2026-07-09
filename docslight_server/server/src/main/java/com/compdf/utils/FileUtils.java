package com.compdf.utils;

import cn.hutool.core.util.RandomUtil;
import com.compdf.enums.ErrorInfoEnum;
import com.compdf.exception.ComPDFKitException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.jetbrains.annotations.NotNull;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.DigestUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

/**
 * @author ComPDFKit-WPH 2022/11/11
 * <p>
 * 文件处理工具类
 */
@Slf4j
public class FileUtils {
    /**
     * 获取随机文件名
     *
     * @return 随机文件名 没有后缀
     */
    public static String getFileRandomName() {
        return sysPath() + "/" + System.currentTimeMillis() + new Random().nextInt(999);
    }

    /**
     * 系统默认地址
     *
     * @return 系统默认地址
     */
    public static String sysPath() {
        return System.getProperty("java.io.tmpdir");
    }


    /**
     * multipartFile转file
     *
     * @param file file
     * @return new File
     */
    public static File multipartFileToFile(MultipartFile file, String tmpPath, Integer language) {
        return multipartFileToFile(file, tmpPath, null, language);
    }

    public static File multipartFileToFile(MultipartFile file, String tmpPath,String fileName, Integer language) {
        if (ObjectUtils.isEmpty(file) || file.getSize() <= 0) {
            throw new ComPDFKitException(ErrorInfoEnum.ERROR_FILE_NOT_NULL);
        }
        File toFile = null;
        try (InputStream ins = file.getInputStream()) {
            if (file.getSize() > 0) {
                if (StringUtils.isEmpty(fileName)){
                    toFile = File.createTempFile("temp-", "-" + RandomUtil.randomNumbers(9) +
                            getFileExtension(file.getOriginalFilename()), new File(tmpPath));
                }else {
                    toFile = new File(tmpPath + "/" + RandomUtil.randomNumbers(9) + "/" + fileName);
                    toFile.getParentFile().mkdirs();
                }

                inputStreamToFile(ins, toFile);
            }
        } catch (IOException e) {
            // 根据应用程序适当处理异常
            throw new ComPDFKitException(ErrorInfoEnum.ERROR_FILE_DOWNLOAD, language);
        }

        return toFile;
    }

    public static MultipartFile convertToMultipartFile(File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        return new MockMultipartFile(file.getName(), file.getName(), "application/octet-stream", fileInputStream);
    }

    public static String getRandomFileName(String fileName) {
        return String.valueOf(System.currentTimeMillis()).concat(String.valueOf(new Random().nextInt(999))).concat(fileName);
    }

    //获取流文件
    private static void inputStreamToFile(InputStream ins, File file) {
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(ins);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(Files.newOutputStream(file.toPath()))) {
            IOUtils.copy(bufferedInputStream, bufferedOutputStream);
        } catch (Exception e) {
            throw new ComPDFKitException(ErrorInfoEnum.ERROR_FILE_DOWNLOAD);
        }
    }

    public static String stringToFile(String fileName, String tempPath, String content) {
        String outPath = tempPath + "/" + fileName;
        try {
            Files.createDirectories(new File(outPath).getParentFile().toPath());
            Files.write(Paths.get(outPath), content.getBytes());
        } catch (IOException e) {
            throw new ComPDFKitException(ErrorInfoEnum.ERROR_FILE_DOWNLOAD);
        }
        return outPath;
    }

    /**
     * 返回文件流
     *
     * @param filePath filePath
     * @param fileName fileName
     * @param response response
     */
    public static void returnFileStream(String filePath, String fileName, HttpServletResponse response) {
        response.setContentType("application/octet-stream");
        try (BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(Paths.get(filePath)));
             BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream())) {
            // 对文件名进行URL编码
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", String.format("attachment; filename=\"%s\"; filename*=utf-8''%s", encodedFileName, encodedFileName));
            IOUtils.copy(bis, bos);
            response.flushBuffer();
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new ComPDFKitException(ErrorInfoEnum.ERROR_FILE_DOWNLOAD);
        }
    }

    /**
     * 返回文件流
     *
     * @param inputStream inputStream
     * @param fileName fileName
     * @param response response
     */
    public static void returnFileStream(InputStream inputStream, String fileName, HttpServletResponse response) {
        response.setContentType("application/octet-stream");
        try (BufferedInputStream bis = new BufferedInputStream(inputStream);
             BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream())) {
            // 对文件名进行URL编码
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", String.format("attachment; filename=\"%s\"; filename*=utf-8''%s", encodedFileName, encodedFileName));
            IOUtils.copy(bis, bos);
            response.flushBuffer();
        } catch (IOException e) {
            log.error(e.getMessage());
            // TODO 文件返回异常
            throw new ComPDFKitException(ErrorInfoEnum.ERROR_FILE_DOWNLOAD);
        }
    }

    public static String getFileExtension(String filePath) {
        File file = new File(filePath);
        String name = file.getName();
        int dotIndex = name.lastIndexOf(".");
        if (dotIndex >= 0 && dotIndex < name.length() - 1) {
            return name.substring(dotIndex);
        } else {
            return "";
        }
    }

    /**
     * 文件保存本地
     *
     * @param file      file
     * @param localPath localPath
     * @return String
     */
    public static String saveLocal(MultipartFile file, String localPath) {
        log.info("开始保存文件到本地：{}", LocalDateTime.now());
        try (InputStream inputStream = file.getInputStream()) {
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            String filePath = localPath.concat(uuid.concat("@").concat(URLEncoder.encode(Objects.requireNonNull(file.getOriginalFilename()), "UTF-8")));
            org.apache.commons.io.FileUtils.copyToFile(inputStream, new File(filePath));
            log.info("结束保存文件到本地：{}", LocalDateTime.now());
            return filePath;
        } catch (IOException e) {
            throw new ComPDFKitException(ErrorInfoEnum.ERROR_FILE_DOWNLOAD);
        }
    }

    public static String copyToTemporaryFile(String sourceFilePath) throws IOException {
        File sourceFile = new File(sourceFilePath);

        // 创建临时文件
        File temporaryFile = new File(getFileRandomName() + ".pdf");

        // 复制文件到临时文件
        Path sourcePath = sourceFile.toPath();
        Path temporaryPath = temporaryFile.toPath();
        Files.copy(sourcePath, temporaryPath, StandardCopyOption.REPLACE_EXISTING);

        // 返回临时文件路径
        return temporaryFile.getAbsolutePath();
    }

    public static String getFileMD5(InputStream inputStream) throws IOException {
        return DigestUtils.md5DigestAsHex(inputStream);
    }

    public static String getFileName(String name) {
        return name.substring(0, name.lastIndexOf("."));
    }

    public static String getFileDownUrl(String filePath, HttpServletRequest request) {
        if (request == null){
            return filePath;
        }
        String smartBaseUrl = getSmartBaseUrl(request);
        try {
            return smartBaseUrl + "/api/idp/get-file?path=" + URLEncoder.encode(filePath,"utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new ComPDFKitException(ErrorInfoEnum.PARSING_ERROR);
        }
    }

    public static String getFileDownUrl(String fileId, Integer flag, HttpServletRequest request) {
        if (request == null){
            return fileId;
        }
        String smartBaseUrl = getSmartBaseUrl(request);
        try {
            return smartBaseUrl + "/api/idp/get-file?id=" + URLEncoder.encode(fileId,"utf-8") + "&flag=" + flag;
        } catch (UnsupportedEncodingException e) {
            throw new ComPDFKitException(ErrorInfoEnum.PARSING_ERROR);
        }
    }



    public static String getSmartBaseUrl(HttpServletRequest request) {
        // 优先检查代理头
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        String forwardedHost = request.getHeader("X-Forwarded-Host");
        String forwardedPort = request.getHeader("X-Forwarded-Port");
//        log.info("X-Forwarded-Proto: {}, X-Forwarded-Host: {}, X-Forwarded-Port: {}", forwardedProto, forwardedHost, forwardedPort);
        if (forwardedHost != null) {
            // 处理协议（默认为 http）
            String scheme = (forwardedProto != null) ? forwardedProto : "http";

            // 关键修复：检查 forwardedHost 是否已包含端口
            if (forwardedHost.contains(":")) {
                return scheme + "://" + forwardedHost; // 直接使用 host:port 格式
            }

            // 处理端口逻辑
            if (forwardedPort != null) {
                // 明确排除默认端口
                if (("http".equals(scheme) && "80".equals(forwardedPort)) ||
                        ("https".equals(scheme) && "443".equals(forwardedPort))) {
                    return scheme + "://" + forwardedHost;
                }
                return scheme + "://" + forwardedHost + ":" + forwardedPort;
            }

            // 无端口头时根据协议推断默认端口（不显示）
            return scheme + "://" + forwardedHost;
        }

        // 无代理头时使用 request 原生方法
        return request.getScheme() + "://" +
                request.getServerName() +
                (isStandardPort(request) ? "" : ":" + request.getServerPort());
    }

    // 辅助方法：检查是否标准端口
    private static boolean isStandardPort(HttpServletRequest request) {
        int port = request.getServerPort();
        return port == 80 || port == 443;
    }

    /**
     * 获取文件页数
     * @param path 文件 PDF或图片
     * @return 文件页数
     */
    public static Integer getPageCount(String path) {
        return getPageCount(path,"");
    }

    public static Integer getPageCount(String path, String password) {
        String newPath = path.toLowerCase();
        if (newPath.endsWith(".pdf")) {
            try (PDDocument document = PDDocument.load(new File(path), password)) {
                return document.getNumberOfPages();
            } catch (IOException e) {
                throw new ComPDFKitException(ErrorInfoEnum.ERROR_FILE_DOWNLOAD);
            }
        } else if (newPath.endsWith(".png") || newPath.endsWith(".jpg") || newPath.endsWith(".jpeg")) {
            return 1;
        }
        return null;
    }


    public static File imageCropped(String inputImagePath, String outputImagePath, int x, int y, int width, int height){
        try {
            // 读取原始图片
            BufferedImage originalImage = ImageIO.read(new File(inputImagePath));
            // 执行裁剪操作
            BufferedImage croppedImage = originalImage.getSubimage(x, y, width, height);
            // 保存裁剪后的图片
            File output = new File(outputImagePath);
            ImageIO.write(croppedImage, "png", output);
            return output;
        } catch (IOException e) {
            throw new ComPDFKitException("Cannot read image:" + e.getMessage());
        } catch (RasterFormatException e) {
            throw new ComPDFKitException("Crop area outside the image:" + e.getMessage());
        }

    }

    public static void deleteFolder(Path path) {

        try {
            if (Files.exists(path)) {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @NotNull
                    @Override
                    public FileVisitResult visitFile(Path file, @NotNull BasicFileAttributes attrs)
                            throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @NotNull
                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                            throws IOException {
                        if (exc != null) {
                            throw exc;
                        }
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        } catch (Exception e) {
            log.error("Failed to delete folder: {}", path);
        }
    }

    public static void deleteFile(Path filePath) {
        try {
            Files.delete(filePath);
        } catch (Exception e) {
            log.error("Failed to delete file: {}", filePath);
        }
    }
}
