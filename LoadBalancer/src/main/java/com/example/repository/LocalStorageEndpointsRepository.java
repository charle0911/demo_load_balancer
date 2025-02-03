package com.example.repository;


import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LocalStorageEndpointsRepository implements EndpointsRepository {
    // Avoid race condition (Because loadbalancer is read-heavy so use copy write is better than lock)
    private final CopyOnWriteArrayList<String> endpoints = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<String> probationEndpoints = new CopyOnWriteArrayList<>();

    @Override
    public boolean register(String endpoint) {
        return endpoints.addIfAbsent(endpoint);
    }

    @Override
    public List<String> getEndpoints() {
        return endpoints;
    }

    @Override
    public boolean remove(String targetEndpoint) {
        boolean removedFromNormal = endpoints.remove(targetEndpoint);
        boolean removedFromProbation = probationEndpoints.remove(targetEndpoint);
        return removedFromNormal || removedFromProbation;
    }

    @Override
    public List<String> getProbationEndpoints() {
        return probationEndpoints;
    }

    @Override
    public void downgrade(String endpoint) {
        endpoints.remove(endpoint);
        probationEndpoints.add(endpoint);
    }

    @Override
    public void upgrade(String endpoint) {
        probationEndpoints.remove(endpoint);
        endpoints.add(endpoint);
    }
}
