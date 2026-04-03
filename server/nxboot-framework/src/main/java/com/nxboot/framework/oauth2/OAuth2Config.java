package com.nxboot.framework.oauth2;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * OAuth2 配置，读取 nxboot.oauth2.providers 下的平台配置。
 * <p>
 * 示例：
 * <pre>
 * nxboot:
 *   oauth2:
 *     providers:
 *       github:
 *         client-id: xxx
 *         client-secret: xxx
 * </pre>
 */
@Component
@ConfigurationProperties(prefix = "nxboot.oauth2")
public class OAuth2Config {

    /** key = provider name (github/google), value = provider config */
    private Map<String, ProviderConfig> providers = Map.of();

    public Map<String, ProviderConfig> getProviders() {
        return providers;
    }

    public void setProviders(Map<String, ProviderConfig> providers) {
        this.providers = providers;
    }

    public static class ProviderConfig {
        private String clientId;
        private String clientSecret;
        private String authorizationUrl;
        private String tokenUrl;
        private String userInfoUrl;
        private String scope;

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public String getAuthorizationUrl() {
            return authorizationUrl;
        }

        public void setAuthorizationUrl(String authorizationUrl) {
            this.authorizationUrl = authorizationUrl;
        }

        public String getTokenUrl() {
            return tokenUrl;
        }

        public void setTokenUrl(String tokenUrl) {
            this.tokenUrl = tokenUrl;
        }

        public String getUserInfoUrl() {
            return userInfoUrl;
        }

        public void setUserInfoUrl(String userInfoUrl) {
            this.userInfoUrl = userInfoUrl;
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }
    }
}
