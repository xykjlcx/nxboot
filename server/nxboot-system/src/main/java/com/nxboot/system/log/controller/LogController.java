package com.nxboot.system.log.controller;

import com.nxboot.common.base.PageQuery;
import com.nxboot.common.base.PageResult;
import com.nxboot.common.result.R;
import com.nxboot.system.log.model.OperationLogVO;
import com.nxboot.system.log.service.LogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志接口（只读）
 */
@RestController
@RequestMapping("/api/v1/system/logs")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping
    @PreAuthorize("@perm.has('system:log:list')")
    public R<PageResult<OperationLogVO>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return R.ok(logService.page(new PageQuery(pageNum, pageSize), keyword, status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('system:log:list')")
    public R<OperationLogVO> getById(@PathVariable Long id) {
        return R.ok(logService.getById(id));
    }
}
