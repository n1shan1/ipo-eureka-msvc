package com.ipo.app.service;

import com.ipo.app.dto.ApplicationDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ApplicationServiceClient {

    @Autowired
    private RestTemplate restTemplate;

    @CircuitBreaker(name = "applicationService", fallbackMethod = "getApplicationFallback")
    public ApplicationDTO getApplication(String applicationId) {
        String url = "http://ipo-application-service/applications/" + applicationId;
        return restTemplate.getForObject(url, ApplicationDTO.class);
    }

    public ApplicationDTO getApplicationFallback(String applicationId, Throwable t) {
        // Return default or cached data
        return new ApplicationDTO("default-investor", 1);
    }
}