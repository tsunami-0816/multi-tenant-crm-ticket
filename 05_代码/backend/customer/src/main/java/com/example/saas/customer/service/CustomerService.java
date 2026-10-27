package com.example.saas.customer.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.saas.customer.config.TenantContext;
import com.example.saas.customer.entity.Customer;
import com.example.saas.customer.mapper.CustomerMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class CustomerService {

    private final CustomerMapper customerMapper;
    private final TenantContext tenantContext;
    private final RedissonClient redisson;
    private final StringRedisTemplate redis;
    private final ObjectMapper om = new ObjectMapper().registerModule(new JavaTimeModule());
    private final Random random = new Random();

    public CustomerService(CustomerMapper customerMapper, TenantContext tenantContext,
                           RedissonClient redisson, StringRedisTemplate redis) {
        this.customerMapper = customerMapper;
        this.tenantContext = tenantContext;
        this.redisson = redisson;
        this.redis = redis;
    }

    public Customer getById(Long id) {
        Long tid = tenantContext.getTenantId();
        String key = "customer:" + tid + ":" + id;

        // 1. 先查缓存
        String cached = redis.opsForValue().get(key);
        if (cached != null) {
            return parse(cached);
        }

        // 2. 布隆过滤器判断 ID 是否存在，防穿透
        RBloomFilter<Long> bloom = redisson.getBloomFilter("customer:bloom");
        bloom.tryInit(100_000L, 0.01);
        if (!bloom.contains(id)) {
            return null;
        }

        // 3. 分布式锁防击穿
        RLock lock = redisson.getLock("lock:customer:" + id);
        try {
            lock.lock(10, TimeUnit.SECONDS);
            // double check
            cached = redis.opsForValue().get(key);
            if (cached != null) return parse(cached);

            Customer c = customerMapper.selectById(id);
            if (c == null) return null;

            // TTL 30min ± 5min，防雪崩
            long ttl = 30 * 60 + random.nextInt(300);
            redis.opsForValue().set(key, serialize(c), ttl, TimeUnit.SECONDS);
            bloom.add(id);
            return c;
        } finally {
            lock.unlock();
        }
    }

    public Customer create(Customer c) {
        c.setTenantId(tenantContext.getTenantId());
        c.setCreatedBy(tenantContext.getUserId());
        customerMapper.insert(c);
        // 新 ID 加入布隆过滤器，防穿透
        RBloomFilter<Long> bloom = redisson.getBloomFilter("customer:bloom");
        bloom.tryInit(100_000L, 0.01);
        bloom.add(c.getId());
        return c;
    }

    public Customer update(Customer c) {
        Customer old = customerMapper.selectById(c.getId());
        if (old == null) throw new RuntimeException("40401:customer not found");
        customerMapper.updateById(c);
        // 延迟双删：先更新 DB，延迟 500ms 删缓存
        Long tid = tenantContext.getTenantId();
        String key = "customer:" + tid + ":" + c.getId();
        new Thread(() -> {
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            redis.delete(key);
        }).start();
        return c;
    }

    private String serialize(Customer c) {
        try { return om.writeValueAsString(c); } catch (Exception e) { throw new RuntimeException(e); }
    }

    private Customer parse(String s) {
        try { return om.readValue(s, Customer.class); } catch (Exception e) { throw new RuntimeException(e); }
    }
}
