package com.example.saas.customer.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 多租户拦截器：自动给 MP 自动生成的 SQL 拼 tenant_id 条件。
 * 注意：XML 手写 SQL 不会自动加，必须显式带 #{tenantId}。
 */
@Configuration
public class MyBatisPlusConfig {

    private final TenantContext tenantContext;

    public MyBatisPlusConfig(TenantContext tenantContext) {
        this.tenantContext = tenantContext;
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {
            @Override
            public net.sf.jsqlparser.expression.Expression getTenantId() {
                Long tid = tenantContext.getTenantId();
                return new LongValue(tid == null ? 0 : tid);
            }

            @Override
            public String getTenantIdColumn() {
                return "tenant_id";
            }

            @Override
            public boolean ignoreTable(String tableName) {
                // tenant/user 表在注册/登录阶段跨租户查；
                // customer_timeline 由 Kafka consumer 线程写入，无 HTTP 上下文，
                // insert 时已手动设 tenant_id，查询不拦截。
                return "tenant".equals(tableName)
                        || "user".equals(tableName)
                        || "customer_timeline".equals(tableName);
            }
        }));
        return interceptor;
    }
}
