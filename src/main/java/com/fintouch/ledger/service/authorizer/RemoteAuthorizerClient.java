package com.fintouch.ledger.service.authorizer;

import com.fintouch.ledger.infra.exception.ExternalDependencyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;

@Component
@ConditionalOnProperty(prefix = "fintouch.authorizer", name = "mode", havingValue = "remote")
public class RemoteAuthorizerClient implements AuthorizerClient {

    private final WebClient webClient;
    private final Duration timeout;

    public RemoteAuthorizerClient(
            WebClient.Builder webClientBuilder,
            @Value("${fintouch.authorizer.base-url}") String baseUrl,
            @Value("${fintouch.authorizer.timeout:2s}") Duration timeout
    ) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.timeout = timeout;
    }

    @Override
    public boolean authorize(Long payerId, Long payeeId, BigDecimal value) {
        try {
            AuthorizerResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/authorize")
                            .queryParam("payer", payerId)
                            .queryParam("payee", payeeId)
                            .queryParam("value", value)
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(AuthorizerResponse.class)
                    .timeout(timeout)
                    .block();

            if (response == null) {
                throw new ExternalDependencyException("Autorizador indisponível");
            }
            return response.authorized();
        } catch (ExternalDependencyException e) {
            throw e;
        } catch (Exception e) {
            throw new ExternalDependencyException("Falha ao contactar autorizador");
        }
    }

    public record AuthorizerResponse(boolean authorized) {
    }
}

