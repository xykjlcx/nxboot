package com.nxboot.system.auth.service;

import com.nxboot.common.exception.BusinessException;
import com.nxboot.common.exception.ErrorCode;
import com.nxboot.framework.security.JwtTokenProvider;
import com.nxboot.framework.security.LoginUser;
import com.nxboot.system.auth.model.LoginRequest;
import com.nxboot.system.auth.model.LoginResponse;
import org.jooq.DSLContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

/**
 * 认证服务
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final DSLContext dsl;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider,
                       DSLContext dsl) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.dsl = dsl;
    }

    /**
     * 登录认证
     */
    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));

            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            String token = tokenProvider.generateToken(loginUser.getUserId(), loginUser.getUsername());

            // 查询昵称
            String nickname = dsl.select(field("nickname"))
                    .from(table("sys_user"))
                    .where(field("id").eq(loginUser.getUserId()))
                    .fetchOneInto(String.class);

            return new LoginResponse(token, loginUser.getUserId(), loginUser.getUsername(), nickname);
        } catch (BadCredentialsException e) {
            throw new BusinessException(ErrorCode.AUTH_BAD_CREDENTIALS);
        } catch (DisabledException e) {
            throw new BusinessException(ErrorCode.AUTH_ACCOUNT_DISABLED);
        }
    }
}
