package model;

import org.bson.Document;
import java.util.List;

/**
 * Interfaz para operaciones de lectura en la base de datos.
 * Aplica el principio ISP (Interface Segregation Principle) de SOLID
 * separando las operaciones de lectura de las de escritura y eliminación.
 * 
 * @author asdw
 * @version 1.0
 */
public interface Readable {
    
    /**
     * Lista todos los documentos de una colección específica.
     * 
     * @param coleccion Nombre de la colección a consultar
     * @return Lista de documentos encontrados
     */
    List<Document> listarTodos(String coleccion);
    
    /**
     * Busca un documento por su ID en una colección específica.
     * 
     * @param coleccion Nombre de la colección donde buscar
     * @param id Identificador único del documento
     * @return Document encontrado o null si no existe
     */
    Document buscarPorId(String coleccion, String id);
    
    /**
     * Busca documentos que coincidan con un filtro específico.
     * 
     * @param coleccion Nombre de la colección donde buscar
     * @param filtro Documento con los criterios de búsqueda
     * @return Lista de documentos que coinciden con el filtro
     */
    List<Document> buscarPorFiltro(String coleccion, Document filtro);
}
