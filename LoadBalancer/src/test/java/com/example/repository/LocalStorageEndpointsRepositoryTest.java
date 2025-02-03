package com.example.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

class LocalStorageEndpointsRepositoryTest {
    private LocalStorageEndpointsRepository repository;

    @BeforeEach
    void setUp() {
        repository = new LocalStorageEndpointsRepository();
    }

    @Test
    void shouldRegisterEndpointSuccessfully() {
        boolean result = repository.register("http://service1");
        assertThat(result).isTrue();
        assertThat(repository.getEndpoints()).containsExactly("http://service1");
    }

    @Test
    void shouldNotRegisterDuplicateEndpoint() {
        repository.register("http://service1");
        boolean result = repository.register("http://service1");

        assertThat(result).isFalse();
        assertThat(repository.getEndpoints()).containsExactly("http://service1");
    }

    @Test
    void shouldRemoveEndpointSuccessfully() {
        repository.register("http://service1");
        boolean result = repository.remove("http://service1");

        assertThat(result).isTrue();
        assertThat(repository.getEndpoints()).isEmpty();
    }

    @Test
    void shouldNotFailWhenRemovingNonExistentEndpoint() {
        boolean result = repository.remove("http://unknown");

        assertThat(result).isFalse();
    }

    @Test
    void shouldDowngradeEndpointSuccessfully() {
        repository.register("http://service1");
        repository.downgrade("http://service1");

        assertThat(repository.getEndpoints()).doesNotContain("http://service1");
        assertThat(repository.getProbationEndpoints()).containsExactly("http://service1");
    }

    @Test
    void shouldUpgradeEndpointSuccessfully() {
        repository.register("http://service1");
        repository.downgrade("http://service1");
        repository.upgrade("http://service1");

        assertThat(repository.getProbationEndpoints()).doesNotContain("http://service1");
        assertThat(repository.getEndpoints()).containsExactly("http://service1");
    }

    @Test
    void shouldReturnCorrectProbationEndpoints() {
        repository.register("http://service1");
        repository.register("http://service2");
        repository.downgrade("http://service2");

        List<String> probationEndpoints = repository.getProbationEndpoints();
        assertThat(probationEndpoints).containsExactly("http://service2");
    }
}