package com.g5.relpapel.cloudgateway.resolucionpeticiones.decorator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.g5.relpapel.cloudgateway.resolucionpeticiones.model.GatewayRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PostRequestDecoratorTest {

    private PostRequestDecorator decorator;
    private GatewayRequest gatewayRequest;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("http://localhost/test")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token123")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        exchange.getAttributes().put(
                ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR,
                URI.create("http://target-service/api/resource")
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, "application/json");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer token123");

        gatewayRequest = new GatewayRequest();
        gatewayRequest.setTargetMethod(HttpMethod.POST);
        gatewayRequest.setExchange(exchange);
        gatewayRequest.setHeaders(headers);
        gatewayRequest.setBody(Map.of("key", "value"));

        decorator = new PostRequestDecorator(gatewayRequest, objectMapper);
    }

    // ── TEST 1: getMethod() retorna POST ──────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("T1 - getMethod() retorna HttpMethod.POST")
    void testGetMethod() {
        assertEquals(HttpMethod.POST, decorator.getMethod());
    }

    // ── TEST 2: getBody() retorna Flux con datos serializados ─────────────────

    @Test
    @Order(2)
    @DisplayName("T2 - getBody() retorna Flux con el body JSON serializado")
    void testGetBody() {
        Flux<DataBuffer> body = decorator.getBody();

        assertNotNull(body);
        StepVerifier.create(body)
                .assertNext(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    String bodyStr = new String(bytes, StandardCharsets.UTF_8);
                    assertNotNull(bodyStr);
                    assertTrue(bodyStr.contains("key"));
                    assertTrue(bodyStr.contains("value"));
                })
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

    // ── TEST 4: getURI() retorna URI correcta ─────────────────────────────────

    @Test
    @Order(4)
    @DisplayName("T4 - getURI() retorna URI correcta del target service")
    void testGetURI() {
        URI uri = decorator.getURI();

        assertNotNull(uri);
        assertEquals("http", uri.getScheme());
        assertEquals("target-service", uri.getHost());
        assertEquals("/api/resource", uri.getPath());
    }

    // ── TEST 5: getBody() con ObjectMapper que lanza excepción ───────────────
    // Cubre la línea del @SneakyThrows en getBody()

    @Test
    @Order(5)
    @DisplayName("T5 - getBody() lanza excepcion cuando ObjectMapper falla")
    void testGetBody_ObjectMapperFalla() throws JsonProcessingException {
        // Given: ObjectMapper mockeado que lanza excepción
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        when(mockMapper.writeValueAsBytes(any())).thenThrow(new JsonProcessingException("Error de serialización") {});

        PostRequestDecorator decoratorConError = new PostRequestDecorator(gatewayRequest, mockMapper);

        // When + Then: @SneakyThrows relanza la excepción original (no wrappea en RuntimeException)
        assertThrows(Throwable.class, () ->
                decoratorConError.getBody().blockFirst()
        );
    }
}