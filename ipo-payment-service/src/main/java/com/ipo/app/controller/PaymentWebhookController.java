package com.ipo.app.controller;

import com.ipo.app.entity.Mandate;
import com.ipo.app.repository.MandateRepository;
import com.ipo.events.MandateApprovedEvent;
import com.ipo.events.MandateFailedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/webhook")
public class PaymentWebhookController {

    @Autowired
    private MandateRepository mandateRepository;

    @Autowired
    private JmsTemplate jmsTemplate;

    @PostMapping
    public ResponseEntity<String> handleWebhook(@RequestBody Map<String, Object> payload) {
        String mandateId = (String) payload.get("mandateId");
        String status = (String) payload.get("status"); // APPROVED or FAILED

        Optional<Mandate> optionalMandate = mandateRepository.findById(mandateId);
        if (optionalMandate.isEmpty()) {
            return ResponseEntity.badRequest().body("Mandate not found");
        }

        Mandate mandate = optionalMandate.get();
        mandate.setStatus(status);
        mandateRepository.save(mandate);

        if ("APPROVED".equals(status)) {
            MandateApprovedEvent approvedEvent = new MandateApprovedEvent();
            approvedEvent.setApplicationId(mandate.getApplicationId());
            approvedEvent.setMandateId(mandateId);
            jmsTemplate.convertAndSend("mandate.approved.topic", approvedEvent);
        } else if ("FAILED".equals(status)) {
            MandateFailedEvent failedEvent = new MandateFailedEvent();
            failedEvent.setApplicationId(mandate.getApplicationId());
            failedEvent.setMandateId(mandateId);
            failedEvent.setReason("Bank rejected");
            jmsTemplate.convertAndSend("mandate.failed.topic", failedEvent);
        }

        return ResponseEntity.ok("Webhook processed");
    }
}