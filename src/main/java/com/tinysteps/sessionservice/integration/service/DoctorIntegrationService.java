package com.tinysteps.sessionservice.integration.service;

import com.tinysteps.sessionservice.config.IntegrationProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DoctorIntegrationService {

    private final WebClient secureWebClient;
    private final IntegrationProperties integrationProperties;
    private final CircuitBreaker doctorServiceCircuitBreaker;

    public void validateDoctorExistsOrThrow(UUID doctorId) {
        String url = integrationProperties.getDoctorService().getBaseUrl() + "/" + doctorId;
        secureWebClient.get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> Mono.error(new IllegalArgumentException("Doctor not found: " + doctorId)))
                .bodyToMono(Void.class)
                .transformDeferred(CircuitBreakerOperator.of(doctorServiceCircuitBreaker))
                .block();
    }

    public boolean validateDoctorOwnership(UUID doctorId, String userId) {
        String url = integrationProperties.getDoctorService().getBaseUrl()
                + "/" + doctorId + "/owner?userId=" + userId;
        return secureWebClient.get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        resp -> Mono.error(new RuntimeException("Doctor ownership check failed")))
                .bodyToMono(Boolean.class)
                .transformDeferred(CircuitBreakerOperator.of(doctorServiceCircuitBreaker))
                .blockOptional()
                .orElse(false);
    }
}
