package support;

import model.*;
import memento.ModeloMemento;
import java.util.*;

/**
 * Servicio de modelo falso en memoria para pruebas de patrones.
 */
public class FakeModeloService implements IModeloService {
    private final List<Cliente> clientes = new ArrayList<>();
    private final List<Habitacion> habitaciones = new ArrayList<>();
    private final List<Reserva> reservas = new ArrayList<>();

    public FakeModeloService(){
        for(int i=1;i<=3;i++){
            habitaciones.add(new Habitacion("HAB-00"+i, String.format("%03d", i), i==1?"Suite":"Simple", false, i==1?120:50));
        }
    }

    // CLIENTES
    @Override public boolean registrarCliente(Cliente c){ if(c.getId()==null) c.setId("CLI-"+(clientes.size()+1)); return clientes.add(c); }
    @Override public boolean eliminarCliente(String id){ return clientes.removeIf(x->x.getId().equals(id)); }
    @Override public Cliente buscarClientePorCedula(String ced){ return clientes.stream().filter(c->c.getCedula().equals(ced)).findFirst().orElse(null);}    
    @Override public Cliente buscarClientePorId(String id){ return clientes.stream().filter(c->c.getId().equals(id)).findFirst().orElse(null);}    
    @Override public List<Cliente> obtenerTodosClientes(){ return new ArrayList<>(clientes);}    
    @Override public boolean actualizarCliente(Cliente c){ int i=indexCliente(c.getId()); if(i<0) return false; clientes.set(i,c); return true;}    
    private int indexCliente(String id){ for(int i=0;i<clientes.size();i++) if(clientes.get(i).getId().equals(id)) return i; return -1;}    
    @Override public boolean existeCedula(String ced){ return buscarClientePorCedula(ced)!=null;}    
    @Override public boolean existeTelefono(String tel){ return clientes.stream().anyMatch(c->tel.equals(c.getTelefono())); }

    // HABITACIONES
    @Override public List<Habitacion> obtenerHabitacionesDisponibles(){ List<Habitacion> r=new ArrayList<>(); for(Habitacion h:habitaciones) if(!h.isOcupada()) r.add(h); return r; }
    @Override public List<Habitacion> obtenerHabitacionesOcupadas(){ List<Habitacion> r=new ArrayList<>(); for(Habitacion h:habitaciones) if(h.isOcupada()) r.add(h); return r; }
    @Override public List<HabitacionOcupadaInfo> obtenerHabitacionesOcupadasConCliente(){ return new ArrayList<>(); }
    @Override public List<Habitacion> obtenerTodasHabitaciones(){ return new ArrayList<>(habitaciones);}    
    @Override public Habitacion buscarHabitacionPorNumero(String numero){ return habitaciones.stream().filter(h->h.getNumero().equals(numero)).findFirst().orElse(null);}    
    @Override public boolean actualizarEstadoHabitacion(String idHab, boolean ocupada){ for(Habitacion h:habitaciones) if(h.getId().equals(idHab)){ h.setOcupada(ocupada); return true;} return false;}    
    @Override public void inicializarHabitaciones(){}

    // RESERVAS
    @Override public boolean crearReserva(Reserva r){ if(r.getId()==null) r.setId("RES-"+(reservas.size()+1)); reservas.add(r); actualizarEstadoHabitacion(r.getIdHabitacion(), true); return true; }
    @Override public Reserva buscarReservaActivaPorCedula(String ced){ Cliente c=buscarClientePorCedula(ced); if(c==null) return null; return reservas.stream().filter(x->x.getIdCliente().equals(c.getId()) && x.getFechaSalida()==null).findFirst().orElse(null);}    
    @Override public boolean finalizarReserva(String id){ for(Reserva r:reservas) if(r.getId().equals(id)&&r.getFechaSalida()==null){ r.setFechaSalida(new Date()); actualizarEstadoHabitacion(r.getIdHabitacion(), false); return true;} return false;}    
    @Override public List<Reserva> obtenerTodasReservas(){ return new ArrayList<>(reservas);}    
    @Override public List<Reserva> obtenerReservasRecientes(int limite){ return reservas.subList(0, Math.min(limite, reservas.size())); }

    // MEMENTO
    @Override public ModeloMemento crearMemento(){ return new ModeloMemento(clientes, habitaciones, reservas);}    
    @Override public void restaurarEstadoCompleto(ModeloMemento m){ if(m==null) return; clientes.clear(); clientes.addAll(m.getClientes()); habitaciones.clear(); habitaciones.addAll(m.getHabitaciones()); reservas.clear(); reservas.addAll(m.getReservas()); }
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
