package com.g5.relpapel.cloudgateway.resolucionpeticiones.decorator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.g5.relpapel.cloudgateway.resolucionpeticiones.model.GatewayRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RequestDecoratorFactoryTest {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RequestDecoratorFactory factory;

    private MockServerWebExchange buildExchange(HttpMethod method) {
        MockServerHttpRequest request = MockServerHttpRequest
                .method(method, "http://localhost/test")
                .build();
        return MockServerWebExchange.from(request);
    }

    // ── TEST 1: GET → GetRequestDecorator ────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("T1 - GET method returns GetRequestDecorator")
    void testGetDecorator_GET() {
        // Given
        MockServerWebExchange exchange = buildExchange(HttpMethod.GET);
        GatewayRequest request = new GatewayRequest();
        request.setTargetMethod(HttpMethod.GET);
        request.setExchange(exchange);
        request.setHeaders(exchange.getRequest().getHeaders());

        // When
        ServerHttpRequestDecorator decorator = factory.getDecorator(request);

        // Then
        assertNotNull(decorator);
        assertInstanceOf(GetRequestDecorator.class, decorator);
        assertEquals(HttpMethod.GET, decorator.getMethod());
    }

    // ── TEST 2: POST → PostRequestDecorator ──────────────────────────────────

    @Test
    @Order(2)
    @DisplayName("T2 - POST method returns PostRequestDecorator")
    void testGetDecorator_POST() {
        // Given
        MockServerWebExchange exchange = buildExchange(HttpMethod.POST);
        GatewayRequest request = new GatewayRequest();
        request.setTargetMethod(HttpMethod.POST);
        request.setExchange(exchange);
        request.setHeaders(exchange.getRequest().getHeaders());

        // When
        ServerHttpRequestDecorator decorator = factory.getDecorator(request);

        // Then
        assertNotNull(decorator);
        assertInstanceOf(PostRequestDecorator.class, decorator);
        assertEquals(HttpMethod.POST, decorator.getMethod());
    }

    // ── TEST 3: Método inválido → IllegalArgumentException ───────────────────

    @Test
    @Order(3)
    @DisplayName("T3 - Invalid method throws IllegalArgumentException")
    void testGetDecorator_InvalidMethod() {
        // Given
        MockServerWebExchange exchange = buildExchange(HttpMethod.PUT);
        GatewayRequest request = new GatewayRequest();
        request.setTargetMethod(HttpMethod.PUT);
        request.setExchange(exchange);
        request.setHeaders(exchange.getRequest().getHeaders());

        // When + Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> factory.getDecorator(request)
        );
        assertEquals("Invalid http method", exception.getMessage());
    }
}
