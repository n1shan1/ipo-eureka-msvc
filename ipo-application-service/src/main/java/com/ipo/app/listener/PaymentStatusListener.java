package com.ipo.app.listener;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.ipo.events.MandateApprovedEvent;
import com.ipo.events.MandateFailedEvent;
import com.ipo.app.entity.IPOApplication;
import com.ipo.app.repository.ApplicationRepository;

@Component
public class PaymentStatusListener {

    @Autowired
    private ApplicationRepository applicationRepository;

    @JmsListener(destination = "mandate.approved.topic", containerFactory = "topicListenerFactory")
    public void handleMandateApproved(MandateApprovedEvent event) {
        // Find the application by its ID
        Optional<IPOApplication> applicationOpt = applicationRepository.findByApplicationId(event.getApplicationId());
        if (applicationOpt.isPresent()) {
            IPOApplication application = applicationOpt.get();
            application.setStatus("APPROVED"); // Update status to approved
            applicationRepository.save(application); // Save the change
        }
    }

    @JmsListener(destination = "mandate.failed.topic", containerFactory = "topicListenerFactory")
    public void handleMandateFailed(MandateFailedEvent event) {
        // Find the application by its ID
        Optional<IPOApplication> applicationOpt = applicationRepository.findByApplicationId(event.getApplicationId());
        if (applicationOpt.isPresent()) {
            IPOApplication application = applicationOpt.get();
            application.setStatus("REJECTED"); // Update status to rejected
            applicationRepository.save(application); // Save the change
        }
    }
}