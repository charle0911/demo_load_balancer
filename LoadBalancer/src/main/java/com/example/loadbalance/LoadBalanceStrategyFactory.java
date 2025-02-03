package com.example.loadbalance;

import com.example.repository.EndpointsRepository;

import java.util.HashMap;
import java.util.Map;

public class LoadBalanceStrategyFactory {
    private final Map<LoadBalanceAlgorithm, LoadBalanceStrategy> strategyMap;
    private final LoadBalanceStrategy defaultStrategy;


    public LoadBalanceStrategyFactory(EndpointsRepository endpointsRepository) {
        this.strategyMap = new HashMap<>();
        defaultStrategy = new RoundRobinStrategy(endpointsRepository);
        strategyMap.put(LoadBalanceAlgorithm.ROUND_ROBIN, defaultStrategy);

    }

    public LoadBalanceStrategy getLoadBalanceStrategy(LoadBalanceAlgorithm loadBalanceAlgorithm) {
        return strategyMap.getOrDefault(loadBalanceAlgorithm, defaultStrategy);
    }
}
