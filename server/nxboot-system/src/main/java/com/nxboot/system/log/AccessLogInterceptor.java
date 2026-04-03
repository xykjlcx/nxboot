package com.nxboot.system.log;

import com.nxboot.common.annotation.Log;
import com.nxboot.framework.security.SecurityUtils;
import com.nxboot.system.log.service.LogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;

/**
 * 操作日志拦截器——自动记录标注了 @Log 注解的 Controller 方法调用。
 */
@Component
public class AccessLogInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AccessLogInterceptor.class);
    private static final String START_TIME_ATTR = "nx-log-start";

    private final LogService logService;

    public AccessLogInterceptor(LogService logService) {
        this.logService = logService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        if (!(handler instanceof HandlerMethod hm)) {
            return;
        }

        Log logAnno = hm.getMethodAnnotation(Log.class);
        if (logAnno == null) {
            return;
        }

        try {
            String module = logAnno.module();
            String operation = logAnno.operation();

            // 方法名：类名.方法名
            String method = hm.getBeanType().getSimpleName() + "." + hm.getMethod().getName();

            String requestUrl = request.getRequestURI();
            String requestMethod = request.getMethod();

            // 请求参数
            String requestParams = extractRequestParams(request);

            // 操作人
            String operator = SecurityUtils.getCurrentUsername();
            if (operator == null) {
                operator = "anonymous";
            }

            // 操作人 IP
            String operatorIp = getClientIp(request);

            // 耗时
            Long startTime = (Long) request.getAttribute(START_TIME_ATTR);
            long duration = startTime != null ? System.currentTimeMillis() - startTime : 0L;

            // 状态：0=成功，1=失败（GlobalExceptionHandler 会吞掉异常，所以也检查 HTTP 状态码）
            int status = (ex != null || response.getStatus() >= 400) ? 1 : 0;
            String errorMsg = ex != null ? ex.getMessage() : null;

            logService.save(module, operation, method, requestUrl, requestMethod,
                    requestParams, null, operator, operatorIp, status, errorMsg, duration);
        } catch (Exception e) {
            // 日志记录失败不能影响正常业务
            logger.error("操作日志记录失败", e);
        }
    }

    /**
     * 提取请求参数
     */
    private String extractRequestParams(HttpServletRequest request) {
        // GET 请求用 queryString
        if ("GET".equalsIgnoreCase(request.getMethod()) || "DELETE".equalsIgnoreCase(request.getMethod())) {
            return request.getQueryString();
        }

        // POST/PUT 尝试从 ContentCachingRequestWrapper 读取 body
        if (request instanceof ContentCachingRequestWrapper wrapper) {
            byte[] body = wrapper.getContentAsByteArray();
            if (body.length > 0) {
                // 截断过长的请求体，最多保存 2000 字符
                String bodyStr = new String(body, StandardCharsets.UTF_8);
                return bodyStr.length() > 2000 ? bodyStr.substring(0, 2000) : bodyStr;
            }
        }

        return request.getQueryString();
    }

    /**
     * 获取客户端真实 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            // 多级代理时取第一个
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}
