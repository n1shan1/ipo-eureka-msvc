package com.ipo.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ipo.app.scheduler.LotteryScheduler;

@RestController
@RequestMapping("/api/v1/allotment")
public class AllotmentController {

    @Autowired
    private LotteryScheduler lotteryScheduler;

    @PostMapping("/trigger")
    public ResponseEntity<String> triggerAllotment() {
        try {
            lotteryScheduler.runAllotmentProcess();
            return ResponseEntity.ok("Allotment process triggered successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}