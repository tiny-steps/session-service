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
public class AddressIntegrationService {

    private final WebClient secureWebClient;
    private final IntegrationProperties integrationProperties;
    private final CircuitBreaker addressServiceCircuitBreaker;

    public void validatePracticeExistsOrThrow(UUID practiceId) {
        String url = integrationProperties.getAddressService().getBaseUrl() + "/" + practiceId;
        secureWebClient.get()
                .uri(url)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(),
                        resp -> Mono.error(new IllegalArgumentException("Practice not found: " + practiceId)))
                .bodyToMono(Void.class)
                .transformDeferred(CircuitBreakerOperator.of(addressServiceCircuitBreaker))
                .block();
    }

    public boolean validatePracticeOwnershipByUserId(UUID practiceId, String userId) {
        String url = integrationProperties.getAddressService().getBaseUrl()
                + "/" + practiceId + "/owner?userId=" + userId;
        return secureWebClient.get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        resp -> Mono.error(new RuntimeException("Practice ownership check failed")))
                .bodyToMono(Boolean.class)
                .transformDeferred(CircuitBreakerOperator.of(addressServiceCircuitBreaker))
                .blockOptional()
                .orElse(false);
    }
}
