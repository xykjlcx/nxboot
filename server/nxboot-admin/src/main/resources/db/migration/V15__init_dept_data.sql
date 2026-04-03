-- V15__init_dept_data.sql
INSERT INTO sys_dept (id, parent_id, dept_name, sort_order, leader, enabled, create_by, create_time, update_by, update_time, deleted)
VALUES
(100, 0, 'NxBoot科技', 0, 'admin', 1, 'admin', NOW(), 'admin', NOW(), 0),
(101, 100, '技术部', 1, NULL, 1, 'admin', NOW(), 'admin', NOW(), 0),
(102, 100, '产品部', 2, NULL, 1, 'admin', NOW(), 'admin', NOW(), 0),
(103, 100, '运营部', 3, NULL, 1, 'admin', NOW(), 'admin', NOW(), 0);

-- 用户表增加部门关联
ALTER TABLE sys_user ADD COLUMN dept_id BIGINT DEFAULT NULL;
COMMENT ON COLUMN sys_user.dept_id IS '所属部门ID';

-- 更新 admin 用户的部门
UPDATE sys_user SET dept_id = 100 WHERE username = 'admin';

-- 菜单：在系统管理下添加部门管理
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, permission, icon, sort_order, visible, enabled, create_by, create_time, update_by, update_time, deleted)
SELECT 1080, id, '部门管理', 'C', '/system/dept', NULL, 'system:dept:list', 'TeamOutlined', 3, 1, 1, 'admin', NOW(), 'admin', NOW(), 0
FROM sys_menu WHERE menu_name = '系统管理' AND menu_type = 'M' LIMIT 1;
