package com.nxboot.system.job.controller;

import com.nxboot.common.base.PageQuery;
import com.nxboot.common.base.PageResult;
import com.nxboot.common.result.R;
import com.nxboot.system.job.model.JobLogVO;
import com.nxboot.system.job.service.JobLogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 定时任务执行日志接口（只读）
 */
@RestController
@RequestMapping("/api/v1/system/job-logs")
public class JobLogController {

    private final JobLogService jobLogService;

    public JobLogController(JobLogService jobLogService) {
        this.jobLogService = jobLogService;
    }

    @GetMapping
    @PreAuthorize("@perm.has('system:job:list')")
    public R<PageResult<JobLogVO>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return R.ok(jobLogService.page(new PageQuery(pageNum, pageSize), jobId, keyword, status));
    }
}
