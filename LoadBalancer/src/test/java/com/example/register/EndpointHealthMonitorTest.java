package com.example.register;

import com.example.common.TaskScheduler;
import com.example.http.HttpResponse;
import com.example.http.RequestDispatcher;
import com.example.repository.EndpointsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class EndpointHealthMonitorTest {
    private EndpointsRepository endpointsRepository;
    private TaskScheduler taskScheduler;
    private RequestDispatcher requestDispatcher;
    private EndpointHealthMonitor healthMonitor;

    @BeforeEach
    void setUp() {
        endpointsRepository = mock(EndpointsRepository.class);
        taskScheduler = mock(TaskScheduler.class);
        requestDispatcher = spy(new RequestDispatcher());

        healthMonitor = new EndpointHealthMonitor(endpointsRepository, taskScheduler, requestDispatcher);
    }

    @Test
    void shouldDowngradeEndpointWhenHealthCheckExceedsResponseTimeLimit() throws IOException, URISyntaxException {
        String endpoint = "http://service1";
        int delayMs = 250;

        doAnswer(invocation -> {
            Thread.sleep(delayMs);
            return new HttpResponse(200, "OK");
        }).when(requestDispatcher).forwardRequest(endpoint + "/health");

        boolean result = healthMonitor.healthCheck(endpoint);

        assertThat(result).isTrue();
        verify(endpointsRepository).downgrade(endpoint);
        verify(taskScheduler).scheduleTask(any(Runnable.class), eq(5L), eq(TimeUnit.SECONDS));
    }

    @Test
    void shouldNotDowngradeEndpointWhenResponseTimeIsWithinLimit() throws IOException, URISyntaxException {
        String endpoint = "http://service1";
        int delayMs = 150;

        doAnswer(invocation -> {
            Thread.sleep(delayMs);
            return new HttpResponse(200, "OK");
        }).when(requestDispatcher).forwardRequest(endpoint + "/health");

        boolean result = healthMonitor.healthCheck(endpoint);

        assertThat(result).isTrue();
        verify(endpointsRepository, never()).downgrade(endpoint);
        verify(taskScheduler).scheduleTask(any(Runnable.class), eq(1L), eq(TimeUnit.SECONDS));
    }
}