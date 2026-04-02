-- 菜单表
CREATE TABLE sys_menu (
    id          BIGINT       PRIMARY KEY,
    parent_id   BIGINT       DEFAULT 0,
    menu_name   VARCHAR(64)  NOT NULL,
    menu_type   CHAR(1)      NOT NULL,
    path        VARCHAR(256),
    component   VARCHAR(256),
    permission  VARCHAR(128),
    icon        VARCHAR(64),
    sort_order  INT          NOT NULL DEFAULT 0,
    visible     INT          NOT NULL DEFAULT 1,
    enabled     INT          NOT NULL DEFAULT 1,
    create_by   VARCHAR(64),
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by   VARCHAR(64),
    update_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     INT          NOT NULL DEFAULT 0
);

COMMENT ON TABLE sys_menu IS '菜单表';
COMMENT ON COLUMN sys_menu.parent_id IS '父菜单ID，顶级为0';
COMMENT ON COLUMN sys_menu.menu_type IS '菜单类型：M=目录，C=菜单，F=按钮';
COMMENT ON COLUMN sys_menu.path IS '路由路径';
COMMENT ON COLUMN sys_menu.component IS '组件路径';
COMMENT ON COLUMN sys_menu.permission IS '权限标识';
COMMENT ON COLUMN sys_menu.visible IS '是否可见：1=可见，0=隐藏';
COMMENT ON COLUMN sys_menu.enabled IS '状态：1=启用，0=禁用';
COMMENT ON COLUMN sys_menu.deleted IS '逻辑删除：0=未删除，1=已删除';
