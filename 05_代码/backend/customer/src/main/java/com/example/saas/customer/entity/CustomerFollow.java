package com.example.saas.customer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("customer_follow")
public class CustomerFollow {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long customerId;
    private String content;
    private Long followBy;
    private LocalDateTime createdAt;
}
