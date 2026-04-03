package com.nxboot.system.auth.repository;

import com.nxboot.common.util.SnowflakeIdGenerator;
import com.nxboot.framework.oauth2.OAuth2UserInfo;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

import static com.nxboot.generated.jooq.tables.SysUserSocial.SYS_USER_SOCIAL;

/**
 * 用户社会化登录绑定数据访问
 */
@Repository
public class UserSocialRepository {

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
                .from(SYS_USER_SOCIAL)
                .where(SYS_USER_SOCIAL.PROVIDER.eq(provider))
                .and(SYS_USER_SOCIAL.PROVIDER_ID.eq(providerId))
                .fetchOne();
    }

    /**
     * 创建社会化登录绑定
     */
    public void insert(Long userId, OAuth2UserInfo userInfo) {
        dsl.insertInto(SYS_USER_SOCIAL)
                .set(SYS_USER_SOCIAL.ID, idGenerator.nextId())
                .set(SYS_USER_SOCIAL.USER_ID, userId)
                .set(SYS_USER_SOCIAL.PROVIDER, userInfo.provider())
                .set(SYS_USER_SOCIAL.PROVIDER_ID, userInfo.providerId())
                .set(SYS_USER_SOCIAL.USERNAME, userInfo.username())
                .set(SYS_USER_SOCIAL.EMAIL, userInfo.email())
                .set(SYS_USER_SOCIAL.AVATAR, userInfo.avatar())
                .set(SYS_USER_SOCIAL.CREATE_TIME, LocalDateTime.now())
                .execute();
    }

    /**
     * 更新绑定信息（每次登录时同步三方平台最新信息）
     */
    public void updateUserInfo(String provider, String providerId, OAuth2UserInfo userInfo) {
        dsl.update(SYS_USER_SOCIAL)
                .set(SYS_USER_SOCIAL.USERNAME, userInfo.username())
                .set(SYS_USER_SOCIAL.EMAIL, userInfo.email())
                .set(SYS_USER_SOCIAL.AVATAR, userInfo.avatar())
                .where(SYS_USER_SOCIAL.PROVIDER.eq(provider))
                .and(SYS_USER_SOCIAL.PROVIDER_ID.eq(providerId))
                .execute();
    }
}
