package com.example.register;

import com.example.repository.EndpointsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class EndpointRegisterTest {
    private EndpointsRepository endpointsRepository;
    private EndpointHealthMonitor endpointHealthMonitor;
    private EndpointRegister endpointRegister;

    @BeforeEach
    void setUp() {
        endpointsRepository = mock(EndpointsRepository.class);
        endpointHealthMonitor = mock(EndpointHealthMonitor.class);

        endpointRegister = new EndpointRegister(endpointsRepository, endpointHealthMonitor);
    }

    @Test
    void shouldRegisterEndpointSuccessfullyWhenHealthCheckPasses() {
        String endpoint = "http://service1";

        when(endpointsRepository.register(endpoint)).thenReturn(true);
        when(endpointHealthMonitor.healthCheck(endpoint)).thenReturn(true);

        boolean result = endpointRegister.register(endpoint);

        assertThat(result).isTrue();
        verify(endpointsRepository).register(endpoint);
        verify(endpointHealthMonitor).healthCheck(endpoint);
        verify(endpointsRepository, never()).remove(any());
    }

    @Test
    void shouldNotRegisterWhenRepositoryRejectsIt() {
        String endpoint = "http://service1";

        when(endpointsRepository.register(endpoint)).thenReturn(false);

        boolean result = endpointRegister.register(endpoint);

        assertThat(result).isFalse();
        verify(endpointsRepository).register(endpoint);
        verify(endpointHealthMonitor, never()).healthCheck(any());
        verify(endpointsRepository, never()).remove(any());
    }

    @Test
    void shouldRemoveEndpointWhenHealthCheckFails() {
        String endpoint = "http://service1";

        when(endpointsRepository.register(endpoint)).thenReturn(true);
        when(endpointHealthMonitor.healthCheck(endpoint)).thenReturn(false);

        boolean result = endpointRegister.register(endpoint);

        assertThat(result).isFalse();
        verify(endpointsRepository).register(endpoint);
        verify(endpointHealthMonitor).healthCheck(endpoint);
        verify(endpointsRepository).remove(endpoint);
    }
}