package com.example.http;

import org.apache.logging.log4j.util.Strings;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class RequestDispatcher {
    public HttpResponse forwardRequest(String targetUrl, String jsonPayload) throws IOException {
        URI uri = URI.create(targetUrl);
        URL url = uri.toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = connection.getOutputStream()) {
            os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = connection.getResponseCode();
        return getHttpResponse(connection, responseCode);
    }

    public HttpResponse forwardRequest(String targetUrl) throws IOException, URISyntaxException {
        URI uri = new URI(targetUrl);
        URL url = uri.toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(2000);
        connection.setReadTimeout(2000);
        int responseCode = connection.getResponseCode();
        return getHttpResponse(connection, responseCode);
    }

    private HttpResponse getHttpResponse(HttpURLConnection connection, int responseCode) throws IOException {
        String responseBody;
        try (InputStream is = responseCode < 400 ? connection.getInputStream() : connection.getErrorStream()) {
            responseBody = is != null ? new String(is.readAllBytes(), StandardCharsets.UTF_8) : Strings.EMPTY;
        }

        return new HttpResponse(responseCode, responseBody);
    }
}
