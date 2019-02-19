package com.demo.resilience.circuit.breaker;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.util.function.Function;
import org.junit.Before;
import org.junit.Test;

/**
 * The Resilient4j Unit Test based on https://www.baeldung.com/resilience4j
 *
 * @author Yury Nino
 * @version 1.0
 * @since 2019-02-15
 */
public class Resilience4jUnitTest {

    interface RemoteService {

        int process(int i);
    }

    private RemoteService service;

    @Before
    public void setUp() {
        service = mock(RemoteService.class);
    }

    @Test
    public void whenRemoteServiceFails_thenCircuitBreakerIsUsed() {

        // First, we need to define the settings to use with:
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            // Percentage of failures to start short-circuit
            .failureRateThreshold(20)
            // Min number of call attempts
            .ringBufferSizeInClosedState(5)
            .build();

        // We create a CircuitBreaker object and call the remote service through it:
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        CircuitBreaker circuitBreaker = registry.circuitBreaker("circuit-breaker");
        Function<Integer, Integer> decorated = CircuitBreaker
            .decorateFunction(circuitBreaker, service::process);

        when(service.process(anyInt())).thenThrow(new RuntimeException());

        for (int i = 0; i < 10; i++) {
            try {
                decorated.apply(i);
            } catch (Exception ignore) {
            }
        }

        verify(service, times(5)).process(any(Integer.class));
    }

}