-- 新增"普通用户"默认角色，用于 OAuth2 自动注册的用户
INSERT INTO sys_role (id, role_name, role_key, sort_order, enabled, data_scope, version, create_by, create_time, update_by, update_time, deleted)
VALUES (2, '普通用户', 'user', 2, 1, 5, 0, 1, NOW(), 1, NOW(), 0);

-- 普通用户只赋予基础查看权限（系统管理目录 + 个人可见的菜单）
-- 具体菜单权限由管理员后续配置，这里只绑定顶级目录让用户能登录不报 403
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 100);
