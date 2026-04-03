-- 角色数据权限范围
-- 1=全部 2=自定义部门 3=本部门 4=本部门及下级 5=仅本人
ALTER TABLE sys_role ADD COLUMN IF NOT EXISTS data_scope INT DEFAULT 1;
COMMENT ON COLUMN sys_role.data_scope IS '数据权限范围(1全部 2自定义 3本部门 4本部门及下级 5仅本人)';

-- 角色-部门关联表（用于自定义数据权限）
CREATE TABLE IF NOT EXISTS sys_role_dept (
    role_id BIGINT NOT NULL,
    dept_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, dept_id)
);
COMMENT ON TABLE sys_role_dept IS '角色-部门关联（自定义数据权限）';
