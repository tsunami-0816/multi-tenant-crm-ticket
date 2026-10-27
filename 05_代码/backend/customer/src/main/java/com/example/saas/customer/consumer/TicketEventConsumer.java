package com.example.saas.customer.consumer;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.saas.customer.entity.CustomerTimeline;
import com.example.saas.customer.mapper.CustomerTimelineMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 消费 ticket.status.changed 事件，写入客户时间线。
 * 幂等：event_id 唯一索引，重复消息直接跳过。
 */
@Component
public class TicketEventConsumer {

    private final CustomerTimelineMapper timelineMapper;
    private final ObjectMapper om = new ObjectMapper();

    public TicketEventConsumer(CustomerTimelineMapper timelineMapper) {
        this.timelineMapper = timelineMapper;
    }

    @KafkaListener(topics = "ticket.status.changed", groupId = "customer-group")
    public void onMessage(String payload) {
        try {
            Map<String, Object> event = om.readValue(payload, Map.class);
            String eventId = (String) event.get("eventId");

            // 幂等：已存在则跳过
            Long exists = timelineMapper.selectCount(
                    Wrappers.<CustomerTimeline>lambdaQuery().eq(CustomerTimeline::getEventId, eventId));
            if (exists > 0) return;

            CustomerTimeline t = new CustomerTimeline();
            t.setEventId(eventId);
            t.setTenantId(Long.valueOf(event.get("tenantId").toString()));
            t.setCustomerId(Long.valueOf(event.get("customerId").toString()));
            t.setType((String) event.get("type"));
            t.setTitle((String) event.get("title"));
            t.setOccurredAt(java.time.LocalDateTime.now());
            timelineMapper.insert(t);
        } catch (Exception e) {
            // 失败 3 次由 Kafka 重试机制进 DLQ
            throw new RuntimeException(e);
        }
    }
}
