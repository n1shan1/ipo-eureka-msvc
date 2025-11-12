package com.ipo.app.listener;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.ipo.events.AllotmentDoneEvent;

@Component
public class NotificationListener {
    @JmsListener(destination = "allotment.done.topic", containerFactory = "topicListenerFactory")
    public void handleAllotmentDone(AllotmentDoneEvent event) {
        event.getWinnerApplicationIds()
                .forEach(appId -> System.out.println("Mock: Sending 'CONGRATS' email to user " + appId + "."));

        event.getNonWinnerApplicationIds()
                .forEach(appId -> System.out.println("Mock: Sending 'SORRY' email to user " + appId + "."));
    }
}