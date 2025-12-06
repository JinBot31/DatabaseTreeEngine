package com.nosqlmanager.manager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.nosqlmanager.model.JsonDocument;
import com.nosqlmanager.tree.AVLTree;

/**
 * Orquestador de acceso a datos para la aplicación.
 * Administra un índice AVL en memoria y la persistencia en archivo JSON,
 * ofreciendo operaciones CRUD y búsquedas por campo.
 *
 * Responsabilidades:
 * - Cargar y guardar documentos desde/hacia un archivo JSON.
 * - Mantener un índice AVL por ID para operaciones eficientes.
 * - Exponer utilidades de búsqueda por predicado y por campo.
 */
public class DatabaseManager {

    private final File file;
    private final ObjectMapper objectMapper;
    private final AVLTree<Integer, JsonDocument> index;

    /**
     * Crea un gestor asociado a un archivo de base de datos.
     * @param filePath ruta del archivo JSON de datos
     */
    public DatabaseManager(String filePath) {
        this.file = new File(filePath);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.index = new AVLTree<>();
        loadFromFile();
    }

    /**
     * Carga los documentos desde el archivo de persistencia, si existe.
     * Población inicial del índice AVL.
     */
    private void loadFromFile() {
        if (file.exists() && file.length() > 0) {
            try {
                List<JsonDocument> documents = objectMapper.readValue(file, new TypeReference<List<JsonDocument>>() {});
                for (JsonDocument doc : documents) {
                    index.insert(doc.getId(), doc);
                }
            } catch (IOException e) {
                // Si hay error, el árbol queda vacío
            }
        }
    }

    /**
     * Persiste el contenido actual en el archivo de datos.
     * Lanza RuntimeException en caso de error de escritura.
     */
    private void saveToFile() {
        try {
            List<JsonDocument> documents = getAllDocuments();
            objectMapper.writeValue(file, documents);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar en archivo: " + e.getMessage(), e);
        }
    }

    /**
     * Inserta o actualiza un documento en el índice y lo persiste en disco.
     * @param document documento a guardar
     * @throws IllegalArgumentException si el documento o su ID son nulos
     */
    public void save(JsonDocument document) {
        if (document == null || document.getId() == null) {
            throw new IllegalArgumentException("El documento y su ID no pueden ser nulos");
        }
        index.insert(document.getId(), document);
        saveToFile();
    }

    /**
     * Busca un documento por su ID.
     * @param id identificador único del documento
     * @return Optional con el documento si existe
     */
    public Optional<JsonDocument> findById(Integer id) {
        return index.search(id);
    }

    /**
     * Busca documentos que cumplan un criterio arbitrario.
     * @param predicate condición de filtrado
     * @return lista de documentos que cumplen el criterio
     */
    public List<JsonDocument> findByPredicate(Predicate<JsonDocument> predicate) {
        List<JsonDocument> results = new ArrayList<>();
        for (JsonDocument doc : getAllDocuments()) {
            if (predicate.test(doc)) {
                results.add(doc);
            }
        }
        return results;
    }

    /**
     * Busca documentos cuyo campo contenga el valor dado.
     * @param fieldName nombre del campo
     * @param value valor a buscar (contains)
     * @return lista de documentos coincidentes
     */
    public List<JsonDocument> findByField(String fieldName, String value) {
        return findByPredicate(doc -> {
            JsonNode data = doc.getData();
            if (data == null) return false;
            JsonNode field = data.get(fieldName);
            if (field == null) return false;
            return field.asText().contains(value);
        });
    }


    /**
     * Busca documentos cuyo campo sea exactamente igual al valor dado.
     * @param fieldName nombre del campo
     * @param value valor esperado (equals)
     * @return lista de documentos coincidentes
     */
    public List<JsonDocument> findByFieldEquals(String fieldName, String value) {
        return findByPredicate(doc -> {
            JsonNode data = doc.getData();
            if (data == null) return false;
            JsonNode field = data.get(fieldName);
            if (field == null) return false;
            return field.asText().equals(value);
        });
    }

    /**
     * Actualiza un documento existente y persiste el cambio.
     * @param document documento con los nuevos datos
     * @return true si el documento existía y se actualizó
     * @throws IllegalArgumentException si el documento o su ID son nulos
     */
    public boolean update(JsonDocument document) {
        if (document == null || document.getId() == null) {
            throw new IllegalArgumentException("El documento y su ID no pueden ser nulos");
        }
        if (!index.contains(document.getId())) {
            return false;
        }
        index.insert(document.getId(), document);
        saveToFile();
        return true;
    }

    /**
     * Elimina un documento por ID y persiste el estado.
     * @param id identificador del documento
     * @return true si se eliminó; false si no existía
     */
    public boolean deleteById(Integer id) {
        if (!index.delete(id)) {
            return false;
        }
        saveToFile();
        return true;
    }

    /**
     * Verifica si existe un documento por su ID.
     * @param id identificador del documento
     * @return true si existe, false en caso contrario
     */
    public boolean existsById(Integer id) {
        return index.contains(id);
    }

    /**
     * Obtiene todos los documentos en orden por ID (según el índice AVL).
     * @return lista de documentos
     */
    public List<JsonDocument> getAllDocuments() {
        List<JsonDocument> documents = new ArrayList<>();
        for (Integer key : index.getAllKeys()) {
            index.search(key).ifPresent(documents::add);
        }
        return documents;
    }


    /**
     * Número de documentos en el índice.
     */
    public int getSize() {
        return index.getSize();
    }

    /**
     * Indica si no hay documentos almacenados.
     */
    public boolean isEmpty() {
        return index.isEmpty();
    }

    /**
     * Borra todos los documentos del índice y los persiste en disco.
     */
    public void clear() {
        index.clear();
        saveToFile();
    }

  
    /**
     * Imprime el árbol AVL del índice por consola.
     */
    public void printIndex() {
        index.printTree();
    }


    /**
     * Obtiene todas las claves (IDs) del índice.
     */
    public List<Integer> getAllKeys() {
        return index.getAllKeys();
    }

    /**
     * Devuelve el índice AVL subyacente.
     */
    public AVLTree<Integer, JsonDocument> getIndex() {
        return index;
    }
}
