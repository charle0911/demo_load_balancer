package com.example.register;

import com.example.repository.EndpointsRepository;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class EndpointRegister {
    private final EndpointsRepository endpointsRepository;
    private final EndpointHealthMonitor endpointHealthMonitor;

    public EndpointRegister(EndpointsRepository endpointsRepository, EndpointHealthMonitor endpointHealthMonitor) {
        this.endpointsRepository = endpointsRepository;
        this.endpointHealthMonitor = endpointHealthMonitor;
        endpointHealthMonitor.start();
        log.info("Endpoint Health Monitor started");
    }

    public boolean register(String endpoint) {
        boolean result = endpointsRepository.register(endpoint);
        boolean healthCheck = true;
        if (result) {
            healthCheck = endpointHealthMonitor.healthCheck(endpoint);
            if (!healthCheck) {
                log.info("Endpoint Health Monitor health check failed when registering endpoint {}", endpoint);
                endpointsRepository.remove(endpoint);
            }
        }
        return result && healthCheck;
    }
}
