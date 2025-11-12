package com.ipo.app.controller;

import com.ipo.app.service.LotteryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/allotment")
public class LotteryController {

    @Autowired
    private LotteryService lotteryService;

    @PostMapping("/allot")
    public ResponseEntity<String> performAllotment(@RequestParam String ipoId, @RequestParam int totalShares) {
        lotteryService.performAllotment(ipoId, totalShares);
        return ResponseEntity.ok("Allotment completed for IPO: " + ipoId);
    }
}