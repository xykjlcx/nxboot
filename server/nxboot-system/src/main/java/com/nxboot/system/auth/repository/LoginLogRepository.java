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

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

/**
 * 登录日志数据访问
 */
@Repository
public class LoginLogRepository {

    private static final String TABLE = "sys_login_log";

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
        Condition keywordCond = JooqHelper.keywordCondition(keyword, "username", "ip");
        if (keywordCond != null) {
            condition = condition.and(keywordCond);
        }

        if (status != null) {
            condition = condition.and(field("status").eq(status));
        }
        if (beginTime != null) {
            condition = condition.and(field("login_time").ge(beginTime));
        }
        if (endTime != null) {
            condition = condition.and(field("login_time").le(endTime));
        }

        long total = dsl.selectCount()
                .from(table(TABLE))
                .where(condition)
                .fetchOneInto(Long.class);

        if (total == 0) return PageResult.empty();

        List<LoginLogVO> list = dsl.select()
                .from(table(TABLE))
                .where(condition)
                .orderBy(field("login_time").desc())
                .offset(offset).limit(size)
                .fetch(this::toVO);

        return PageResult.of(list, total);
    }

    /**
     * 插入登录日志
     */
    public void insert(String username, String ip, String userAgent, int status, String message) {
        dsl.insertInto(table(TABLE))
                .set(field("id"), SnowflakeIdGenerator.getInstance().nextId())
                .set(field("username"), username)
                .set(field("ip"), ip)
                .set(field("user_agent"), userAgent)
                .set(field("status"), status)
                .set(field("message"), message)
                .set(field("login_time"), LocalDateTime.now())
                .execute();
    }

    private LoginLogVO toVO(Record r) {
        return new LoginLogVO(
                r.get("id", Long.class),
                r.get("username", String.class),
                r.get("ip", String.class),
                r.get("user_agent", String.class),
                r.get("status", Integer.class),
                r.get("message", String.class),
                r.get("login_time", LocalDateTime.class)
        );
    }
}
