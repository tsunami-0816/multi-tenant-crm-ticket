package com.example.saas.customer.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 从网关透传的 Header 里读当前请求的租户/用户上下文。
 * 网关已经做了 JWT 校验，下游直接信任 X-Tenant-Id / X-User-Id / X-User-Role。
 */
@Component
public class TenantContext {

    public Long getTenantId() {
        HttpServletRequest req = currentRequest();
        String v = req.getHeader("X-Tenant-Id");
        return v == null ? null : Long.parseLong(v);
    }

    public Long getUserId() {
        HttpServletRequest req = currentRequest();
        String v = req.getHeader("X-User-Id");
        return v == null ? null : Long.parseLong(v);
    }

    public String getRole() {
        return currentRequest().getHeader("X-User-Role");
    }

    private HttpServletRequest currentRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }
}
