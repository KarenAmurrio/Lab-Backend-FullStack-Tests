package com.unir.ms_books_catalogue.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unir.ms_books_catalogue.controller.model.LibroDto;
import com.unir.ms_books_catalogue.data.LibroRepository;
import com.unir.ms_books_catalogue.data.model.Libro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LibrosServiceImplTest {

    @Mock
    private LibroRepository repository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private LibrosServiceImpl service;

    private Libro libroMock;
    private LibroDto libroDtoMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        libroMock = new Libro();
        libroMock.setId(1L);
        libroMock.setTitulo("Clean Code");
        libroMock.setAutor("Robert Martin");
        libroMock.setEditorial("Prentice Hall");
        libroMock.setAnio(2008);
        libroMock.setIsbn("978-0132350884");
        libroMock.setGenero("Tecnologia");
        libroMock.setPrecio(45.99f);
        libroMock.setStock(10);
        libroMock.setVisible(true);
        libroMock.setValoracion(5);

        libroDtoMock = new LibroDto();
        libroDtoMock.setTitulo("Clean Code");
        libroDtoMock.setAutor("Robert Martin");
        libroDtoMock.setEditorial("Prentice Hall");
        libroDtoMock.setAnio(2008);
        libroDtoMock.setIsbn("978-0132350884");
        libroDtoMock.setGenero("Tecnologia");
        libroDtoMock.setPrecio(45.99f);
        libroDtoMock.setStock(10);
        libroDtoMock.setVisible(true);
        libroDtoMock.setValoracion(5);
    }

    // =========================================================
    // getLibros
    // =========================================================

    @Test
    @DisplayName("getLibros sin filtros retorna lista cuando hay datos")
    void getLibros_sinFiltros_conDatos_retornaLista() {
        // Given
        when(repository.getLibros()).thenReturn(List.of(libroMock));

        // When
        List<Libro> result = service.getLibros(null, null, null, null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Clean Code", result.get(0).getTitulo());
        verify(repository, times(1)).getLibros();
    }

    @Test
    @DisplayName("getLibros sin filtros retorna null cuando lista vacia")
    void getLibros_sinFiltros_listaVacia_retornaNull() {
        // Given
        when(repository.getLibros()).thenReturn(Collections.emptyList());

        // When
        List<Libro> result = service.getLibros(null, null, null, null, null, null, null);

        // Then
        assertNull(result);
        verify(repository, times(1)).getLibros();
    }

    @Test
    @DisplayName("getLibros con filtro titulo llama a repository.search")
    void getLibros_conFiltroTitulo_llamaSearch() {
        // Given
        when(repository.search("Clean Code", null, null, null, null, null, null))
                .thenReturn(List.of(libroMock));

        // When
        List<Libro> result = service.getLibros("Clean Code", null, null, null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repository, times(1)).search("Clean Code", null, null, null, null, null, null);
        verify(repository, never()).getLibros();
    }

    @Test
    @DisplayName("getLibros con filtro autor llama a repository.search")
    void getLibros_conFiltroAutor_llamaSearch() {
        // Given
        when(repository.search(null, "Robert Martin", null, null, null, null, null))
                .thenReturn(List.of(libroMock));

        // When
        List<Libro> result = service.getLibros(null, "Robert Martin", null, null, null, null, null);

        // Then
        assertNotNull(result);
        verify(repository, times(1)).search(null, "Robert Martin", null, null, null, null, null);
        verify(repository, never()).getLibros();
    }

    @Test
    @DisplayName("getLibros con filtro visible llama a repository.search")
    void getLibros_conFiltroVisible_llamaSearch() {
        // Given
        when(repository.search(null, null, null, null, null, null, true))
                .thenReturn(List.of(libroMock));

        // When
        List<Libro> result = service.getLibros(null, null, null, null, null, null, true);

        // Then
        assertNotNull(result);
        verify(repository, times(1)).search(null, null, null, null, null, null, true);
        verify(repository, never()).getLibros();
    }

    // =========================================================
    // getLibro
    // =========================================================

    @Test
    @DisplayName("getLibro con ID existente retorna el libro")
    void getLibro_idExiste_retornaLibro() {
        // Given
        when(repository.getById(1L)).thenReturn(libroMock);

        // When
        Libro result = service.getLibro("1");

        // Then
        assertNotNull(result);
        assertEquals("Clean Code", result.getTitulo());
        assertEquals("Robert Martin", result.getAutor());
        verify(repository, times(1)).getById(1L);
    }

    @Test
    @DisplayName("getLibro con ID inexistente retorna null")
    void getLibro_idNoExiste_retornaNull() {
        // Given
        when(repository.getById(99L)).thenReturn(null);

        // When
        Libro result = service.getLibro("99");

        // Then
        assertNull(result);
        verify(repository, times(1)).getById(99L);
    }

    // =========================================================
    // eliminarLibro
    // =========================================================

    @Test
    @DisplayName("eliminarLibro con ID existente elimina y retorna true")
    void eliminarLibro_idExiste_retornaTrue() {
        // Given
        when(repository.getById(1L)).thenReturn(libroMock);
        doNothing().when(repository).delete(libroMock);

        // When
        Boolean result = service.eliminarLibro("1");

        // Then
        assertTrue(result);
        verify(repository, times(1)).getById(1L);
        verify(repository, times(1)).delete(libroMock);
    }

    @Test
    @DisplayName("eliminarLibro con ID inexistente retorna false")
    void eliminarLibro_idNoExiste_retornaFalse() {
        // Given
        when(repository.getById(99L)).thenReturn(null);

        // When
        Boolean result = service.eliminarLibro("99");

        // Then
        assertFalse(result);
        verify(repository, times(1)).getById(99L);
        verify(repository, never()).delete(any());
    }

    // =========================================================
    // crearLibro
    // =========================================================

    @Test
    @DisplayName("crearLibro con datos validos crea y retorna el libro")
    void crearLibro_datosValidos_retornaLibroCreado() {
        // Given
        when(repository.save(any(Libro.class))).thenReturn(libroMock);

        // When
        Libro result = service.crearLibro(libroDtoMock);

        // Then
        assertNotNull(result);
        assertEquals("Clean Code", result.getTitulo());
        verify(repository, times(1)).save(any(Libro.class));
    }

    @Test
    @DisplayName("crearLibro con LibroDto null retorna null")
    void crearLibro_libroDtoNull_retornaNull() {
        // When
        Libro result = service.crearLibro(null);

        // Then
        assertNull(result);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("crearLibro con titulo vacio retorna null")
    void crearLibro_tituloVacio_retornaNull() {
        // Given
        libroDtoMock.setTitulo("   ");

        // When
        Libro result = service.crearLibro(libroDtoMock);

        // Then
        assertNull(result);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("crearLibro con autor vacio retorna null")
    void crearLibro_autorVacio_retornaNull() {
        // Given
        libroDtoMock.setAutor("");

        // When
        Libro result = service.crearLibro(libroDtoMock);

        // Then
        assertNull(result);
        verify(repository, never()).save(any());
    }

    // =========================================================
    // actualizarLibro PUT
    // =========================================================

    @Test
    @DisplayName("actualizarLibro PUT con ID existente actualiza y retorna libro")
    void actualizarLibro_PUT_idExiste_retornaLibroActualizado() {
        // Given
        when(repository.getById(1L)).thenReturn(libroMock);
        when(repository.save(libroMock)).thenReturn(libroMock);

        // When
        Libro result = service.actualizarLibro("1", libroDtoMock);

        // Then
        assertNotNull(result);
        verify(repository, times(1)).getById(1L);
        verify(repository, times(1)).save(libroMock);
    }

    @Test
    @DisplayName("actualizarLibro PUT con ID inexistente retorna null")
    void actualizarLibro_PUT_idNoExiste_retornaNull() {
        // Given
        when(repository.getById(99L)).thenReturn(null);

        // When
        Libro result = service.actualizarLibro("99", libroDtoMock);

        // Then
        assertNull(result);
        verify(repository, times(1)).getById(99L);
        verify(repository, never()).save(any());
    }
}