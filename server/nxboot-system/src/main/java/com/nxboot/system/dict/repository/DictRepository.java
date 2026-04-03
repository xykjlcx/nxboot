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

import static com.nxboot.generated.jooq.tables.SysDictData.SYS_DICT_DATA;
import static com.nxboot.generated.jooq.tables.SysDictType.SYS_DICT_TYPE;

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
        Condition extra = JooqHelper.keywordCondition(keyword, SYS_DICT_TYPE.DICT_TYPE, SYS_DICT_TYPE.DICT_NAME);
        return JooqHelper.page(dsl, SYS_DICT_TYPE, extra, offset, size, this::toTypeVO);
    }

    public DictTypeVO findTypeById(Long id) {
        Record r = JooqHelper.findById(dsl, SYS_DICT_TYPE, id);
        return r != null ? toTypeVO(r) : null;
    }

    public boolean existsByDictType(String dictType) {
        return dsl.fetchExists(
                dsl.selectOne().from(SYS_DICT_TYPE)
                        .where(SYS_DICT_TYPE.DICT_TYPE.eq(dictType))
                        .and(JooqHelper.notDeleted())
        );
    }

    public Long insertType(String dictType, String dictName, Integer enabled,
                           String remark, String operator) {
        Long id = idGenerator.nextId();
        LocalDateTime now = LocalDateTime.now();

        dsl.insertInto(SYS_DICT_TYPE)
                .set(SYS_DICT_TYPE.ID, id)
                .set(SYS_DICT_TYPE.DICT_TYPE, dictType)
                .set(SYS_DICT_TYPE.DICT_NAME, dictName)
                .set(SYS_DICT_TYPE.ENABLED, enabled != null ? enabled : Constants.ENABLED)
                .set(SYS_DICT_TYPE.REMARK, remark)
                .set(SYS_DICT_TYPE.CREATE_BY, operator)
                .set(SYS_DICT_TYPE.CREATE_TIME, now)
                .set(SYS_DICT_TYPE.UPDATE_BY, operator)
                .set(SYS_DICT_TYPE.UPDATE_TIME, now)
                .set(SYS_DICT_TYPE.DELETED, Constants.NOT_DELETED)
                .execute();

        return id;
    }

    public void updateType(Long id, String dictName, Integer enabled, String remark, String operator) {
        var step = dsl.update(SYS_DICT_TYPE)
                .set(SYS_DICT_TYPE.UPDATE_BY, operator)
                .set(SYS_DICT_TYPE.UPDATE_TIME, LocalDateTime.now());

        if (dictName != null) step = step.set(SYS_DICT_TYPE.DICT_NAME, dictName);
        if (enabled != null) step = step.set(SYS_DICT_TYPE.ENABLED, enabled);
        if (remark != null) step = step.set(SYS_DICT_TYPE.REMARK, remark);

        step.where(SYS_DICT_TYPE.ID.eq(id)).and(JooqHelper.notDeleted()).execute();
    }

    public void softDeleteType(Long id, String operator) {
        JooqHelper.softDelete(dsl, SYS_DICT_TYPE, id, operator);
    }

    // ========== 字典数据 ==========

    public List<DictDataVO> findDataByType(String dictType) {
        return dsl.select().from(SYS_DICT_DATA)
                .where(SYS_DICT_DATA.DICT_TYPE.eq(dictType))
                .and(JooqHelper.notDeleted())
                .orderBy(SYS_DICT_DATA.SORT_ORDER.asc())
                .fetch(this::toDataVO);
    }

    public DictDataVO findDataById(Long id) {
        Record r = JooqHelper.findById(dsl, SYS_DICT_DATA, id);
        return r != null ? toDataVO(r) : null;
    }

    public Long insertData(String dictType, String dictLabel, String dictValue,
                           Integer sortOrder, Integer enabled, String remark, String operator) {
        Long id = idGenerator.nextId();
        LocalDateTime now = LocalDateTime.now();

        dsl.insertInto(SYS_DICT_DATA)
                .set(SYS_DICT_DATA.ID, id)
                .set(SYS_DICT_DATA.DICT_TYPE, dictType)
                .set(SYS_DICT_DATA.DICT_LABEL, dictLabel)
                .set(SYS_DICT_DATA.DICT_VALUE, dictValue)
                .set(SYS_DICT_DATA.SORT_ORDER, sortOrder != null ? sortOrder : 0)
                .set(SYS_DICT_DATA.ENABLED, enabled != null ? enabled : Constants.ENABLED)
                .set(SYS_DICT_DATA.REMARK, remark)
                .set(SYS_DICT_DATA.CREATE_BY, operator)
                .set(SYS_DICT_DATA.CREATE_TIME, now)
                .set(SYS_DICT_DATA.UPDATE_BY, operator)
                .set(SYS_DICT_DATA.UPDATE_TIME, now)
                .set(SYS_DICT_DATA.DELETED, Constants.NOT_DELETED)
                .execute();

        return id;
    }

    public void updateData(Long id, String dictLabel, String dictValue,
                           Integer sortOrder, Integer enabled, String remark, String operator) {
        var step = dsl.update(SYS_DICT_DATA)
                .set(SYS_DICT_DATA.UPDATE_BY, operator)
                .set(SYS_DICT_DATA.UPDATE_TIME, LocalDateTime.now());

        if (dictLabel != null) step = step.set(SYS_DICT_DATA.DICT_LABEL, dictLabel);
        if (dictValue != null) step = step.set(SYS_DICT_DATA.DICT_VALUE, dictValue);
        if (sortOrder != null) step = step.set(SYS_DICT_DATA.SORT_ORDER, sortOrder);
        if (enabled != null) step = step.set(SYS_DICT_DATA.ENABLED, enabled);
        if (remark != null) step = step.set(SYS_DICT_DATA.REMARK, remark);

        step.where(SYS_DICT_DATA.ID.eq(id)).and(JooqHelper.notDeleted()).execute();
    }

    public void softDeleteData(Long id, String operator) {
        JooqHelper.softDelete(dsl, SYS_DICT_DATA, id, operator);
    }

    private DictTypeVO toTypeVO(Record r) {
        Integer enabledVal = r.get(SYS_DICT_TYPE.ENABLED);
        return new DictTypeVO(
                r.get(SYS_DICT_TYPE.ID),
                r.get(SYS_DICT_TYPE.DICT_TYPE),
                r.get(SYS_DICT_TYPE.DICT_NAME),
                enabledVal != null && enabledVal == 1,
                r.get(SYS_DICT_TYPE.REMARK),
                r.get(SYS_DICT_TYPE.CREATE_TIME)
        );
    }

    private DictDataVO toDataVO(Record r) {
        Integer enabledVal = r.get(SYS_DICT_DATA.ENABLED);
        return new DictDataVO(
                r.get(SYS_DICT_DATA.ID),
                r.get(SYS_DICT_DATA.DICT_TYPE),
                r.get(SYS_DICT_DATA.DICT_LABEL),
                r.get(SYS_DICT_DATA.DICT_VALUE),
                r.get(SYS_DICT_DATA.SORT_ORDER),
                enabledVal != null && enabledVal == 1,
                r.get(SYS_DICT_DATA.REMARK),
                r.get(SYS_DICT_DATA.CREATE_TIME)
        );
    }
}
