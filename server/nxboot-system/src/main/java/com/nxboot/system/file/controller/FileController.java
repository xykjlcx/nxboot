package com.nxboot.system.file.controller;

import com.nxboot.common.annotation.Log;
import com.nxboot.common.base.PageQuery;
import com.nxboot.common.base.PageResult;
import com.nxboot.common.result.R;
import com.nxboot.system.file.model.FileVO;
import com.nxboot.system.file.service.FileService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件管理接口
 */
@RestController
@RequestMapping("/api/v1/system/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * 上传文件
     */
    @Log(module = "文件管理", operation = "上传文件")
    @PostMapping("/upload")
    @PreAuthorize("@perm.has('system:file:upload')")
    public R<FileVO> upload(@RequestParam("file") MultipartFile file) {
        return R.ok(fileService.upload(file));
    }

    /**
     * 分页查询
     */
    @GetMapping
    @PreAuthorize("@perm.has('system:file:list')")
    public R<PageResult<FileVO>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(fileService.page(new PageQuery(pageNum, pageSize)));
    }

    /**
     * 查询详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('system:file:list')")
    public R<FileVO> getById(@PathVariable Long id) {
        return R.ok(fileService.getById(id));
    }

    /**
     * 删除文件
     */
    @Log(module = "文件管理", operation = "删除文件")
    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('system:file:delete')")
    public R<Void> delete(@PathVariable Long id) {
        fileService.delete(id);
        return R.ok();
    }
}
