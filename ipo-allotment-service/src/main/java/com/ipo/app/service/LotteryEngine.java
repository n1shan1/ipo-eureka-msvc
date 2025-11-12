package com.ipo.app.service;

import com.ipo.app.entity.EligibleApplicant;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class LotteryEngine {

    public Set<EligibleApplicant> performLottery(List<EligibleApplicant> applicants, int totalShares, String seed) {
        // Create list of entries based on lots
        List<String> entries = new ArrayList<>();
        for (EligibleApplicant applicant : applicants) {
            for (int i = 0; i < applicant.getLots(); i++) {
                entries.add(applicant.getId());
            }
        }

        // If fewer entries than shares, all win
        if (entries.size() <= totalShares) {
            return new HashSet<>(applicants);
        }

        // Calculate hash scores for each entry
        List<EntryScore> scores = new ArrayList<>();
        for (String entry : entries) {
            String hash = sha256(seed + ":" + entry);
            scores.add(new EntryScore(entry, hash));
        }

        // Sort by hash
        scores.sort(Comparator.comparing(EntryScore::getHash));

        // Select winners
        Set<EligibleApplicant> winners = new HashSet<>();
        for (int i = 0; i < totalShares && i < scores.size(); i++) {
            String winnerId = scores.get(i).getEntry();
            EligibleApplicant winner = applicants.stream()
                    .filter(a -> a.getId().equals(winnerId))
                    .findFirst().orElse(null);
            if (winner != null) {
                winners.add(winner);
            }
        }

        return winners;
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static class EntryScore {
        private String entry;
        private String hash;

        public EntryScore(String entry, String hash) {
            this.entry = entry;
            this.hash = hash;
        }

        public String getEntry() {
            return entry;
        }

        public String getHash() {
            return hash;
        }
    }
}