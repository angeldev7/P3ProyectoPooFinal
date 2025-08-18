# Manual de Usuario

## 1. Introducción
Este sistema de gestión hotelera (proyecto académico) permite administrar clientes, habitaciones, reservas (planificadas y check‑in inmediato), servicios a la habitación, reportes y un historial de acciones con Undo/Redo mediante patrones de diseño (Command, Memento, Builder, Singleton y una fachada de servicios). 

La aplicación está desarrollada en Java (Swing) con persistencia en MongoDB. Ofrece dos perfiles operativos principales (Admin y Recepcionista) con acceso a las mismas funciones centrales, aunque el Admin suele encargarse de tareas de mantenimiento (migración de IDs, inicialización, análisis de métricas).

## 2. Requisitos del Sistema
- Java JDK 17 o superior en el PATH (`java -version`).
- Maven 3.8+ (`mvn -v`).
- MongoDB en ejecución (local por defecto `mongodb://localhost:27017`).
- 4 GB de RAM libre recomendada.
- Windows, Linux o macOS.

## 3. Instalación / Preparación
1. Clonar el repositorio.
2. Verificar que el servicio de MongoDB esté levantado.
3. Ejecutar la compilación: `mvn clean package`.
4. El artefacto quedará en `target/P3ProyectoPooFinal-1.0-SNAPSHOT.jar`.
5. (Opcional) Configurar variables de entorno para URL de Mongo si difiere del local.

## 4. Ejecución
Desde la carpeta del proyecto:
```
java -jar target/P3ProyectoPooFinal-1.0-SNAPSHOT.jar
```
Al iniciar aparece la ventana de selección / principal desde la cual se navega a la ventana administrativa (dashboard) y a los paneles de datos.

## 5. Conceptos Clave
| Concepto | Descripción | Estado / Campos Clave |
|----------|-------------|------------------------|
| Reserva Planificada | Creada con fechas planificadas sin ocupar la habitación aún. | `fechaInicioPlanificada`, `fechaFinPlanificada`, `fechaIngreso = null` |
| Reserva Activa (Check-in) | Huésped ya ingresó, habitación ocupada. | `fechaIngreso != null`, `habitacion.ocupada = true` |
| Servicios a Habitación | Ítems (COMIDA, BEBIDA, LIMPIEZA, etc.) agregables mientras la reserva está activa. | Asociados a la reserva; suman costo. |
| Undo / Redo | Reversión y repetición de comandos que modifican el modelo (via Memento + Command). | Historial en memoria. |
| Migración IDs | Conversión a IDs legibles (CLI0001, RES0001, SRV0001). | Operación administrativa. |

## 6. Roles y Acceso
- Recepcionista: Operaciones diarias (planificar reservas, check-in rápido, finalizar, anular, agregar servicios, consultar disponibilidad, reportes básicos, undo/redo).
- Admin: Todo lo anterior + mantenimiento (migrar IDs, inicializar habitaciones, auditoría de métricas, ajustes de planificación global).

## 7. Navegación de la Interfaz
Panel / Ventana | Función Principal
----------------|-----------------
VentanaSelector | Entrada / elección de flujo (si aplica).
VentanaPrincipal | Menú de acceso a módulos.
VentanaAdmin | Dashboard, atajos a paneles, botones Undo/Redo, métricas.
PanelClientes | Alta, edición, eliminación, búsqueda.
PanelHabitaciones | Vista estado (libre / ocupada), acción Check-in rápido, refresco disponibilidad.
PanelReservas | Listado, filtros, creación planificada, ajustes de fechas, anulación y finalización.
PanelServicios | Listado servicios de la reserva seleccionada y adición de nuevos.
PanelReportes | Reportes diarios, métricas de ocupación, ingresos estimados.

## 8. Flujos Operativos Esenciales
### 8.1 Planificar una Reserva
1. Ir a PanelReservas > "Nueva Reserva".
2. Seleccionar cliente (o crearlo). 
3. Elegir habitación disponible (no se marcará ocupada aún). 
4. Indicar fechas planificadas (inicio/fin). 
5. Guardar: la reserva queda en estado planificada (sin check-in). 

Validaciones: Fechas coherentes (fin >= inicio), habitación existente, cliente válido.

### 8.2 Check-in Rápido
1. Ir a PanelHabitaciones o PanelReservas.
2. Seleccionar habitación libre y usar "Check-in Rápido".
3. Se genera de inmediato una reserva activa (`fechaIngreso` = ahora) y la habitación pasa a ocupada.

### 8.3 Check-in de Reserva Planificada
(En caso de existir un flujo manual) Seleccionar reserva planificada y presionar "Convertir a Check-in" (si la UI lo expone) o usar la acción definida para activarla. Internamente se asigna `fechaIngreso` y se marca ocupada la habitación.

### 8.4 Agregar Servicios a la Habitación
1. Con una reserva activa seleccionada, abrir PanelServicios.
2. Pulsar "Agregar" y elegir tipo (COMIDA, BEBIDA, LIMPIEZA, etc.).
3. Confirmar: se agrega el registro y el costo se refleja en el total estimado.

Restricción: No se permite agregar servicios a reservas sólo planificadas o finalizadas.

### 8.5 Finalizar Reserva (Check-out)
1. Seleccionar reserva activa.
2. Pulsar "Finalizar".
3. El sistema calcula el total final (noches * tarifa + servicios) y libera la habitación.

### 8.6 Anular Reserva
- Sólo para planificadas (o eventualmente activas antes de ingresar servicios, según reglas). Libera cualquier bloqueo lógico (si existiera) y la descarta del listado activo.

### 8.7 Ajustar Planificación
1. Seleccionar reserva planificada.
2. Editar fechas (inicio/fin) y guardar.
3. Validación de no solapamiento según disponibilidad.

### 8.8 Undo / Redo
- Botones disponibles en la VentanaAdmin. 
- Operaciones soportadas: crear reserva, check-in rápido, finalizar, anular, agregar servicio, ajustes de planificación.
- Undo restaura un snapshot completo del modelo (clientes, habitaciones, reservas, servicios) previo al comando.
- Redo vuelve a aplicar el comando desapilado.

### 8.9 Migrar IDs Legibles
- Acción Admin para convertir IDs internos a formato legible incremental. Ejecutar preferentemente en un entorno sin operaciones concurrentes para evitar confusión.

### 8.10 Inicializar Habitaciones
- Genera o resetea el catálogo base de habitaciones (ej. HAB101, HAB102...). Úsese sólo una vez al arrancar o para reset controlado.

## 9. Reportes / Dashboard
PanelReportes muestra métricas como:
- Ocupación actual (% habitaciones ocupadas).
- Ingresos estimados de reservas activas.
- Servicios consumidos (conteo por tipo).
- Historial de comandos aplicados (si se expone un log resumido).

## 10. Estados de Reserva y Reglas
Estado | Descripción | Transiciones
-------|-------------|-------------
Planificada | Creada, fechas futuras, habitación aún libre | -> Activa (Check-in) / -> Anulada
Activa | Huésped alojado, fechaIngreso asignada | -> Finalizada (Check-out)
Finalizada | Cerrada, costos calculados | (sin transiciones)
Anulada | Cancelada antes del check-in | (sin transiciones)

Regla crítica: Nunca mostrar una reserva en estado Activa (o Check-in) con `fechaIngreso` futura. El check-in asigna el momento real.

## 11. Validaciones Comunes
- Fechas: inicio <= fin; noches > 0.
- Disponibilidad: habitación no ocupada para planificar en las mismas fechas (ver lógica de solapamiento).
- Servicios: sólo sobre reserva activa. 
- Undo/Redo: si no hay historial, los botones se deshabilitan.

## 12. Mensajes de Error Frecuentes (Resumen)
Mensaje | Causa | Acción Sugerida
--------|-------|----------------
"Habitación no disponible" | Solapamiento o ya ocupada | Seleccionar otra o ajustar fechas.
"Reserva no activa" | Intento de agregar servicio a planificada/finalizada | Hacer check-in primero.
"No hay acción para deshacer" | Historial vacío | Ejecutar una operación válida y reintentar.
"Fechas inválidas" | Rango invertido o nulo | Corregir inicio/fin.

## 13. Buenas Prácticas de Operación
- Planificar reservas para futuros ingresos en vez de usar check-in rápido si la llegada es posterior.
- Aplicar migración de IDs sólo cuando el sistema esté estable (poca actividad en vivo).
- Usar Undo para revertir errores inmediatos; para correcciones posteriores preferir edición / ajustes.
- Revisar métricas diariamente para detectar anomalías (reservas sin finalizar, servicios excesivos, etc.).

## 14. Seguridad y Consistencia
- Evitar cierre forzado de la aplicación durante operaciones de escritura.
- Mantener respaldos periódicos de la base Mongo (dump) si se usa con datos reales.

## 15. FAQ
P: ¿Por qué no puedo agregar servicios?  
R: Asegúrate que la reserva esté activa (check-in completado).  

P: Undo no hace nada.  
R: Verifica que hayas ejecutado primero una operación soportada (crear, check-in, finalizar, etc.).  

P: Se creó una reserva activa con fecha futura.  
R: Corrige mediante Undo o ajusta el flujo: el check-in debe hacerse en el momento real de ingreso.  

P: ¿Cómo recalculo los IDs?  
R: Usa la función de migración (Admin). Haz copia de seguridad antes si son datos importantes.  

## 16. Solución de Problemas
Problema | Diagnóstico | Solución
---------|-------------|---------
La app no abre | Falta Java o jar corrupto | Reinstalar JDK / recompilar.
Error conexión Mongo | Servicio caído o puerto distinto | Iniciar Mongo o ajustar string de conexión.
Undo se salta cambios | Comandos no generan snapshot | Verificar que operaciones sean de las soportadas.
Servicios no suman al total | Falta actualización vista | Refrescar panel / revisar lógica cálculo (reportar si persiste).

## 17. Extensiones Futuras (Ideas)
- Roles con permisos diferenciados reales.
- Exportación PDF/Excel de reportes.
- Integración pasarela de pago.
- Multi-idioma UI.

## 18. Créditos
Proyecto académico de POO con patrones de diseño y enfoque en mantenibilidad, pruebas (JUnit + JaCoCo) y documentación (PlantUML).

---
**Fin del Manual**
