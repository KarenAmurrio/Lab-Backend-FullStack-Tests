package com.unir.ms_books_catalogue.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unir.ms_books_catalogue.controller.model.LibroDto;
import com.unir.ms_books_catalogue.data.model.Libro;
import com.unir.ms_books_catalogue.service.LibrosService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LibrosController.class)
class LibrosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LibrosService service;

    @Autowired
    private ObjectMapper objectMapper;

    private Libro libroMock;
    private LibroDto libroDtoMock;

    @BeforeEach
    void setUp() {
        libroMock = new Libro();
        libroMock.setTitulo("El Quijote");
        libroMock.setAutor("Cervantes");
        libroMock.setEditorial("Planeta");
        libroMock.setAnio(1605);
        libroMock.setIsbn("123-456");
        libroMock.setGenero("Novela");
        libroMock.setPrecio(19.99f);
        libroMock.setVisible(true);
        libroMock.setStock(10);

        libroDtoMock = new LibroDto();
        libroDtoMock.setTitulo("El Quijote");
        libroDtoMock.setAutor("Cervantes");
        libroDtoMock.setEditorial("Planeta");
        libroDtoMock.setAnio(1605);
        libroDtoMock.setIsbn("123-456");
        libroDtoMock.setGenero("Novela");
        libroDtoMock.setPrecio(19.99f);
        libroDtoMock.setVisible(true);
        libroDtoMock.setStock(10);
    }

    // ─── GET /libros ──────────────────────────────────────────────────────────

    @Test
    void getLibros_cuandoExistenLibros_retorna200ConLista() throws Exception {
        when(service.getLibros(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(libroMock));

        mockMvc.perform(get("/libros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("El Quijote"))
                .andExpect(jsonPath("$[0].autor").value("Cervantes"));
    }

    @Test
    void getLibros_cuandoServiceRetornaNull_retorna200ConListaVacia() throws Exception {
        when(service.getLibros(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(null);

        mockMvc.perform(get("/libros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getLibros_conFiltros_retorna200() throws Exception {
        when(service.getLibros("El Quijote", "Cervantes", null, null, null, "Novela", true))
                .thenReturn(List.of(libroMock));

        mockMvc.perform(get("/libros")
                        .param("titulo", "El Quijote")
                        .param("autor", "Cervantes")
                        .param("genero", "Novela")
                        .param("visible", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("El Quijote"));
    }

    @Test
    void getLibros_cuandoNoHayLibros_retorna200ConListaVacia() throws Exception {
        when(service.getLibros(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/libros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ─── GET /libros/{libroId} ────────────────────────────────────────────────

    @Test
    void getLibro_cuandoExiste_retorna200ConLibro() throws Exception {
        when(service.getLibro("123")).thenReturn(libroMock);

        mockMvc.perform(get("/libros/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("El Quijote"))
                .andExpect(jsonPath("$.autor").value("Cervantes"));
    }

    @Test
    void getLibro_cuandoNoExiste_retorna404() throws Exception {
        when(service.getLibro("999")).thenReturn(null);

        mockMvc.perform(get("/libros/999"))
                .andExpect(status().isNotFound());
    }

    // ─── DELETE /libros/{libroId} ─────────────────────────────────────────────

    @Test
    void deleteLibro_cuandoExiste_retorna200() throws Exception {
        when(service.eliminarLibro("123")).thenReturn(true);

        mockMvc.perform(delete("/libros/123"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteLibro_cuandoNoExiste_retorna404() throws Exception {
        when(service.eliminarLibro("999")).thenReturn(false);

        mockMvc.perform(delete("/libros/999"))
                .andExpect(status().isNotFound());
    }

    // ─── POST /libros ─────────────────────────────────────────────────────────

    @Test
    void addLibro_cuandoDatosValidos_retorna201ConLibro() throws Exception {
        when(service.crearLibro(any(LibroDto.class))).thenReturn(libroMock);

        mockMvc.perform(post("/libros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(libroDtoMock)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("El Quijote"));
    }

    @Test
    void addLibro_cuandoServiceRetornaNull_retorna400() throws Exception {
        when(service.crearLibro(any(LibroDto.class))).thenReturn(null);

        mockMvc.perform(post("/libros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(libroDtoMock)))
                .andExpect(status().isBadRequest());
    }

    // ─── PATCH /libros/{libroId} ──────────────────────────────────────────────

    @Test
    void patchLibro_cuandoExiste_retorna200ConLibroActualizado() throws Exception {
        when(service.actualizarLibro(eq("123"), anyString())).thenReturn(libroMock);

        mockMvc.perform(patch("/libros/123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\": \"Nuevo Titulo\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("El Quijote"));
    }

    @Test
    void patchLibro_cuandoNoExiste_retorna404() throws Exception {
        when(service.actualizarLibro(eq("999"), anyString())).thenReturn(null);

        mockMvc.perform(patch("/libros/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\": \"Nuevo Titulo\"}"))
                .andExpect(status().isNotFound());
    }

    // ─── PUT /libros/{libroId} ────────────────────────────────────────────────

    @Test
    void updateLibro_cuandoExiste_retorna200ConLibroActualizado() throws Exception {
        when(service.actualizarLibro(eq("123"), any(LibroDto.class))).thenReturn(libroMock);

        mockMvc.perform(put("/libros/123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(libroDtoMock)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("El Quijote"));
    }

    @Test
    void updateLibro_cuandoNoExiste_retorna404() throws Exception {
        when(service.actualizarLibro(eq("999"), any(LibroDto.class))).thenReturn(null);

        mockMvc.perform(put("/libros/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(libroDtoMock)))
                .andExpect(status().isNotFound());
    }
}