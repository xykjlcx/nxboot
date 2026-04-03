package com.nxboot.system.job.controller;

import com.nxboot.common.annotation.Log;
import com.nxboot.common.base.PageQuery;
import com.nxboot.common.base.PageResult;
import com.nxboot.common.result.R;
import com.nxboot.system.job.model.JobCommand;
import com.nxboot.system.job.model.JobVO;
import com.nxboot.system.job.service.JobService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 定时任务接口
 */
@RestController
@RequestMapping("/api/v1/system/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    @PreAuthorize("@perm.has('system:job:list')")
    public R<PageResult<JobVO>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword) {
        return R.ok(jobService.page(new PageQuery(pageNum, pageSize), keyword));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('system:job:list')")
    public R<JobVO> getById(@PathVariable Long id) {
        return R.ok(jobService.getById(id));
    }

    @Log(module = "定时任务", operation = "新增任务")
    @PostMapping
    @PreAuthorize("@perm.has('system:job:create')")
    public R<Long> create(@Valid @RequestBody JobCommand.Create cmd) {
        return R.ok(jobService.create(cmd));
    }

    @Log(module = "定时任务", operation = "修改任务")
    @PutMapping("/{id}")
    @PreAuthorize("@perm.has('system:job:update')")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody JobCommand.Update cmd) {
        jobService.update(id, cmd);
        return R.ok();
    }

    @Log(module = "定时任务", operation = "删除任务")
    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('system:job:delete')")
    public R<Void> delete(@PathVariable Long id) {
        jobService.delete(id);
        return R.ok();
    }

    /**
     * 立即执行一次
     */
    @Log(module = "定时任务", operation = "执行任务")
    @PostMapping("/{id}/run")
    @PreAuthorize("@perm.has('system:job:update')")
    public R<Void> runOnce(@PathVariable Long id) {
        jobService.runOnce(id);
        return R.ok();
    }
}
