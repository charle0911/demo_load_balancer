package com.example.repository;

import java.util.List;

public interface EndpointsRepository {
    boolean register(String endpoint);

    List<String> getEndpoints();

    boolean remove(String endpoint);

    List<String> getProbationEndpoints();

    void downgrade(String endpoint);

    void upgrade(String endpoint);
}
