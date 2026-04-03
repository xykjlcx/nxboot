-- 登录日志表
CREATE TABLE sys_login_log (
    id          BIGINT PRIMARY KEY,
    username    VARCHAR(64) NOT NULL,
    ip          VARCHAR(128),
    user_agent  VARCHAR(500),
    status      INT DEFAULT 0,              -- 0=成功 1=失败
    message     VARCHAR(500),               -- 失败原因
    login_time  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE sys_login_log IS '登录日志';
COMMENT ON COLUMN sys_login_log.status IS '0=成功 1=失败';
COMMENT ON COLUMN sys_login_log.message IS '失败原因';

CREATE INDEX idx_login_log_username ON sys_login_log(username);
CREATE INDEX idx_login_log_time ON sys_login_log(login_time);
