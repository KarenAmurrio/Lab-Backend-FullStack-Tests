package com.g5.relpapel.msbookpayments.MsBookPayments.service;

import com.g5.relpapel.msbookpayments.MsBookPayments.model.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
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
    void setup(){
        ReflectionTestUtils.setField(buscadorService, "buscadorUrl", "http://api-test/books/");

    }

    @Test
    void validarItemTest_HayStock(){
        Item itemMock = new Item("1", "Quijoque Test", "1111", 15,true);
        ResponseEntity<Item> response = new ResponseEntity<>(itemMock, HttpStatus.OK);

        Mockito.when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(),
                eq(Item.class)
        )).thenReturn(response);
        Item resultado = buscadorService.validarItem("1", 5);
        assertNotNull(resultado);
        assertEquals("Quijoque Test", resultado.getTitulo());
    }


}