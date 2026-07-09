package com.compdf.config;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.internal.OSSHeaders;
import com.aliyun.oss.model.*;
import com.compdf.exception.ComPDFKitException;
import com.compdf.properties.OssProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

/**
 * @author ZhouQiang 2022/7/13
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OssFileClient {

    private final OssProperties ossProperties;
    private final OSS ossClient;

    /**
     * 普通上传
     *
     * @param bytes    文件字节数组
     * @param fileName 文件名
     * @return fileKey
     * @throws IOException 异常
     */
    public String upload(byte[] bytes, String fileName) throws IOException {
        String fileKey = generateFileKey(fileName);
        PutObjectRequest request = new PutObjectRequest(ossProperties.getBucketName(), fileKey, new ByteArrayInputStream(bytes));
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setHeader(OSSHeaders.CONTENT_DISPOSITION, "attachment;filename="+
                URLEncoder.encode(fileName,"UTF-8")+";filename*=UTF-8''" +
                URLEncoder.encode(fileName,"UTF-8"));
        request.setMetadata(metadata);
        ossClient.putObject(request);
        Arrays.fill(bytes, (byte) 0);
        return fileKey;
    }

    /**
     * 普通上传
     *
     * @param file 文件
     * @return fileKey
     * @throws IOException 异常
     */
    public String upload(MultipartFile file) throws IOException {
        String fileKey = generateFileKey(file.getOriginalFilename());
        PutObjectRequest request = new PutObjectRequest(ossProperties.getBucketName(), fileKey, file.getInputStream());
        ossClient.putObject(request);
        return fileKey;
    }

    public String upload(File file) throws IOException {
        String fileKey = generateFileKey(file.getName());
        FileInputStream fileInputStream = new FileInputStream(file);
        PutObjectRequest request = new PutObjectRequest(ossProperties.getBucketName(), fileKey, fileInputStream);
        ossClient.putObject(request);
        return fileKey;
    }

    /**
     * 普通上传
     *
     * @param filePath 文件路径
     * @param fileName 文件名
     * @return fileKey 文件key
     * @throws IOException 异常
     */
    public String upload(String filePath, String fileName) throws IOException {
        String fileKey = generateFileKey(fileName);
        PutObjectRequest request = new PutObjectRequest(ossProperties.getBucketName(), fileKey, new BufferedInputStream(new FileInputStream(filePath)));
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setHeader(OSSHeaders.CONTENT_DISPOSITION, "attachment;filename="+
                URLEncoder.encode(fileName,"UTF-8")+";filename*=UTF-8''" +
                URLEncoder.encode(fileName,"UTF-8"));
        request.setMetadata(metadata);
        ossClient.putObject(request);
        return fileKey;
    }

    /**
     * 分片上传
     *
     * @param file      文件
     * @param sliceSize 分片
     * @return fileKey
     */
    public String fragmentUpload(MultipartFile file, Long sliceSize) {

        String fileKey = generateFileKey(file.getOriginalFilename());
        InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(ossProperties.getBucketName(), fileKey);
        try {
            // 初始化分片。
            InitiateMultipartUploadResult upResult = ossClient.initiateMultipartUpload(request);
            // 返回uploadId，它是分片上传事件的唯一标识。您可以根据该uploadId发起相关的操作，例如取消分片上传、查询分片上传等。
            String uploadId = upResult.getUploadId();

            // partETags是PartETag的集合。PartETag由分片的ETag和分片号组成。
            List<PartETag> partETags = new ArrayList<>();
            // 每个分片的大小，用于计算文件有多少个分片。单位为字节。
            final long partSize = sliceSize == null ? 1024 * 1024L : sliceSize;   //1 MB。

            // 填写本地文件的完整路径。如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件。
            long fileLength = file.getSize();
            int partCount = (int) (fileLength / partSize);
            if (fileLength % partSize != 0) {
                partCount++;
            }
            // 遍历分片上传。
            for (int i = 0; i < partCount; i++) {
                long startPos = i * partSize;
                long curPartSize = (i + 1 == partCount) ? (fileLength - startPos) : partSize;
                // 跳过已经上传的分片。
                InputStream is = file.getInputStream();
                is.skip(startPos);
                UploadPartRequest uploadPartRequest = new UploadPartRequest();
                uploadPartRequest.setBucketName(ossProperties.getBucketName());
                uploadPartRequest.setKey(fileKey);
                uploadPartRequest.setUploadId(uploadId);
                uploadPartRequest.setInputStream(is);
                // 设置分片大小。除了最后一个分片没有大小限制，其他的分片最小为100 KB。
                uploadPartRequest.setPartSize(curPartSize);
                // 设置分片号。每一个上传的分片都有一个分片号，取值范围是1~10000，如果超出此范围，OSS将返回InvalidArgument错误码。
                uploadPartRequest.setPartNumber(i + 1);
                // 每个分片不需要按顺序上传，甚至可以在不同客户端上传，OSS会按照分片号排序组成完整的文件。
                UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);
                // 每次上传分片之后，OSS的返回结果包含PartETag。PartETag将被保存在partETags中。
                partETags.add(uploadPartResult.getPartETag());
            }

            // 创建CompleteMultipartUploadRequest对象。
            // 在执行完成分片上传操作时，需要提供所有有效的partETags。OSS收到提交的partETags后，会逐一验证每个分片的有效性。当所有的数据分片验证通过后，OSS将把这些分片组合成一个完整的文件。
            CompleteMultipartUploadRequest completeMultipartUploadRequest =
                    new CompleteMultipartUploadRequest(ossProperties.getBucketName(), fileKey, uploadId, partETags);

            // 完成分片上传。
            CompleteMultipartUploadResult completeMultipartUploadResult = ossClient.completeMultipartUpload(completeMultipartUploadRequest);
            System.out.println(completeMultipartUploadResult.getETag());

            return fileKey;
        } catch (IOException e) {
            log.error("分片上传异常", e);
            throw new ComPDFKitException("文件上传异常");
        }
    }

    /**
     * 文件下载
     *
     * @param fileObject 文件Key
     * @param pathName   下载保存的文件全路径
     */
    public void download(String fileObject, String pathName) {
        ossClient.getObject(new GetObjectRequest(ossProperties.getBucketName(), fileObject), new File(pathName));
    }

    /**
     * 文件下载
     *
     * @param fileObject 文件key
     * @return 文件流
     */
    public InputStream download(String fileObject) {
        OSS oss = ossClient();
        OSSObject ossObject = oss.getObject(ossProperties.getBucketName(), fileObject);
        return ossObject.getObjectContent();
    }

    /**
     * 生成有时效性的oss文件下载地址
     *
     * @param fileObject 文件key
     * @param expiration 有效时间（毫秒）
     * @return URL
     */
    public URL generateTermUrl(String fileObject, long expiration) {
        return ossClient.generatePresignedUrl(ossProperties.getBucketName(), fileObject, new Date(new Date().getTime() + expiration));
    }

    /**
     * 生成fileKey
     *
     * @param fileName 文件名
     * @return fileKey
     */
    private String generateFileKey(String fileName) {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        return ossProperties.getFilePrefix() + "/" + LocalDate.now().toString().replaceAll("-", "/") + "/" + uuid + "@" + fileName.replace('+', ' ');
    }

    /**
     * police策略
     *
     * @return map
     */
    public Map<String, String> policy() {

        // 设置上传到OSS文件的前缀，可置空此项。置空后，文件将上传至Bucket的根目录下。
        String dir = LocalDate.now().toString().replaceAll("-", "/");
        long expireTime = 30;
        long expireEndTime = expireTime * 1000;
        Date expiration = new Date(System.currentTimeMillis() + expireEndTime);
        // TODO 修改自己服务的回调接口
        String callbackUrl = "https://localhost/upload/callback";
        PolicyConditions policyConds = new PolicyConditions();
        policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 10L * 1024 * 1024 * 1024);
        policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);

        String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);
        byte[] binaryData = postPolicy.getBytes(StandardCharsets.UTF_8);
        String encodedPolicy = BinaryUtil.toBase64String(binaryData);
        String postSignature = ossClient.calculatePostSignature(postPolicy);

        Map<String, String> respMap = new LinkedHashMap<>();
        respMap.put("accessid", ossProperties.getAccessKeyId());
        respMap.put("policy", encodedPolicy);
        respMap.put("signature", postSignature);
        respMap.put("dir", dir);
        respMap.put("host", ossProperties.getEndpoint().replaceFirst("https://", "https://" + ossProperties.getBucketName() + "."));
        respMap.put("expire", String.valueOf(expireEndTime / 1000));

        JSONObject jasonCallback = new JSONObject();
        jasonCallback.put("callbackUrl", callbackUrl);
        jasonCallback.put("callbackBody",
                "filename=${object}&size=${size}&mimeType=${mimeType}&height=${imageInfo.height}&width=${imageInfo.width}");
        jasonCallback.put("callbackBodyType", "application/x-www-form-urlencoded");
        String base64CallbackBody = BinaryUtil.toBase64String(jasonCallback.toString().getBytes());
        respMap.put("callback", base64CallbackBody);

        return respMap;
    }


    public void deletePrefixFile(String fileDirectory) {
        String bucketName = ossProperties.getBucketName();
        String filePrefix = ossProperties.getFilePrefix();
        StringBuilder prefix = new StringBuilder(filePrefix);
        prefix.append("/").append(fileDirectory).append("/");
        try {
            // 删除目录及目录下的所有文件。
            String nextMarker = null;
            ObjectListing objectListing = null;
            do {
                ListObjectsRequest listObjectsRequest = new ListObjectsRequest(bucketName)
                        .withPrefix(prefix.toString())
                        .withMarker(nextMarker);

                objectListing = ossClient.listObjects(listObjectsRequest);
                if (objectListing.getObjectSummaries().size() > 0) {
                    List<String> keys = new ArrayList<>();
                    for (OSSObjectSummary s : objectListing.getObjectSummaries()) {
                        keys.add(s.getKey());
                    }
                    DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucketName).withKeys(keys).withEncodingType("url");
                    DeleteObjectsResult deleteObjectsResult = ossClient.deleteObjects(deleteObjectsRequest);
                    List<String> deletedObjects = deleteObjectsResult.getDeletedObjects();
                    try {
                        for (String obj : deletedObjects) {
                            String deleteObj = URLDecoder.decode(obj, "UTF-8");
                            System.out.println(deleteObj);
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                nextMarker = objectListing.getNextMarker();
            } while (objectListing.isTruncated());
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        }
    }

    public OSS ossClient() {
        return new OSSClientBuilder().build(ossProperties.getEndpoint(), ossProperties.getAccessKeyId(), ossProperties.getAccessKeySecret());
    }
}
