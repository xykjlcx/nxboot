package com.nxboot.system.dict.repository;

import com.nxboot.common.base.PageResult;
import com.nxboot.common.constant.Constants;
import com.nxboot.common.util.SnowflakeIdGenerator;
import com.nxboot.framework.jooq.JooqHelper;
import com.nxboot.system.dict.model.DictDataVO;
import com.nxboot.system.dict.model.DictTypeVO;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

/**
 * 字典数据访问
 */
@Repository
public class DictRepository {

    private static final String TABLE_TYPE = "sys_dict_type";
    private static final String TABLE_DATA = "sys_dict_data";

    private final DSLContext dsl;
    private final SnowflakeIdGenerator idGenerator;

    public DictRepository(DSLContext dsl, SnowflakeIdGenerator idGenerator) {
        this.dsl = dsl;
        this.idGenerator = idGenerator;
    }

    // ========== 字典类型 ==========

    public PageResult<DictTypeVO> pageTypes(int offset, int size, String keyword) {
        Condition extra = JooqHelper.keywordCondition(keyword, "dict_type", "dict_name");
        return JooqHelper.page(dsl, TABLE_TYPE, extra, offset, size, this::toTypeVO);
    }

    public DictTypeVO findTypeById(Long id) {
        Record r = JooqHelper.findById(dsl, TABLE_TYPE, id);
        return r != null ? toTypeVO(r) : null;
    }

    public boolean existsByDictType(String dictType) {
        return dsl.fetchExists(
                dsl.selectOne().from(table(TABLE_TYPE))
                        .where(field("dict_type").eq(dictType))
                        .and(JooqHelper.notDeleted())
        );
    }

    public Long insertType(String dictType, String dictName, Integer enabled,
                           String remark, String operator) {
        Long id = idGenerator.nextId();
        LocalDateTime now = LocalDateTime.now();

        dsl.insertInto(table(TABLE_TYPE))
                .set(field("id"), id)
                .set(field("dict_type"), dictType)
                .set(field("dict_name"), dictName)
                .set(field("enabled"), enabled != null ? enabled : Constants.ENABLED)
                .set(field("remark"), remark)
                .set(field("create_by"), operator)
                .set(field("create_time"), now)
                .set(field("update_by"), operator)
                .set(field("update_time"), now)
                .set(field("deleted"), Constants.NOT_DELETED)
                .execute();

        return id;
    }

    public void updateType(Long id, String dictName, Integer enabled, String remark, String operator) {
        var step = dsl.update(table(TABLE_TYPE))
                .set(field("update_by"), operator)
                .set(field("update_time"), LocalDateTime.now());

        if (dictName != null) step = step.set(field("dict_name"), dictName);
        if (enabled != null) step = step.set(field("enabled"), enabled);
        if (remark != null) step = step.set(field("remark"), remark);

        step.where(field("id").eq(id)).and(JooqHelper.notDeleted()).execute();
    }

    public void softDeleteType(Long id, String operator) {
        JooqHelper.softDelete(dsl, TABLE_TYPE, id, operator);
    }

    // ========== 字典数据 ==========

    public List<DictDataVO> findDataByType(String dictType) {
        return dsl.select().from(table(TABLE_DATA))
                .where(field("dict_type").eq(dictType))
                .and(JooqHelper.notDeleted())
                .orderBy(field("sort_order").asc())
                .fetch(this::toDataVO);
    }

    public DictDataVO findDataById(Long id) {
        Record r = JooqHelper.findById(dsl, TABLE_DATA, id);
        return r != null ? toDataVO(r) : null;
    }

    public Long insertData(String dictType, String dictLabel, String dictValue,
                           Integer sortOrder, Integer enabled, String remark, String operator) {
        Long id = idGenerator.nextId();
        LocalDateTime now = LocalDateTime.now();

        dsl.insertInto(table(TABLE_DATA))
                .set(field("id"), id)
                .set(field("dict_type"), dictType)
                .set(field("dict_label"), dictLabel)
                .set(field("dict_value"), dictValue)
                .set(field("sort_order"), sortOrder != null ? sortOrder : 0)
                .set(field("enabled"), enabled != null ? enabled : Constants.ENABLED)
                .set(field("remark"), remark)
                .set(field("create_by"), operator)
                .set(field("create_time"), now)
                .set(field("update_by"), operator)
                .set(field("update_time"), now)
                .set(field("deleted"), Constants.NOT_DELETED)
                .execute();

        return id;
    }

    public void updateData(Long id, String dictLabel, String dictValue,
                           Integer sortOrder, Integer enabled, String remark, String operator) {
        var step = dsl.update(table(TABLE_DATA))
                .set(field("update_by"), operator)
                .set(field("update_time"), LocalDateTime.now());

        if (dictLabel != null) step = step.set(field("dict_label"), dictLabel);
        if (dictValue != null) step = step.set(field("dict_value"), dictValue);
        if (sortOrder != null) step = step.set(field("sort_order"), sortOrder);
        if (enabled != null) step = step.set(field("enabled"), enabled);
        if (remark != null) step = step.set(field("remark"), remark);

        step.where(field("id").eq(id)).and(JooqHelper.notDeleted()).execute();
    }

    public void softDeleteData(Long id, String operator) {
        JooqHelper.softDelete(dsl, TABLE_DATA, id, operator);
    }

    private DictTypeVO toTypeVO(Record r) {
        Integer enabledVal = r.get("enabled", Integer.class);
        return new DictTypeVO(
                r.get("id", Long.class),
                r.get("dict_type", String.class),
                r.get("dict_name", String.class),
                enabledVal != null && enabledVal == 1,
                r.get("remark", String.class),
                r.get("create_time", LocalDateTime.class)
        );
    }

    private DictDataVO toDataVO(Record r) {
        Integer enabledVal = r.get("enabled", Integer.class);
        return new DictDataVO(
                r.get("id", Long.class),
                r.get("dict_type", String.class),
                r.get("dict_label", String.class),
                r.get("dict_value", String.class),
                r.get("sort_order", Integer.class),
                enabledVal != null && enabledVal == 1,
                r.get("remark", String.class),
                r.get("create_time", LocalDateTime.class)
        );
    }
}
