package model;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación de operaciones CRUD para MongoDB.
 * Aplica los principios SOLID:
 * - ISP: Implementa interfaces específicas (Readable, Writable, Deletable)
 * - DIP: Cumple con las abstracciones definidas por las interfaces
 * - SRP: Se encarga únicamente de operaciones de base de datos
 * 
 * @author asdw
 * @version 1.0
 */
public class MongoCRUD implements Readable, Writable, Deletable {

    private final ConexionBD conexion;

    /**
     * Constructor que recibe la conexión a la base de datos.
     * Aplica DIP al depender de la abstracción ConexionBD.
     * 
     * @param conexion Instancia de conexión a MongoDB
     */
    public MongoCRUD(ConexionBD conexion) {
        this.conexion = conexion;
    }

    @Override
    public void insertar(String nombreColeccion, Document doc) {
        MongoCollection<Document> col = conexion.getColeccion(nombreColeccion);
        col.insertOne(doc);
    }

    @Override
    public Document buscarPorId(String nombreColeccion, String id) {
        MongoCollection<Document> col = conexion.getColeccion(nombreColeccion);
        return col.find(Filters.eq("_id", id)).first();
    }

    @Override
    public List<Document> buscarPorFiltro(String nombreColeccion, Document filtro) {
        MongoCollection<Document> col = conexion.getColeccion(nombreColeccion);
        List<Document> lista = new ArrayList<>();
        for (Document doc : col.find(filtro)) {
            lista.add(doc);
        }
        return lista;
    }

    @Override
    public boolean actualizarPorId(String nombreColeccion, String id, Bson actualizacion) {
        MongoCollection<Document> col = conexion.getColeccion(nombreColeccion);
        UpdateResult res = col.updateOne(Filters.eq("_id", id), actualizacion);
        return res.getModifiedCount() > 0;
    }

    @Override
    public void eliminarPorId(String nombreColeccion, String id) {
        MongoCollection<Document> col = conexion.getColeccion(nombreColeccion);
        col.deleteOne(Filters.eq("_id", id));
    }

    @Override
    public boolean eliminarPorIdConResultado(String nombreColeccion, String id) {
        MongoCollection<Document> col = conexion.getColeccion(nombreColeccion);
        DeleteResult res = col.deleteOne(Filters.eq("_id", id));
        return res.getDeletedCount() > 0;
    }

    @Override
    public void eliminarTodos(String nombreColeccion) {
        MongoCollection<Document> col = conexion.getColeccion(nombreColeccion);
        col.deleteMany(new Document());
    }

    @Override
    public List<Document> listarTodos(String nombreColeccion) {
        MongoCollection<Document> col = conexion.getColeccion(nombreColeccion);
        List<Document> lista = new ArrayList<>();
        for (Document doc : col.find()) {
            lista.add(doc);
        }
        return lista;
    }

    /**
     * Obtiene la conexión actual para operaciones avanzadas.
     * 
     * @return Instancia de ConexionBD
     */
    public ConexionBD getConexion() {
        return conexion;
    }
}
