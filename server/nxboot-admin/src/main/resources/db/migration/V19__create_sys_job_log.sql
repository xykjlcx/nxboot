CREATE TABLE IF NOT EXISTS sys_job_log (
    id              BIGINT PRIMARY KEY,
    job_id          BIGINT NOT NULL,
    job_name        VARCHAR(128) NOT NULL,
    job_group       VARCHAR(64),
    invoke_target   VARCHAR(500),
    status          INT DEFAULT 0,      -- 0=成功 1=失败
    error_msg       TEXT,
    start_time      TIMESTAMP,
    end_time        TIMESTAMP,
    duration        BIGINT DEFAULT 0    -- 毫秒
);

COMMENT ON TABLE sys_job_log IS '定时任务执行日志';
CREATE INDEX IF NOT EXISTS idx_job_log_job_id ON sys_job_log(job_id);
CREATE INDEX IF NOT EXISTS idx_job_log_time ON sys_job_log(start_time);
