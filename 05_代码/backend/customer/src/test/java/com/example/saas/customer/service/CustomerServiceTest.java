package com.example.saas.customer.service;

import com.example.saas.customer.config.TenantContext;
import com.example.saas.customer.entity.Customer;
import com.example.saas.customer.mapper.CustomerMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private TenantContext tenantContext;

    @Mock
    private RedissonClient redisson;

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private RBloomFilter<Long> bloomFilter;

    @InjectMocks
    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        lenient().when(tenantContext.getTenantId()).thenReturn(1L);
        lenient().when(tenantContext.getUserId()).thenReturn(1L);
        doReturn(bloomFilter).when(redisson).getBloomFilter(anyString());
    }

    @Test
    void create_shouldSetTenantAndInsert() {
        Customer c = new Customer();
        c.setName("测试客户");
        c.setContact("张三");
        c.setPhone("13800000000");

        customerService.create(c);

        assertEquals(1L, c.getTenantId());
        assertEquals(1L, c.getCreatedBy());
        verify(customerMapper).insert(c);
        verify(bloomFilter).tryInit(anyLong(), anyDouble());
    }

    @Test
    void create_bloomFilterAddsNewId() {
        Customer c = new Customer();
        c.setId(99L);
        customerService.create(c);
        verify(bloomFilter).add(99L);
    }
}
