-- 给"普通用户"角色（id=2）分配真实可访问的菜单页面
-- 选择字典管理和参数配置：这两个是只读参考数据，安全且有实际价值
-- 幂等写法：已存在的绑定不会重复插入

-- 字典管理（C 菜单）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 1004) ON CONFLICT DO NOTHING;
-- 参数配置（C 菜单）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 1005) ON CONFLICT DO NOTHING;
