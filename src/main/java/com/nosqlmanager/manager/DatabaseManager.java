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
 * Gestor principal de la base de datos NoSQL.
 * Utiliza un árbol AVL para indexar documentos JSON por su clave principal (id).
 * Permite operaciones CRUD rápidas y persistencia en archivo JSON.
 */
public class DatabaseManager {

    private final File file;
    private final ObjectMapper objectMapper;
    private final AVLTree<Integer, JsonDocument> index;

    /**
     * Crea el gestor y carga los datos desde el archivo JSON (si existe).
     * Así, todo queda indexado y listo para usarse desde el principio.
     *
     * @param filePath Ruta del archivo donde se guardan los datos.
     */
    public DatabaseManager(String filePath) {
        this.file = new File(filePath);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.index = new AVLTree<>();
        loadFromFile();
    }

    /**
     * Lee todos los documentos del archivo y los mete al árbol AVL para búsquedas rápidas.
     * Si el archivo no existe o está vacío, simplemente deja el árbol vacío.
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
     * Guarda todos los documentos actuales en el archivo JSON.
     * Así, nada se pierde si cierras el programa.
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
     * Guarda un documento nuevo o actualiza uno que ya existe.
     * Lo mete al árbol y lo deja guardado en el archivo.
     *
     * @param document El documento a guardar o actualizar.
     */
    public void save(JsonDocument document) {
        if (document == null || document.getId() == null) {
            throw new IllegalArgumentException("El documento y su ID no pueden ser nulos");
        }
        index.insert(document.getId(), document);
        saveToFile();
    }

    /**
     * Busca un documento por su clave (id) usando el árbol AVL.
     * Es muy rápido incluso con muchos datos.
     *
     * @param id La clave principal del documento.
     * @return El documento si existe, o vacío si no.
     */
    public Optional<JsonDocument> findById(Integer id) {
        return index.search(id);
    }

    /**
     * Busca documentos que cumplan cualquier condición que tú definas.
     * Por ejemplo, puedes buscar todos los que tengan "ciudad = Bogotá".
     *
     * @param predicate Una función que dice si un documento cumple el criterio.
     * @return Lista de documentos que cumplen lo que pidas.
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
     * Busca documentos donde un campo específico contenga cierto texto.
     * Por ejemplo, todos los que tengan "nombre" que contenga "Juan".
     *
     * @param fieldName El nombre del campo a buscar.
     * @param value El texto que debe contener ese campo.
     * @return Lista de documentos que coinciden.
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
     * Busca documentos donde un campo sea exactamente igual a un valor.
     * Por ejemplo, todos los que tengan "ciudad" igual a "Bogotá".
     *
     * @param fieldName El nombre del campo a buscar.
     * @param value El valor exacto que debe tener ese campo.
     * @return Lista de documentos que coinciden exactamente.
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
     * Actualiza un documento que ya existe (por id).
     * Cambia los datos en el árbol y en el archivo.
     *
     * @param document El documento con los nuevos datos (debe tener id existente).
     * @return true si se actualizó, false si no existía.
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
     * Elimina un documento por su id.
     * Lo borra del árbol y del archivo.
     *
     * @param id El id del documento a eliminar.
     * @return true si se eliminó, false si no existía.
     */
    public boolean deleteById(Integer id) {
        if (!index.delete(id)) {
            return false;
        }
        saveToFile();
        return true;
    }

    /**
     * Comprueba si existe un documento con el id dado.
     *
     * @param id El id a buscar.
     * @return true si existe, false si no.
     */
    public boolean existsById(Integer id) {
        return index.contains(id);
    }

    /**
     * Devuelve todos los documentos guardados, ordenados por id.
     *
     * @return Lista de todos los documentos.
     */
    public List<JsonDocument> getAllDocuments() {
        List<JsonDocument> documents = new ArrayList<>();
        for (Integer key : index.getAllKeys()) {
            index.search(key).ifPresent(documents::add);
        }
        return documents;
    }

    /**
     * Devuelve el número total de documentos guardados.
     *
     * @return El número total de documentos.
     */
    public int getSize() {
        return index.getSize();
    }

    /**
     * Comprueba si no hay ningún documento guardado.
     *
     * @return true si no hay nada guardado, false si hay al menos uno.
     */
    public boolean isEmpty() {
        return index.isEmpty();
    }

    /**
     * Borra todo: elimina todos los documentos del árbol y del archivo.
     */
    public void clear() {
        index.clear();
        saveToFile();
    }

    /**
     * Muestra la estructura interna del árbol AVL en consola.
     */
    public void printIndex() {
        index.printTree();
    }

    /**
     * Devuelve todas las claves (ids) ordenadas.
     *
     * @return Lista de ids ordenados.
     */
    public List<Integer> getAllKeys() {
        return index.getAllKeys();
    }

    /**
     * Devuelve el árbol AVL interno para visualización.
     * Útil para la GUI que dibuja el árbol.
     *
     * @return El árbol AVL con los documentos indexados.
     */
    public AVLTree<Integer, JsonDocument> getIndex() {
        return index;
    }
}
