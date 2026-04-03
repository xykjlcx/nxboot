-- 登录日志菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, permission, icon, sort_order, visible, enabled, create_by, create_time, update_by, update_time, deleted)
VALUES (1009, 100, '登录日志', 'C', 'login-log', 'system/login-log/index', 'system:loginLog:list', 'Solution', 9, 1, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- 管理员角色关联登录日志菜单
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1009);
