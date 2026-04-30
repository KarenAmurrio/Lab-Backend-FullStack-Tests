package com.unir.ms_books_catalogue.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class LibrosServiceImplTest {

    // =========================================================
    // Mocks — objetos falsos que reemplazan las dependencias
    // reales del service. Con esto no necesitamos BD ni Jackson real.
    // =========================================================

    @Mock
    private LibroRepository repository;  // simula el acceso a BD

    @Mock
    private ObjectMapper objectMapper;   // simula la conversion JSON (solo PATCH)

    // =========================================================
    // InjectMocks — crea el service REAL e inyecta los mocks
    // =========================================================

    @InjectMocks
    private LibrosServiceImpl service;

    // =========================================================
    // Datos de prueba reutilizables
    // =========================================================

    private Libro libroMock;
    private LibroDto libroDtoMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Libro completo con todos los campos obligatorios
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

        // DTO con los mismos datos
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
    // getLibros — 5 tests
    // JUnit: estructura y asserts
    // Mockito: simula repository.getLibros() y repository.search()
    // =========================================================

    @Test
    @DisplayName("getLibros sin filtros retorna lista cuando hay datos")
    void getLibros_sinFiltros_conDatos_retornaLista() {
        // Given — programamos el mock para devolver una lista con un libro
        when(repository.getLibros()).thenReturn(List.of(libroMock));

        // When — ejecutamos el metodo real del service
        List<Libro> result = service.getLibros(null, null, null, null, null, null, null);

        // Then — JUnit verifica el resultado, Mockito verifica las llamadas
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Clean Code", result.get(0).getTitulo());
        verify(repository, times(1)).getLibros();
        verify(repository, never()).search(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("getLibros sin filtros retorna null cuando lista esta vacia")
    void getLibros_sinFiltros_listaVacia_retornaNull() {
        // Given — el repositorio devuelve lista vacia
        when(repository.getLibros()).thenReturn(Collections.emptyList());

        // When
        List<Libro> result = service.getLibros(null, null, null, null, null, null, null);

        // Then — el service convierte lista vacia en null
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

        // Then — cuando hay filtro nunca debe llamar a getLibros()
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

    @Test
    @DisplayName("getLibros con filtro editorial llama a repository.search")
    void getLibros_conFiltroEditorial_llamaSearch() {
        when(repository.search(null, null, "Prentice Hall", null, null, null, null))
                .thenReturn(List.of(libroMock));

        List<Libro> result = service.getLibros(null, null, "Prentice Hall", null, null, null, null);

        assertNotNull(result);
        verify(repository).search(null, null, "Prentice Hall", null, null, null, null);
    }

    @Test
    @DisplayName("getLibros con filtro anio llama a repository.search")
    void getLibros_conFiltroAnio_llamaSearch() {
        when(repository.search(null, null, null, 2008, null, null, null))
                .thenReturn(List.of(libroMock));

        List<Libro> result = service.getLibros(null, null, null, 2008, null, null, null);

        assertNotNull(result);
        verify(repository).search(null, null, null, 2008, null, null, null);
    }

    @Test
    @DisplayName("getLibros con filtro resumen llama a repository.search")
    void getLibros_conFiltroResumen_llamaSearch() {
        when(repository.search(null, null, null, null, "guia", null, null))
                .thenReturn(List.of(libroMock));

        List<Libro> result = service.getLibros(null, null, null, null, "guia", null, null);

        assertNotNull(result);
        verify(repository).search(null, null, null, null, "guia", null, null);
    }

    @Test
    @DisplayName("getLibros con filtro genero llama a repository.search")
    void getLibros_conFiltroGenero_llamaSearch() {
        when(repository.search(null, null, null, null, null, "Tecnologia", null))
                .thenReturn(List.of(libroMock));

        List<Libro> result = service.getLibros(null, null, null, null, null, "Tecnologia", null);

        assertNotNull(result);
        verify(repository).search(null, null, null, null, null, "Tecnologia", null);
    }

    @Test
    @DisplayName("crearLibro con visible null retorna null")
    void crearLibro_visibleNull_retornaNull() {
        libroDtoMock.setVisible(null);

        Libro result = service.crearLibro(libroDtoMock);

        assertNull(result);
        verify(repository, never()).save(any());
    }

    // =========================================================
    // getLibro — 2 tests
    // JUnit: asserts del resultado
    // Mockito: simula repository.getById()
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
        // Given — getById devuelve null cuando no encuentra
        when(repository.getById(99L)).thenReturn(null);

        // When
        Libro result = service.getLibro("99");

        // Then
        assertNull(result);
        verify(repository, times(1)).getById(99L);
    }

    // =========================================================
    // eliminarLibro — 2 tests
    // JUnit: verifica el boolean retornado
    // Mockito: simula getById() y verifica que delete() se llamo o no
    // =========================================================

    @Test
    @DisplayName("eliminarLibro con ID existente elimina el libro y retorna true")
    void eliminarLibro_idExiste_retornaTrue() {
        // Given
        when(repository.getById(1L)).thenReturn(libroMock);
        doNothing().when(repository).delete(libroMock);

        // When
        Boolean result = service.eliminarLibro("1");

        // Then
        assertTrue(result);
        verify(repository, times(1)).getById(1L);
        verify(repository, times(1)).delete(libroMock); // debe haberse llamado
    }

    @Test
    @DisplayName("eliminarLibro con ID inexistente retorna false sin llamar delete")
    void eliminarLibro_idNoExiste_retornaFalse() {
        // Given
        when(repository.getById(99L)).thenReturn(null);

        // When
        Boolean result = service.eliminarLibro("99");

        // Then
        assertFalse(result);
        verify(repository, times(1)).getById(99L);
        verify(repository, never()).delete(any()); // nunca debe llamar delete
    }

    // =========================================================
    // crearLibro — 8 tests
    // JUnit: verifica el libro retornado o null
    // Mockito: simula repository.save() y verifica que se llamo o no
    // =========================================================

    @Test
    @DisplayName("crearLibro con todos los campos validos crea y retorna el libro")
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
        // When — pasamos null directamente
        Libro result = service.crearLibro(null);

        // Then — el service debe retornar null sin llamar al repositorio
        assertNull(result);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("crearLibro con titulo vacio retorna null")
    void crearLibro_tituloVacio_retornaNull() {
        // Given — titulo con solo espacios
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

    @Test
    @DisplayName("crearLibro con editorial vacia retorna null")
    void crearLibro_editorialVacia_retornaNull() {
        // Given
        libroDtoMock.setEditorial("   ");

        // When
        Libro result = service.crearLibro(libroDtoMock);

        // Then
        assertNull(result);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("crearLibro con genero vacio retorna null")
    void crearLibro_generoVacio_retornaNull() {
        // Given
        libroDtoMock.setGenero("");

        // When
        Libro result = service.crearLibro(libroDtoMock);

        // Then
        assertNull(result);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("crearLibro con precio null retorna null")
    void crearLibro_precioNull_retornaNull() {
        // Given
        libroDtoMock.setPrecio(null);

        // When
        Libro result = service.crearLibro(libroDtoMock);

        // Then
        assertNull(result);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("crearLibro con stock null retorna null")
    void crearLibro_stockNull_retornaNull() {
        // Given
        libroDtoMock.setStock(null);

        // When
        Libro result = service.crearLibro(libroDtoMock);

        // Then
        assertNull(result);
        verify(repository, never()).save(any());
    }

    // =========================================================
    // actualizarLibro PATCH — 3 tests
    // JUnit: verifica el resultado
    // Mockito: simula repository.getById(), objectMapper (JSON processing)
    // Este es el metodo mas complejo porque usa ObjectMapper
    // =========================================================

    @Test
    @DisplayName("actualizarLibro PATCH con ID existente y JSON valido retorna libro actualizado")
    void actualizarLibro_PATCH_idExiste_retornaLibroActualizado() throws Exception {
        // Usamos ObjectMapper real porque JsonMergePatch.fromJson es estatico
        // y Mockito no puede mockearlo directamente
        ObjectMapper realMapper = new ObjectMapper();
        LibrosServiceImpl serviceReal = new LibrosServiceImpl(repository, realMapper);

        String jsonPatch = "{\"titulo\": \"Nuevo Titulo\"}";
        when(repository.getById(1L)).thenReturn(libroMock);
        when(repository.save(any(Libro.class))).thenReturn(libroMock);

        // When
        Libro result = serviceReal.actualizarLibro("1", jsonPatch);

        // Then
        assertNotNull(result);
        verify(repository, times(1)).getById(1L);
        verify(repository, times(1)).save(any(Libro.class));
    }

    @Test
    @DisplayName("actualizarLibro PATCH con ID inexistente retorna null")
    void actualizarLibro_PATCH_idNoExiste_retornaNull() {
        // Given
        when(repository.getById(99L)).thenReturn(null);

        // When
        Libro result = service.actualizarLibro("99", "{\"titulo\": \"Nuevo Titulo\"}");

        // Then — si no existe el libro retorna null sin procesar JSON
        assertNull(result);
        verify(repository, times(1)).getById(99L);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("actualizarLibro PATCH con JSON invalido retorna null")
    void actualizarLibro_PATCH_jsonInvalido_retornaNull() throws Exception {
        // Given — el libro existe pero el objectMapper lanza excepcion
        when(repository.getById(1L)).thenReturn(libroMock);
        when(objectMapper.readTree(anyString())).thenThrow(new JsonProcessingException("JSON invalido") {});

        // When
        Libro result = service.actualizarLibro("1", "json-invalido");

        // Then — cuando falla el JSON el service captura la excepcion y retorna null
        assertNull(result);
        verify(repository, never()).save(any());
    }

    // =========================================================
    // actualizarLibro PUT — 2 tests
    // JUnit: verifica el resultado
    // Mockito: simula repository.getById() y repository.save()
    // No usa ObjectMapper — es mas simple que el PATCH
    // =========================================================

    @Test
    @DisplayName("actualizarLibro PUT con ID existente actualiza y retorna el libro")
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
    @DisplayName("actualizarLibro PUT con ID inexistente retorna null sin guardar")
    void actualizarLibro_PUT_idNoExiste_retornaNull() {
        // Given
        when(repository.getById(99L)).thenReturn(null);

        // When
        Libro result = service.actualizarLibro("99", libroDtoMock);

        // Then
        assertNull(result);
        verify(repository, times(1)).getById(99L);
        verify(repository, never()).save(any()); // nunca debe guardar
    }
}