-- 定时任务表
CREATE TABLE sys_job (
    id              BIGINT       PRIMARY KEY,
    job_name        VARCHAR(128) NOT NULL,
    job_group       VARCHAR(64)  DEFAULT 'DEFAULT',
    invoke_target   VARCHAR(512) NOT NULL,
    cron_expression VARCHAR(64)  NOT NULL,
    misfire_policy  INT          DEFAULT 1,
    concurrent      INT          DEFAULT 0,
    enabled         INT          NOT NULL DEFAULT 1,
    remark          VARCHAR(512),
    create_by       VARCHAR(64),
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by       VARCHAR(64),
    update_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         INT          NOT NULL DEFAULT 0
);

COMMENT ON TABLE sys_job IS '定时任务表';
COMMENT ON COLUMN sys_job.invoke_target IS '调用目标（Spring Bean 名称.方法名）';
COMMENT ON COLUMN sys_job.cron_expression IS 'Cron 表达式';
COMMENT ON COLUMN sys_job.misfire_policy IS '计划执行策略：1=立即执行，2=执行一次，3=放弃执行';
COMMENT ON COLUMN sys_job.concurrent IS '是否并发执行：0=禁止，1=允许';

-- 定时任务日志表
CREATE TABLE sys_job_log (
    id          BIGINT       PRIMARY KEY,
    job_id      BIGINT       NOT NULL,
    job_name    VARCHAR(128),
    invoke_target VARCHAR(512),
    message     TEXT,
    status      INT          NOT NULL DEFAULT 1,
    error_msg   TEXT,
    duration    BIGINT,
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE sys_job_log IS '定时任务日志表';
COMMENT ON COLUMN sys_job_log.status IS '状态：1=成功，0=失败';
COMMENT ON COLUMN sys_job_log.duration IS '耗时（毫秒）';
