package com.ipo.app.service;

import com.ipo.app.entity.EligibleApplicant;
import com.ipo.app.repository.ApplicantRepository;
import com.ipo.events.AllotmentDoneEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class LotteryService {

    @Autowired
    private ApplicantRepository applicantRepository;

    @Autowired
    private LotteryEngine lotteryEngine;

    @Autowired
    private JmsTemplate jmsTemplate;

    public void performAllotment(String ipoId, int totalShares) {
        // Get all eligible applicants for the IPO
        List<EligibleApplicant> applicants = applicantRepository.findAll(); // In real, filter by ipoId if needed

        // Perform lottery
        Set<EligibleApplicant> winners = lotteryEngine.performLottery(applicants, totalShares, ipoId);

        // Update winners status
        for (EligibleApplicant winner : winners) {
            winner.setStatus("ALLOTTED");
            applicantRepository.save(winner);
        }

        // Publish AllotmentDoneEvent
        AllotmentDoneEvent event = new AllotmentDoneEvent();
        event.setIpoId(ipoId);
        event.setWinnerApplicationIds(
                winners.stream().map(EligibleApplicant::getApplicationId).collect(java.util.stream.Collectors.toSet()));
        event.setNonWinnerApplicationIds(applicants.stream().filter(a -> !winners.contains(a))
                .map(EligibleApplicant::getApplicationId).collect(java.util.stream.Collectors.toSet()));
        jmsTemplate.convertAndSend("allotment.done.topic", event);
    }
}