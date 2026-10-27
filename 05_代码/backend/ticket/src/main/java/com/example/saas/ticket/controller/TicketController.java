package com.example.saas.ticket.controller;

import com.example.saas.ticket.entity.Ticket;
import com.example.saas.ticket.service.TicketService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public Map<String, Object> list(@RequestHeader("X-Tenant-Id") Long tenantId,
                                     @RequestParam(required = false) Long customerId) {
        List<Ticket> list = ticketService.list(tenantId, customerId);
        return Map.of("code", 0, "data", list);
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, Object> body,
                                      @RequestHeader("X-Tenant-Id") Long tenantId,
                                      @RequestHeader("X-User-Id") Long userId) {
        return Map.of("code", 0, "data", ticketService.create(
                tenantId,
                Long.valueOf(body.get("customerId").toString()),
                (String) body.get("title"),
                (String) body.get("description"),
                userId));
    }

    @PatchMapping("/{id}")
    public Map<String, Object> changeStatus(@PathVariable Long id,
                                            @RequestBody Map<String, String> body,
                                            @RequestHeader("X-Tenant-Id") Long tenantId) {
        try {
            return Map.of("code", 0, "data", ticketService.changeStatus(id, body.get("status"), tenantId));
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            int code = msg.startsWith("404") ? 40402 : 40901;
            return Map.of("code", code, "message", msg);
        }
    }
}
