package com.example;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Random;

@SpringBootApplication
@RestController
@Log4j2
@RequestMapping("/")
public class SpringApplication implements CommandLineRunner {
    @Value("${register.domain}")
    private String registerCenterDomain;

    @Value("${register.port}")
    private int registerCenterPort;

    @Value("${server.port}")
    private int exportPort;

    @Value("${mock.enable: false}")
    private boolean isMockTimeout;

    @Value("${mock.timeout: 300}")
    private int mockTimeout;

    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(SpringApplication.class, args);
    }

    @PostMapping("/**")
    public Map<String, Object> processRequest(@RequestBody Map<String, Object> payload) {
        return payload;
    }

    @GetMapping("/health")
    public ResponseEntity<Void> healthCheck() throws InterruptedException {
        if (isMockTimeout) {
            Random random = new Random();
            Thread.sleep(random.nextInt(mockTimeout));
        }
        log.info("Health check started");
        return ResponseEntity.ok().build();
    }

    @Override
    public void run(String... args) throws Exception {
        String url = registerCenterDomain + ":" + registerCenterPort + "/register";
        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> request = Map.of("endpoint", "http://localhost:" + exportPort);
        log.info("Sending request to {}", url);
        try {
            Map response = restTemplate.postForObject(url, request, Map.class);
            log.info("Received response: {}", response);
        } catch (Exception e) {
            System.err.println("Failed to send POST request: " + e.getMessage());
        }
    }
}
