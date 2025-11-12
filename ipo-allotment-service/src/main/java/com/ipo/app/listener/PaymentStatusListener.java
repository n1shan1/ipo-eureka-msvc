package com.ipo.app.listener;

import com.ipo.app.dto.ApplicationDTO;
import com.ipo.app.entity.EligibleApplicant;
import com.ipo.app.repository.ApplicantRepository;
import com.ipo.app.service.ApplicationServiceClient;
import com.ipo.events.MandateApprovedEvent;
import com.ipo.events.MandateFailedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class PaymentStatusListener {

    @Autowired
    private ApplicantRepository applicantRepository;

    @Autowired
    private ApplicationServiceClient applicationServiceClient;

    @JmsListener(destination = "mandate.approved.topic")
    public void handleMandateApproved(MandateApprovedEvent event) {
        // Get application details
        ApplicationDTO application = applicationServiceClient.getApplication(event.getApplicationId());
        if (application != null) {
            EligibleApplicant applicant = new EligibleApplicant();
            applicant.setId(UUID.randomUUID().toString());
            applicant.setApplicationId(event.getApplicationId());
            applicant.setInvestorId(application.getInvestorId());
            applicant.setLots(application.getLots());
            applicant.setStatus("ELIGIBLE");
            applicantRepository.save(applicant);
        }
    }

    @JmsListener(destination = "mandate.failed.topic")
    public void handleMandateFailed(MandateFailedEvent event) {
        // Update status to FAILED
        Optional<EligibleApplicant> optionalApplicant = applicantRepository
                .findByApplicationId(event.getApplicationId());
        if (optionalApplicant.isPresent()) {
            EligibleApplicant applicant = optionalApplicant.get();
            applicant.setStatus("FAILED");
            applicantRepository.save(applicant);
        }
    }
}