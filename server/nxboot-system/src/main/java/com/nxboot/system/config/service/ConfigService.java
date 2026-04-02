package com.nxboot.system.config.service;

import com.nxboot.common.base.PageQuery;
import com.nxboot.common.base.PageResult;
import com.nxboot.common.exception.ErrorCode;
import com.nxboot.common.util.AssertUtils;
import com.nxboot.framework.security.SecurityUtils;
import com.nxboot.system.config.model.ConfigCommand;
import com.nxboot.system.config.model.ConfigVO;
import com.nxboot.system.config.repository.ConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统配置服务
 */
@Service
public class ConfigService {

    private final ConfigRepository configRepository;

    public ConfigService(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    public PageResult<ConfigVO> page(PageQuery query, String keyword) {
        return configRepository.page(query.offset(), query.pageSize(), keyword);
    }

    public ConfigVO getById(Long id) {
        ConfigVO config = configRepository.findById(id);
        AssertUtils.notNull(config, "配置", id);
        return config;
    }

    public ConfigVO getByKey(String configKey) {
        ConfigVO config = configRepository.findByKey(configKey);
        AssertUtils.notNull(config, "配置", configKey);
        return config;
    }

    @Transactional
    public Long create(ConfigCommand.Create cmd) {
        AssertUtils.isTrue(!configRepository.existsByKey(cmd.configKey()),
                ErrorCode.BIZ_DUPLICATE, "配置键已存在: " + cmd.configKey());
        String operator = SecurityUtils.getCurrentUsername();
        return configRepository.insert(cmd.configKey(), cmd.configValue(),
                cmd.configName(), cmd.remark(), operator);
    }

    @Transactional
    public void update(Long id, ConfigCommand.Update cmd) {
        AssertUtils.notNull(configRepository.findById(id), "配置", id);
        String operator = SecurityUtils.getCurrentUsername();
        configRepository.update(id, cmd.configValue(), cmd.configName(), cmd.remark(), operator);
    }

    @Transactional
    public void delete(Long id) {
        AssertUtils.notNull(configRepository.findById(id), "配置", id);
        String operator = SecurityUtils.getCurrentUsername();
        configRepository.softDelete(id, operator);
    }
}
