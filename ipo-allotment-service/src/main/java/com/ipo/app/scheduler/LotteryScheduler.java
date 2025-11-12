package com.ipo.app.scheduler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.ipo.app.entity.EligibleApplicant;
import com.ipo.app.entity.Allotment;
import com.ipo.app.repository.AllotmentRepository;
import com.ipo.app.service.LotteryEngine;
import com.ipo.events.AllotmentDoneEvent;

@Component
public class LotteryScheduler {

    @Autowired
    private LotteryEngine lotteryEngine;

    @Autowired
    private AllotmentRepository allotmentRepository;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Scheduled(cron = "0 0 0 * * ?")
    public void runAllotmentProcess() {
        System.out.println("Starting allotment process...");

        String ipoId = "testipo";

        List<String> approvedApplicationIds = restTemplate.exchange(
                "http://localhost:8081/api/v1/ipo/" + ipoId + "/applications",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<String>>() {
                }).getBody();

        // For demo, create mock eligible applicants
        List<EligibleApplicant> applicants = approvedApplicationIds.stream()
                .map(id -> {
                    EligibleApplicant applicant = new EligibleApplicant();
                    applicant.setId(id);
                    applicant.setApplicationId(id);
                    applicant.setLots(5); // Mock lots
                    return applicant;
                })
                .collect(Collectors.toList());

        Set<EligibleApplicant> winners = lotteryEngine.performLottery(applicants, 100, "seed");

        Set<String> winnerIds = winners.stream()
                .map(EligibleApplicant::getApplicationId)
                .collect(Collectors.toSet());

        Set<String> nonWinners = new HashSet<>(approvedApplicationIds);
        nonWinners.removeAll(winnerIds);

        Allotment allotment = new Allotment();
        allotment.setIpoId(ipoId);
        allotment.setWinnerApplicationIds(winnerIds);
        allotment.setNonWinnerApplicationIds(nonWinners);
        allotmentRepository.save(allotment);

        AllotmentDoneEvent event = new AllotmentDoneEvent();
        event.setIpoId(ipoId);
        event.setWinnerApplicationIds(winnerIds);
        event.setNonWinnerApplicationIds(nonWinners);
        jmsTemplate.convertAndSend("allotment.done.topic", event);
    }
}