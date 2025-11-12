package com.ipo.app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.stereotype.Component;

@Component
public class JmsListenerStarter implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private JmsListenerEndpointRegistry jmsListenerEndpointRegistry;

    @Override
    public void onApplicationEvent(@SuppressWarnings("null") ContextRefreshedEvent event) {
        // Start JMS listeners after application context is fully initialized
        // Add a delay to ensure ActiveMQ is ready
        new Thread(() -> {
            try {
                Thread.sleep(30000); // Wait 30 seconds for ActiveMQ to be ready
                jmsListenerEndpointRegistry.start();
                System.out.println("JMS listeners started successfully");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}