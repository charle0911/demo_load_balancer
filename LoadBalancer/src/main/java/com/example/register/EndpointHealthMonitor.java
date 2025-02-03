package com.example.register;

import com.example.common.TaskScheduler;
import com.example.http.HttpResponse;
import com.example.http.RequestDispatcher;
import com.example.repository.EndpointsRepository;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Log4j2
public class EndpointHealthMonitor {
    private final EndpointsRepository endpointsRepository;
    private final TaskScheduler taskScheduler;
    private final RequestDispatcher requestDispatcher;
    private final long HEALTH_RESPONSE_RESTRICTION_TIME_MILLIS = 200;
    private final int CHECK_PROBATION_INTERVAL_SECONDS = 5;
    private final int CHECK_INTERVAL_SECONDS = 1;

    public EndpointHealthMonitor(EndpointsRepository endpointsRepository, TaskScheduler taskScheduler, RequestDispatcher requestDispatcher) {
        this.endpointsRepository = endpointsRepository;
        this.taskScheduler = taskScheduler;
        this.requestDispatcher = requestDispatcher;
    }

    public void start() {
        checkAllEndpoints();
    }

    public void checkAllEndpoints() {
        List<String> endpoints = endpointsRepository.getEndpoints();
        log.info("Initialize health check. Size of endpoints: {}", endpoints.size());
        for (String endpoint : endpoints) {
            taskScheduler.scheduleTask(() -> healthCheck(endpoint), CHECK_INTERVAL_SECONDS, TimeUnit.SECONDS);
        }
    }

    public boolean healthCheck(String endpoint) {
        // normal
        long start = System.currentTimeMillis();
        boolean isHealthy = checkEndpoint(endpoint);
        log.debug("Endpoint {} is healthy: {}", endpoint, isHealthy);
        if (!isHealthy) {
            endpointsRepository.remove(endpoint);
            return false;
        }
        long duration = System.currentTimeMillis() - start;
        // downgrade
        if (duration >= HEALTH_RESPONSE_RESTRICTION_TIME_MILLIS) {
            log.info("Endpoint {} is over the health timeout restriction. Send to probation. cost {}", endpoint, duration);
            endpointsRepository.downgrade(endpoint);
            taskScheduler.scheduleTask(() -> probationHealthCheck(endpoint), CHECK_PROBATION_INTERVAL_SECONDS, TimeUnit.SECONDS);
        } else {
            taskScheduler.scheduleTask(() -> healthCheck(endpoint), CHECK_INTERVAL_SECONDS, TimeUnit.SECONDS);
        }
        return true;
    }

    private void probationHealthCheck(String endpoint) {
        // normal
        long start = System.currentTimeMillis();
        boolean isHealthy = checkEndpoint(endpoint);
        if (!isHealthy) {
            endpointsRepository.remove(endpoint);
            return;
        }
        long duration = System.currentTimeMillis() - start;
        // downgrade
        if (duration < HEALTH_RESPONSE_RESTRICTION_TIME_MILLIS) {
            log.info("Endpoint {} is back to normal", endpoint);
            endpointsRepository.upgrade(endpoint);
            taskScheduler.scheduleTask(() -> healthCheck(endpoint), CHECK_INTERVAL_SECONDS, TimeUnit.SECONDS);
        } else {
            taskScheduler.scheduleTask(() -> probationHealthCheck(endpoint), CHECK_PROBATION_INTERVAL_SECONDS, TimeUnit.SECONDS);
        }
    }

    private boolean checkEndpoint(String endpoint) {
        try {
            if (!endpoint.endsWith("/")) {
                endpoint += "/";
            }
            String healthCheckEndpoint = endpoint + "health";
            HttpResponse httpResponse = requestDispatcher.forwardRequest(healthCheckEndpoint);
            log.info("Endpoint {}, Response Code {}, ", healthCheckEndpoint, httpResponse);
            return httpResponse.statusCode() == 200;
        } catch (Exception e) {
            log.error("Health check fail", e);
            return false;
        }
    }
}