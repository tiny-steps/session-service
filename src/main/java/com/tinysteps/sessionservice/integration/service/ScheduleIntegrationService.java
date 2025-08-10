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
public class ScheduleIntegrationService {

    private final WebClient secureWebClient;
    private final IntegrationProperties integrationProperties;
    private final CircuitBreaker appointmentServiceCircuitBreaker;

    public boolean hasActiveAppointment(UUID userId, UUID doctorId, UUID sessionTypeId) {
        String url = integrationProperties.getAppointmentService().getBaseUrl()
                + "/active?userId=" + userId + "&doctorId=" + doctorId
                + "&sessionTypeId=" + sessionTypeId;
        return secureWebClient.get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        resp -> Mono.error(new RuntimeException("Appointment status check failed")))
                .bodyToMono(Boolean.class)
                .transformDeferred(CircuitBreakerOperator.of(appointmentServiceCircuitBreaker))
                .blockOptional()
                .orElse(false);
    }
}
