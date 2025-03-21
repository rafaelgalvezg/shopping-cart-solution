# Shopping Cart Solution

Bienvenido al proyecto **Shopping Cart Solution**, una solución que simula un sistema de carrito de compras distribuido usando Java, Spring Boot y contenedores. Este proyecto está dividido en tres submódulos que actúan como microservicios: `orders-api`, `payment-api` y `products-api`. Utiliza PostgreSQL para persistencia, Docker con Docker Compose para orquestación, y un proxy para integrar la API externa Fake Store API, destacando buenas prácticas, principios SOLID, patrones de diseño y atributos de calidad como mantenibilidad, escalabilidad y testabilidad.

## Tabla de Contenidos
1. [Descripción del Proyecto](#descripción-del-proyecto)
2. [Arquitectura](#arquitectura)
3. [Tecnologías Utilizadas](#tecnologías-utilizadas)
4. [Buenas Prácticas y Principios](#buenas-prácticas-y-principios)
5. [Instalación y Ejecución](#instalación-y-ejecución)
6. [Uso](#uso)
7. [Tests](#tests)
8. [Atributos de Calidad](#atributos-de-calidad)
9. [Seguridad](#seguridad)

---

## Descripción del Proyecto
**Shopping Cart Solution** simula un sistema de carrito de compras con:
- **orders-api**: Gestiona órdenes (creación, actualización, eliminación, consulta) y clientes, integrándose con `products-api` y `payment-api`.
- **payment-api**: Procesa pagos asociados a órdenes, con simulación de éxito/fallo.
- **products-api**: Provee información de productos mediante un proxy (`FakeStoreApiProxy`) que consulta la Fake Store API.

---

## Arquitectura
La solución sigue una arquitectura de microservicios con tres submódulos independientes:
- **`orders-api`**: Maneja órdenes y clientes, consumiendo `products-api` y `payment-api`.
- **`payment-api`**: Procesa pagos de manera autónoma.
- **`products-api`**: Actúa como intermediario entre el sistema y la Fake Store API, usando `FakeStoreApiProxy`.

### Componentes Clave
- **Base de Datos**: PostgreSQL para persistencia de órdenes, clientes y pagos.
- **Contenedores**: Docker para encapsular servicios y la base de datos.
- **Orquestación**: Docker Compose para gestionar dependencias.
- **Patrón Proxy**: `FakeStoreApiProxy` en `products-api` como capa de abstracción para la Fake Store API.
- **Exception Handling**: Controladores de excepciones centralizados (`@ExceptionHandler`).

### Estructura del Código
- **Paquetes**: Organización por capas (`service`, `repository`, `entity`, `dto`, `exception`, `proxy`).
- **Persistencia**: Mapeo ORM con Spring Data JPA.

---

## Tecnologías Utilizadas
- **Java 17**: Lenguaje principal para rendimiento moderno.
- **Spring Boot 3**: Framework para microservicios, con soporte REST y transacciones.
- **Lombok**: Reducción de código boilerplate.
- **Spring Data JPA**: Persistencia con repositorios y PostgreSQL.
- **PostgreSQL**: Base de datos relacional robusta.
- **Docker**: Contenerización de servicios y base de datos.
- **Docker Compose**: Orquestación de múltiples contenedores.
- **RestTemplate**: Cliente HTTP para el proxy `FakeStoreApiProxy`.
- **JUnit 5**: Framework de pruebas unitarias.
- **Mockito**: Mocking de dependencias.
- **AssertJ**: Aserciones legibles y expresivas.
- **Gradle**: Gestión de dependencias y construcción.

---

## Buenas Prácticas y Principios
Este proyecto incorpora:

### **Principios SOLID**
- **S (Single Responsibility)**: Cada clase tiene una única responsabilidad (e.g., `FakeStoreApiProxy` solo maneja comunicación con Fake Store API).
- **O (Open/Closed)**: Servicios extensibles mediante interfaces sin modificar código existente.
- **L (Liskov Substitution)**: Interfaces (`OrderService`, `ClientService`, `PaymentService`) para substitución.
- **I (Interface Segregation)**: Interfaces específicas y pequeñas por servicio.
- **D (Dependency Inversion)**: Dependencias inyectadas con `@RequiredArgsConstructor`.

### **Patrones de Diseño**
- **Factory**: `OrderTestFactory`, `ClientTestFactory`, `PaymentTestFactory` para datos de prueba.
- **DTO**: Separación entre entidades y objetos de transferencia.
- **Repository**: Abstracción de acceso a datos con JPA.
- **Proxy**: `FakeStoreApiProxy` como capa de abstracción para la Fake Store API.

### **Buenas Prácticas**
- **Código Limpio**: Nombres descriptivos, estructura modular.
- **Logging**: `@Slf4j` para trazabilidad detallada.
- **Gestión de Excepciones**: Excepciones personalizadas (`ExternalServiceException`, `ResourceNotFoundException`) manejadas con `@ExceptionHandler`.
- **Contenerización**: Docker para aislamiento y portabilidad.

---

## Instalación y Ejecución
### Prerrequisitos
- Docker y Docker Compose instalados.
- Conexión a internet (para `FakeStoreApiProxy`).
- Opcional: Git para clonar el repositorio.

### Pasos
1. Clona el repositorio:
   ```bash
   git clone https://github.com/[tu-usuario]/shopping-cart-solution.git
   ```
2. Navega a la raíz del proyecto:
   ```bash
   cd shopping-cart-solution
   ```
3. Levanta los servicios con Docker Compose:
   ```bash
   docker-compose up --build
   ```
   - El flag `--build` asegura que las imágenes se construyan desde cero.
   - Esto inicia `orders-api` (puerto 8080), `payment-api` (puerto 8083), `products-api` (puerto 8081) y PostgreSQL (puerto 5432).

### Verificación
- Comprueba que los servicios estén corriendo:
  ```bash
  docker ps
  ```
- Para detener los servicios:
  ```bash
  docker-compose down
  ```

---

## Uso
Una vez levantados los servicios con `docker-compose up --build`, puedes interactuar con los microservicios a través de sus endpoints REST:

- **orders-api** (http://localhost:8080):
  - `POST /orders`: Crear una orden.
  - `PUT /orders/{id}`: Actualizar una orden.
  - `POST /orders/{id}/payment`: Procesar pago.
  - `GET /orders/{id}`: Consultar una orden.
  - `GET /orders`: Listar todas las órdenes.
  - `DELETE /orders/{id}`: Eliminar una orden.
- **payment-api** (http://localhost:8083):
  - `POST /payments`: Procesar un pago.
  - `GET /payments`: Listar todos los pagos.
- **products-api** (http://localhost:8081):
  - `GET /products/{id}`: Consultar un producto desde Fake Store API.
  - `GET /products`: Listar todos los productos desde Fake Store API.

### Ejemplo de Solicitud
- Crear una orden:
  ```bash
  curl -X POST http://localhost:8080/orders -H "Content-Type: application/json" -d '{"clientName":"Rafael Galvez","clientEmail":"rafael@example.com","details":[{"productId":1,"quantity":2}]}'
  ```

---

## Tests
Los tests unitarios garantizan robustez:
- **Cobertura**: Métodos principales con happy path, casos límite y excepciones.
- **Factories**: Datos consistentes con factories dedicados.
- **Herramientas**: JUnit 5, Mockito, AssertJ, ReflectionTestUtils.
- **Ejecución** (sin Docker):
  ```bash
  cd orders-api
  ./gradlew test
  cd ../payment-api
  ./gradlew test
  cd ../products-api
  ./gradlew test
  ```

---

## Atributos de Calidad
- **Mantenibilidad**: Código modular, tests y documentación.
- **Escalabilidad**: Microservicios y Docker para crecimiento independiente.
- **Testabilidad**: Diseño orientado a pruebas.
- **Portabilidad**: Contenedores Docker aseguran consistencia.
- **Robustez**: Manejo de errores con `@ExceptionHandler` y proxy.

---
