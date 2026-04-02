-- 角色表
CREATE TABLE sys_role (
    id          BIGINT       PRIMARY KEY,
    role_key    VARCHAR(64)  NOT NULL UNIQUE,
    role_name   VARCHAR(64)  NOT NULL,
    sort_order  INT          NOT NULL DEFAULT 0,
    enabled     INT          NOT NULL DEFAULT 1,
    remark      VARCHAR(512),
    create_by   VARCHAR(64),
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by   VARCHAR(64),
    update_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     INT          NOT NULL DEFAULT 0
);

COMMENT ON TABLE sys_role IS '角色表';
COMMENT ON COLUMN sys_role.role_key IS '角色标识';
COMMENT ON COLUMN sys_role.role_name IS '角色名称';
COMMENT ON COLUMN sys_role.sort_order IS '排序';
COMMENT ON COLUMN sys_role.enabled IS '状态：1=启用，0=禁用';
COMMENT ON COLUMN sys_role.deleted IS '逻辑删除：0=未删除，1=已删除';
