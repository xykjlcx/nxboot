package com.nxboot.framework.oauth2;

/**
 * OAuth2 社会化登录提供者抽象。
 * 每个三方平台实现此接口，类似 better-auth 的 provider 模式。
 */
public interface OAuth2Provider {

    /**
     * 提供者标识（github/google/wechat 等）
     */
    String name();

    /**
     * 生成授权 URL（用户点击后跳转到三方平台）
     *
     * @param redirectUri 回调地址
     * @param state       防 CSRF 随机串
     */
    String buildAuthorizationUrl(String redirectUri, String state);

    /**
     * 用授权码换取用户信息
     *
     * @param code        三方平台返回的授权码
     * @param redirectUri 回调地址（部分平台校验需要）
     */
    OAuth2UserInfo exchangeForUserInfo(String code, String redirectUri);
}
