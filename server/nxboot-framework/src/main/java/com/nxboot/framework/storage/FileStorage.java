package com.nxboot.framework.storage;

import java.io.InputStream;

/**
 * 文件存储抽象——屏蔽本地/OSS/S3 等底层差异。
 * <p>
 * 当前实现：{@link LocalFileStorage}。
 * 扩展 OSS 只需新增实现类 + 切换配置。
 */
public interface FileStorage {

    /**
     * 上传文件。
     *
     * @param relativePath 相对路径（如 2026/04/03/uuid.jpg）
     * @param inputStream  文件内容
     * @param contentType  MIME 类型
     * @return 访问 URL 或路径
     */
    String upload(String relativePath, InputStream inputStream, String contentType);

    /**
     * 删除文件。
     *
     * @param relativePath 相对路径
     */
    void delete(String relativePath);

    /**
     * 获取文件访问 URL。
     *
     * @param relativePath 相对路径
     * @return 完整 URL（OSS 返回签名 URL，本地返回 API 路径）
     */
    String getUrl(String relativePath);
}
