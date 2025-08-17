package model;

import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para {@link ServicioHabitacion} con el objetivo de cubrir
 * todas las ramas de lógica (factories, validaciones, switch de costos,
 * serialización a Document y setters que fuerzan reglas de negocio).
 */
public class ServicioHabitacionTest {

    @Test
    @DisplayName("Factory COMIDA establece tipo, costo fijo y copia lista de especias")
    void crearComida_ok() {
        List<String> especias = Arrays.asList("pimienta", "oregano", "\t ", "");
        ServicioHabitacion s = ServicioHabitacion.crearComida("RES-1", "HAB-1", "Almuerzo", especias);

        assertNull(s.getId(), "ID inicial debe ser null hasta que el servicio la asigne");
        assertEquals(ServicioHabitacion.TIPO_COMIDA, s.getTipo());
        assertEquals(15.0, s.getCosto());
        // Debe copiar, no ser la misma referencia
        assertNotSame(especias, s.getEspecias());
        assertTrue(s.getEspecias().contains("pimienta"));
    }

    @Test
    @DisplayName("Factory BEBIDA establece costo y tipo correctos sin especias")
    void crearBebida_ok() {
        ServicioHabitacion s = ServicioHabitacion.crearBebida("RES-2", "HAB-2", "Gaseosa grande");
        assertEquals(ServicioHabitacion.TIPO_BEBIDA, s.getTipo());
        assertEquals(5.0, s.getCosto());
        assertNotNull(s.getEspecias());
        assertTrue(s.getEspecias().isEmpty());
    }

    @Test
    @DisplayName("Factory LIMPIEZA usa descripción por defecto y costo fijo")
    void crearLimpieza_ok() {
        ServicioHabitacion s = ServicioHabitacion.crearLimpieza("RES-3", "HAB-3");
        assertEquals(ServicioHabitacion.TIPO_LIMPIEZA, s.getTipo());
        assertEquals(10.0, s.getCosto());
        assertEquals("Servicio de limpieza", s.getDescripcion());
    }

    @Test
    @DisplayName("costoPorTipo cubre los tres tipos y default")
    void costoPorTipo_switch() {
        assertEquals(15.0, ServicioHabitacion.costoPorTipo("COMIDA"));
        assertEquals(5.0, ServicioHabitacion.costoPorTipo("bebida")); // case insensitive
        assertEquals(10.0, ServicioHabitacion.costoPorTipo("limpieza"));
        assertEquals(0.0, ServicioHabitacion.costoPorTipo("otro"));
        assertEquals(0.0, ServicioHabitacion.costoPorTipo(null));
    }

    @Test
    @DisplayName("tipoValido retorna true solo para tipos soportados")
    void tipoValido_varios() {
        assertTrue(ServicioHabitacion.tipoValido("comida"));
        assertTrue(ServicioHabitacion.tipoValido("BEBIDA"));
        assertTrue(ServicioHabitacion.tipoValido("LIMPIEZA"));
        assertFalse(ServicioHabitacion.tipoValido(""));
        assertFalse(ServicioHabitacion.tipoValido("x"));
        assertFalse(ServicioHabitacion.tipoValido(null));
    }

    @Test
    @DisplayName("setCosto ignora valor externo y mantiene costo por tipo")
    void setCosto_ignoraExterno() {
        ServicioHabitacion s = ServicioHabitacion.crearBebida("RES-9", "HAB-9", null);
        assertEquals(5.0, s.getCosto());
        s.setCosto(12345.0); // Debe recalcular según tipo (BEBIDA)
        assertEquals(5.0, s.getCosto());
        // Cambiamos tipo y forzamos nuevamente costo
        s.setTipo(ServicioHabitacion.TIPO_COMIDA);
        s.setCosto(0.0);
        assertEquals(15.0, s.getCosto());
    }

    @Test
    @DisplayName("Serialización a Document y fromDocument mantiene datos clave")
    void document_roundtrip() {
        ServicioHabitacion original = ServicioHabitacion.crearComida("RES-7", "HAB-7", "Cena", Collections.singletonList("sal"));
        original.setId("SRV-001");
        Document d = original.toDocument();
        assertEquals("SRV-001", d.getString("_id"));
        assertEquals("COMIDA", d.getString("tipo"));
        assertEquals(15.0, d.getDouble("costo"));
        assertEquals(1, ((List<?>)d.get("especias")).size());

        ServicioHabitacion copia = ServicioHabitacion.fromDocument(d);
        assertEquals(original.getId(), copia.getId());
        assertEquals(original.getTipo(), copia.getTipo());
        // El constructor vuelve a calcular el costo por tipo, debe ser el mismo
        assertEquals(15.0, copia.getCosto());
        assertEquals("sal", copia.getEspecias().get(0));
    }

    @Test
    @DisplayName("Setters básicos asignan valores y setCosto sigue regla de tipo")
    void setters_basicos() {
        ServicioHabitacion s = new ServicioHabitacion();
        s.setId("SRV-X");
        s.setIdReserva("RES-X");
        s.setIdHabitacion("HAB-X");
        s.setTipo(ServicioHabitacion.TIPO_LIMPIEZA);
        s.setDescripcion("Desc");
        s.setEspecias(Arrays.asList("a","b"));
        s.setCosto(999); // debe forzar a costoPorTipo(LIMPIEZA)=10.0
        assertEquals("SRV-X", s.getId());
        assertEquals("RES-X", s.getIdReserva());
        assertEquals("HAB-X", s.getIdHabitacion());
        assertEquals("Desc", s.getDescripcion());
        assertEquals(2, s.getEspecias().size());
        assertEquals(10.0, s.getCosto());
    }
}
