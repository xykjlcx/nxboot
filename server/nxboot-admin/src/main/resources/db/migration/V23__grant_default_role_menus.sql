-- 给"普通用户"角色（id=2）分配真实可访问的菜单页面
-- 选择字典管理和参数配置：这两个是只读参考数据，安全且有实际价值

-- 字典管理（C 菜单）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 1004);
-- 参数配置（C 菜单）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 1005);
