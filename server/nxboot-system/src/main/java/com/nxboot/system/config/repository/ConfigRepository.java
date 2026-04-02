package com.nxboot.system.config.repository;

import com.nxboot.common.base.PageResult;
import com.nxboot.common.constant.Constants;
import com.nxboot.common.util.SnowflakeIdGenerator;
import com.nxboot.system.config.model.ConfigVO;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

/**
 * 系统配置数据访问
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
        Condition condition = field("deleted").eq(Constants.NOT_DELETED);
        if (keyword != null && !keyword.isBlank()) {
            condition = condition.and(
                    field("config_key").likeIgnoreCase("%" + keyword + "%")
                            .or(field("config_name").likeIgnoreCase("%" + keyword + "%"))
            );
        }

        long total = dsl.selectCount()
                .from(table("sys_config"))
                .where(condition)
                .fetchOneInto(Long.class);

        if (total == 0) return PageResult.empty();

        List<ConfigVO> list = dsl.select()
                .from(table("sys_config"))
                .where(condition)
                .orderBy(field("create_time").desc())
                .offset(offset).limit(size)
                .fetch(this::toVO);

        return PageResult.of(list, total);
    }

    public ConfigVO findById(Long id) {
        Record r = dsl.select().from(table("sys_config"))
                .where(field("id").eq(id))
                .and(field("deleted").eq(Constants.NOT_DELETED))
                .fetchOne();
        return r != null ? toVO(r) : null;
    }

    public ConfigVO findByKey(String configKey) {
        Record r = dsl.select().from(table("sys_config"))
                .where(field("config_key").eq(configKey))
                .and(field("deleted").eq(Constants.NOT_DELETED))
                .fetchOne();
        return r != null ? toVO(r) : null;
    }

    public boolean existsByKey(String configKey) {
        return dsl.fetchExists(
                dsl.selectOne().from(table("sys_config"))
                        .where(field("config_key").eq(configKey))
                        .and(field("deleted").eq(Constants.NOT_DELETED))
        );
    }

    public Long insert(String configKey, String configValue, String configName,
                       String remark, String operator) {
        Long id = idGenerator.nextId();
        LocalDateTime now = LocalDateTime.now();

        dsl.insertInto(table("sys_config"))
                .set(field("id"), id)
                .set(field("config_key"), configKey)
                .set(field("config_value"), configValue)
                .set(field("config_name"), configName)
                .set(field("remark"), remark)
                .set(field("create_by"), operator)
                .set(field("create_time"), now)
                .set(field("update_by"), operator)
                .set(field("update_time"), now)
                .set(field("deleted"), Constants.NOT_DELETED)
                .execute();

        return id;
    }

    public void update(Long id, String configValue, String configName, String remark, String operator) {
        var step = dsl.update(table("sys_config"))
                .set(field("update_by"), operator)
                .set(field("update_time"), LocalDateTime.now());

        if (configValue != null) step = step.set(field("config_value"), configValue);
        if (configName != null) step = step.set(field("config_name"), configName);
        if (remark != null) step = step.set(field("remark"), remark);

        step.where(field("id").eq(id)).and(field("deleted").eq(Constants.NOT_DELETED)).execute();
    }

    public void softDelete(Long id, String operator) {
        dsl.update(table("sys_config"))
                .set(field("deleted"), Constants.DELETED)
                .set(field("update_by"), operator)
                .set(field("update_time"), LocalDateTime.now())
                .where(field("id").eq(id)).execute();
    }

    private ConfigVO toVO(Record r) {
        return new ConfigVO(
                r.get(field("id", Long.class)),
                r.get(field("config_key", String.class)),
                r.get(field("config_value", String.class)),
                r.get(field("config_name", String.class)),
                r.get(field("remark", String.class)),
                r.get("create_time", LocalDateTime.class)
        );
    }
}
