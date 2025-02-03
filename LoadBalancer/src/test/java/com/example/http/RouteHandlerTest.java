package com.example.http;

import com.example.repository.EndpointsRepository;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RouteHandlerTest {
    private RequestDispatcher requestDispatcher;
    private HttpExchange exchange;
    private RouteHandler routeHandler;
    private EndpointsRepository endpointsRepository;
    private ByteArrayOutputStream responseOutputStream;

    @BeforeEach
    void setUp() throws URISyntaxException {
        requestDispatcher = mock(RequestDispatcher.class);
        this.endpointsRepository = mock(EndpointsRepository.class);
        exchange = mock(HttpExchange.class);
        responseOutputStream = new ByteArrayOutputStream();

        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestURI()).thenReturn(new java.net.URI("/api/test"));
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream("{\"key\":\"value\"}".getBytes()));
        when(exchange.getResponseBody()).thenReturn(responseOutputStream);

        when(endpointsRepository.getEndpoints()).thenReturn(List.of("http://service1"));
        routeHandler = new RouteHandler(requestDispatcher, endpointsRepository);
    }

    @Test
    void shouldForwardRequestToSelectedEndpoint() throws IOException {
        HttpResponse mockResponse = new HttpResponse(200, "{\"status\":\"ok\"}");
        when(requestDispatcher.forwardRequest("http://service1/api/test", "{\"key\":\"value\"}"))
                .thenReturn(mockResponse);

        routeHandler.handle(exchange);

        verify(requestDispatcher).forwardRequest("http://service1/api/test", "{\"key\":\"value\"}");
        verify(exchange).sendResponseHeaders(200, mockResponse.body().length());
        assertThat(responseOutputStream.toString()).isEqualTo("{\"status\":\"ok\"}");
    }

    @Test
    void shouldRetryRequestWhenDispatcherFails() throws IOException {
        when(requestDispatcher.forwardRequest("http://service1/api/test", "{\"key\":\"value\"}"))
                .thenThrow(new RuntimeException("Service Unavailable"))
                .thenThrow(new RuntimeException("Service Unavailable"))
                .thenReturn(new HttpResponse(200, "{\"status\":\"ok\"}"));

        routeHandler.handle(exchange);

        verify(exchange).sendResponseHeaders(200, "{\"status\":\"ok\"}".length());
        assertThat(responseOutputStream.toString()).isEqualTo("{\"status\":\"ok\"}");
    }

    @Test
    void shouldReturn503WhenAllRetriesFail() throws IOException {
        when(requestDispatcher.forwardRequest("http://service1/api/test", "{\"key\":\"value\"}"))
                .thenThrow(new RuntimeException("Service Unavailable"));

        routeHandler.handle(exchange);

        verify(exchange).sendResponseHeaders(503, -1); // 503 Service Unavailable
    }

    @Test
    void shouldReturn405WhenMethodIsNotPost() throws IOException {
        when(exchange.getRequestMethod()).thenReturn("GET");

        routeHandler.handle(exchange);

        verify(exchange).sendResponseHeaders(405, -1); // 405 Method Not Allowed
    }
}