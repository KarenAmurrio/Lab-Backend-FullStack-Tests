package com.g5.relpapel.msbookpayments.MsBookPayments.service;

import com.g5.relpapel.msbookpayments.MsBookPayments.model.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class BuscadorServiceTest {
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private BuscadorService buscadorService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(buscadorService, "buscadorUrl", "http://api-test/books/");

    }

    @Test
    void testInitMethod() {
        assertDoesNotThrow(() -> buscadorService.init());
    }

    @Test
    void validarItemTest_HayStock() {
        // GIVEN=
        Item itemMock = new Item("1", "Quijoque Test", "1111", 15, true);
        ResponseEntity<Item> response = new ResponseEntity<>(itemMock, HttpStatus.OK);

        Mockito.when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(),
                eq(Item.class))).thenReturn(response);
        Item resultado = buscadorService.validarItem("1", 5);
        assertNotNull(resultado);
        assertEquals("Quijoque Test", resultado.getTitulo());
    }

    @Test
    void validarItemTest_NoVisible() {
        // GIVEN
        Item itemMock = new Item("1", "Quijoque Test", "1111", 15, false);
        ResponseEntity<Item> response = new ResponseEntity<>(itemMock, HttpStatus.OK);

        Mockito.when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(),
                eq(Item.class))).thenReturn(response);
        Item resultado = buscadorService.validarItem("1", 5);
        assertNull(resultado);

    }

    @Test
    void validarItemTest_NoHayStockSuficiente() {
        // GIVEN
        Item itemMock = new Item("1", "Quijoque Test", "1111", 15, true);
        ResponseEntity<Item> response = new ResponseEntity<>(itemMock, HttpStatus.OK);

        Mockito.when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(),
                eq(Item.class))).thenReturn(response);
        Item resultado = buscadorService.validarItem("1", 50);
        assertNull(resultado);
    }

    @Test
    void validarItemTest_ResponseBodyNull_RetornaNull() {
        // GIVEN:
        ResponseEntity<Item> response = new ResponseEntity<>(null, HttpStatus.OK);

        Mockito.when(restTemplate.exchange(anyString(),
                eq(HttpMethod.GET),
                any(),
                eq(Item.class)))
                .thenReturn(response);

        // WHEN & THEN:
        Item resultado = buscadorService.validarItem("1", 5);
        assertNull(resultado);
    }

    @Test
    void validarItemTest_StatusNoOk_RetornaNull() {
        // GIVEN:
        Item itemMock = new Item("1", "Test", "111", 10, true);
        ResponseEntity<Item> response = new ResponseEntity<>(itemMock, HttpStatus.ACCEPTED);

        Mockito.when(restTemplate.exchange(anyString(),
                        eq(HttpMethod.GET),
                        any(),
                        eq(Item.class)))
                .thenReturn(response);

        // WHEN
        Item resultado = buscadorService.validarItem("1", 5);

        // THEN
        assertNull(resultado);
    }

    @Test
    void validarItemTest_HttpClientError() {
        Mockito.when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Item.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        Item resultado = buscadorService.validarItem("999", 1);
        assertNull(resultado);
    }

    @Test
    void validarItemTest_HttpServerError() {
        Mockito.when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Item.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        Item resultado = buscadorService.validarItem("1", 1);
        assertNull(resultado);
    }

    @Test
    void validarItemTest_GenericException_RetornaNull() {
        Mockito.when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Item.class)))
                .thenThrow(new RuntimeException("Error inesperado"));

        Item resultado = buscadorService.validarItem("1", 1);
        assertNull(resultado);
    }

    @Test
    void notificarCompraTest_Exitosa() {
        Item libro = new Item("1", "Quijoque Test", "1111", 15, true);
        ResponseEntity<Item> response = new ResponseEntity<>(libro, HttpStatus.OK);

        Mockito.when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.PATCH),
                any(HttpEntity.class),
                eq(Item.class))).thenReturn(response);
        assertDoesNotThrow(() -> {
            buscadorService.notificarCompra(libro, 3);
        });
    }

    @Test
    void notificarCompraTest_Error() {
        // GIVEN:
        Item libro = new Item("1", "Quijoque Test", "1111", 15, true);
        ResponseEntity<Item> response = new ResponseEntity<>(libro, HttpStatus.INTERNAL_SERVER_ERROR);

        Mockito.when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.PATCH),
                any(HttpEntity.class),
                eq(Item.class))).thenReturn(response);
        Exception exception = assertThrows(Exception.class, () -> {
            buscadorService.notificarCompra(libro, 3);
        });

        assertEquals("Imposible completar el registro de compra", exception.getMessage());
    }

    @Test
    void notificarCompraTest_StockInsuficiente_LanzaExcepcion() {
        // GIVEN:
        Item libro = new Item("1", "Libro Test", "123", 5, true);

        // WHEN & THEN
        Exception exception = assertThrows(Exception.class, () -> {
            buscadorService.notificarCompra(libro, 100);
        });

        assertEquals("No se pudo completar el registro de la compra", exception.getMessage());
    }

    @Test
    void notificarCompraTest_LibroNull_LanzaExcepcion() {
        // WHEN & THEN:
        Exception exception = assertThrows(Exception.class, () -> {
            buscadorService.notificarCompra(null, 1);
        });

        assertEquals("No se pudo completar el registro de la compra", exception.getMessage());
    }

    @Test
    void notificarCompra_DebeEnviarHeadersCorrectos() throws Exception {
        // GIVEN
        Item libro = new Item("1", "Libro", "123", 10, true);
        ResponseEntity<Item> responseOk = new ResponseEntity<>(HttpStatus.OK);
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        Mockito.when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.PATCH),
                entityCaptor.capture(),
                eq(Item.class))).thenReturn(responseOk);

        // WHEN
        buscadorService.notificarCompra(libro, 1);

        // THEN
        HttpEntity entityEnviada = entityCaptor.getValue();
        assertEquals(MediaType.APPLICATION_JSON, entityEnviada.getHeaders().getContentType());
    }

}