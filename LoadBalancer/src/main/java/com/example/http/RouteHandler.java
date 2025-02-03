package com.example.http;

import com.example.loadbalance.LoadBalanceAlgorithm;
import com.example.loadbalance.LoadBalanceStrategy;
import com.example.loadbalance.LoadBalanceStrategyFactory;
import com.example.repository.EndpointsRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@Log4j2
public class RouteHandler implements HttpHandler {
    private final RequestDispatcher requestDispatcher;
    private final LoadBalanceStrategyFactory loadBalanceStrategyFactory;
    private final int MAX_RETRIES = 3;


    public RouteHandler(RequestDispatcher requestDispatcher, EndpointsRepository endpointsRepository) {
        this.requestDispatcher = requestDispatcher;
        this.loadBalanceStrategyFactory = new LoadBalanceStrategyFactory(endpointsRepository);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            LoadBalanceStrategy loadBalanceStrategy = loadBalanceStrategyFactory.getLoadBalanceStrategy(LoadBalanceAlgorithm.ROUND_ROBIN);
            int attempts = 0;
            while (attempts < MAX_RETRIES) {
                try {
                    String endPointPath = loadBalanceStrategy.selectEndpoint() + exchange.getRequestURI().getPath();
                    log.info("Request forward to path: {}, body: {}", endPointPath, requestBody);
                    HttpResponse response = requestDispatcher.forwardRequest(endPointPath, requestBody);

                    exchange.getResponseHeaders().set("src", endPointPath);
                    exchange.sendResponseHeaders(response.statusCode(), response.body().length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.body().getBytes(StandardCharsets.UTF_8));
                    }
                    return;
                } catch (Exception e) {
                    log.error(e);
                    attempts++;
                }
            }
            exchange.sendResponseHeaders(503, -1);  // 503 Service Unavailable
        } else {
            exchange.sendResponseHeaders(405, -1);  // 405 Method Not Allowed
        }
    }
}
