-- 补充缺失的菜单记录：在线用户、任务日志、服务器监控
-- 同时修正部门管理的 sort_order（避免与菜单管理冲突，原值都是 3）

-- 在线用户管理（system:online:list）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, permission, icon, sort_order, visible, enabled, create_by, create_time, update_by, update_time, deleted)
VALUES (1010, 100, '在线用户', 'C', '/system/online', NULL, 'system:online:list', 'UserOutlined', 10, 1, 1, 1, NOW(), 1, NOW(), 0);

-- 任务执行日志（system:jobLog:list）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, permission, icon, sort_order, visible, enabled, create_by, create_time, update_by, update_time, deleted)
VALUES (1011, 100, '任务日志', 'C', '/system/job-log', NULL, 'system:jobLog:list', 'ScheduleOutlined', 11, 1, 1, 1, NOW(), 1, NOW(), 0);

-- 在线用户按钮权限
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, permission, icon, sort_order, visible, enabled, create_by, create_time, update_by, update_time, deleted)
VALUES (2010, 1010, '在线查询', 'F', '', NULL, 'system:online:query', NULL, 1, 1, 1, 1, NOW(), 1, NOW(), 0);

INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, permission, icon, sort_order, visible, enabled, create_by, create_time, update_by, update_time, deleted)
VALUES (2011, 1010, '强制下线', 'F', '', NULL, 'system:online:forceLogout', NULL, 2, 1, 1, 1, NOW(), 1, NOW(), 0);

-- admin 角色绑定新菜单
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1010);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1011);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2010);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2011);

-- 修正部门管理 sort_order 避免与菜单管理冲突
-- 仅在 sort_order 仍为初始默认值时才更新，不覆盖用户手工调整
UPDATE sys_menu SET sort_order = 3 WHERE id = 1080 AND sort_order = 3;
UPDATE sys_menu SET sort_order = 4 WHERE id = 1003 AND sort_order = 3;
UPDATE sys_menu SET sort_order = 5 WHERE id = 1004 AND sort_order = 4;
UPDATE sys_menu SET sort_order = 6 WHERE id = 1005 AND sort_order = 5;
UPDATE sys_menu SET sort_order = 7 WHERE id = 1006 AND sort_order = 6;
UPDATE sys_menu SET sort_order = 8 WHERE id = 1007 AND sort_order = 7;
UPDATE sys_menu SET sort_order = 9 WHERE id = 1008 AND sort_order = 8;
UPDATE sys_menu SET sort_order = 12 WHERE id = 1009 AND sort_order = 9;
