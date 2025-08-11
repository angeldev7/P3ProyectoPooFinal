package builder;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import model.Reserva;
import java.util.Date;

public class ReservaBuilderAdditionalTest {

    @Test
    void setIdYFechaSalidaQuedanEnResultado(){
        Date ingreso = new Date();
        Date salida = new Date(ingreso.getTime()+3600000);
        String customId = "RES-CUSTOM";
        Reserva r = new ReservaBuilder()
                .setId(customId)
                .setCliente("CLI-99")
                .setHabitacion("HAB-99")
                .setFechaIngreso(ingreso)
                .setFechaSalida(salida)
                .setTotal(10.5)
                .setObservaciones("Obs")
                .build();
        assertEquals(customId, r.getId());
        assertEquals(salida, r.getFechaSalida());
    }

    @Test
    void faltaHabitacionLanzaExcepcion(){
        ReservaBuilder b = new ReservaBuilder()
                .setCliente("CLI-100")
                .setTotal(5.0);
        assertThrows(IllegalStateException.class, b::build);
    }

    @Test
    void fechaIngresoNullLanzaExcepcion(){
        ReservaBuilder b = new ReservaBuilder()
                .setCliente("CLI-101")
                .setHabitacion("HAB-101")
                .setFechaIngreso(null);
        assertThrows(IllegalStateException.class, b::build);
    }
}
