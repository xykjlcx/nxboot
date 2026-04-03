package com.nxboot.system.file.service;

import com.nxboot.common.base.PageQuery;
import com.nxboot.common.base.PageResult;
import com.nxboot.common.exception.BusinessException;
import com.nxboot.common.exception.ErrorCode;
import com.nxboot.common.util.AssertUtils;
import com.nxboot.common.util.SnowflakeIdGenerator;
import com.nxboot.framework.security.SecurityUtils;
import com.nxboot.framework.storage.FileStorage;
import com.nxboot.system.file.model.FileVO;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static com.nxboot.generated.jooq.tables.SysFile.SYS_FILE;

/**
 * 文件服务——通过 FileStorage 接口屏蔽存储后端差异（本地/OSS）。
 */
@Service
public class FileService {

    private final DSLContext dsl;
    private final SnowflakeIdGenerator idGenerator;
    private final FileStorage fileStorage;

    public FileService(DSLContext dsl, SnowflakeIdGenerator idGenerator, FileStorage fileStorage) {
        this.dsl = dsl;
        this.idGenerator = idGenerator;
        this.fileStorage = fileStorage;
    }

    private static final int MAX_FILE_NAME_LENGTH = 200;

    /**
     * 上传文件
     */
    @Transactional
    public FileVO upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "上传文件不能为空");
        }

        String originalName = sanitizeFileName(file.getOriginalFilename());
        String extension = "";
        if (originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }

        // 按日期分目录
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
        String relativePath = datePath + "/" + fileName;

        // 委托给存储后端
        try {
            fileStorage.upload(relativePath, file.getInputStream(), file.getContentType());
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "文件上传失败: " + e.getMessage());
        }

        // 保存文件记录
        Long id = idGenerator.nextId();
        String operator = SecurityUtils.getCurrentUsername();
        LocalDateTime now = LocalDateTime.now();

        dsl.insertInto(SYS_FILE)
                .set(SYS_FILE.ID, id)
                .set(SYS_FILE.FILE_NAME, fileName)
                .set(SYS_FILE.ORIGINAL_NAME, originalName)
                .set(SYS_FILE.FILE_PATH, relativePath)
                .set(SYS_FILE.FILE_SIZE, file.getSize())
                .set(SYS_FILE.FILE_TYPE, file.getContentType())
                .set(SYS_FILE.CREATE_BY, operator)
                .set(SYS_FILE.CREATE_TIME, now)
                .execute();

        return new FileVO(id, fileName, originalName, relativePath,
                file.getSize(), file.getContentType(), operator, now);
    }

    /**
     * 分页查询
     */
    public PageResult<FileVO> page(PageQuery query) {
        long total = dsl.selectCount()
                .from(SYS_FILE)
                .fetchOneInto(Long.class);

        if (total == 0) return PageResult.empty();

        List<FileVO> list = dsl.select()
                .from(SYS_FILE)
                .orderBy(SYS_FILE.CREATE_TIME.desc())
                .offset(query.offset()).limit(query.pageSize())
                .fetch(this::toVO);

        return PageResult.of(list, total);
    }

    /**
     * 根据 ID 查询
     */
    public FileVO getById(Long id) {
        Record r = dsl.select().from(SYS_FILE)
                .where(SYS_FILE.ID.eq(id))
                .fetchOne();
        AssertUtils.notNull(r, "文件", id);
        return toVO(r);
    }

    /**
     * 删除文件
     */
    @Transactional
    public void delete(Long id) {
        Record r = dsl.select().from(SYS_FILE)
                .where(SYS_FILE.ID.eq(id))
                .fetchOne();
        AssertUtils.notNull(r, "文件", id);

        String filePath = r.get(SYS_FILE.FILE_PATH);
        fileStorage.delete(filePath);

        dsl.deleteFrom(SYS_FILE)
                .where(SYS_FILE.ID.eq(id))
                .execute();
    }

    private String sanitizeFileName(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            return "unnamed";
        }
        String name = Paths.get(originalName).getFileName().toString();
        name = name.replaceAll("[\\\\/:*?\"<>|]", "_");
        if (name.isBlank() || name.equals("..") || name.equals(".")) {
            return "unnamed";
        }
        if (name.length() > MAX_FILE_NAME_LENGTH) {
            String ext = "";
            int dotIdx = name.lastIndexOf(".");
            if (dotIdx > 0) {
                ext = name.substring(dotIdx);
            }
            name = name.substring(0, MAX_FILE_NAME_LENGTH - ext.length()) + ext;
        }
        return name;
    }

    private FileVO toVO(Record r) {
        return new FileVO(
                r.get(SYS_FILE.ID),
                r.get(SYS_FILE.FILE_NAME),
                r.get(SYS_FILE.ORIGINAL_NAME),
                r.get(SYS_FILE.FILE_PATH),
                r.get(SYS_FILE.FILE_SIZE),
                r.get(SYS_FILE.FILE_TYPE),
                r.get(SYS_FILE.CREATE_BY),
                r.get(SYS_FILE.CREATE_TIME)
        );
    }
}
