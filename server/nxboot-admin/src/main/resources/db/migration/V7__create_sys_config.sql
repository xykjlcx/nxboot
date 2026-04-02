-- 系统配置表
CREATE TABLE sys_config (
    id           BIGINT       PRIMARY KEY,
    config_key   VARCHAR(128) NOT NULL UNIQUE,
    config_value VARCHAR(512),
    config_name  VARCHAR(128) NOT NULL,
    remark       VARCHAR(512),
    create_by    VARCHAR(64),
    create_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by    VARCHAR(64),
    update_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted      INT          NOT NULL DEFAULT 0
);

COMMENT ON TABLE sys_config IS '系统配置表';
COMMENT ON COLUMN sys_config.config_key IS '配置键';
COMMENT ON COLUMN sys_config.config_value IS '配置值';
COMMENT ON COLUMN sys_config.config_name IS '配置名称';
