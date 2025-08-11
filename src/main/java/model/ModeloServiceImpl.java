package model;

import memento.ModeloMemento;
import singleton.GestorDisponibilidad;
import org.bson.Document;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

/**
 * Implementación concreta del servicio del modelo.
 * Encapsula toda la lógica de negocio y operaciones con MongoDB.
 * 
 * @author asdw
 * @version 1.0
 */
public class ModeloServiceImpl implements IModeloService {
    
    private final MongoCRUD mongoCRUD;
    private final GestorDisponibilidad gestorDisponibilidad;
    
    /**
     * Constructor que inyecta las dependencias necesarias.
     */
    public ModeloServiceImpl() {
        ConexionBD conexion = new ConexionBD();
        this.mongoCRUD = new MongoCRUD(conexion);
        this.gestorDisponibilidad = GestorDisponibilidad.getInstance();
    }
    
    // === OPERACIONES DE CLIENTES ===
    
    @Override
    public boolean registrarCliente(Cliente cliente) {
        if (cliente == null || cliente.getCedula() == null || cliente.getCedula().trim().isEmpty()) {
            return false;
        }
        
        // Verificar que no exista ya la cédula o teléfono
        if (existeCedula(cliente.getCedula()) || existeTelefono(cliente.getTelefono())) {
            return false;
        }
        
        try {
            // Generar / normalizar ID si no existe o no es legible (patrón CLI-0001)
            if (cliente.getId() == null || !cliente.getId().matches("CLI-\\d{4}")) {
                cliente.setId(generarCodigoCliente());
            }
            
            mongoCRUD.insertar("clientes", cliente.toDocument());
            return true;
        } catch (Exception e) {
            System.err.println("Error al registrar cliente: " + e.getMessage());
            return false;
        }
    }

    /**
     * MIGRACIÓN: Convierte IDs de clientes existentes que no sigan el formato CLI-0001
     * a uno nuevo incremental, actualizando además las reservas que apunten al ID antiguo.
     * Es idempotente: si todos los IDs son legibles no hace cambios.
     */
    public void migrarIdsClientesLegibles(){
        try {
            List<Document> docs = mongoCRUD.listarTodos("clientes");
            MongoCollection<Document> colClientes = mongoCRUD.getConexion().getColeccion("clientes");
            MongoCollection<Document> colReservas = mongoCRUD.getConexion().getColeccion("reservas");
            for (Document doc : docs){
                String oldId = doc.getString("_id");
                if (oldId == null || !oldId.matches("CLI-\\d{4}")) {
                    // Generar nuevo ID legible evitando colisiones actuales
                    String nuevoId = generarCodigoCliente();
                    // Crear nuevo documento con el mismo contenido excepto _id
                    Document nuevo = new Document("_id", nuevoId)
                            .append("nombre", doc.getString("nombre"))
                            .append("apellido", doc.getString("apellido"))
                            .append("cedula", doc.getString("cedula"))
                            .append("telefono", doc.getString("telefono"));
                    // Eliminar antiguo e insertar nuevo (no se puede modificar _id)
                    colClientes.deleteOne(Filters.eq("_id", oldId));
                    colClientes.insertOne(nuevo);
                    // Actualizar reservas que referencian al cliente
                    colReservas.updateMany(Filters.eq("idCliente", oldId), Updates.set("idCliente", nuevoId));
                }
            }
        } catch (Exception e){
            System.err.println("Error en migrarIdsClientesLegibles: "+e.getMessage());
        }
    }
    
    @Override
    public boolean eliminarCliente(String idCliente) {
        if (idCliente == null || idCliente.trim().isEmpty()) {
            return false;
        }
        
        try {
            return mongoCRUD.eliminarPorIdConResultado("clientes", idCliente);
        } catch (Exception e) {
            System.err.println("Error al eliminar cliente: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public Cliente buscarClientePorCedula(String cedula) {
        if (cedula == null || cedula.trim().isEmpty()) {
            return null;
        }
        
        try {
            Document filtro = new Document("cedula", cedula);
            List<Document> documentos = mongoCRUD.buscarPorFiltro("clientes", filtro);
            
            if (!documentos.isEmpty()) {
                return Cliente.fromDocument(documentos.get(0));
            }
        } catch (Exception e) {
            System.err.println("Error al buscar cliente por cédula: " + e.getMessage());
        }
        return null;
    }
    
    @Override
    public List<Cliente> obtenerTodosClientes() {
        try {
            List<Document> documentos = mongoCRUD.listarTodos("clientes");
            List<Cliente> clientes = new ArrayList<>();
            for (Document doc : documentos) {
                clientes.add(Cliente.fromDocument(doc));
            }
            return clientes;
        } catch (Exception e) {
            System.err.println("Error al obtener todos los clientes: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Cliente buscarClientePorId(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        try {
            Document doc = mongoCRUD.buscarPorId("clientes", id);
            return doc != null ? Cliente.fromDocument(doc) : null;
        } catch (Exception e) {
            System.err.println("Error al buscar cliente por ID: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean actualizarCliente(Cliente cliente) {
        if (cliente == null || cliente.getId() == null) return false;
        try {
            Document update = new Document("$set", new Document("nombre", cliente.getNombre())
                .append("apellido", cliente.getApellido())
                .append("cedula", cliente.getCedula())
                .append("telefono", cliente.getTelefono()));
            return mongoCRUD.actualizarPorId("clientes", cliente.getId(), update);
        } catch (Exception e) {
            System.err.println("Error al actualizar cliente: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean existeCedula(String cedula) {
        return buscarClientePorCedula(cedula) != null;
    }
    
    @Override
    public boolean existeTelefono(String telefono) {
        if (telefono == null || telefono.trim().isEmpty()) {
            return false;
        }
        
        try {
            Document filtro = new Document("telefono", telefono);
            List<Document> documentos = mongoCRUD.buscarPorFiltro("clientes", filtro);
            return !documentos.isEmpty();
        } catch (Exception e) {
            System.err.println("Error al verificar teléfono: " + e.getMessage());
            return false;
        }
    }

    // === GENERACIÓN DE CÓDIGOS LEGIBLES ===
    /** Genera código incremental CLI-0001, CLI-0002 ... basándose en los IDs existentes. */
    private String generarCodigoCliente(){
        try {
            List<Document> docs = mongoCRUD.listarTodos("clientes");
            int max = 0;
            for (Document d: docs){
                String id = d.getString("_id");
                if (id != null && id.startsWith("CLI-")){
                    try {int n = Integer.parseInt(id.substring(4)); if (n>max) max=n;} catch(Exception ignore){}
                }
            }
            return String.format("CLI-%04d", max+1);
        } catch (Exception e){
            return "CLI-"+java.util.UUID.randomUUID().toString().substring(0,8).toUpperCase();
        }
    }

    /** Genera código incremental RES-0001, RES-0002 ... */
    private String generarCodigoReserva(){
        try {
            List<Document> docs = mongoCRUD.listarTodos("reservas");
            int max = 0;
            for (Document d: docs){
                String id = d.getString("_id");
                if (id != null && id.startsWith("RES-")){
                    try {int n = Integer.parseInt(id.substring(4)); if (n>max) max=n;} catch(Exception ignore){}
                }
            }
            return String.format("RES-%04d", max+1);
        } catch (Exception e){
            return "RES-"+java.util.UUID.randomUUID().toString().substring(0,8).toUpperCase();
        }
    }
    
    // === OPERACIONES DE HABITACIONES ===
    
    @Override
    public List<Habitacion> obtenerHabitacionesDisponibles() {
        try {
            List<Document> documentos = mongoCRUD.listarTodos("habitaciones");
            List<Habitacion> disponibles = new ArrayList<>();
            
            for (Document doc : documentos) {
                Habitacion hab = Habitacion.fromDocument(doc);
                if (!hab.isOcupada()) {
                    disponibles.add(hab);
                }
            }
            return disponibles;
        } catch (Exception e) {
            System.err.println("Error al obtener habitaciones disponibles: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Habitacion> obtenerHabitacionesOcupadas() {
        try {
            List<Document> documentos = mongoCRUD.listarTodos("habitaciones");
            List<Habitacion> ocupadas = new ArrayList<>();
            
            for (Document doc : documentos) {
                Habitacion hab = Habitacion.fromDocument(doc);
                if (hab.isOcupada()) {
                    ocupadas.add(hab);
                }
            }
            return ocupadas;
        } catch (Exception e) {
            System.err.println("Error al obtener habitaciones ocupadas: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<HabitacionOcupadaInfo> obtenerHabitacionesOcupadasConCliente() {
        List<HabitacionOcupadaInfo> resultado = new ArrayList<>();
        
        try {
            // Obtener habitaciones ocupadas
            List<Habitacion> habitacionesOcupadas = obtenerHabitacionesOcupadas();
            
            // Obtener todas las reservas activas
            List<Reserva> reservas = obtenerTodasReservas();
            
            for (Habitacion habitacion : habitacionesOcupadas) {
                // Buscar la reserva activa para esta habitación
                for (Reserva reserva : reservas) {
                    if (habitacion.getId().equals(reserva.getIdHabitacion()) && reserva.getFechaSalida() == null) {
                        // Buscar el cliente de esta reserva por ID
                        Cliente cliente = buscarClientePorId(reserva.getIdCliente());
                        
                        if (cliente != null) {
                            HabitacionOcupadaInfo info = new HabitacionOcupadaInfo(
                                habitacion.getNumero(),
                                habitacion.getTipo(),
                                habitacion.getPrecio(),
                                cliente.getNombre(),
                                cliente.getApellido(),
                                cliente.getCedula(),
                                reserva.getId()
                            );
                            resultado.add(info);
                        }
                        break; // Ya encontramos la reserva para esta habitación
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error al obtener habitaciones ocupadas con cliente: " + e.getMessage());
        }
        
        return resultado;
    }
    
    @Override
    public List<Habitacion> obtenerTodasHabitaciones() {
        try {
            List<Document> documentos = mongoCRUD.listarTodos("habitaciones");
            List<Habitacion> habitaciones = new ArrayList<>();
            
            for (Document doc : documentos) {
                habitaciones.add(Habitacion.fromDocument(doc));
            }
            return habitaciones;
        } catch (Exception e) {
            System.err.println("Error al obtener todas las habitaciones: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public Habitacion buscarHabitacionPorNumero(String numero) {
        if (numero == null || numero.trim().isEmpty()) {
            return null;
        }
        
        try {
            Document filtro = new Document("numero", numero);
            List<Document> documentos = mongoCRUD.buscarPorFiltro("habitaciones", filtro);
            
            if (!documentos.isEmpty()) {
                return Habitacion.fromDocument(documentos.get(0));
            }
        } catch (Exception e) {
            System.err.println("Error al buscar habitación: " + e.getMessage());
        }
        return null;
    }
    
    @Override
    public boolean actualizarEstadoHabitacion(String idHabitacion, boolean ocupada) {
        try {
            Document actualizacion = new Document("$set", new Document("ocupada", ocupada));
            return mongoCRUD.actualizarPorId("habitaciones", idHabitacion, actualizacion);
        } catch (Exception e) {
            System.err.println("Error al actualizar habitación: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public void inicializarHabitaciones() {
        try {
            List<Habitacion> habitaciones = obtenerTodasHabitaciones();
            
            // Si hay menos de 20 habitaciones, reinicializar todas
            if (habitaciones.size() < 20) {
                // Limpiar habitaciones existentes
                List<Document> docs = mongoCRUD.listarTodos("habitaciones");
                for (Document doc : docs) {
                    mongoCRUD.eliminarPorId("habitaciones", doc.getString("_id"));
                }
                
                // Crear las 20 habitaciones como en el código original
                for (int i = 1; i <= 20; i++) {
                    String numero = String.format("%03d", i); // 001, 002, 003...
                    String id = "HAB-" + numero; // ID legible
                    String tipo = (i <= 5) ? "Suite" : (i <= 12) ? "Doble" : "Simple";
                    double precio = (i <= 5) ? 120.0 : (i <= 12) ? 80.0 : 50.0;
                    
                    Habitacion hab = new Habitacion(id, numero, tipo, false, precio);
                    mongoCRUD.insertar("habitaciones", hab.toDocument());
                }
                
                // Inicializar gestor de disponibilidad
                gestorDisponibilidad.inicializar(obtenerTodasHabitaciones());
            }
        } catch (Exception e) {
            System.err.println("Error al inicializar habitaciones: " + e.getMessage());
        }
    }
    
    // === OPERACIONES DE RESERVAS ===
    
    @Override
    public boolean crearReserva(Reserva reserva) {
        if (reserva == null || reserva.getIdCliente() == null || reserva.getIdHabitacion() == null) {
            return false;
        }
        
        // Verificar que el cliente existe (buscar por ID, no por cédula)
        if (buscarClientePorId(reserva.getIdCliente()) == null) {
            return false;
        }
        
        // Verificar que la habitación existe y está disponible (buscar por ID)
        List<Habitacion> todasHabitaciones = obtenerTodasHabitaciones();
        Habitacion habitacion = todasHabitaciones.stream()
            .filter(h -> h.getId().equals(reserva.getIdHabitacion()))
            .findFirst()
            .orElse(null);
            
        if (habitacion == null || habitacion.isOcupada()) {
            return false;
        }
        
        try {
            // Generar ID si no existe
            if (reserva.getId() == null || reserva.getId().trim().isEmpty()) {
                reserva.setId(generarCodigoReserva());
            }
            
            // Crear la reserva
            mongoCRUD.insertar("reservas", reserva.toDocument());
            
            // Marcar habitación como ocupada
            actualizarEstadoHabitacion(habitacion.getId(), true);
            
            // Actualizar gestor de disponibilidad
            gestorDisponibilidad.reservarHabitacion(habitacion.getId());
            
            return true;
        } catch (Exception e) {
            System.err.println("Error al crear reserva: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public Reserva buscarReservaActivaPorCedula(String cedula) {
        if (cedula == null || cedula.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Primero buscar el cliente por cédula para obtener su ID
            Cliente cliente = buscarClientePorCedula(cedula);
            if (cliente == null) {
                return null;
            }
            
            // Luego buscar la reserva activa por ID del cliente
            Document filtro = new Document("idCliente", cliente.getId()).append("fechaSalida", null);
            List<Document> documentos = mongoCRUD.buscarPorFiltro("reservas", filtro);
            if (!documentos.isEmpty()) {
                return Reserva.fromDocument(documentos.get(0));
            }
        } catch (Exception e) {
            System.err.println("Error al buscar reserva activa: " + e.getMessage());
        }
        return null;
    }
    
    @Override
    public boolean finalizarReserva(String idReserva) {
        try {
            Document actualizacion = new Document("$set", new Document("fechaSalida", new Date()));
            boolean actualizada = mongoCRUD.actualizarPorId("reservas", idReserva, actualizacion);
            
            if (actualizada) {
                // Buscar la reserva para obtener el ID de habitación
                Document reservaDoc = mongoCRUD.buscarPorId("reservas", idReserva);
                if (reservaDoc != null) {
                    Reserva reserva = Reserva.fromDocument(reservaDoc);
                    // Buscar habitación por ID, no por número
                    List<Habitacion> todasHabitaciones = obtenerTodasHabitaciones();
                    Habitacion habitacion = todasHabitaciones.stream()
                        .filter(h -> h.getId().equals(reserva.getIdHabitacion()))
                        .findFirst()
                        .orElse(null);
                    
                    if (habitacion != null) {
                        // Liberar habitación
                        actualizarEstadoHabitacion(habitacion.getId(), false);
                        gestorDisponibilidad.liberarHabitacion(habitacion.getId());
                    }
                }
            }
            return actualizada;
        } catch (Exception e) {
            System.err.println("Error al finalizar reserva: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public List<Reserva> obtenerTodasReservas() {
        try {
            List<Document> documentos = mongoCRUD.listarTodos("reservas");
            List<Reserva> reservas = new ArrayList<>();
            
            for (Document doc : documentos) {
                reservas.add(Reserva.fromDocument(doc));
            }
            return reservas;
        } catch (Exception e) {
            System.err.println("Error al obtener reservas: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Reserva> obtenerReservasRecientes(int limite) {
        try {
            List<Document> documentos = mongoCRUD.listarTodos("reservas");
            List<Reserva> reservas = new ArrayList<>();
            
            for (Document doc : documentos) {
                reservas.add(Reserva.fromDocument(doc));
            }
            
            // Ordenar por fecha de ingreso (más recientes primero)
            reservas.sort((r1, r2) -> r2.getFechaIngreso().compareTo(r1.getFechaIngreso()));
            
            // Limitar el resultado
            return reservas.stream()
                .limit(limite)
                .collect(java.util.stream.Collectors.toList());
                
        } catch (Exception e) {
            System.err.println("Error al obtener reservas recientes: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    // === OPERACIONES DE MEMENTO ===
    
    @Override
    public ModeloMemento crearMemento() {
        List<Cliente> clientes = obtenerTodosClientes();
        List<Habitacion> habitaciones = obtenerTodasHabitaciones();
        List<Reserva> reservas = obtenerTodasReservas();
        
        return new ModeloMemento(clientes, habitaciones, reservas);
    }
    
    @Override
    public void restaurarEstadoCompleto(ModeloMemento memento) {
        if (memento == null) {
            return;
        }
        
        try {
            // Limpiar colecciones existentes
            mongoCRUD.eliminarTodos("clientes");
            mongoCRUD.eliminarTodos("habitaciones");
            mongoCRUD.eliminarTodos("reservas");
            
            // Restaurar datos del memento
            for (Cliente cliente : memento.getClientes()) {
                mongoCRUD.insertar("clientes", cliente.toDocument());
            }
            
            for (Habitacion habitacion : memento.getHabitaciones()) {
                mongoCRUD.insertar("habitaciones", habitacion.toDocument());
            }
            
            for (Reserva reserva : memento.getReservas()) {
                mongoCRUD.insertar("reservas", reserva.toDocument());
            }
            
            // Actualizar gestor de disponibilidad
            gestorDisponibilidad.inicializar(memento.getHabitaciones());
            
        } catch (Exception e) {
            System.err.println("Error al restaurar estado: " + e.getMessage());
        }
    }
    
    @Override
    public boolean verificarDisponibilidad() {
        try {
            // Verificar conectividad de base de datos
            List<Document> test = mongoCRUD.listarTodos("habitaciones");
            return test != null;
        } catch (Exception e) {
            System.err.println("Error en verificación de disponibilidad: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Método de debug para verificar el estado de las habitaciones.
     */
    public void debugEstadoHabitaciones() {
        System.out.println("=== DEBUG ESTADO HABITACIONES ===");
        try {
            List<Habitacion> todasHabitaciones = obtenerTodasHabitaciones();
            System.out.println("Total habitaciones: " + todasHabitaciones.size());
            
            int disponibles = 0, ocupadas = 0;
            for (Habitacion h : todasHabitaciones) {
                if (h.isOcupada()) {
                    ocupadas++;
                    System.out.println("OCUPADA: " + h.getNumero() + " - " + h.getTipo() + " (ID: " + h.getId() + ")");
                } else {
                    disponibles++;
                }
            }
            
            System.out.println("Disponibles: " + disponibles + ", Ocupadas: " + ocupadas);
            
            // Verificar reservas
            List<Reserva> reservas = obtenerTodasReservas();
            System.out.println("Total reservas: " + reservas.size());
            for (Reserva r : reservas) {
                if (r.getFechaSalida() == null) {
                    System.out.println("RESERVA ACTIVA: Cliente " + r.getIdCliente() + " -> Habitación " + r.getIdHabitacion());
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error en debug: " + e.getMessage());
        }
        System.out.println("=== FIN DEBUG ===");
    }
}
