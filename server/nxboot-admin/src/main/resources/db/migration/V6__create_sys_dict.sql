-- 字典类型表
CREATE TABLE sys_dict_type (
    id          BIGINT       PRIMARY KEY,
    dict_type   VARCHAR(128) NOT NULL UNIQUE,
    dict_name   VARCHAR(128) NOT NULL,
    enabled     INT          NOT NULL DEFAULT 1,
    remark      VARCHAR(512),
    create_by   VARCHAR(64),
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by   VARCHAR(64),
    update_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     INT          NOT NULL DEFAULT 0
);

COMMENT ON TABLE sys_dict_type IS '字典类型表';

-- 字典数据表
CREATE TABLE sys_dict_data (
    id          BIGINT       PRIMARY KEY,
    dict_type   VARCHAR(128) NOT NULL,
    dict_label  VARCHAR(128) NOT NULL,
    dict_value  VARCHAR(128) NOT NULL,
    sort_order  INT          NOT NULL DEFAULT 0,
    enabled     INT          NOT NULL DEFAULT 1,
    remark      VARCHAR(512),
    create_by   VARCHAR(64),
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by   VARCHAR(64),
    update_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     INT          NOT NULL DEFAULT 0
);

COMMENT ON TABLE sys_dict_data IS '字典数据表';
