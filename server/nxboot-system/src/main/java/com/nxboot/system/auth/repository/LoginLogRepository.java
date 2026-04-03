package com.nxboot.system.auth.repository;

import com.nxboot.common.base.PageResult;
import com.nxboot.common.util.SnowflakeIdGenerator;
import com.nxboot.framework.jooq.JooqHelper;
import com.nxboot.system.auth.model.LoginLogVO;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.nxboot.generated.jooq.tables.SysLoginLog.SYS_LOGIN_LOG;

/**
 * 登录日志数据访问
 */
@Repository
public class LoginLogRepository {

    private final DSLContext dsl;

    public LoginLogRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * 分页查询
     */
    public PageResult<LoginLogVO> page(int offset, int size, String keyword,
                                        Integer status, LocalDateTime beginTime, LocalDateTime endTime) {
        Condition condition = DSL.trueCondition();

        // 关键词搜索：用户名 / IP
        Condition keywordCond = JooqHelper.keywordCondition(keyword, SYS_LOGIN_LOG.USERNAME, SYS_LOGIN_LOG.IP);
        if (keywordCond != null) {
            condition = condition.and(keywordCond);
        }

        if (status != null) {
            condition = condition.and(SYS_LOGIN_LOG.STATUS.eq(status));
        }
        if (beginTime != null) {
            condition = condition.and(SYS_LOGIN_LOG.LOGIN_TIME.ge(beginTime));
        }
        if (endTime != null) {
            condition = condition.and(SYS_LOGIN_LOG.LOGIN_TIME.le(endTime));
        }

        long total = dsl.selectCount()
                .from(SYS_LOGIN_LOG)
                .where(condition)
                .fetchOneInto(Long.class);

        if (total == 0) return PageResult.empty();

        List<LoginLogVO> list = dsl.select()
                .from(SYS_LOGIN_LOG)
                .where(condition)
                .orderBy(SYS_LOGIN_LOG.LOGIN_TIME.desc())
                .offset(offset).limit(size)
                .fetch(this::toVO);

        return PageResult.of(list, total);
    }

    /**
     * 插入登录日志
     */
    public void insert(String username, String ip, String userAgent, int status, String message) {
        dsl.insertInto(SYS_LOGIN_LOG)
                .set(SYS_LOGIN_LOG.ID, SnowflakeIdGenerator.getInstance().nextId())
                .set(SYS_LOGIN_LOG.USERNAME, username)
                .set(SYS_LOGIN_LOG.IP, ip)
                .set(SYS_LOGIN_LOG.USER_AGENT, userAgent)
                .set(SYS_LOGIN_LOG.STATUS, status)
                .set(SYS_LOGIN_LOG.MESSAGE, message)
                .set(SYS_LOGIN_LOG.LOGIN_TIME, LocalDateTime.now())
                .execute();
    }

    private LoginLogVO toVO(Record r) {
        return new LoginLogVO(
                r.get(SYS_LOGIN_LOG.ID),
                r.get(SYS_LOGIN_LOG.USERNAME),
                r.get(SYS_LOGIN_LOG.IP),
                r.get(SYS_LOGIN_LOG.USER_AGENT),
                r.get(SYS_LOGIN_LOG.STATUS),
                r.get(SYS_LOGIN_LOG.MESSAGE),
                r.get(SYS_LOGIN_LOG.LOGIN_TIME)
        );
    }
}
