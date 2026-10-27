package com.example.saas.customer.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.saas.customer.entity.Customer;
import com.example.saas.customer.mapper.CustomerMapper;
import com.example.saas.customer.service.CustomerService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerMapper customerMapper;

    public CustomerController(CustomerService customerService, CustomerMapper customerMapper) {
        this.customerService = customerService;
        this.customerMapper = customerMapper;
    }

    @GetMapping
    public Map<String, Object> list(@RequestParam(defaultValue = "1") long page,
                                    @RequestParam(defaultValue = "20") long size) {
        IPage<Customer> p = customerMapper.selectPage(new Page<>(page, size), null);
        return Map.of("code", 0, "data", Map.of("records", p.getRecords(), "total", p.getTotal()));
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Customer c) {
        return Map.of("code", 0, "data", customerService.create(c));
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        Customer c = customerService.getById(id);
        if (c == null) return Map.of("code", 40401, "message", "customer not found");
        return Map.of("code", 0, "data", c);
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Customer c) {
        c.setId(id);
        return Map.of("code", 0, "data", customerService.update(c));
    }
}
