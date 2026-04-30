package com.g5.relpapel.cloudgateway.resolucionpeticiones.filter;

import com.g5.relpapel.cloudgateway.resolucionpeticiones.decorator.GetRequestDecorator;
import com.g5.relpapel.cloudgateway.resolucionpeticiones.decorator.RequestDecoratorFactory;
import com.g5.relpapel.cloudgateway.resolucionpeticiones.model.GatewayRequest;
import com.g5.relpapel.cloudgateway.resolucionpeticiones.utils.RequestBodyExtractor;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.util.LinkedMultiValueMap;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RequestTranslationFilterTest {

    @Mock
    private RequestBodyExtractor requestBodyExtractor;

    @Mock
    private RequestDecoratorFactory requestDecoratorFactory;

    @Mock
    private GatewayFilterChain chain;

    @InjectMocks
    private RequestTranslationFilter filter;

    // ── TEST 1: Sin ContentType → 400 ────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("T1 - Request sin ContentType retorna 400 Bad Request")
    void testFilter_SinContentType() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("http://localhost/test")
                .build(); // sin Content-Type
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    // ── TEST 2: GET con ContentType → 400 (no es POST) ───────────────────────

    @Test
    @Order(2)
    @DisplayName("T2 - Request GET (no POST) retorna 400 Bad Request")
    void testFilter_NoEsPost() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost/test")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    // ── TEST 3: POST válido con queryParams → pasa al chain ──────────────────

    @Test
    @Order(3)
    @DisplayName("T3 - POST válido con ContentType y queryParams pasa al chain")
    void testFilter_PostValido_ConQueryParams() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("http://localhost/test")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .body("{\"targetMethod\":\"GET\"}");
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        URI targetUri = URI.create("http://target-service/api/resource");
        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, targetUri);

        // GatewayRequest CON queryParams (para cubrir el branch request.getQueryParams() != null)
        LinkedMultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("param1", "value1");

        GatewayRequest gatewayRequest = new GatewayRequest();
        gatewayRequest.setTargetMethod(HttpMethod.GET);
        gatewayRequest.setExchange(exchange);
        gatewayRequest.setHeaders(exchange.getRequest().getHeaders());
        gatewayRequest.setQueryParams(queryParams);

        when(requestBodyExtractor.getRequest(any(), any())).thenReturn(gatewayRequest);
        when(requestDecoratorFactory.getDecorator(any())).thenReturn(new GetRequestDecorator(gatewayRequest));
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .expectComplete()
                .verify();

        verify(chain, times(1)).filter(any());
        verify(requestBodyExtractor, times(1)).getRequest(any(), any());
        verify(requestDecoratorFactory, times(1)).getDecorator(any());

        // Verificar que queryParams fue limpiado (branch cubierto)
        assertTrue(gatewayRequest.getQueryParams().isEmpty());
    }

    // ── TEST 4: POST válido sin queryParams → cubre el branch null ────────────

    @Test
    @Order(4)
    @DisplayName("T4 - POST válido sin queryParams cubre branch null")
    void testFilter_PostValido_SinQueryParams() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("http://localhost/test")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .body("{\"targetMethod\":\"GET\"}");
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        URI targetUri = URI.create("http://target-service/api/resource");
        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, targetUri);

        // GatewayRequest SIN queryParams (cubre el branch queryParams == null)
        GatewayRequest gatewayRequest = new GatewayRequest();
        gatewayRequest.setTargetMethod(HttpMethod.GET);
        gatewayRequest.setExchange(exchange);
        gatewayRequest.setHeaders(exchange.getRequest().getHeaders());
        gatewayRequest.setQueryParams(null); // ← branch null

        when(requestBodyExtractor.getRequest(any(), any())).thenReturn(gatewayRequest);
        when(requestDecoratorFactory.getDecorator(any())).thenReturn(new GetRequestDecorator(gatewayRequest));
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .expectComplete()
                .verify();

        verify(chain, times(1)).filter(any());
    }
}