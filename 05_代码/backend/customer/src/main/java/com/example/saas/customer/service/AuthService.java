package com.example.saas.customer.service;

import com.example.saas.customer.config.JwtUtil;
import com.example.saas.customer.entity.Tenant;
import com.example.saas.customer.entity.User;
import com.example.saas.customer.mapper.TenantMapper;
import com.example.saas.customer.mapper.UserMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final TenantMapper tenantMapper;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

    public AuthService(TenantMapper tenantMapper, UserMapper userMapper, JwtUtil jwtUtil) {
        this.tenantMapper = tenantMapper;
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public Map<String, Object> register(String companyName, String username, String password, String role) {
        Tenant t = new Tenant();
        t.setCompanyName(companyName);
        t.setStatus(1);
        tenantMapper.insert(t);

        User u = new User();
        u.setTenantId(t.getId());
        u.setUsername(username);
        u.setPasswordHash(encoder.encode(password));
        u.setRole(role);
        userMapper.insert(u);

        Map<String, Object> res = new HashMap<>();
        res.put("tenantId", t.getId());
        res.put("userId", u.getId());
        return res;
    }

    public Map<String, Object> login(String username, String password) {
        User u = userMapper.selectOne(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<User>lambdaQuery()
                        .eq(User::getUsername, username));
        if (u == null || !encoder.matches(password, u.getPasswordHash())) {
            throw new RuntimeException("40001:invalid credentials");
        }
        String token = jwtUtil.generate(u.getId(), u.getTenantId(), u.getRole());
        Map<String, Object> res = new HashMap<>();
        res.put("token", token);
        res.put("userId", u.getId());
        res.put("tenantId", u.getTenantId());
        res.put("role", u.getRole());
        return res;
    }
}
