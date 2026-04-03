package com.nxboot.framework.jooq;

import com.nxboot.common.base.PageResult;
import com.nxboot.common.constant.Constants;
import com.nxboot.common.exception.BusinessException;
import com.nxboot.common.exception.ErrorCode;
import com.nxboot.common.util.SnowflakeIdGenerator;
import com.nxboot.framework.security.DataScopeContext;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.InsertSetMoreStep;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.UpdateSetMoreStep;
import org.jooq.impl.DSL;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * jOOQ 通用工具——减少 Repository 重复代码。
 * <p>
 * 设计为静态方法而非 BaseRepository 继承，保持每个 Repository 透明可读（AI 友好）。
 * 所有字段引用通过 table.field() 从表元数据运行时解析，不依赖 DSL.field() 字符串常量。
 */
public final class JooqHelper {

    private JooqHelper() {
    }

    /* ──────────── 分页查询 ──────────── */

    /**
     * 通用分页查询（自动加 deleted=0 条件）。
     *
     * @param dsl    DSLContext
     * @param table  表引用
     * @param extra  额外查询条件（可为 null）
     * @param offset 偏移量
     * @param size   页大小
     * @param mapper Record → VO 映射函数
     */
    public static <T> PageResult<T> page(DSLContext dsl, Table<?> table,
                                          Condition extra, int offset, int size,
                                          Function<Record, T> mapper) {
        Condition condition = table.field("deleted", Integer.class).eq(Constants.NOT_DELETED);
        if (extra != null) {
            condition = condition.and(extra);
        }

        long total = dsl.selectCount()
                .from(table)
                .where(condition)
                .fetchOneInto(Long.class);

        if (total == 0) {
            return PageResult.empty();
        }

        List<T> list = dsl.select()
                .from(table)
                .where(condition)
                .orderBy(table.field("create_time", LocalDateTime.class).desc())
                .offset(offset)
                .limit(size)
                .fetch(mapper::apply);

        return PageResult.of(list, total);
    }

    /* ──────────── 单条查询 ──────────── */

    /**
     * 根据 ID 查询（自动加 deleted=0 条件）。
     */
    public static Record findById(DSLContext dsl, Table<?> table, Long id) {
        return dsl.select()
                .from(table)
                .where(table.field("id", Long.class).eq(id))
                .and(table.field("deleted", Integer.class).eq(Constants.NOT_DELETED))
                .fetchOne();
    }

    /* ──────────── 软删除 ──────────── */

    /**
     * 逻辑删除。
     */
    public static void softDelete(DSLContext dsl, Table<?> table, Long id, String operator) {
        dsl.update(table)
                .set(table.field("deleted", Integer.class), Constants.DELETED)
                .set(table.field("update_by", String.class), operator)
                .set(table.field("update_time", LocalDateTime.class), LocalDateTime.now())
                .where(table.field("id", Long.class).eq(id))
                .execute();
    }

    /* ──────────── 审计字段自动填充 ──────────── */

    /**
     * 为 INSERT 设置审计字段（id + create_by/time + update_by/time + deleted）。
     *
     * @param step     INSERT 语句
     * @param table    表引用（用于解析字段）
     * @param idGen    雪花 ID 生成器
     * @param operator 操作人
     * @return 生成的 ID
     */
    public static Long setAuditInsert(InsertSetMoreStep<?> step, Table<?> table,
                                       SnowflakeIdGenerator idGen, String operator) {
        Long id = idGen.nextId();
        LocalDateTime now = LocalDateTime.now();
        step.set(table.field("id", Long.class), id)
            .set(table.field("create_by", String.class), operator)
            .set(table.field("create_time", LocalDateTime.class), now)
            .set(table.field("update_by", String.class), operator)
            .set(table.field("update_time", LocalDateTime.class), now)
            .set(table.field("deleted", Integer.class), Constants.NOT_DELETED);
        return id;
    }

    /**
     * 为 UPDATE 设置审计字段（update_by + update_time）。
     *
     * @param step     UPDATE 语句
     * @param table    表引用（用于解析字段）
     * @param operator 操作人
     */
    public static void setAuditUpdate(UpdateSetMoreStep<?> step, Table<?> table, String operator) {
        step.set(table.field("update_by", String.class), operator)
            .set(table.field("update_time", LocalDateTime.class), LocalDateTime.now());
    }

    /* ──────────── 乐观锁 ──────────── */

    /**
     * 乐观锁更新——version 不匹配时抛异常。
     *
     * @return 更新后的 version 值
     */
    public static int optimisticUpdate(DSLContext dsl, Table<?> table, Long id, int currentVersion,
                                        Consumer<UpdateSetMoreStep<?>> setter) {
        var step = dsl.update(table)
                .set(table.field("version", Integer.class), currentVersion + 1);
        setter.accept(step);
        int rows = step.where(table.field("id", Long.class).eq(id))
                .and(table.field("version", Integer.class).eq(currentVersion))
                .and(table.field("deleted", Integer.class).eq(Constants.NOT_DELETED))
                .execute();
        if (rows == 0) {
            throw new BusinessException(ErrorCode.BIZ_REFERENCED, "数据已被其他用户修改，请刷新后重试");
        }
        return currentVersion + 1;
    }

    /* ──────────── 关键词搜索条件 ──────────── */

    /**
     * 构建多字段关键词模糊搜索条件。
     *
     * @param keyword 搜索词（空或 null 返回 null）
     * @param fields  要搜索的字段引用
     */
    @SafeVarargs
    public static Condition keywordCondition(String keyword, Field<String>... fields) {
        if (keyword == null || keyword.isBlank() || fields.length == 0) {
            return null;
        }
        String pattern = "%" + keyword + "%";
        Condition result = DSL.falseCondition();
        for (Field<String> f : fields) {
            result = result.or(f.likeIgnoreCase(pattern));
        }
        return result;
    }

    /* ──────────── 数据权限 ──────────── */

    /**
     * 获取数据权限条件（由 DataScopeAspect 通过 ThreadLocal 注入）。
     * 在 Repository 的分页查询中调用，返回 null 表示无限制。
     */
    public static Condition dataScopeCondition() {
        return DataScopeContext.get();
    }

    /* ──────────── 批量操作 ──────────── */

    /**
     * 批量插入。
     *
     * @param dsl     DSLContext
     * @param table   表引用
     * @param fields  字段引用列表
     * @param records 每条记录的字段值列表
     */
    @SuppressWarnings("unchecked")
    public static void batchInsert(DSLContext dsl, Table<?> table,
                                    List<Field<?>> fields, List<List<Object>> records) {
        if (records == null || records.isEmpty()) return;

        var fieldRefs = fields.toArray(new Field[0]);
        var insert = dsl.insertInto(table, fieldRefs);
        for (List<Object> values : records) {
            insert = insert.values(values);
        }
        insert.execute();
    }

}
