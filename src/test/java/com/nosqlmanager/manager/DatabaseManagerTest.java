package com.nosqlmanager.manager;

import java.io.File;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nosqlmanager.model.JsonDocument;

/**
 * Pruebas unitarias para el DatabaseManager.
 */
class DatabaseManagerTest {

    private static final String TEST_FILE = "test_db.json";
    private DatabaseManager manager;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Eliminar archivo de prueba si existe
        File file = new File(TEST_FILE);
        if (file.exists()) {
            file.delete();
        }
        manager = new DatabaseManager(TEST_FILE);
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() {
        // Limpiar archivo de prueba
        File file = new File(TEST_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    private JsonDocument createDocument(Integer id, String nombre, int edad, String ciudad) {
        ObjectNode data = objectMapper.createObjectNode();
        data.put("nombre", nombre);
        data.put("edad", edad);
        data.put("ciudad", ciudad);
        return new JsonDocument(id, data);
    }

    @Test
    void testSaveAndFindById() {
        System.out.println("\n[testSaveAndFindById]");
        JsonDocument doc = createDocument(1, "Juan", 25, "Bogotá");

        manager.save(doc);
        manager.printIndex();

        Optional<JsonDocument> found = manager.findById(1);
        assertTrue(found.isPresent());
        assertEquals("Juan", found.get().getData().get("nombre").asText());
        assertEquals(25, found.get().getData().get("edad").asInt());
    }

    @Test
    void testSaveMultipleAndPrintTree() {
        System.out.println("\n[testSaveMultipleAndPrintTree]");
        manager.save(createDocument(5, "Ana", 30, "Medellín"));
        manager.save(createDocument(3, "Pedro", 22, "Cali"));
        manager.save(createDocument(7, "María", 28, "Bogotá"));
        manager.save(createDocument(1, "Luis", 35, "Barranquilla"));
        manager.save(createDocument(9, "Carla", 27, "Cartagena"));

        System.out.println("Claves ordenadas: " + manager.getAllKeys());
        manager.printIndex();

        assertEquals(5, manager.getSize());
    }

    @Test
    void testFindByField() {
        System.out.println("\n[testFindByField]");
        manager.save(createDocument(1, "Juan", 25, "Bogotá"));
        manager.save(createDocument(2, "Ana", 30, "Bogotá"));
        manager.save(createDocument(3, "Pedro", 22, "Cali"));

        List<JsonDocument> bogotanos = manager.findByFieldEquals("ciudad", "Bogotá");
        System.out.println("Documentos en Bogotá: " + bogotanos.size());

        assertEquals(2, bogotanos.size());
    }

    @Test
    void testFindByFieldContains() {
        System.out.println("\n[testFindByFieldContains]");
        manager.save(createDocument(1, "Juan Carlos", 25, "Bogotá"));
        manager.save(createDocument(2, "Ana María", 30, "Medellín"));
        manager.save(createDocument(3, "Carlos Pedro", 22, "Cali"));

        List<JsonDocument> conCarlos = manager.findByField("nombre", "Carlos");
        System.out.println("Documentos con 'Carlos' en nombre: " + conCarlos.size());

        assertEquals(2, conCarlos.size());
    }

    @Test
    void testUpdate() {
        System.out.println("\n[testUpdate]");
        manager.save(createDocument(1, "Juan", 25, "Bogotá"));

        System.out.println("Antes de actualizar:");
        manager.printIndex();

        JsonDocument updated = createDocument(1, "Juan Actualizado", 26, "Medellín");
        boolean result = manager.update(updated);

        System.out.println("Después de actualizar:");
        manager.printIndex();

        assertTrue(result);
        Optional<JsonDocument> found = manager.findById(1);
        assertTrue(found.isPresent());
        assertEquals("Juan Actualizado", found.get().getData().get("nombre").asText());
        assertEquals(26, found.get().getData().get("edad").asInt());
    }

    @Test
    void testUpdateNonExistent() {
        System.out.println("\n[testUpdateNonExistent]");
        JsonDocument doc = createDocument(999, "Fantasma", 0, "Nada");

        boolean result = manager.update(doc);

        assertFalse(result);
    }

    @Test
    void testDelete() {
        System.out.println("\n[testDelete]");
        manager.save(createDocument(1, "Juan", 25, "Bogotá"));
        manager.save(createDocument(2, "Ana", 30, "Medellín"));
        manager.save(createDocument(3, "Pedro", 22, "Cali"));

        System.out.println("Antes de eliminar:");
        manager.printIndex();

        boolean result = manager.deleteById(2);

        System.out.println("Después de eliminar '2':");
        manager.printIndex();

        assertTrue(result);
        assertEquals(2, manager.getSize());
        assertFalse(manager.existsById(2));
    }

    @Test
    void testDeleteNonExistent() {
        System.out.println("\n[testDeleteNonExistent]");
        manager.save(createDocument(1, "Juan", 25, "Bogotá"));

        boolean result = manager.deleteById(999);

        assertFalse(result);
        assertEquals(1, manager.getSize());
    }

    @Test
    void testPersistence() {
        System.out.println("\n[testPersistence]");
        manager.save(createDocument(1, "Juan", 25, "Bogotá"));
        manager.save(createDocument(2, "Ana", 30, "Medellín"));

        // Crear nuevo manager que carga desde archivo
        DatabaseManager manager2 = new DatabaseManager(TEST_FILE);

        System.out.println("Datos cargados desde archivo:");
        manager2.printIndex();

        assertEquals(2, manager2.getSize());
        assertTrue(manager2.findById(1).isPresent());
        assertTrue(manager2.findById(2).isPresent());
    }

    @Test
    void testClear() {
        System.out.println("\n[testClear]");
        manager.save(createDocument(1, "Juan", 25, "Bogotá"));
        manager.save(createDocument(2, "Ana", 30, "Medellín"));

        manager.clear();

        assertTrue(manager.isEmpty());
        assertEquals(0, manager.getSize());
    }

    @Test
    void testFindByPredicate() {
        System.out.println("\n[testFindByPredicate]");
        manager.save(createDocument(1, "Juan", 25, "Bogotá"));
        manager.save(createDocument(2, "Ana", 30, "Medellín"));
        manager.save(createDocument(3, "Pedro", 35, "Cali"));
        manager.save(createDocument(4, "María", 28, "Bogotá"));

        // Buscar mayores de 27 años
        List<JsonDocument> mayores = manager.findByPredicate(doc -> 
            doc.getData().get("edad").asInt() > 27
        );

        System.out.println("Mayores de 27 años: " + mayores.size());
        assertEquals(3, mayores.size());
    }

    @Test
    void testBalanceAfterOperations() {
        System.out.println("\n[testBalanceAfterOperations]");
        
        // Insertar en orden ascendente (peor caso para BST sin balance)
        for (int i = 1; i <= 10; i++) {
            manager.save(createDocument(i, "Persona" + i, 20 + i, "Ciudad" + i));
        }

        System.out.println("Después de insertar 1-10 en orden:");
        manager.printIndex();

        // Eliminar algunos
        manager.deleteById(5);
        manager.deleteById(3);
        manager.deleteById(8);

        System.out.println("Después de eliminar 5, 3, 8:");
        manager.printIndex();

        assertEquals(7, manager.getSize());
    }
}
