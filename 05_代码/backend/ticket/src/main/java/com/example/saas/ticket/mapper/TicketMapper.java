package com.example.saas.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.saas.ticket.entity.Ticket;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TicketMapper extends BaseMapper<Ticket> {}
