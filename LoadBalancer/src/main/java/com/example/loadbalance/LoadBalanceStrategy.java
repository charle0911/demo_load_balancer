package com.example.loadbalance;

public interface LoadBalanceStrategy {
    String selectEndpoint();
}
