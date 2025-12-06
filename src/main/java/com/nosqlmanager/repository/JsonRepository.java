package com.nosqlmanager.repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.nosqlmanager.model.JsonDocument;

/**
 * Interfaz que define las operaciones CRUD para documentos JSON.
 */
public interface JsonRepository {

    /**
     * Guarda un nuevo documento o actualiza uno existente.
     * @param document Documento a guardar.
     */
    void save(JsonDocument document);

    /**
     * Busca un documento por su clave principal.
     * @param id Clave principal del documento.
     * @return Optional con el documento si existe, vacío si no.
     */
    Optional<JsonDocument> findById(Integer id);

    /**
     * Obtiene todos los documentos almacenados.
     * @return Lista de documentos.
     */
    List<JsonDocument> findAll();

    /**
     * Busca documentos que cumplan un criterio dado.
     * Permite búsqueda por atributos distintos a la clave principal.
     * @param predicate Criterio de búsqueda.
     * @return Lista de documentos que cumplen el criterio.
     */
    List<JsonDocument> findByPredicate(Predicate<JsonDocument> predicate);

    /**
     * Busca documentos cuyo campo contenga el valor dado.
     * @param fieldName Nombre del campo.
     * @param value Valor a buscar.
     * @return Lista de documentos coincidentes.
     */
    List<JsonDocument> findByField(String fieldName, String value);

    /**
     * Elimina un documento por su clave principal.
     * @param id Clave principal del documento a eliminar.
     * @return true si se eliminó, false si no existía.
     */
    boolean deleteById(Integer id);

    /**
     * Verifica si existe un documento con la clave dada.
     * @param id Clave principal.
     * @return true si existe, false si no.
     */
    boolean existsById(Integer id);

    /**
     * Actualiza un documento existente.
     * @param document Documento con los nuevos datos.
     * @return true si se actualizó, false si no existía.
     */
    boolean update(JsonDocument document);
}
