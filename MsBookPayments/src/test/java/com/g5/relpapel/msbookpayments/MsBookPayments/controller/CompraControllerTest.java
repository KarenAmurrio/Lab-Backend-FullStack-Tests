package com.g5.relpapel.msbookpayments.MsBookPayments.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.g5.relpapel.msbookpayments.MsBookPayments.model.Compra;
import com.g5.relpapel.msbookpayments.MsBookPayments.model.Item;
import com.g5.relpapel.msbookpayments.MsBookPayments.repository.CompraRepository;
import com.g5.relpapel.msbookpayments.MsBookPayments.service.BuscadorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(CompraController.class)
class CompraControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BuscadorService buscadorService; // Mock de Spring

    @MockitoBean
    private CompraRepository compraRepository; // Mock de Spring

    @Autowired
    private ObjectMapper objectMapper;

    private Compra compraMock;
    private Item itemMock;

    @BeforeEach
    void setUp() {
        compraMock = new Compra();
        compraMock.setLibroId("1");
        compraMock.setCantidad(2);
        compraMock.setPrecioTotal(50.0);

        itemMock = new Item("1", "Quijote", "Cervantes", 10, true);
    }

    @Test
    void registrarCompra_Exito201() throws Exception {
        when(buscadorService.validarItem(anyString(), anyInt())).thenReturn(itemMock);
        when(compraRepository.save(any(Compra.class))).thenReturn(compraMock);

        // When & Then
        mockMvc.perform(post("/api/compras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(compraMock)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Compra registrada con éxito"));

        verify(compraRepository, times(1)).save(any(Compra.class));
        verify(buscadorService).notificarCompra(eq(itemMock), eq(2));
    }

    @Test
    void registrarCompra_Error400() throws Exception {
        when(buscadorService.validarItem(anyString(), anyInt())).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/api/compras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(compraMock)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Item no válido, no visible o fuera de stock"));
        verify(compraRepository, never()).save(any(Compra.class));
    }
}