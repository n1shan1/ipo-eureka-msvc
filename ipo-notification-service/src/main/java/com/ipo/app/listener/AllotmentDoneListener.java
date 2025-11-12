package com.ipo.app.listener;

import com.ipo.events.AllotmentDoneEvent;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class AllotmentDoneListener {

    @JmsListener(destination = "allotment.done.topic")
    public void handleAllotmentDone(AllotmentDoneEvent event) {
        // Send notifications to allotted investors
        for (String applicationId : event.getWinnerApplicationIds()) {
            // Mock: Send email/SMS notification
            System.out.println("Sending notification to investor for application: " + applicationId
                    + " - Allotment successful for IPO: " + event.getIpoId());
            // In real implementation, integrate with email/SMS service
        }
    }
}