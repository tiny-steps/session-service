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

    // validatePracticeExistsOrThrow method removed - no longer needed after Practice entity removal
    
    // validatePracticeOwnershipByUserId method removed - no longer needed after Practice entity removal
}
