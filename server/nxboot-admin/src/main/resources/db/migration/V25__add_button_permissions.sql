-- 补齐所有模块的 F 类型按钮权限种子数据
-- 解决 P1-2：非 admin 角色无法配置写操作权限

-- ========== 角色管理按钮权限（parent: 1002）==========
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, permission, icon, sort_order, visible, enabled, create_by, create_time, update_by, update_time, deleted) VALUES
(2020, 1002, '角色新增', 'F', '', NULL, 'system:role:create', NULL, 1, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(2021, 1002, '角色修改', 'F', '', NULL, 'system:role:update', NULL, 2, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(2022, 1002, '角色删除', 'F', '', NULL, 'system:role:delete', NULL, 3, 1, 1, 'system', NOW(), 'system', NOW(), 0)
ON CONFLICT (id) DO NOTHING;

-- ========== 菜单管理按钮权限（parent: 1003）==========
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, permission, icon, sort_order, visible, enabled, create_by, create_time, update_by, update_time, deleted) VALUES
(2030, 1003, '菜单新增', 'F', '', NULL, 'system:menu:create', NULL, 1, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(2031, 1003, '菜单修改', 'F', '', NULL, 'system:menu:update', NULL, 2, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(2032, 1003, '菜单删除', 'F', '', NULL, 'system:menu:delete', NULL, 3, 1, 1, 'system', NOW(), 'system', NOW(), 0)
ON CONFLICT (id) DO NOTHING;

-- ========== 字典管理按钮权限（parent: 1004）==========
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, permission, icon, sort_order, visible, enabled, create_by, create_time, update_by, update_time, deleted) VALUES
(2040, 1004, '字典新增', 'F', '', NULL, 'system:dict:create', NULL, 1, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(2041, 1004, '字典修改', 'F', '', NULL, 'system:dict:update', NULL, 2, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(2042, 1004, '字典删除', 'F', '', NULL, 'system:dict:delete', NULL, 3, 1, 1, 'system', NOW(), 'system', NOW(), 0)
ON CONFLICT (id) DO NOTHING;

-- ========== 参数配置按钮权限（parent: 1005）==========
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, permission, icon, sort_order, visible, enabled, create_by, create_time, update_by, update_time, deleted) VALUES
(2050, 1005, '配置新增', 'F', '', NULL, 'system:config:create', NULL, 1, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(2051, 1005, '配置修改', 'F', '', NULL, 'system:config:update', NULL, 2, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(2052, 1005, '配置删除', 'F', '', NULL, 'system:config:delete', NULL, 3, 1, 1, 'system', NOW(), 'system', NOW(), 0)
ON CONFLICT (id) DO NOTHING;

-- ========== 文件管理按钮权限（parent: 1007）==========
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, permission, icon, sort_order, visible, enabled, create_by, create_time, update_by, update_time, deleted) VALUES
(2070, 1007, '文件上传', 'F', '', NULL, 'system:file:upload', NULL, 1, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(2071, 1007, '文件删除', 'F', '', NULL, 'system:file:delete', NULL, 2, 1, 1, 'system', NOW(), 'system', NOW(), 0)
ON CONFLICT (id) DO NOTHING;

-- ========== 任务管理按钮权限（parent: 1008）==========
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, permission, icon, sort_order, visible, enabled, create_by, create_time, update_by, update_time, deleted) VALUES
(2080, 1008, '任务新增', 'F', '', NULL, 'system:job:create', NULL, 1, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(2081, 1008, '任务修改', 'F', '', NULL, 'system:job:update', NULL, 2, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(2082, 1008, '任务删除', 'F', '', NULL, 'system:job:delete', NULL, 3, 1, 1, 'system', NOW(), 'system', NOW(), 0)
ON CONFLICT (id) DO NOTHING;

-- ========== 部门管理按钮权限（parent: 1080）==========
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, permission, icon, sort_order, visible, enabled, create_by, create_time, update_by, update_time, deleted) VALUES
(2160, 1080, '部门新增', 'F', '', NULL, 'system:dept:create', NULL, 1, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(2161, 1080, '部门修改', 'F', '', NULL, 'system:dept:update', NULL, 2, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(2162, 1080, '部门删除', 'F', '', NULL, 'system:dept:delete', NULL, 3, 1, 1, 'system', NOW(), 'system', NOW(), 0)
ON CONFLICT (id) DO NOTHING;

-- ========== admin 角色绑定新增按钮权限 ==========
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 2020), (1, 2021), (1, 2022),
(1, 2030), (1, 2031), (1, 2032),
(1, 2040), (1, 2041), (1, 2042),
(1, 2050), (1, 2051), (1, 2052),
(1, 2070), (1, 2071),
(1, 2080), (1, 2081), (1, 2082),
(1, 2160), (1, 2161), (1, 2162)
ON CONFLICT DO NOTHING;
