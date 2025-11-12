package com.ipo.app.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ipo.events.ApplicationCreatedEvent;
import com.ipo.app.dto.ApplicationRequest;
import com.ipo.app.entity.IPOApplication;
import com.ipo.app.repository.ApplicationRepository;

@RestController
@RequestMapping("/api/v1/ipo")
public class ApplicationController {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private JmsTemplate jmsTemplate;

    @PostMapping("/{ipoId}/apply")
    public ResponseEntity<IPOApplication> applyForIPO(@PathVariable String ipoId,
            @RequestBody ApplicationRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        // Check if this request was already processed (idempotency)
        Optional<IPOApplication> existing = applicationRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return ResponseEntity.ok(existing.get()); // Return existing application
        }

        // Prevent duplicate applications for the same user and IPO
        if (applicationRepository.existsByIpoIdAndInvestorId(ipoId, request.getInvestorId())) {
            return ResponseEntity.status(409).build(); // Conflict if duplicate
        }

        // Creating a new application record
        IPOApplication application = new IPOApplication();
        application.setApplicationId(UUID.randomUUID().toString());
        application.setIpoId(ipoId);
        application.setInvestorId(request.getInvestorId());
        application.setLots(request.getLots());
        application.setStatus("PENDING");
        application.setIdempotencyKey(idempotencyKey);

        IPOApplication saved = applicationRepository.save(application);

        // Sending an event to the payment svc
        ApplicationCreatedEvent event = new ApplicationCreatedEvent();
        event.setApplicationId(saved.getApplicationId());
        event.setInvestorId(saved.getInvestorId());
        event.setUserUpiId(request.getUserUpiId());
        event.setAmount(calculateAmount(saved.getLots()));

        jmsTemplate.convertAndSend("app.created.queue", event);

        return ResponseEntity.accepted().body(saved);
    }

    private double calculateAmount(int lots) {
        return lots * 100.0;
    }
}