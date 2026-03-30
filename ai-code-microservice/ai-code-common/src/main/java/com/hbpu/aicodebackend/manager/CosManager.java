package com.hbpu.aicodebackend.manager;

import com.hbpu.aicodebackend.config.TencentCosConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * COS object storage manager.
 */
@Component
@Slf4j
public class CosManager {

    private final TencentCosConfig tencentCosConfig;

    private final COSClient cosClient;

    @Value("${tencent.cos.bucketName:}")
    private String bucketName;

    public CosManager(ObjectProvider<TencentCosConfig> tencentCosConfigProvider,
                      ObjectProvider<COSClient> cosClientProvider) {
        this.tencentCosConfig = tencentCosConfigProvider.getIfAvailable();
        this.cosClient = cosClientProvider.getIfAvailable();
    }

    /**
     * Upload an object to COS.
     */
    public PutObjectResult putObject(String key, File file) {
        ensureConfigured();
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * Upload a file to COS and return its public URL.
     */
    public String uploadFile(String key, File file) {
        PutObjectResult result = putObject(key, file);
        if (result != null) {
            String url = String.format(
                    "https://%s.cos.%s.myqcloud.com/%s",
                    bucketName,
                    tencentCosConfig.getRegion(),
                    key
            );
            log.info("File uploaded to COS successfully: {} -> {}", file.getName(), url);
            return url;
        }
        log.error("File upload to COS failed, result is null");
        return null;
    }

    /**
     * Delete an object from COS.
     */
    public void deleteObject(String key) throws CosClientException {
        if (key == null || key.isBlank()) {
            return;
        }
        ensureConfigured();
        cosClient.deleteObject(bucketName, key);
    }

    private void ensureConfigured() {
        if (tencentCosConfig == null || cosClient == null || bucketName == null || bucketName.isBlank()) {
            throw new IllegalStateException(
                    "Tencent COS is not configured. Please provide tencent.cos.secretId, " +
                            "tencent.cos.secretKey, tencent.cos.region and tencent.cos.bucketName"
            );
        }
    }
}
