package com.example.loadbalance;

import com.example.repository.EndpointsRepository;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinStrategy implements LoadBalanceStrategy {
    private final AtomicInteger counter = new AtomicInteger(0);
    EndpointsRepository endpointsRepository;

    public RoundRobinStrategy(EndpointsRepository endpointsRepository) {
        this.endpointsRepository = endpointsRepository;
    }

    @Override
    public String selectEndpoint() {
        int currentCount = counter.incrementAndGet();

        if (currentCount >= Integer.MAX_VALUE - 10) {
            counter.compareAndSet(currentCount, 0);
        }

        List<String> endpoints = endpointsRepository.getEndpoints();
        if (endpoints.isEmpty()) {
            throw new IllegalStateException("No endpoints available");
        }

        int target = currentCount % endpoints.size();
        return endpoints.get(target);
    }

    // Visible for testing
    protected AtomicInteger getCounter() {
        return counter;
    }
}
