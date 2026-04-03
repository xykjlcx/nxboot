package com.nxboot.framework.web;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * XSS 防护过滤器——对请求参数进行 HTML 实体转义。
 * JSON 请求体不在此处理（由 Jackson 反序列化时处理）。
 */
@Component
@Order(1)
public class XssFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        chain.doFilter(new XssRequestWrapper((HttpServletRequest) request), response);
    }

    private static class XssRequestWrapper extends HttpServletRequestWrapper {
        XssRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getParameter(String name) {
            String value = super.getParameter(name);
            return value != null ? escapeHtml(value) : null;
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] values = super.getParameterValues(name);
            if (values == null) return null;
            String[] escaped = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                escaped[i] = escapeHtml(values[i]);
            }
            return escaped;
        }

        @Override
        public String getHeader(String name) {
            String value = super.getHeader(name);
            return value != null ? escapeHtml(value) : null;
        }

        /**
         * 基础 HTML 实体转义——防止脚本注入。
         */
        private static String escapeHtml(String input) {
            if (input == null || input.isEmpty()) return input;
            StringBuilder sb = new StringBuilder(input.length() + 16);
            for (int i = 0; i < input.length(); i++) {
                char c = input.charAt(i);
                switch (c) {
                    case '<' -> sb.append("&lt;");
                    case '>' -> sb.append("&gt;");
                    case '&' -> sb.append("&amp;");
                    case '"' -> sb.append("&quot;");
                    case '\'' -> sb.append("&#39;");
                    default -> sb.append(c);
                }
            }
            return sb.toString();
        }
    }
}
