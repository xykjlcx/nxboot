package com.nxboot.system.file.model;

import java.time.LocalDateTime;

/**
 * 文件视图对象
 */
public record FileVO(
        Long id,
        String fileName,
        String originalName,
        String filePath,
        Long fileSize,
        String fileType,
        String createBy,
        LocalDateTime createTime
) {
}
