package com.example.loadbalance;

import com.example.repository.EndpointsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class RoundRobinStrategyTest {
    private EndpointsRepository endpointsRepository;
    private RoundRobinStrategy roundRobinStrategy;

    @BeforeEach
    void setUp() {
        endpointsRepository = mock(EndpointsRepository.class);
        roundRobinStrategy = new RoundRobinStrategy(endpointsRepository);
    }

    @Test
    void shouldSelectEndpointsInRoundRobinOrder() {
        List<String> endpoints = List.of("http://service1", "http://service2", "http://service3");
        when(endpointsRepository.getEndpoints()).thenReturn(endpoints);

        assertThat(roundRobinStrategy.selectEndpoint()).isEqualTo("http://service2");
        assertThat(roundRobinStrategy.selectEndpoint()).isEqualTo("http://service3");
        assertThat(roundRobinStrategy.selectEndpoint()).isEqualTo("http://service1");
        assertThat(roundRobinStrategy.selectEndpoint()).isEqualTo("http://service2");
        assertThat(roundRobinStrategy.selectEndpoint()).isEqualTo("http://service3");
        assertThat(roundRobinStrategy.selectEndpoint()).isEqualTo("http://service1");

        verify(endpointsRepository, times(6)).getEndpoints();
    }

    @Test
    void shouldThrowExceptionWhenNoEndpointsAvailable() {
        when(endpointsRepository.getEndpoints()).thenReturn(List.of());

        assertThatThrownBy(() -> roundRobinStrategy.selectEndpoint())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No endpoints available");

        verify(endpointsRepository).getEndpoints();
    }

    @Test
    void shouldResetCounterWhenExceedingMaxValue() {
        List<String> endpoints = List.of("http://service1", "http://service2");
        when(endpointsRepository.getEndpoints()).thenReturn(endpoints);

        roundRobinStrategy.getCounter().set(Integer.MAX_VALUE - 5);

        IntStream.range(0, 10).forEach(i -> roundRobinStrategy.selectEndpoint());

        assertThat(roundRobinStrategy.getCounter().get()).isLessThan(10);
    }

    @Test
    void shouldHandleConcurrencyCorrectly() {
        List<String> endpoints = List.of("http://service1", "http://service2", "http://service3");
        when(endpointsRepository.getEndpoints()).thenReturn(endpoints);

        IntStream.range(0, 1000).parallel().forEach(i -> roundRobinStrategy.selectEndpoint());

        assertThat(roundRobinStrategy.getCounter().get()).isGreaterThan(0);
        verify(endpointsRepository, atLeastOnce()).getEndpoints();
    }
}