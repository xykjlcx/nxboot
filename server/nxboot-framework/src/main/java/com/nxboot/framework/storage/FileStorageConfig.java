package com.nxboot.framework.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文件存储自动配置。
 * <p>
 * 通过 {@code nxboot.file.storage-type} 切换实现：
 * <ul>
 *   <li>{@code local}（默认）— 本地磁盘存储</li>
 *   <li>{@code oss} — 阿里云 OSS（需额外引入 SDK 依赖）</li>
 * </ul>
 */
@Configuration
public class FileStorageConfig {

    @Bean
    @ConditionalOnProperty(name = "nxboot.file.storage-type", havingValue = "local", matchIfMissing = true)
    public FileStorage localFileStorage(@Value("${nxboot.file.upload-dir:./uploads}") String uploadDir) {
        return new LocalFileStorage(uploadDir);
    }

    // OSS 实现示例（取消注释 + 引入 aliyun-oss-sdk 即可启用）：
    //
    // @Bean
    // @ConditionalOnProperty(name = "nxboot.file.storage-type", havingValue = "oss")
    // public FileStorage ossFileStorage(
    //         @Value("${nxboot.oss.endpoint}") String endpoint,
    //         @Value("${nxboot.oss.access-key-id}") String accessKeyId,
    //         @Value("${nxboot.oss.access-key-secret}") String accessKeySecret,
    //         @Value("${nxboot.oss.bucket-name}") String bucketName) {
    //     return new OssFileStorage(endpoint, accessKeyId, accessKeySecret, bucketName);
    // }
}
