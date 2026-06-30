package com.compdf.utils;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.*;

/**
 * zip 压缩文件
 */
public class ZipUtil {
    public static String zipFolder(String sourceDir, String outputZip) throws IOException {
        // 创建输出目录（如果不存在）
        Path outputPath = Paths.get(outputZip).getParent();
        if (outputPath != null && !Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }
        Path sourcePath = Paths.get(sourceDir);

        try (FileOutputStream fos = new FileOutputStream(outputZip);
             ZipOutputStream zos = new ZipOutputStream(fos);
             Stream<Path> walk = Files.walk(sourcePath);) {

            // 使用Files.walk递归遍历文件夹

            walk
                    .filter(path -> !Files.isDirectory(path)) // 只处理文件
                    .forEach(file -> {
                        try {
                            // 计算相对路径以保持目录结构
                            String relativePath = sourcePath.relativize(file).toString();

                            // 确保使用正确的路径分隔符（Windows使用\，Zip要求使用/）
                            if (File.separatorChar != '/') {
                                relativePath = relativePath.replace(File.separatorChar, '/');
                            }

                            // 创建zip条目
                            ZipEntry zipEntry = new ZipEntry(relativePath);
                            zos.putNextEntry(zipEntry);

                            // 复制文件内容到zip
                            Files.copy(file, zos);

                            // 关闭当前条目
                            zos.closeEntry();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        }
        return outputZip;
    }

    public static String zipFile(String outFilePath, List<File> fileList) throws IOException {
        // 文件的压缩包路径
        String zipPath = outFilePath + ".zip";
        new File(zipPath).getParentFile().mkdirs();
        // 获取文件压缩包输出流
        try (OutputStream outputStream = new FileOutputStream(zipPath);
             CheckedOutputStream checkedOutputStream = new CheckedOutputStream(outputStream, new Adler32());
             ZipOutputStream zipOut = new ZipOutputStream(checkedOutputStream)) {
            for (File file : fileList) {
                // 获取文件输入流
                InputStream fileIn = new FileInputStream(file);
                // 使用 common.io中的IOUtils获取文件字节数组
                byte[] bytes = IOUtils.toByteArray(fileIn);
                // 写入数据并刷新
                zipOut.putNextEntry(new ZipEntry(file.getName()));
                zipOut.write(bytes, 0, bytes.length);
                zipOut.flush();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return zipPath;
    }

    public static File zipFileAndReturn(String outFilePath, List<File> fileList) throws IOException {
        // 文件的压缩包路径
        String zipPath = outFilePath + ".zip";
        // 获取文件压缩包输出流
        try (OutputStream outputStream = new FileOutputStream(zipPath);
             CheckedOutputStream checkedOutputStream = new CheckedOutputStream(outputStream, new Adler32());
             ZipOutputStream zipOut = new ZipOutputStream(checkedOutputStream)) {
            for (File file : fileList) {
                // 获取文件输入流
                InputStream fileIn = new FileInputStream(file);
                // 使用 common.io中的IOUtils获取文件字节数组
                byte[] bytes = IOUtils.toByteArray(fileIn);
                // 写入数据并刷新
                zipOut.putNextEntry(new ZipEntry(file.getName()));
                zipOut.write(bytes, 0, bytes.length);
                zipOut.flush();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return new File(zipPath);
    }

    public static void unZip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            Files.createDirectories(destDir.toPath());
        }
        ZipInputStream zipIn = new ZipInputStream(Files.newInputStream(Paths.get(zipFilePath)));
        ZipEntry entry = zipIn.getNextEntry();
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                extractFile(zipIn, filePath);
            } else {
                File dir = new File(filePath);
                dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }

    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        new File(filePath).getParentFile().mkdirs();
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[4096];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

}
