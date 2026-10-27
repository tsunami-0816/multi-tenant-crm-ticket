package com.example.saas.customer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("customer_timeline")
public class CustomerTimeline {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long customerId;
    private String eventId;
    private String type;
    private String title;
    private LocalDateTime occurredAt;
}
