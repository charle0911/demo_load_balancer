package com.example.http;

import com.example.register.EndpointRegister;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Log4j2
public class RegisterHandler implements HttpHandler {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final EndpointRegister endpointRegister;

    public RegisterHandler(EndpointRegister endpointRegister) {
        this.endpointRegister = endpointRegister;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                log.info("Request Body: {}", requestBody);
                try {
                    JsonNode jsonNode = objectMapper.readTree(requestBody);
                    if (jsonNode.has("endpoint")) {
                        String endpoint = jsonNode.get("endpoint").asText();
                        log.info("Register endpoint: {}", endpoint);
                        boolean result = endpointRegister.register(endpoint);
                        if (result) {
                            sendResponse(exchange, HttpStatus.OK, Map.of("code", 10000));
                        } else {
                            sendResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR, Map.of("error", "Internal Server Error"));
                        }
                    } else {
                        sendResponse(exchange, HttpStatus.BAD_REQUEST, Map.of("error", "Missing 'endpoint' field"));
                    }
                } catch (Exception e) {
                    sendResponse(exchange, HttpStatus.BAD_REQUEST, Map.of("error", "Invalid JSON format"));
                }
            } else {
                sendResponse(exchange, HttpStatus.METHOD_NOT_ALLOWED, Map.of("error", "Method Not Allowed"));
            }
        } catch (Exception e) {
            log.error(e);
            sendResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR, Map.of("error", "Internal Server Error"));
        }
    }

    private void sendResponse(HttpExchange exchange, HttpStatus status, Map<String, Object> responseMap) throws IOException {
        String response = objectMapper.writeValueAsString(responseMap);
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status.getCode(), responseBytes.length);

        try (exchange; OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}