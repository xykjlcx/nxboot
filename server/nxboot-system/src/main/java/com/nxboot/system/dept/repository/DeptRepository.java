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

import static com.nxboot.generated.jooq.tables.SysDept.SYS_DEPT;

/**
 * 部门数据访问
 */
@Repository
public class DeptRepository {

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
                .from(SYS_DEPT)
                .where(JooqHelper.notDeleted())
                .orderBy(SYS_DEPT.SORT_ORDER.asc())
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
        Record record = JooqHelper.findById(dsl, SYS_DEPT, id);
        return record != null ? toVO(record) : null;
    }

    /**
     * 插入部门，返回 ID
     */
    public Long insert(Long parentId, String deptName, Integer sortOrder,
                       String leader, String phone, String email, String operator) {
        var step = dsl.insertInto(SYS_DEPT)
                .set(SYS_DEPT.PARENT_ID, parentId != null ? parentId : 0L)
                .set(SYS_DEPT.DEPT_NAME, deptName)
                .set(SYS_DEPT.SORT_ORDER, sortOrder != null ? sortOrder : 0)
                .set(SYS_DEPT.LEADER, leader)
                .set(SYS_DEPT.PHONE, phone)
                .set(SYS_DEPT.EMAIL, email)
                .set(SYS_DEPT.ENABLED, Constants.ENABLED);

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
        var step = dsl.update(SYS_DEPT)
                .set(SYS_DEPT.UPDATE_BY, operator)
                .set(SYS_DEPT.UPDATE_TIME, LocalDateTime.now());

        if (parentId != null) step = step.set(SYS_DEPT.PARENT_ID, parentId);
        if (deptName != null) step = step.set(SYS_DEPT.DEPT_NAME, deptName);
        if (sortOrder != null) step = step.set(SYS_DEPT.SORT_ORDER, sortOrder);
        if (leader != null) step = step.set(SYS_DEPT.LEADER, leader);
        if (phone != null) step = step.set(SYS_DEPT.PHONE, phone);
        if (email != null) step = step.set(SYS_DEPT.EMAIL, email);
        if (enabled != null) step = step.set(SYS_DEPT.ENABLED, enabled ? Constants.ENABLED : Constants.DISABLED);

        step.where(SYS_DEPT.ID.eq(id)).and(JooqHelper.notDeleted()).execute();
    }

    /**
     * 逻辑删除
     */
    public void softDelete(Long id, String operator) {
        JooqHelper.softDelete(dsl, SYS_DEPT, id, operator);
    }

    /**
     * 检查是否有子部门
     */
    public boolean hasChildren(Long parentId) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(SYS_DEPT)
                        .where(SYS_DEPT.PARENT_ID.eq(parentId))
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
        Integer enabledVal = r.get(SYS_DEPT.ENABLED);
        return new DeptVO(
                r.get(SYS_DEPT.ID),
                r.get(SYS_DEPT.PARENT_ID),
                r.get(SYS_DEPT.DEPT_NAME),
                r.get(SYS_DEPT.SORT_ORDER),
                r.get(SYS_DEPT.LEADER),
                r.get(SYS_DEPT.PHONE),
                r.get(SYS_DEPT.EMAIL),
                enabledVal != null && enabledVal == 1,
                r.get(SYS_DEPT.CREATE_TIME),
                null
        );
    }
}
