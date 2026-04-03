package com.nxboot.system.auth.service;

import com.nxboot.common.base.PageQuery;
import com.nxboot.common.base.PageResult;
import com.nxboot.system.auth.model.LoginLogVO;
import com.nxboot.system.auth.repository.LoginLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 登录日志服务
 */
@Service
public class LoginLogService {

    private final LoginLogRepository loginLogRepository;

    public LoginLogService(LoginLogRepository loginLogRepository) {
        this.loginLogRepository = loginLogRepository;
    }

    /**
     * 分页查询
     */
    public PageResult<LoginLogVO> page(PageQuery query, String keyword,
                                        Integer status, LocalDateTime beginTime, LocalDateTime endTime) {
        return loginLogRepository.page(query.offset(), query.pageSize(), keyword, status, beginTime, endTime);
    }

    /**
     * 记录登录日志（异常不影响主流程）
     */
    public void record(String username, String ip, String userAgent, int status, String message) {
        try {
            loginLogRepository.insert(username, ip, userAgent, status, message);
        } catch (Exception e) {
            // 日志记录失败不影响登录流程
        }
    }
}
