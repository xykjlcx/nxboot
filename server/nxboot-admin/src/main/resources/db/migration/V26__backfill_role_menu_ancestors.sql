-- 回填 sys_role_menu 中缺失的祖先目录节点
-- 解决 P1-3 历史坏数据：升级前只保存了叶子菜单的角色，导航树为空
-- 幂等：ON CONFLICT DO NOTHING，重复执行不报错

WITH RECURSIVE menu_ancestors AS (
    -- 起点：所有现有 role_menu 关联的直接父节点
    SELECT rm.role_id, m.parent_id AS ancestor_id
    FROM sys_role_menu rm
    JOIN sys_menu m ON rm.menu_id = m.id
    WHERE m.parent_id != 0
      AND m.deleted = 0

    UNION

    -- 递归向上：祖先的父节点
    SELECT ma.role_id, m.parent_id AS ancestor_id
    FROM menu_ancestors ma
    JOIN sys_menu m ON ma.ancestor_id = m.id
    WHERE m.parent_id != 0
      AND m.deleted = 0
)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT role_id, ancestor_id
FROM menu_ancestors
WHERE ancestor_id != 0
ON CONFLICT DO NOTHING;
