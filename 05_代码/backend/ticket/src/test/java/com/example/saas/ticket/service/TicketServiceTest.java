package com.example.saas.ticket.service;

import com.example.saas.ticket.entity.Ticket;
import com.example.saas.ticket.mapper.LocalMessageMapper;
import com.example.saas.ticket.mapper.TicketMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketMapper ticketMapper;

    @Mock
    private LocalMessageMapper localMessageMapper;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        // 初始化事务同步（单测环境无 Spring 事务）
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.initSynchronization();
        }
        // mock insert 时回填 id（lenient 因为 changeStatus 测试不走 insert）
        lenient().when(ticketMapper.insert(any(Ticket.class)))
                .thenAnswer(inv -> { ((Ticket) inv.getArgument(0)).setId(1L); return 1; });
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.clear();
    }

    @Test
    void create_shouldInitializeStatusOpen() {
        Ticket t = ticketService.create(1L, 1L, "登录报错", "用户无法登录", 1L);
        assertEquals("OPEN", t.getStatus());
        assertEquals(1L, t.getTenantId());
        verify(ticketMapper).insert(any(Ticket.class));
    }

    @Test
    void changeStatus_validTransition_shouldUpdate() {
        Ticket existing = new Ticket();
        existing.setId(1L);
        existing.setTenantId(1L);
        existing.setCustomerId(1L);
        existing.setStatus("OPEN");
        when(ticketMapper.selectById(1L)).thenReturn(existing);

        Ticket updated = ticketService.changeStatus(1L, "PROCESSING", 1L);
        assertEquals("PROCESSING", updated.getStatus());
        verify(ticketMapper).updateById(existing);
    }

    @Test
    void changeStatus_illegalTransition_shouldThrow() {
        Ticket existing = new Ticket();
        existing.setId(1L);
        existing.setTenantId(1L);
        existing.setCustomerId(1L);
        existing.setStatus("OPEN");
        when(ticketMapper.selectById(1L)).thenReturn(existing);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> ticketService.changeStatus(1L, "CLOSED", 1L));
        assertTrue(ex.getMessage().contains("40901"));
    }

    @Test
    void changeStatus_wrongTenant_shouldThrow404() {
        Ticket existing = new Ticket();
        existing.setId(1L);
        existing.setTenantId(1L);
        existing.setCustomerId(1L);
        existing.setStatus("OPEN");
        when(ticketMapper.selectById(1L)).thenReturn(existing);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> ticketService.changeStatus(1L, "PROCESSING", 2L));
        assertTrue(ex.getMessage().contains("40402"));
    }
}
