package com.example.saas.ticket.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.saas.ticket.entity.LocalMessage;
import com.example.saas.ticket.entity.Ticket;
import com.example.saas.ticket.mapper.LocalMessageMapper;
import com.example.saas.ticket.mapper.TicketMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 工单服务：状态机 + Kafka 事件发送。
 * 关键设计：业务方法加 @Transactional，事务提交后 afterCommit 回调立即发 Kafka，
 * 失败才落 local_message 由定时任务补发。
 */
@Service
public class TicketService {

    private final TicketMapper ticketMapper;
    private final LocalMessageMapper localMessageMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper om = new ObjectMapper();

    /** 合法状态流转 */
    private static final Map<String, Set<String>> TRANSITIONS = Map.of(
            "OPEN", Set.of("PROCESSING"),
            "PROCESSING", Set.of("RESOLVED"),
            "RESOLVED", Set.of("CLOSED", "PROCESSING"),
            "CLOSED", Set.of()
    );

    public TicketService(TicketMapper ticketMapper, LocalMessageMapper localMessageMapper,
                         KafkaTemplate<String, String> kafkaTemplate) {
        this.ticketMapper = ticketMapper;
        this.localMessageMapper = localMessageMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public Ticket create(Long tenantId, Long customerId, String title, String description, Long userId) {
        Ticket t = new Ticket();
        t.setTenantId(tenantId);
        t.setCustomerId(customerId);
        t.setTitle(title);
        t.setDescription(description);
        t.setStatus("OPEN");
        ticketMapper.insert(t);
        publishEvent(tenantId, customerId, "TICKET_CREATED", title, t.getId());
        return t;
    }

    public List<Ticket> list(Long tenantId, Long customerId) {
        var q = Wrappers.<Ticket>lambdaQuery()
                .eq(Ticket::getTenantId, tenantId)
                .orderByDesc(Ticket::getId);
        if (customerId != null) q.eq(Ticket::getCustomerId, customerId);
        return ticketMapper.selectList(q);
    }

    @Transactional
    public Ticket changeStatus(Long id, String newStatus, Long tenantId) {
        Ticket t = ticketMapper.selectById(id);
        if (t == null || !t.getTenantId().equals(tenantId)) {
            throw new RuntimeException("40402:ticket not found");
        }
        // 状态机校验
        Set<String> allowed = TRANSITIONS.getOrDefault(t.getStatus(), Set.of());
        if (!allowed.contains(newStatus)) {
            throw new RuntimeException("40901:invalid transition " + t.getStatus() + " -> " + newStatus);
        }
        t.setStatus(newStatus);
        ticketMapper.updateById(t);
        publishEvent(tenantId, t.getCustomerId(), "TICKET_STATUS_CHANGED", t.getTitle() + " -> " + newStatus, t.getId());
        return t;
    }

    /**
     * 事务提交后立即发 Kafka；失败落本地消息表兜底。
     */
    private void publishEvent(Long tenantId, Long customerId, String type, String title, Long ticketId) {
        String eventId = "evt-" + ticketId + "-" + System.currentTimeMillis();
        Map<String, Object> event = Map.of(
                "eventId", eventId,
                "tenantId", tenantId,
                "customerId", customerId,
                "ticketId", ticketId,
                "type", type,
                "title", title);
        String payload;
        try { payload = om.writeValueAsString(event); }
        catch (Exception e) { throw new RuntimeException(e); }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    kafkaTemplate.send("ticket.status.changed", payload).get();
                } catch (Exception e) {
                    // 发送失败，落本地消息表
                    LocalMessage lm = new LocalMessage();
                    lm.setTopic("ticket.status.changed");
                    lm.setPayload(payload);
                    lm.setStatus(0);
                    lm.setRetryCount(0);
                    localMessageMapper.insert(lm);
                }
            }
        });
    }
}
