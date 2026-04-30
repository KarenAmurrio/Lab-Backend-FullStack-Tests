# Lab-Backend-FullStack — Pruebas Unitarias

Repositorio de pruebas unitarias para el ecosistema de microservicios **Lab-Backend-FullStack**, desarrollado como parte de la materia **Prueba y Mantenimiento de Software (COM470)**.

---

## 👥 Equipo

| Integrante | Rol | Módulo |
|---|---|---|
| Karen Amurrio Huaygua | Scrum Master / Tester | MsBooksCatalogue — Service |
| Susana Manjon Blanco | Developer / Tester | MsBooksCatalogue — Controller |
| Max Rodas Palacios | Developer / Tester | MsBookPayments — Service, Controller, Model |

---

## 📁 Estructura del proyecto

Los tests se encuentran dentro de la carpeta `src/test/java` de cada módulo:

```
Lab-Backend-FullStack/
├── ms-books-catalogue-main/
│   └── src/test/java/com/unir/ms_books_catalogue/
│       ├── controller/
│       │   └── LibrosControllerTest.java       (Susana — 14 tests)
│       └── service/
│           └── LibrosServiceImplTest.java      (Karen — 27 tests)
│
└── MsBookPayments/
    └── src/test/java/com/g5/relpapel/msbookpayments/
        ├── controller/
        │   └── CompraControllerTest.java       (Max — 2 tests)
        ├── model/
        │   └── ItemTest.java                   (Max — 3 tests)
        └── service/
            └── BuscadorServiceTest.java        (Max — 13 tests)
```

---

## 🧪 Resumen de pruebas

| Clase | Integrante | Tests | Cobertura |
|---|---|---|---|
| LibrosServiceImpl | Karen | 27 | 100% |
| LibrosController | Susana | 14 | 100% |
| BuscadorService | Max | 13 | 100% |
| CompraController | Max | 2 | 100% |
| Item | Max | 3 | 100% |
| **TOTAL** | | **59** | **100%** |

---

## 🛠️ Herramientas utilizadas

- **JUnit 5** — estructura y ejecución de tests
- **Mockito** — mocks de dependencias (Repository, RestTemplate, ObjectMapper)
- **MockMvc** — tests de controllers REST sin servidor real
- **Spring Boot Test 3.4.x** — `@WebMvcTest`, `@ExtendWith`
- **ReflectionTestUtils** — inyección de valores `@Value` en tests
- **IntelliJ Coverage** — medición de cobertura

---

## ▶️ Cómo ejecutar los tests

### Desde IntelliJ IDEA

1. Abrir el módulo que quieres testear (`ms-books-catalogue-main` o `MsBookPayments`)
2. Click derecho sobre la carpeta `test`
3. Selecciona **"Run 'All Tests'"** o **"Run with Coverage"**


## 🚀 Cómo levantar el ecosistema completo

Levantar los servicios en este orden:

```bash
# 1. Servidor de registro
cd EurekaServerApp
mvn spring-boot:run

# 2. Microservicio de libros
cd ms-books-catalogue-main
mvn spring-boot:run

# 3. Microservicio de pagos
cd MsBookPayments
mvn spring-boot:run

# 4. Gateway proxy
cd CloudGatewayProxy
mvn spring-boot:run
```

Verificar que todo está corriendo:
- **Eureka panel:** http://localhost:8761
- **Swagger Catalogue:** http://localhost:8080/swagger-ui/index.html
- **Gateway routes:** http://localhost:8181/actuator/gateway/routes

---

## 📋 Módulos del ecosistema

| Módulo | Puerto | Descripción | Tests      |
|---|--------|---|------------|
| EurekaServerApp | 8761   | Servidor de registro de servicios | No aplica  |
| ms-books-catalogue | 8080   | CRUD de libros con búsqueda por filtros | ✅ 41 tests |
| MsBookPayments | 7171   | Registro de compras y validación de stock | ✅ 18 tests |
| CloudGatewayProxy | 8181   | Proxy inverso y enrutamiento | No aplica  |


---

## 📄 Documentación

El plan de pruebas completo se encuentra en el archivo `PlanDePruebas_Lab_Backend_FullStack.docx` en la raíz del repositorio. Incluye:

- Objetivo y alcance de las pruebas
- Definición y justificación de módulos seleccionados
- 59 casos de prueba detallados
- Estrategia de testing con JUnit y Mockito
- Resultados reales de cobertura
- Problemas encontrados y soluciones aplicadas
- Organización Scrum del equipo