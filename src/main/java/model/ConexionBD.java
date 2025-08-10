package model;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

/**
 * Clase para gestionar la conexión a MongoDB y obtener colecciones.
 * Implementa el patrón de conexión centralizada para la base de datos.
 * 
 * @author asdw
 * @version 1.0
 */
public class ConexionBD {
    public static final String URI = "mongodb://localhost:27017";
    public static final String NOMBRE_BD = "HotelRefactorizado"; 
    
    private MongoClient mongoClient;
    private MongoDatabase database;

    /**
     * Constructor que inicializa la conexión a la base de datos.
     */
    public ConexionBD() {
        conectar();
    }

    /**
     * Establece la conexión con MongoDB.
     */
    private void conectar() {
        try {
            mongoClient = MongoClients.create(URI);
            database = mongoClient.getDatabase(NOMBRE_BD);
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null, 
                "Error al conectar a la base de datos: " + e.toString());
        }
    }

    /**
     * Obtiene una colección de la base de datos por su nombre.
     * 
     * @param nombreColeccion Nombre de la colección ("habitaciones", "clientes", "reservas", etc.)
     * @return La colección solicitada
     */
    public MongoCollection<Document> getColeccion(String nombreColeccion) {
        return database.getCollection(nombreColeccion);
    }

    /**
     * Obtiene la colección de habitaciones.
     * 
     * @return MongoCollection para habitaciones
     */
    public MongoCollection<Document> getHabitacionesCollection() {
        return getColeccion("habitaciones");
    }
    
    /**
     * Obtiene la colección de clientes.
     * 
     * @return MongoCollection para clientes
     */
    public MongoCollection<Document> getClientesCollection() {
        return getColeccion("clientes");
    }
    
    /**
     * Obtiene la colección de reservas.
     * 
     * @return MongoCollection para reservas
     */
    public MongoCollection<Document> getReservasCollection() {
        return getColeccion("reservas");
    }

    /**
     * Verifica si la conexión está activa.
     * 
     * @return true si está conectado, false en caso contrario
     */
    public boolean isConectado() {
        try {
            return mongoClient != null && database != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Cierra la conexión a la base de datos.
     */
    public void cerrar() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}
