package command;

import org.junit.jupiter.api.Test;
import model.*;
import memento.ModeloMemento;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class CrearReservaCommandFailureTest {

    // Servicio minimal que fuerza fallo al crearReserva
    static class FailingModeloService implements IModeloService {
        @Override public boolean crearReserva(Reserva r){ return false; }
        // Métodos no usados en este test (stubs simples)
        @Override public boolean registrarCliente(Cliente c){ return true; }
        @Override public boolean eliminarCliente(String id){ return false; }
        @Override public Cliente buscarClientePorCedula(String ced){ return null; }
        @Override public Cliente buscarClientePorId(String id){ return null; }
        @Override public List<Cliente> obtenerTodosClientes(){ return Collections.emptyList(); }
        @Override public boolean actualizarCliente(Cliente c){ return false; }
        @Override public boolean existeCedula(String ced){ return false; }
        @Override public boolean existeTelefono(String tel){ return false; }
        @Override public List<Habitacion> obtenerHabitacionesDisponibles(){ return Collections.singletonList(new Habitacion("HAB-1","101","Simple", false, 50)); }
        @Override public List<Habitacion> obtenerHabitacionesOcupadas(){ return Collections.emptyList(); }
        @Override public List<HabitacionOcupadaInfo> obtenerHabitacionesOcupadasConCliente(){ return Collections.emptyList(); }
        @Override public List<Habitacion> obtenerTodasHabitaciones(){ return obtenerHabitacionesDisponibles(); }
        @Override public Habitacion buscarHabitacionPorNumero(String numero){ return null; }
        @Override public boolean actualizarEstadoHabitacion(String idHab, boolean ocupada){ return true; }
        @Override public void inicializarHabitaciones(){}
        @Override public Reserva buscarReservaActivaPorCedula(String ced){ return null; }
        @Override public boolean finalizarReserva(String id){ return false; }
        @Override public List<Reserva> obtenerTodasReservas(){ return Collections.emptyList(); }
        @Override public List<Reserva> obtenerReservasRecientes(int limite){ return Collections.emptyList(); }
        @Override public ModeloMemento crearMemento(){ return null; }
        @Override public void restaurarEstadoCompleto(ModeloMemento m){}
        @Override public boolean verificarDisponibilidad(){ return true; }

        @Override
        public boolean registrarServicioHabitacion(ServicioHabitacion servicio) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        @Override
        public List<ServicioHabitacion> obtenerServiciosPorReserva(String idReserva) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        @Override
        public String generarCodigoServicio() {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        @Override
        public boolean actualizarPlanificacionReserva(String idReserva, Date nuevaFechaInicioPlanificada, int noches, String nuevasObservaciones) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }
    }

    @Test
    void crearReservaFallaLanzaExcepcion(){
        FailingModeloService service = new FailingModeloService();
        CrearReservaCommand cmd = new CrearReservaCommand(service, "CLI-X", "HAB-1", 10.0, "desc");
        RuntimeException ex = assertThrows(RuntimeException.class, cmd::execute);
        assertTrue(ex.getMessage().contains("No se pudo crear"));
    }
}
