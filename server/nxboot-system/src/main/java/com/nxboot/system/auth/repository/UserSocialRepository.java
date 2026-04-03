package com.nxboot.system.auth.repository;

import com.nxboot.common.util.SnowflakeIdGenerator;
import com.nxboot.framework.oauth2.OAuth2UserInfo;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

/**
 * 用户社会化登录绑定数据访问
 */
@Repository
public class UserSocialRepository {

    private static final String TABLE = "sys_user_social";

    private final DSLContext dsl;
    private final SnowflakeIdGenerator idGenerator;

    public UserSocialRepository(DSLContext dsl, SnowflakeIdGenerator idGenerator) {
        this.dsl = dsl;
        this.idGenerator = idGenerator;
    }

    /**
     * 根据三方平台标识查找绑定记录
     *
     * @return 绑定记录，未找到返回 null
     */
    public Record findByProviderAndProviderId(String provider, String providerId) {
        return dsl.select()
                .from(table(TABLE))
                .where(field("provider").eq(provider))
                .and(field("provider_id").eq(providerId))
                .fetchOne();
    }

    /**
     * 创建社会化登录绑定
     */
    public void insert(Long userId, OAuth2UserInfo userInfo) {
        dsl.insertInto(table(TABLE))
                .set(field("id"), idGenerator.nextId())
                .set(field("user_id"), userId)
                .set(field("provider"), userInfo.provider())
                .set(field("provider_id"), userInfo.providerId())
                .set(field("username"), userInfo.username())
                .set(field("email"), userInfo.email())
                .set(field("avatar"), userInfo.avatar())
                .set(field("create_time"), LocalDateTime.now())
                .execute();
    }

    /**
     * 更新绑定信息（每次登录时同步三方平台最新信息）
     */
    public void updateUserInfo(String provider, String providerId, OAuth2UserInfo userInfo) {
        dsl.update(table(TABLE))
                .set(field("username"), userInfo.username())
                .set(field("email"), userInfo.email())
                .set(field("avatar"), userInfo.avatar())
                .where(field("provider").eq(provider))
                .and(field("provider_id").eq(providerId))
                .execute();
    }
}
