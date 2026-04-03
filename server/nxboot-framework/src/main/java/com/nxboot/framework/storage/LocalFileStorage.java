package com.nxboot.framework.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 本地文件存储实现。
 */
public class LocalFileStorage implements FileStorage {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorage.class);

    private final String basePath;

    public LocalFileStorage(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public String upload(String relativePath, InputStream inputStream, String contentType) {
        Path target = Paths.get(basePath, relativePath);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
        return relativePath;
    }

    @Override
    public void delete(String relativePath) {
        try {
            Files.deleteIfExists(Paths.get(basePath, relativePath));
        } catch (IOException e) {
            log.warn("文件删除失败: {}", relativePath, e);
        }
    }

    @Override
    public String getUrl(String relativePath) {
        // 本地存储通过 API 接口访问
        return "/api/v1/system/files/download/" + relativePath;
    }
}
