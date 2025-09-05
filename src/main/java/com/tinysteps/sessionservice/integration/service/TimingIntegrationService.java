package com.tinysteps.sessionservice.integration.service;

import com.tinysteps.sessionservice.config.IntegrationProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TimingIntegrationService {

    private final WebClient secureWebClient;
    private final IntegrationProperties integrationProperties;
    private final CircuitBreaker timingServiceCircuitBreaker;

    public boolean isSlotAvailable(UUID doctorId, LocalDate date, String slot) {
        // practiceId parameter removed - no longer needed after Practice entity removal
        String url = integrationProperties.getTimingService().getBaseUrl()
                + "/doctors/" + doctorId
                + "/slots/available?date=" + date + "&slot=" + slot;
        return secureWebClient.get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        resp -> Mono.error(new RuntimeException("Slot availability check failed")))
                .bodyToMono(Boolean.class)
                .transformDeferred(CircuitBreakerOperator.of(timingServiceCircuitBreaker))
                .blockOptional()
                .orElse(false);
    }
}
