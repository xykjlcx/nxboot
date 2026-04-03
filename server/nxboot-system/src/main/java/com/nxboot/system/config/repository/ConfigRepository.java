package com.nxboot.system.config.repository;

import com.nxboot.common.base.PageResult;
import com.nxboot.common.constant.Constants;
import com.nxboot.common.util.SnowflakeIdGenerator;
import com.nxboot.framework.jooq.JooqHelper;
import com.nxboot.system.config.model.ConfigVO;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

import static com.nxboot.generated.jooq.tables.SysConfig.SYS_CONFIG;

/**
 * 系统配置数据访问
 *
 * 已迁移至 jOOQ codegen 类型安全引用。
 */
@Repository
public class ConfigRepository {

    private final DSLContext dsl;
    private final SnowflakeIdGenerator idGenerator;

    public ConfigRepository(DSLContext dsl, SnowflakeIdGenerator idGenerator) {
        this.dsl = dsl;
        this.idGenerator = idGenerator;
    }

    public PageResult<ConfigVO> page(int offset, int size, String keyword) {
        Condition extra = JooqHelper.keywordCondition(keyword, SYS_CONFIG.CONFIG_KEY, SYS_CONFIG.CONFIG_NAME);
        return JooqHelper.page(dsl, SYS_CONFIG, extra, offset, size, this::toVO);
    }

    public ConfigVO findById(Long id) {
        Record r = JooqHelper.findById(dsl, SYS_CONFIG, id);
        return r != null ? toVO(r) : null;
    }

    public ConfigVO findByKey(String configKey) {
        Record r = dsl.select().from(SYS_CONFIG)
                .where(SYS_CONFIG.CONFIG_KEY.eq(configKey))
                .and(SYS_CONFIG.DELETED.eq(Constants.NOT_DELETED))
                .fetchOne();
        return r != null ? toVO(r) : null;
    }

    public boolean existsByKey(String configKey) {
        return dsl.fetchExists(
                dsl.selectOne().from(SYS_CONFIG)
                        .where(SYS_CONFIG.CONFIG_KEY.eq(configKey))
                        .and(SYS_CONFIG.DELETED.eq(Constants.NOT_DELETED))
        );
    }

    public Long insert(String configKey, String configValue, String configName,
                       String remark, String operator) {
        Long id = idGenerator.nextId();
        LocalDateTime now = LocalDateTime.now();

        dsl.insertInto(SYS_CONFIG)
                .set(SYS_CONFIG.ID, id)
                .set(SYS_CONFIG.CONFIG_KEY, configKey)
                .set(SYS_CONFIG.CONFIG_VALUE, configValue)
                .set(SYS_CONFIG.CONFIG_NAME, configName)
                .set(SYS_CONFIG.REMARK, remark)
                .set(SYS_CONFIG.CREATE_BY, operator)
                .set(SYS_CONFIG.CREATE_TIME, now)
                .set(SYS_CONFIG.UPDATE_BY, operator)
                .set(SYS_CONFIG.UPDATE_TIME, now)
                .set(SYS_CONFIG.DELETED, Constants.NOT_DELETED)
                .execute();

        return id;
    }

    public void update(Long id, String configValue, String configName, String remark, String operator) {
        var step = dsl.update(SYS_CONFIG)
                .set(SYS_CONFIG.UPDATE_BY, operator)
                .set(SYS_CONFIG.UPDATE_TIME, LocalDateTime.now());

        if (configValue != null) step = step.set(SYS_CONFIG.CONFIG_VALUE, configValue);
        if (configName != null) step = step.set(SYS_CONFIG.CONFIG_NAME, configName);
        if (remark != null) step = step.set(SYS_CONFIG.REMARK, remark);

        step.where(SYS_CONFIG.ID.eq(id)).and(SYS_CONFIG.DELETED.eq(Constants.NOT_DELETED)).execute();
    }

    public void softDelete(Long id, String operator) {
        JooqHelper.softDelete(dsl, SYS_CONFIG, id, operator);
    }

    private ConfigVO toVO(Record r) {
        return new ConfigVO(
                r.get(SYS_CONFIG.ID),
                r.get(SYS_CONFIG.CONFIG_KEY),
                r.get(SYS_CONFIG.CONFIG_VALUE),
                r.get(SYS_CONFIG.CONFIG_NAME),
                r.get(SYS_CONFIG.REMARK),
                r.get(SYS_CONFIG.CREATE_TIME)
        );
    }
}
