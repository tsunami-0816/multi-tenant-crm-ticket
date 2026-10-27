package com.example.saas.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.saas.customer.entity.Customer;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {}
