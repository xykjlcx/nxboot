-- 服务器监控菜单（幂等写法）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, permission, icon, sort_order, visible, enabled, create_by, create_time, update_by, update_time, deleted)
VALUES (1012, 100, '服务器监控', 'C', '/system/monitor', NULL, 'system:monitor:list', 'DashboardOutlined', 13, 1, 1, 1, NOW(), 1, NOW(), 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1012) ON CONFLICT DO NOTHING;
