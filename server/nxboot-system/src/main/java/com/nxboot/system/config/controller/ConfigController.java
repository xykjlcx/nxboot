package com.nxboot.system.config.controller;

import com.nxboot.common.base.PageQuery;
import com.nxboot.common.base.PageResult;
import com.nxboot.common.result.R;
import com.nxboot.system.config.model.ConfigCommand;
import com.nxboot.system.config.model.ConfigVO;
import com.nxboot.system.config.service.ConfigService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 系统配置接口
 */
@RestController
@RequestMapping("/api/v1/system/configs")
public class ConfigController {

    private final ConfigService configService;

    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping
    @PreAuthorize("@perm.has('system:config:list')")
    public R<PageResult<ConfigVO>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword) {
        return R.ok(configService.page(new PageQuery(pageNum, pageSize), keyword));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('system:config:list')")
    public R<ConfigVO> getById(@PathVariable Long id) {
        return R.ok(configService.getById(id));
    }

    @GetMapping("/key/{configKey}")
    @PreAuthorize("@perm.has('system:config:list')")
    public R<ConfigVO> getByKey(@PathVariable String configKey) {
        return R.ok(configService.getByKey(configKey));
    }

    @PostMapping
    @PreAuthorize("@perm.has('system:config:create')")
    public R<Long> create(@Valid @RequestBody ConfigCommand.Create cmd) {
        return R.ok(configService.create(cmd));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.has('system:config:update')")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody ConfigCommand.Update cmd) {
        configService.update(id, cmd);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('system:config:delete')")
    public R<Void> delete(@PathVariable Long id) {
        configService.delete(id);
        return R.ok();
    }
}
