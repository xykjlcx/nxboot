package com.nxboot.framework.jooq;

import com.nxboot.common.base.PageResult;
import com.nxboot.common.constant.Constants;
import com.nxboot.common.exception.BusinessException;
import com.nxboot.common.exception.ErrorCode;
import com.nxboot.common.util.SnowflakeIdGenerator;
import com.nxboot.framework.security.DataScopeContext;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.InsertSetMoreStep;
import org.jooq.Record;
import org.jooq.UpdateSetMoreStep;
import org.jooq.impl.DSL;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

/**
 * jOOQ 通用工具——减少 Repository 重复代码。
 * <p>
 * 设计为静态方法而非 BaseRepository 继承，保持每个 Repository 透明可读（AI 友好）。
 */
public final class JooqHelper {

    private JooqHelper() {
    }

    /* ──────────── 分页查询 ──────────── */

    /**
     * 通用分页查询（自动加 deleted=0 条件）。
     *
     * @param dsl       DSLContext
     * @param tableName 表名
     * @param extra     额外查询条件（可为 null）
     * @param offset    偏移量
     * @param size      页大小
     * @param mapper    Record → VO 映射函数
     */
    public static <T> PageResult<T> page(DSLContext dsl, String tableName,
                                          Condition extra, int offset, int size,
                                          Function<Record, T> mapper) {
        Condition condition = notDeleted();
        if (extra != null) {
            condition = condition.and(extra);
        }

        long total = dsl.selectCount()
                .from(table(tableName))
                .where(condition)
                .fetchOneInto(Long.class);

        if (total == 0) {
            return PageResult.empty();
        }

        List<T> list = dsl.select()
                .from(table(tableName))
                .where(condition)
                .orderBy(field("create_time").desc())
                .offset(offset)
                .limit(size)
                .fetch(mapper::apply);

        return PageResult.of(list, total);
    }

    /* ──────────── 单条查询 ──────────── */

    /**
     * 根据 ID 查询（自动加 deleted=0 条件）。
     */
    public static Record findById(DSLContext dsl, String tableName, Long id) {
        return dsl.select()
                .from(table(tableName))
                .where(field("id").eq(id))
                .and(notDeleted())
                .fetchOne();
    }

    /* ──────────── 软删除 ──────────── */

    /**
     * 逻辑删除。
     */
    public static void softDelete(DSLContext dsl, String tableName, Long id, String operator) {
        dsl.update(table(tableName))
                .set(field("deleted"), Constants.DELETED)
                .set(field("update_by"), operator)
                .set(field("update_time"), LocalDateTime.now())
                .where(field("id").eq(id))
                .execute();
    }

    /* ──────────── 审计字段自动填充 ──────────── */

    /**
     * 为 INSERT 设置审计字段（id + create_by/time + update_by/time + deleted）。
     *
     * @return 生成的 ID
     */
    public static Long setAuditInsert(InsertSetMoreStep<?> step,
                                       SnowflakeIdGenerator idGen, String operator) {
        Long id = idGen.nextId();
        LocalDateTime now = LocalDateTime.now();
        step.set(field("id"), id)
            .set(field("create_by"), operator)
            .set(field("create_time"), now)
            .set(field("update_by"), operator)
            .set(field("update_time"), now)
            .set(field("deleted"), Constants.NOT_DELETED);
        return id;
    }

    /**
     * 为 UPDATE 设置审计字段（update_by + update_time）。
     */
    public static void setAuditUpdate(UpdateSetMoreStep<?> step, String operator) {
        step.set(field("update_by"), operator)
            .set(field("update_time"), LocalDateTime.now());
    }

    /* ──────────── 乐观锁 ──────────── */

    /**
     * 乐观锁更新——version 不匹配时抛异常。
     *
     * @return 更新后的 version 值
     */
    public static int optimisticUpdate(DSLContext dsl, String tableName, Long id, int currentVersion,
                                        Consumer<UpdateSetMoreStep<?>> setter) {
        var step = dsl.update(table(tableName))
                .set(field("version"), currentVersion + 1);
        setter.accept(step);
        int rows = step.where(field("id").eq(id))
                .and(field("version").eq(currentVersion))
                .and(notDeleted())
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
     * @param fields  要搜索的字段名
     */
    public static Condition keywordCondition(String keyword, String... fields) {
        if (keyword == null || keyword.isBlank() || fields.length == 0) {
            return null;
        }
        String pattern = "%" + keyword + "%";
        Condition result = DSL.falseCondition();
        for (String f : fields) {
            result = result.or(field(f).likeIgnoreCase(pattern));
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
     * @param dsl       DSLContext
     * @param tableName 表名
     * @param fields    字段名列表
     * @param records   每条记录的字段值列表
     */
    @SuppressWarnings("unchecked")
    public static void batchInsert(DSLContext dsl, String tableName,
                                    List<String> fields, List<List<Object>> records) {
        if (records == null || records.isEmpty()) return;

        var fieldRefs = fields.stream().map(DSL::field).toArray(org.jooq.Field[]::new);
        var insert = dsl.insertInto(table(tableName), fieldRefs);
        for (List<Object> values : records) {
            insert = insert.values(values);
        }
        insert.execute();
    }

    /* ──────────── 内部方法 ──────────── */

    /**
     * 未删除条件。
     */
    public static Condition notDeleted() {
        return field("deleted").eq(Constants.NOT_DELETED);
    }
}
