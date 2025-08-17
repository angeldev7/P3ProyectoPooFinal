# Manual de Usuario - Sistema de Gestión Hotelera

## 1. Introducción
Aplicación de escritorio (Java Swing + MongoDB) para administrar clientes, habitaciones y reservas. Integra patrones: MVC, Command (undo/redo), Memento (snapshots), Builder (Reserva), Singleton (GestorDisponibilidad).

## 2. Requisitos
- Java 17+
- MongoDB (localhost:27017 por defecto)
- Maven 3.8+

## 3. Ejecución Rápida
1. `mvn -q clean package`
2. `java -jar target/P3ProyectoPooFinal-1.0-SNAPSHOT.jar`
La primera ejecución crea habitaciones (HAB-001..HAB-020) y puede migrar IDs de clientes a formato legible.

## 4. Principales Funcionalidades
| Módulo | Función | Descripción |
|-------|---------|-------------|
| Clientes | Alta / Edición / Eliminación | CRUD con validaciones de cédula y teléfono únicos |
| Clientes | Búsqueda | Por cédula exacta, limpia con botón "Limpiar" |
| Habitaciones | Visualización | Estado (disponible / ocupada), tipo, precio |
| Reservas | Crear | Genera ID RES-#### y ocupa habitación |
| Reservas | Finalizar | Marca fecha salida y libera habitación |
| Reservas | Anular | Elimina reserva activa y libera habitación |
| Check-in rápido | Admin | Crear y confirmar de forma simplificada |
| Undo/Redo | Historial | Revierte / rehace comandos soportados |

## 5. Flujo de Uso Típico
1. Registrar cliente (ID autogenerado CLI-0001...).
2. Crear reserva seleccionando habitación disponible.
3. Finalizar o anular según corresponda.
4. Usar Undo si se cometió un error; Redo para rehacer.

## 6. Identificadores
- Cliente: `CLI-0001` incremental (migración automática de IDs antiguos).
- Reserva: `RES-0001` incremental (fallback UUID parcial si error).
- Habitación: `HAB-###` fijo (creado al inicializar).

## 7. Validaciones Clave
- Cédula / Teléfono duplicados: rechazo de registro.
- Habitación ocupada: bloquea nueva reserva.
- Datos obligatorios vacíos: no se procesa.
- Botones de acción deshabilitados sin selección.

## 8. Undo / Redo (Patrón Command + Memento)
- Acciones que generan snapshot antes de ejecutarse.
- Undo elimina/retrocede y restaura estado previo completo (clientes, habitaciones, reservas).
- Redo reaplica comando original si no se ejecutó uno nuevo tras un Undo.

## 9. Builder de Reserva
ReservaBuilder crea objetos Reserva consistentes (precio, fechas) antes de persistir, aislando reglas de construcción.

## 10. Singleton GestorDisponibilidad
Mantiene lista en memoria de habitaciones libres para búsquedas rápidas sin recorrer todas las colecciones.

## 11. Migración de IDs Antiguos
Si existían clientes con IDs UUID, llamar una vez: `modelo.migrarIdsClientesLegibles();` tras instanciar `ModeloServiceImpl`.

## 12. Pruebas y Cobertura
- Ejecutar: `mvn test`
- Uso de FakeModeloService para pruebas rápidas.
- Cobertura JaCoCo >80% en lógica (UI excluida).

## 13. Resolución de Problemas
| Problema | Causa | Solución |
|----------|-------|----------|
| No crea cliente | Cédula existente | Verificar y cambiar cédula |
| Reserva rechazada | Habitación ocupada | Seleccionar otra o finalizar existente |
| Undo inactivo | Sin historial | Ejecutar una acción válida primero |
| Redo inactivo | No hay undo previo | Usar Undo antes |
| IDs no legibles | Migración no ejecutada | Llamar método de migración |

## 14. Sugerencias Futuras
- Roles y autenticación
- Reportes PDF / CSV
- Internacionalización completa
- Persistir historial undo/redo
- Logging estructurado (SLF4J)

## 15. Desinstalación
Eliminar carpeta del proyecto y (opcional) la base de datos Mongo usada.

---
Fin del manual.
