package com.nxboot.system.log.service;

import com.nxboot.common.base.PageQuery;
import com.nxboot.common.base.PageResult;
import com.nxboot.common.util.AssertUtils;
import com.nxboot.system.log.model.OperationLogVO;
import com.nxboot.system.log.repository.LogRepository;
import org.springframework.stereotype.Service;

/**
 * 操作日志服务（只读）
 */
@Service
public class LogService {

    private final LogRepository logRepository;

    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public PageResult<OperationLogVO> page(PageQuery query, String keyword) {
        return logRepository.page(query.offset(), query.pageSize(), keyword);
    }

    public OperationLogVO getById(Long id) {
        OperationLogVO log = logRepository.findById(id);
        AssertUtils.notNull(log, "操作日志", id);
        return log;
    }
}
