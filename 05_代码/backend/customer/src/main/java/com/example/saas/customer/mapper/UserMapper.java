package com.example.saas.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.saas.customer.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {}
