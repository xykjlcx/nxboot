-- V14__create_sys_dept.sql
CREATE TABLE sys_dept (
    id          BIGINT PRIMARY KEY,
    parent_id   BIGINT NOT NULL DEFAULT 0,
    dept_name   VARCHAR(64) NOT NULL,
    sort_order  INT DEFAULT 0,
    leader      VARCHAR(64),
    phone       VARCHAR(20),
    email       VARCHAR(128),
    enabled     INT DEFAULT 1,
    create_by   VARCHAR(64),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by   VARCHAR(64),
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted     INT DEFAULT 0
);

COMMENT ON TABLE sys_dept IS '部门';
COMMENT ON COLUMN sys_dept.parent_id IS '上级部门ID（0=顶级）';
CREATE INDEX idx_dept_parent ON sys_dept(parent_id);
