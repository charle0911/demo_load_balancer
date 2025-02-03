package com.example.http;

import com.example.common.TaskScheduler;
import com.example.register.EndpointHealthMonitor;
import com.example.register.EndpointRegister;
import com.example.repository.EndpointsRepository;
import com.example.repository.LocalStorageEndpointsRepository;
import com.sun.net.httpserver.HttpServer;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.InetSocketAddress;

@Log4j2
public class HttpServerInitializer {
    private final RegisterHandler registerHandler;
    private final RouteHandler routeHandler;

    public HttpServerInitializer() {
        EndpointsRepository endpointsRepository = new LocalStorageEndpointsRepository();
        RequestDispatcher requestDispatcher = new RequestDispatcher();
        EndpointHealthMonitor endpointHealthMonitor = new EndpointHealthMonitor(endpointsRepository, new TaskScheduler(1), requestDispatcher);
        this.routeHandler = new RouteHandler(requestDispatcher, endpointsRepository);
        this.registerHandler = new RegisterHandler(new EndpointRegister(endpointsRepository, endpointHealthMonitor));
    }

    public void createServer() {
        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(8090), 0);
        } catch (IOException e) {
            log.error("Server started error : ", e);
            throw new RuntimeException(e);
        }
        addHandlers(server);
        server.setExecutor(null);
        server.start();
        log.info("Server started on port 8090");
    }

    private void addHandlers(HttpServer server) {
        server.createContext("/register", registerHandler);
        server.createContext("/", routeHandler);
    }
}
