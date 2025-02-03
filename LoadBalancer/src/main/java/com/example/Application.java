package com.example;

import com.example.http.HttpServerInitializer;

public class Application {
    public static void main(String[] args) {
        HttpServerInitializer initializer = new HttpServerInitializer();
        initializer.createServer();
    }
}
