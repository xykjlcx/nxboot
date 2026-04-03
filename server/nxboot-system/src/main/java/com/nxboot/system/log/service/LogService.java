package com.nxboot.system.log.service;

import com.nxboot.common.base.PageQuery;
import com.nxboot.common.base.PageResult;
import com.nxboot.common.util.AssertUtils;
import com.nxboot.system.log.model.OperationLogVO;
import com.nxboot.system.log.repository.LogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 操作日志服务
 */
@Service
public class LogService {

    private final LogRepository logRepository;

    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public PageResult<OperationLogVO> page(PageQuery query, String keyword, Integer status) {
        return logRepository.page(query.offset(), query.pageSize(), keyword, status);
    }

    public OperationLogVO getById(Long id) {
        OperationLogVO log = logRepository.findById(id);
        AssertUtils.notNull(log, "操作日志", id);
        return log;
    }

    /**
     * 保存操作日志
     */
    @Transactional
    public void save(String module, String operation, String method, String requestUrl,
                     String requestMethod, String requestParams, String responseBody,
                     String operator, String operatorIp, Integer status, String errorMsg, Long duration) {
        logRepository.insert(module, operation, method, requestUrl, requestMethod,
                requestParams, responseBody, operator, operatorIp, status, errorMsg, duration);
    }
}
