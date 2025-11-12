package com.ipo.app.listener;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.ipo.app.entity.Mandate;
import com.ipo.app.repository.MandateRepository;
import com.ipo.events.ApplicationCreatedEvent;
import com.ipo.events.MandateApprovedEvent;
import com.ipo.events.MandateFailedEvent;

@Component
public class ApplicationCreatedListener {

    @Autowired
    private MandateRepository mandateRepository;

    @Autowired
    private JmsTemplate jmsTemplate;

    @JmsListener(destination = "app.created.queue")
    public void handleApplicationCreated(ApplicationCreatedEvent event) {
        // Create mandate with PENDING status
        Mandate mandate = new Mandate();
        mandate.setId(UUID.randomUUID().toString());
        mandate.setApplicationId(event.getApplicationId());
        mandate.setAmount(BigDecimal.valueOf(event.getAmount()));
        mandate.setStatus("PENDING");
        mandate.setBankReferenceId("BANK_REF_" + event.getApplicationId());

        mandateRepository.save(mandate);

        // Simulate approval/failure logic (random for demo)
        boolean approved = Math.random() > 0.5;

        if (approved) {
            MandateApprovedEvent approvedEvent = new MandateApprovedEvent();
            approvedEvent.setApplicationId(event.getApplicationId());
            approvedEvent.setMandateId(mandate.getId());
            jmsTemplate.convertAndSend("mandate.approved.topic", approvedEvent);
        } else {
            MandateFailedEvent failedEvent = new MandateFailedEvent();
            failedEvent.setApplicationId(event.getApplicationId());
            failedEvent.setMandateId(mandate.getId());
            failedEvent.setReason("Insufficient funds");
            jmsTemplate.convertAndSend("mandate.failed.topic", failedEvent);
        }
    }
}