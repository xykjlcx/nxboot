-- 文件表
CREATE TABLE sys_file (
    id            BIGINT       PRIMARY KEY,
    file_name     VARCHAR(256) NOT NULL,
    original_name VARCHAR(256) NOT NULL,
    file_path     VARCHAR(512) NOT NULL,
    file_size     BIGINT,
    file_type     VARCHAR(64),
    create_by     VARCHAR(64),
    create_time   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE sys_file IS '文件表';
COMMENT ON COLUMN sys_file.file_name IS '存储文件名';
COMMENT ON COLUMN sys_file.original_name IS '原始文件名';
COMMENT ON COLUMN sys_file.file_path IS '文件路径';
COMMENT ON COLUMN sys_file.file_size IS '文件大小（字节）';
COMMENT ON COLUMN sys_file.file_type IS '文件类型（MIME）';
