package model;

/**
 * Interfaz para operaciones de eliminación en la base de datos.
 * Aplica el principio ISP (Interface Segregation Principle) de SOLID
 * separando las operaciones de eliminación de las de lectura y escritura.
 * 
 * @author asdw
 * @version 1.0
 */
public interface Deletable {
    
    /**
     * Elimina un documento por su ID de la colección especificada.
     * 
     * @param coleccion Nombre de la colección donde eliminar
     * @param id Identificador único del documento a eliminar
     */
    void eliminarPorId(String coleccion, String id);
    
    /**
     * Elimina un documento por su ID y retorna si fue exitoso.
     * 
     * @param coleccion Nombre de la colección donde eliminar
     * @param id Identificador único del documento a eliminar
     * @return true si se eliminó correctamente, false en caso contrario
     */
    boolean eliminarPorIdConResultado(String coleccion, String id);
    
    /**
     * Elimina todos los documentos de una colección.
     * 
     * @param coleccion Nombre de la colección a limpiar
     */
    void eliminarTodos(String coleccion);
}
