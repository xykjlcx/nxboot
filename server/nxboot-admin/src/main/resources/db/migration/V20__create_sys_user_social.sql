-- 用户社会化登录绑定表
CREATE TABLE sys_user_social (
    id          BIGINT       PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    provider    VARCHAR(32)  NOT NULL,
    provider_id VARCHAR(256) NOT NULL,
    username    VARCHAR(128),
    email       VARCHAR(256),
    avatar      VARCHAR(512),
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_social_provider UNIQUE (provider, provider_id)
);

COMMENT ON TABLE sys_user_social IS '用户社会化登录绑定';
COMMENT ON COLUMN sys_user_social.user_id IS '关联的本地用户ID';
COMMENT ON COLUMN sys_user_social.provider IS '三方平台标识（github/google/wechat）';
COMMENT ON COLUMN sys_user_social.provider_id IS '三方平台用户ID';

CREATE INDEX idx_social_user_id ON sys_user_social(user_id);
