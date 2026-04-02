package com.nxboot.system.dict.repository;

import com.nxboot.common.base.PageResult;
import com.nxboot.common.constant.Constants;
import com.nxboot.common.util.SnowflakeIdGenerator;
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

    private final DSLContext dsl;
    private final SnowflakeIdGenerator idGenerator;

    public DictRepository(DSLContext dsl, SnowflakeIdGenerator idGenerator) {
        this.dsl = dsl;
        this.idGenerator = idGenerator;
    }

    // ========== 字典类型 ==========

    public PageResult<DictTypeVO> pageTypes(int offset, int size, String keyword) {
        Condition condition = field("deleted").eq(Constants.NOT_DELETED);
        if (keyword != null && !keyword.isBlank()) {
            condition = condition.and(
                    field("dict_type").likeIgnoreCase("%" + keyword + "%")
                            .or(field("dict_name").likeIgnoreCase("%" + keyword + "%"))
            );
        }

        long total = dsl.selectCount()
                .from(table("sys_dict_type"))
                .where(condition)
                .fetchOneInto(Long.class);

        if (total == 0) return PageResult.empty();

        List<DictTypeVO> list = dsl.select()
                .from(table("sys_dict_type"))
                .where(condition)
                .orderBy(field("create_time").desc())
                .offset(offset).limit(size)
                .fetch(this::toTypeVO);

        return PageResult.of(list, total);
    }

    public DictTypeVO findTypeById(Long id) {
        Record r = dsl.select().from(table("sys_dict_type"))
                .where(field("id").eq(id))
                .and(field("deleted").eq(Constants.NOT_DELETED))
                .fetchOne();
        return r != null ? toTypeVO(r) : null;
    }

    public boolean existsByDictType(String dictType) {
        return dsl.fetchExists(
                dsl.selectOne().from(table("sys_dict_type"))
                        .where(field("dict_type").eq(dictType))
                        .and(field("deleted").eq(Constants.NOT_DELETED))
        );
    }

    public Long insertType(String dictType, String dictName, Integer enabled,
                           String remark, String operator) {
        Long id = idGenerator.nextId();
        LocalDateTime now = LocalDateTime.now();

        dsl.insertInto(table("sys_dict_type"))
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
        var step = dsl.update(table("sys_dict_type"))
                .set(field("update_by"), operator)
                .set(field("update_time"), LocalDateTime.now());

        if (dictName != null) step = step.set(field("dict_name"), dictName);
        if (enabled != null) step = step.set(field("enabled"), enabled);
        if (remark != null) step = step.set(field("remark"), remark);

        step.where(field("id").eq(id)).and(field("deleted").eq(Constants.NOT_DELETED)).execute();
    }

    public void softDeleteType(Long id, String operator) {
        dsl.update(table("sys_dict_type"))
                .set(field("deleted"), Constants.DELETED)
                .set(field("update_by"), operator)
                .set(field("update_time"), LocalDateTime.now())
                .where(field("id").eq(id)).execute();
    }

    // ========== 字典数据 ==========

    public List<DictDataVO> findDataByType(String dictType) {
        return dsl.select().from(table("sys_dict_data"))
                .where(field("dict_type").eq(dictType))
                .and(field("deleted").eq(Constants.NOT_DELETED))
                .orderBy(field("sort_order").asc())
                .fetch(this::toDataVO);
    }

    public DictDataVO findDataById(Long id) {
        Record r = dsl.select().from(table("sys_dict_data"))
                .where(field("id").eq(id))
                .and(field("deleted").eq(Constants.NOT_DELETED))
                .fetchOne();
        return r != null ? toDataVO(r) : null;
    }

    public Long insertData(String dictType, String dictLabel, String dictValue,
                           Integer sortOrder, Integer enabled, String remark, String operator) {
        Long id = idGenerator.nextId();
        LocalDateTime now = LocalDateTime.now();

        dsl.insertInto(table("sys_dict_data"))
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
        var step = dsl.update(table("sys_dict_data"))
                .set(field("update_by"), operator)
                .set(field("update_time"), LocalDateTime.now());

        if (dictLabel != null) step = step.set(field("dict_label"), dictLabel);
        if (dictValue != null) step = step.set(field("dict_value"), dictValue);
        if (sortOrder != null) step = step.set(field("sort_order"), sortOrder);
        if (enabled != null) step = step.set(field("enabled"), enabled);
        if (remark != null) step = step.set(field("remark"), remark);

        step.where(field("id").eq(id)).and(field("deleted").eq(Constants.NOT_DELETED)).execute();
    }

    public void softDeleteData(Long id, String operator) {
        dsl.update(table("sys_dict_data"))
                .set(field("deleted"), Constants.DELETED)
                .set(field("update_by"), operator)
                .set(field("update_time"), LocalDateTime.now())
                .where(field("id").eq(id)).execute();
    }

    private DictTypeVO toTypeVO(Record r) {
        Integer enabledVal = r.get(field("enabled", Integer.class));
        return new DictTypeVO(
                r.get(field("id", Long.class)),
                r.get(field("dict_type", String.class)),
                r.get(field("dict_name", String.class)),
                enabledVal != null && enabledVal == 1,
                r.get(field("remark", String.class)),
                r.get(field("create_time", LocalDateTime.class))
        );
    }

    private DictDataVO toDataVO(Record r) {
        Integer enabledVal = r.get(field("enabled", Integer.class));
        return new DictDataVO(
                r.get(field("id", Long.class)),
                r.get(field("dict_type", String.class)),
                r.get(field("dict_label", String.class)),
                r.get(field("dict_value", String.class)),
                r.get(field("sort_order", Integer.class)),
                enabledVal != null && enabledVal == 1,
                r.get(field("remark", String.class)),
                r.get(field("create_time", LocalDateTime.class))
        );
    }
}
