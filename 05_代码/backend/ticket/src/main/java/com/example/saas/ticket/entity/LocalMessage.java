package com.example.saas.ticket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("local_message")
public class LocalMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String topic;
    private String payload;
    private Integer status;
    private Integer retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
}
