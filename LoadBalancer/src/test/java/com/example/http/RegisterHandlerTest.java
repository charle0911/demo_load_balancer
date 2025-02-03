package com.example.http;

import com.example.register.EndpointRegister;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RegisterHandlerTest {
    private EndpointRegister endpointRegister;
    private HttpExchange exchange;
    private RegisterHandler registerHandler;
    private ByteArrayOutputStream responseOutputStream;

    @BeforeEach
    void setUp() {
        endpointRegister = mock(EndpointRegister.class);
        exchange = mock(HttpExchange.class);
        responseOutputStream = new ByteArrayOutputStream();

        when(exchange.getResponseBody()).thenReturn(responseOutputStream);
        when(exchange.getResponseHeaders()).thenReturn(new Headers());


        registerHandler = new RegisterHandler(endpointRegister);
    }

    @Test
    void shouldRegisterEndpointSuccessfully() throws IOException {
        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream("{\"endpoint\":\"http://service1\"}".getBytes()));
        when(endpointRegister.register("http://service1")).thenReturn(true);

        registerHandler.handle(exchange);

        verify(endpointRegister).register("http://service1");
        verify(exchange).sendResponseHeaders(200, "{\"code\":10000}".length());

        assertThat(responseOutputStream.toString()).isEqualTo("{\"code\":10000}");
    }

    @Test
    void shouldReturn400WhenMissingEndpointField() throws IOException {
        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream("{\"url\":\"http://service1\"}".getBytes()));

        registerHandler.handle(exchange);

        verify(exchange).sendResponseHeaders(400, "{\"error\":\"Missing 'endpoint' field\"}".length());
        assertThat(responseOutputStream.toString()).isEqualTo("{\"error\":\"Missing 'endpoint' field\"}");
    }

    @Test
    void shouldReturn400WhenInvalidJsonFormat() throws IOException {
        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream("INVALID_JSON".getBytes()));

        registerHandler.handle(exchange);

        verify(exchange).sendResponseHeaders(400, "{\"error\":\"Invalid JSON format\"}".length());
        assertThat(responseOutputStream.toString()).isEqualTo("{\"error\":\"Invalid JSON format\"}");
    }

    @Test
    void shouldReturn500WhenRegisterFails() throws IOException {
        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream("{\"endpoint\":\"http://service1\"}".getBytes()));
        when(endpointRegister.register("http://service1")).thenReturn(false);

        registerHandler.handle(exchange);

        verify(exchange).sendResponseHeaders(500, "{\"error\":\"Internal Server Error\"}".length());
        assertThat(responseOutputStream.toString()).isEqualTo("{\"error\":\"Internal Server Error\"}");
    }

    @Test
    void shouldReturn405WhenMethodIsNotPost() throws IOException {
        when(exchange.getRequestMethod()).thenReturn("GET");

        registerHandler.handle(exchange);

        verify(exchange).sendResponseHeaders(405, "{\"error\":\"Method Not Allowed\"}".length());
        assertThat(responseOutputStream.toString()).isEqualTo("{\"error\":\"Method Not Allowed\"}");
    }

    @Test
    void shouldReturn500WhenIOExceptionOccurs() throws IOException {
        when(exchange.getRequestMethod()).thenReturn("POST");
        // mock null pointer exception
        when(exchange.getRequestBody()).thenReturn(null);
        registerHandler.handle(exchange);

        verify(exchange).sendResponseHeaders(500, "{\"error\":\"Internal Server Error\"}".length());
        assertThat(responseOutputStream.toString()).isEqualTo("{\"error\":\"Internal Server Error\"}");
    }
}