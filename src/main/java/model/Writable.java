package model;

import org.bson.Document;
import org.bson.conversions.Bson;

/**
 * Interfaz para operaciones de escritura en la base de datos.
 * Aplica el principio ISP (Interface Segregation Principle) de SOLID
 * separando las operaciones de escritura de las de lectura y eliminación.
 * 
 * @author asdw
 * @version 1.0
 */
public interface Writable {
    
    /**
     * Inserta un nuevo documento en la colección especificada.
     * 
     * @param coleccion Nombre de la colección donde insertar
     * @param documento Document a insertar
     */
    void insertar(String coleccion, Document documento);
    
    /**
     * Actualiza un documento existente por su ID.
     * 
     * @param coleccion Nombre de la colección donde actualizar
     * @param id Identificador único del documento a actualizar
     * @param actualizacion Operación de actualización a realizar
     * @return true si se actualizó correctamente, false en caso contrario
     */
    boolean actualizarPorId(String coleccion, String id, Bson actualizacion);
}
