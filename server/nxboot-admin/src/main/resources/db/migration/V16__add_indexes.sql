-- 唯一业务索引（部分索引，仅未删除记录）
CREATE UNIQUE INDEX IF NOT EXISTS uk_user_username ON sys_user(username) WHERE deleted = 0;
CREATE UNIQUE INDEX IF NOT EXISTS uk_role_key ON sys_role(role_key) WHERE deleted = 0;
CREATE UNIQUE INDEX IF NOT EXISTS uk_config_key ON sys_config(config_key) WHERE deleted = 0;

-- 查询索引
CREATE INDEX IF NOT EXISTS idx_dict_data_type ON sys_dict_data(dict_type);
-- idx_dept_parent 已在 V14 中创建
