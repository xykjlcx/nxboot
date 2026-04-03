package com.nxboot.framework.oauth2.providers;

import com.fasterxml.jackson.databind.JsonNode;
import com.nxboot.common.exception.BusinessException;
import com.nxboot.common.exception.ErrorCode;
import com.nxboot.framework.oauth2.OAuth2Config;
import com.nxboot.framework.oauth2.OAuth2Provider;
import com.nxboot.framework.oauth2.OAuth2UserInfo;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * GitHub OAuth2 Provider 实现。
 * <p>
 * 流程：
 * 1. 拼接 GitHub 授权 URL，用户跳转授权
 * 2. 拿 code 换 access_token（POST github.com/login/oauth/access_token）
 * 3. 拿 token 取用户信息（GET api.github.com/user）
 */
public class GitHubProvider implements OAuth2Provider {

    private final OAuth2Config.ProviderConfig config;
    private final RestTemplate restTemplate;

    public GitHubProvider(OAuth2Config.ProviderConfig config) {
        this.config = config;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String name() {
        return "github";
    }

    @Override
    public String buildAuthorizationUrl(String redirectUri, String state) {
        String authUrl = config.getAuthorizationUrl();
        if (authUrl == null || authUrl.isBlank()) {
            authUrl = "https://github.com/login/oauth/authorize";
        }
        return authUrl
                + "?client_id=" + encode(config.getClientId())
                + "&redirect_uri=" + encode(redirectUri)
                + "&scope=" + encode(config.getScope() != null ? config.getScope() : "read:user,user:email")
                + "&state=" + encode(state);
    }

    @Override
    public OAuth2UserInfo exchangeForUserInfo(String code, String redirectUri) {
        // 1. 用 code 换 access_token
        String accessToken = exchangeCodeForToken(code, redirectUri);

        // 2. 用 access_token 获取用户信息
        return fetchUserInfo(accessToken);
    }

    /**
     * 用授权码换取 access_token
     */
    private String exchangeCodeForToken(String code, String redirectUri) {
        String tokenUrl = config.getTokenUrl();
        if (tokenUrl == null || tokenUrl.isBlank()) {
            tokenUrl = "https://github.com/login/oauth/access_token";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", config.getClientId());
        params.add("client_secret", config.getClientSecret());
        params.add("code", code);
        params.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    tokenUrl, HttpMethod.POST, request, JsonNode.class);

            JsonNode body = response.getBody();
            if (body == null || body.has("error")) {
                String error = body != null ? body.get("error_description").asText() : "未知错误";
                throw new BusinessException(ErrorCode.BAD_REQUEST, "GitHub 授权失败: " + error);
            }

            return body.get("access_token").asText();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "GitHub Token 交换失败: " + e.getMessage());
        }
    }

    /**
     * 用 access_token 获取 GitHub 用户信息
     */
    private OAuth2UserInfo fetchUserInfo(String accessToken) {
        String userInfoUrl = config.getUserInfoUrl();
        if (userInfoUrl == null || userInfoUrl.isBlank()) {
            userInfoUrl = "https://api.github.com/user";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    userInfoUrl, HttpMethod.GET, request, JsonNode.class);

            JsonNode body = response.getBody();
            if (body == null) {
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "GitHub 用户信息为空");
            }

            String providerId = String.valueOf(body.get("id").asLong());
            String username = body.has("login") ? body.get("login").asText() : null;
            String email = body.has("email") && !body.get("email").isNull() ? body.get("email").asText() : null;
            String avatar = body.has("avatar_url") ? body.get("avatar_url").asText() : null;

            return new OAuth2UserInfo(providerId, "github", username, email, avatar);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "获取 GitHub 用户信息失败: " + e.getMessage());
        }
    }

    private static String encode(String value) {
        if (value == null) return "";
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
