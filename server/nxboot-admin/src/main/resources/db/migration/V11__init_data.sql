-- ========== 初始化管理员用户 ==========
INSERT INTO sys_user (id, username, password, nickname, enabled, create_by, create_time, update_by, update_time, deleted)
VALUES (1, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '超级管理员', 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- ========== 初始化管理员角色 ==========
INSERT INTO sys_role (id, role_key, role_name, sort_order, enabled, create_by, create_time, update_by, update_time, deleted)
VALUES (1, 'admin', '超级管理员', 1, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- ========== 用户-角色关联 ==========
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- ========== 系统管理一级菜单 ==========
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, permission, icon, sort_order, visible, enabled, create_by, create_time, update_by, update_time, deleted) VALUES
(100, 0, '系统管理', 'M', '/system', NULL, NULL, 'Setting', 1, 1, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- ========== 系统管理二级菜单 ==========
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, permission, icon, sort_order, visible, enabled, create_by, create_time, update_by, update_time, deleted) VALUES
(1001, 100, '用户管理', 'C', 'user',   'system/user/index',   'system:user:list',   'User',     1, 1, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(1002, 100, '角色管理', 'C', 'role',   'system/role/index',   'system:role:list',   'Peoples',  2, 1, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(1003, 100, '菜单管理', 'C', 'menu',   'system/menu/index',   'system:menu:list',   'TreeTable', 3, 1, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(1004, 100, '字典管理', 'C', 'dict',   'system/dict/index',   'system:dict:list',   'Dict',     4, 1, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(1005, 100, '参数配置', 'C', 'config', 'system/config/index', 'system:config:list', 'Edit',     5, 1, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(1006, 100, '操作日志', 'C', 'log',    'system/log/index',    'system:log:list',    'Log',      6, 1, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(1007, 100, '文件管理', 'C', 'file',   'system/file/index',   'system:file:list',   'Document', 7, 1, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(1008, 100, '任务管理', 'C', 'job',    'system/job/index',    'system:job:list',    'Job',      8, 1, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- ========== 用户管理按钮权限 ==========
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, permission, icon, sort_order, visible, enabled, create_by, create_time, update_by, update_time, deleted) VALUES
(2001, 1001, '用户查询', 'F', '', NULL, 'system:user:query',    NULL, 1, 1, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2002, 1001, '用户新增', 'F', '', NULL, 'system:user:create',   NULL, 2, 1, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2003, 1001, '用户修改', 'F', '', NULL, 'system:user:update',   NULL, 3, 1, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2004, 1001, '用户删除', 'F', '', NULL, 'system:user:delete',   NULL, 4, 1, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2005, 1001, '重置密码', 'F', '', NULL, 'system:user:resetPwd', NULL, 5, 1, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- ========== 管理员拥有所有菜单权限 ==========
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 100),
(1, 1001), (1, 1002), (1, 1003), (1, 1004), (1, 1005), (1, 1006), (1, 1007), (1, 1008),
(1, 2001), (1, 2002), (1, 2003), (1, 2004), (1, 2005);

-- ========== 默认字典类型 ==========
INSERT INTO sys_dict_type (id, dict_type, dict_name, enabled, create_by, create_time, update_by, update_time, deleted) VALUES
(1, 'sys_common_status', '通用状态', 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'sys_yes_no',        '是否',     1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, sort_order, enabled, create_by, create_time, update_by, update_time, deleted) VALUES
(1, 'sys_common_status', '启用', '1', 1, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'sys_common_status', '禁用', '0', 2, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(3, 'sys_yes_no',        '是',   '1', 1, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(4, 'sys_yes_no',        '否',   '0', 2, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
