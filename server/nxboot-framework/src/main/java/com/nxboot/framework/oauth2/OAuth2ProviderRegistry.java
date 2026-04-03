package com.nxboot.framework.oauth2;

import com.nxboot.common.exception.BusinessException;
import com.nxboot.common.exception.ErrorCode;
import com.nxboot.framework.oauth2.providers.GitHubProvider;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * OAuth2 Provider 注册中心。
 * 根据配置自动注册已启用的 Provider，新增平台只需在此添加。
 */
@Component
public class OAuth2ProviderRegistry {

    private final Map<String, OAuth2Provider> providers = new HashMap<>();

    public OAuth2ProviderRegistry(OAuth2Config config) {
        // 自动注册已配置的 Provider
        if (config.getProviders().containsKey("github")) {
            providers.put("github", new GitHubProvider(config.getProviders().get("github")));
        }
        // 新增平台只需在此添加，例如：
        // if (config.getProviders().containsKey("google")) {
        //     providers.put("google", new GoogleProvider(config.getProviders().get("google")));
        // }
    }

    /**
     * 获取指定 Provider
     *
     * @throws BusinessException 如果 provider 未配置
     */
    public OAuth2Provider getProvider(String name) {
        OAuth2Provider provider = providers.get(name);
        if (provider == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的登录方式: " + name);
        }
        return provider;
    }

    /**
     * 列出所有已启用的 Provider 名称
     */
    public List<String> listProviders() {
        return new ArrayList<>(providers.keySet());
    }
}
