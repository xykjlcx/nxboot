package com.nxboot.system.dept.repository;

import com.nxboot.common.constant.Constants;
import com.nxboot.common.util.SnowflakeIdGenerator;
import com.nxboot.framework.jooq.JooqHelper;
import com.nxboot.system.dept.model.DeptVO;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

/**
 * 部门数据访问
 */
@Repository
public class DeptRepository {

    private static final String TABLE = "sys_dept";

    private final DSLContext dsl;
    private final SnowflakeIdGenerator idGenerator;

    public DeptRepository(DSLContext dsl, SnowflakeIdGenerator idGenerator) {
        this.dsl = dsl;
        this.idGenerator = idGenerator;
    }

    /**
     * 查询所有部门（平铺列表）
     */
    public List<DeptVO> findAll() {
        return dsl.select()
                .from(table(TABLE))
                .where(JooqHelper.notDeleted())
                .orderBy(field("sort_order").asc())
                .fetch(this::toVO);
    }

    /**
     * 构建部门树（全量）
     */
    public List<DeptVO> buildTree() {
        List<DeptVO> allDepts = findAll();
        return buildTree(allDepts, 0L);
    }

    /**
     * 根据 ID 查询
     */
    public DeptVO findById(Long id) {
        Record record = JooqHelper.findById(dsl, TABLE, id);
        return record != null ? toVO(record) : null;
    }

    /**
     * 插入部门，返回 ID
     */
    public Long insert(Long parentId, String deptName, Integer sortOrder,
                       String leader, String phone, String email, String operator) {
        var step = dsl.insertInto(table(TABLE))
                .set(field("parent_id"), parentId != null ? parentId : 0L)
                .set(field("dept_name"), deptName)
                .set(field("sort_order"), sortOrder != null ? sortOrder : 0)
                .set(field("leader"), leader)
                .set(field("phone"), phone)
                .set(field("email"), email)
                .set(field("enabled"), Constants.ENABLED);

        Long id = JooqHelper.setAuditInsert(step, idGenerator, operator);
        step.execute();
        return id;
    }

    /**
     * 更新部门
     */
    public void update(Long id, Long parentId, String deptName, Integer sortOrder,
                       String leader, String phone, String email,
                       Boolean enabled, String operator) {
        var step = dsl.update(table(TABLE))
                .set(field("update_by"), operator)
                .set(field("update_time"), LocalDateTime.now());

        if (parentId != null) step = step.set(field("parent_id"), parentId);
        if (deptName != null) step = step.set(field("dept_name"), deptName);
        if (sortOrder != null) step = step.set(field("sort_order"), sortOrder);
        if (leader != null) step = step.set(field("leader"), leader);
        if (phone != null) step = step.set(field("phone"), phone);
        if (email != null) step = step.set(field("email"), email);
        if (enabled != null) step = step.set(field("enabled"), enabled ? Constants.ENABLED : Constants.DISABLED);

        step.where(field("id").eq(id)).and(JooqHelper.notDeleted()).execute();
    }

    /**
     * 逻辑删除
     */
    public void softDelete(Long id, String operator) {
        JooqHelper.softDelete(dsl, TABLE, id, operator);
    }

    /**
     * 检查是否有子部门
     */
    public boolean hasChildren(Long parentId) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(table(TABLE))
                        .where(field("parent_id").eq(parentId))
                        .and(JooqHelper.notDeleted())
        );
    }

    /**
     * 构建部门树
     */
    private List<DeptVO> buildTree(List<DeptVO> allDepts, Long parentId) {
        Map<Long, List<DeptVO>> grouped = allDepts.stream()
                .collect(Collectors.groupingBy(DeptVO::parentId));

        return buildChildren(grouped, parentId);
    }

    private List<DeptVO> buildChildren(Map<Long, List<DeptVO>> grouped, Long parentId) {
        List<DeptVO> children = grouped.getOrDefault(parentId, new ArrayList<>());
        return children.stream()
                .map(dept -> new DeptVO(
                        dept.id(), dept.parentId(), dept.deptName(), dept.sortOrder(),
                        dept.leader(), dept.phone(), dept.email(), dept.enabled(),
                        dept.createTime(),
                        buildChildren(grouped, dept.id())
                ))
                .toList();
    }

    /**
     * Record → DeptVO 转换
     */
    private DeptVO toVO(Record r) {
        Integer enabledVal = r.get("enabled", Integer.class);
        return new DeptVO(
                r.get("id", Long.class),
                r.get("parent_id", Long.class),
                r.get("dept_name", String.class),
                r.get("sort_order", Integer.class),
                r.get("leader", String.class),
                r.get("phone", String.class),
                r.get("email", String.class),
                enabledVal != null && enabledVal == 1,
                r.get("create_time", LocalDateTime.class),
                null
        );
    }
}
