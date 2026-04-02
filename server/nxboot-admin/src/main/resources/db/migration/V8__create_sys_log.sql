-- 操作日志表
CREATE TABLE sys_log (
    id             BIGINT       PRIMARY KEY,
    module         VARCHAR(64),
    operation      VARCHAR(128),
    method         VARCHAR(256),
    request_url    VARCHAR(512),
    request_method VARCHAR(16),
    request_params TEXT,
    response_body  TEXT,
    operator       VARCHAR(64),
    operator_ip    VARCHAR(64),
    status         INT          NOT NULL DEFAULT 1,
    error_msg      TEXT,
    duration       BIGINT,
    create_time    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE sys_log IS '操作日志表';
COMMENT ON COLUMN sys_log.module IS '操作模块';
COMMENT ON COLUMN sys_log.operation IS '操作描述';
COMMENT ON COLUMN sys_log.method IS '请求方法（类.方法名）';
COMMENT ON COLUMN sys_log.status IS '状态：1=成功，0=失败';
COMMENT ON COLUMN sys_log.duration IS '耗时（毫秒）';
