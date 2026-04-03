package com.nxboot.system.auth.service;

import com.nxboot.common.constant.Constants;
import com.nxboot.common.exception.BusinessException;
import com.nxboot.common.exception.ErrorCode;
import com.nxboot.common.util.SnowflakeIdGenerator;
import com.nxboot.framework.oauth2.OAuth2ProviderRegistry;
import com.nxboot.framework.oauth2.OAuth2Provider;
import com.nxboot.framework.oauth2.OAuth2UserInfo;
import com.nxboot.framework.security.JwtTokenProvider;
import com.nxboot.system.auth.model.LoginResponse;
import com.nxboot.system.auth.repository.UserSocialRepository;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

/**
 * OAuth2 社会化登录服务。
 * <p>
 * 核心逻辑：
 * 1. 用授权码从三方平台换取用户信息
 * 2. 查 sys_user_social 绑定表
 * 3. 已绑定 → 直接签发 JWT
 * 4. 未绑定 → 自动创建本地用户 + 绑定记录 → 签发 JWT
 */
@Service
public class OAuth2Service {

    private final OAuth2ProviderRegistry providerRegistry;
    private final UserSocialRepository userSocialRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final DSLContext dsl;
    private final SnowflakeIdGenerator idGenerator;
    private final LoginLogService loginLogService;
    private final OnlineUserService onlineUserService;

    public OAuth2Service(OAuth2ProviderRegistry providerRegistry,
                         UserSocialRepository userSocialRepository,
                         JwtTokenProvider tokenProvider,
                         PasswordEncoder passwordEncoder,
                         DSLContext dsl,
                         SnowflakeIdGenerator idGenerator,
                         LoginLogService loginLogService,
                         OnlineUserService onlineUserService) {
        this.providerRegistry = providerRegistry;
        this.userSocialRepository = userSocialRepository;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.dsl = dsl;
        this.idGenerator = idGenerator;
        this.loginLogService = loginLogService;
        this.onlineUserService = onlineUserService;
    }

    /**
     * 获取已启用的 OAuth2 提供者列表
     */
    public List<String> listProviders() {
        return providerRegistry.listProviders();
    }

    /**
     * 生成授权 URL
     */
    public String buildAuthorizationUrl(String providerName, String redirectUri, String state) {
        OAuth2Provider provider = providerRegistry.getProvider(providerName);
        return provider.buildAuthorizationUrl(redirectUri, state);
    }

    /**
     * OAuth2 回调处理：用授权码换取用户信息，查找或创建本地用户，返回 JWT
     */
    @Transactional
    public LoginResponse handleCallback(String providerName, String code, String redirectUri,
                                         String ip, String userAgent) {
        // 1. 用授权码从三方平台换取用户信息
        OAuth2Provider provider = providerRegistry.getProvider(providerName);
        OAuth2UserInfo userInfo = provider.exchangeForUserInfo(code, redirectUri);

        // 2. 查找绑定记录
        Record socialRecord = userSocialRepository.findByProviderAndProviderId(
                userInfo.provider(), userInfo.providerId());

        Long userId;
        String username;
        String nickname;

        if (socialRecord != null) {
            // 已绑定 → 获取本地用户
            userId = socialRecord.get("user_id", Long.class);

            // 校验用户是否存在且启用
            Record userRecord = dsl.select()
                    .from(table("sys_user"))
                    .where(field("id").eq(userId))
                    .and(field("deleted").eq(Constants.NOT_DELETED))
                    .fetchOne();

            if (userRecord == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "关联用户已被删除");
            }

            Integer enabled = userRecord.get("enabled", Integer.class);
            if (enabled == null || enabled != Constants.ENABLED) {
                throw new BusinessException(ErrorCode.AUTH_ACCOUNT_DISABLED);
            }

            username = userRecord.get("username", String.class);
            nickname = userRecord.get("nickname", String.class);

            // 同步三方平台最新信息
            userSocialRepository.updateUserInfo(userInfo.provider(), userInfo.providerId(), userInfo);
        } else {
            // 未绑定 → 自动创建本地用户
            username = generateUniqueUsername(userInfo);
            nickname = userInfo.username() != null ? userInfo.username() : username;
            userId = createLocalUser(username, nickname, userInfo.email(), userInfo.avatar());

            // 创建绑定记录
            userSocialRepository.insert(userId, userInfo);
        }

        // 3. 签发 JWT
        String token = tokenProvider.generateToken(userId, username);
        String refreshToken = tokenProvider.generateRefreshToken(userId, username);

        // 记录登录日志
        loginLogService.record(username, ip, userAgent, 0,
                "OAuth2 登录成功 (" + providerName + ")");

        // 注册在线用户
        onlineUserService.login(token, userId, username, ip, userAgent);

        return new LoginResponse(token, refreshToken, userId, username, nickname);
    }

    /**
     * 生成唯一用户名：优先用三方平台用户名，冲突则追加随机后缀
     */
    private String generateUniqueUsername(OAuth2UserInfo userInfo) {
        String base = userInfo.username();
        if (base == null || base.isBlank()) {
            base = userInfo.provider() + "_" + userInfo.providerId();
        }

        // 检查用户名是否已存在
        if (!existsByUsername(base)) {
            return base;
        }

        // 追加随机后缀
        String candidate;
        do {
            candidate = base + "_" + UUID.randomUUID().toString().substring(0, 6);
        } while (existsByUsername(candidate));

        return candidate;
    }

    /**
     * 检查用户名是否已存在
     */
    private boolean existsByUsername(String username) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(table("sys_user"))
                        .where(field("username").eq(username))
                        .and(field("deleted").eq(Constants.NOT_DELETED))
        );
    }

    /** 默认角色 ID：普通用户（role_key = 'user'） */
    private static final long DEFAULT_ROLE_ID = 2L;

    /**
     * 创建本地用户（OAuth2 自动注册，密码为随机值不可登录）+ 分配默认角色
     */
    private Long createLocalUser(String username, String nickname, String email, String avatar) {
        Long userId = idGenerator.nextId();
        LocalDateTime now = LocalDateTime.now();
        // 生成随机密码（OAuth2 用户不需要密码登录）
        String randomPassword = passwordEncoder.encode(UUID.randomUUID().toString());

        dsl.insertInto(table("sys_user"))
                .set(field("id"), userId)
                .set(field("username"), username)
                .set(field("password"), randomPassword)
                .set(field("nickname"), nickname)
                .set(field("email"), email)
                .set(field("avatar"), avatar)
                .set(field("enabled"), Constants.ENABLED)
                .set(field("create_by"), "oauth2")
                .set(field("create_time"), now)
                .set(field("update_by"), "oauth2")
                .set(field("update_time"), now)
                .set(field("deleted"), Constants.NOT_DELETED)
                .execute();

        // 分配默认角色（普通用户）
        dsl.insertInto(table("sys_user_role"))
                .set(field("user_id"), userId)
                .set(field("role_id"), DEFAULT_ROLE_ID)
                .execute();

        return userId;
    }
}
