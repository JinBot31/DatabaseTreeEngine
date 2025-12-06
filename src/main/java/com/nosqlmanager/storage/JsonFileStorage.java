package com.nosqlmanager.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.nosqlmanager.model.JsonDocument;
import com.nosqlmanager.repository.JsonRepository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Implementación de JsonRepository que persiste los documentos en un archivo JSON.
 */
public class JsonFileStorage implements JsonRepository {

    private final File file;
    private final ObjectMapper objectMapper;
    private List<JsonDocument> documents;

    /**
     * Crea una instancia de almacenamiento apuntando al archivo especificado.
     * @param filePath Ruta del archivo de persistencia.
     */
    public JsonFileStorage(String filePath) {
        this.file = new File(filePath);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.documents = new ArrayList<>();
        loadFromFile();
    }

    /**
     * Carga los documentos desde el archivo si existe.
     */
    private void loadFromFile() {
        if (file.exists() && file.length() > 0) {
            try {
                documents = objectMapper.readValue(file, new TypeReference<List<JsonDocument>>() {});
            } catch (IOException e) {
                documents = new ArrayList<>();
            }
        }
    }

    /**
     * Persiste los documentos en el archivo.
     */
    private void saveToFile() {
        try {
            objectMapper.writeValue(file, documents);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar en archivo: " + e.getMessage(), e);
        }
    }

    @Override
    public void save(JsonDocument document) {
        Optional<JsonDocument> existing = findById(document.getId());
        if (existing.isPresent()) {
            // Actualiza el documento existente
            int index = documents.indexOf(existing.get());
            documents.set(index, document);
        } else {
            // Inserta nuevo documento
            documents.add(document);
        }
        saveToFile();
    }

    @Override
    public Optional<JsonDocument> findById(Integer id) {
        return documents.stream()
                .filter(doc -> doc.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<JsonDocument> findAll() {
        return new ArrayList<>(documents);
    }

    @Override
    public List<JsonDocument> findByPredicate(Predicate<JsonDocument> predicate) {
        List<JsonDocument> results = new ArrayList<>();
        for (JsonDocument doc : documents) {
            if (predicate.test(doc)) {
                results.add(doc);
            }
        }
        return results;
    }

    @Override
    public List<JsonDocument> findByField(String fieldName, String value) {
        return findByPredicate(doc -> {
            JsonNode data = doc.getData();
            if (data == null) return false;
            JsonNode field = data.get(fieldName);
            if (field == null) return false;
            return field.asText().contains(value);
        });
    }

    @Override
    public boolean deleteById(Integer id) {
        Optional<JsonDocument> existing = findById(id);
        if (existing.isPresent()) {
            documents.remove(existing.get());
            saveToFile();
            return true;
        }
        return false;
    }

    @Override
    public boolean existsById(Integer id) {
        return documents.stream().anyMatch(doc -> doc.getId().equals(id));
    }

    @Override
    public boolean update(JsonDocument document) {
        Optional<JsonDocument> existing = findById(document.getId());
        if (existing.isPresent()) {
            int index = documents.indexOf(existing.get());
            documents.set(index, document);
            saveToFile();
            return true;
        }
        return false;
    }
}
