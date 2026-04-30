package com.g5.relpapel.cloudgateway.resolucionpeticiones.decorator;

import com.g5.relpapel.cloudgateway.resolucionpeticiones.model.GatewayRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.util.LinkedMultiValueMap;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GetRequestDecoratorTest {

    private GetRequestDecorator decorator;
    private MockServerWebExchange exchange;

    @BeforeEach
    void setUp() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost/test")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token123")
                .build();
        exchange = MockServerWebExchange.from(request);

        // ⚠️ CLAVE: URI que getURI() va a leer del exchange
        exchange.getAttributes().put(
                ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR,
                URI.create("http://target-service/api/resource")
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, "application/json");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer token123");

        LinkedMultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("param1", "value1");

        GatewayRequest gatewayRequest = new GatewayRequest();
        gatewayRequest.setTargetMethod(HttpMethod.GET);
        gatewayRequest.setExchange(exchange);
        gatewayRequest.setHeaders(headers);
        gatewayRequest.setQueryParams(queryParams);

        decorator = new GetRequestDecorator(gatewayRequest);
    }

    // ── TEST 1: getMethod() retorna GET ───────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("T1 - getMethod() retorna HttpMethod.GET")
    void testGetMethod() {
        assertEquals(HttpMethod.GET, decorator.getMethod());
    }

    // ── TEST 2: getBody() retorna Flux vacío ─────────────────────────────────

    @Test
    @Order(2)
    @DisplayName("T2 - getBody() retorna Flux vacío para GET")
    void testGetBody() {
        Flux<?> body = decorator.getBody();

        assertNotNull(body);
        StepVerifier.create(body)
                .expectComplete()
                .verify();
    }

    // ── TEST 3: getHeaders() retorna los headers correctos ────────────────────

    @Test
    @Order(3)
    @DisplayName("T3 - getHeaders() retorna los headers correctos")
    void testGetHeaders() {
        HttpHeaders headers = decorator.getHeaders();

        assertNotNull(headers);
        assertEquals("application/json", headers.getFirst(HttpHeaders.CONTENT_TYPE));
        assertEquals("Bearer token123", headers.getFirst(HttpHeaders.AUTHORIZATION));
    }

    // ── TEST 4: getURI() retorna URI con query params ─────────────────────────

    @Test
    @Order(4)
    @DisplayName("T4 - getURI() retorna URI correcta con query params")
    void testGetURI() {
        URI uri = decorator.getURI();

        assertNotNull(uri);
        assertEquals("http", uri.getScheme());
        assertEquals("target-service", uri.getHost());
        assertTrue(uri.toString().contains("param1=value1"));
    }
}