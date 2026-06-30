package com.compdf.client;

import com.compdf.properties.RustFsProperties;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.UUID;


@Slf4j
public class RustFsClient {

    public final String BUCKET_OTHER = "other";
    public final String BUCKET_SPLIT = "split";
    public final String BUCKET_EXTRACT = "extract";
    public final String BUCKET_LAYOUT = "layout";

    private final S3Client s3;

    public RustFsClient(RustFsProperties rustFsProperties) {
        this.s3 = S3Client.builder().endpointOverride(URI.create(rustFsProperties.getBaseUrl())) // RustFS address
                .region(Region.US_EAST_1) // RustFS does not validate regions
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(rustFsProperties.getAccessKeyId(), rustFsProperties.getSecretAccessKey()))).forcePathStyle(true) // Required for RustFS compatibility
                .build();
        createBucket(BUCKET_SPLIT);
        createBucket(BUCKET_EXTRACT);
        createBucket(BUCKET_LAYOUT);
        createBucket(BUCKET_OTHER);
    }

    /**
     * Create Bucket
     *
     * @param bucketName Bucket name
     */
    public void createBucket(String bucketName) {
        try {
            s3.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
        } catch (AwsServiceException | SdkClientException e) {
            log.error("Failed to create bucket: {}", bucketName);
        }
    }

    /**
     * 上传文件到 RustFs 服务
     */
    public String uploadFile(File file, String bucket) {
        String name = UUID.randomUUID() + "/" + file.getName();
        s3.putObject(PutObjectRequest.builder().bucket(bucket).key(name).build(), file.toPath());
        return bucket + "/" + name;
    }

    public String uploadTemplateFile(File file, String bucket) {
        String name = file.getName();
        s3.putObject(PutObjectRequest.builder().bucket(bucket).key(name).build(), file.toPath());
        return bucket + "/" + name;
    }

    /**
     * 从 RustFs 服务下载文件
     * fileId: bucketName/fileName
     */
    public InputStream downloadFile(String fileId) {
        String[] parts = fileId.split("/", 2);
        String bucket = parts[0];
        String key = parts[1];
        return s3.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build(), ResponseTransformer.toInputStream());
    }
}
