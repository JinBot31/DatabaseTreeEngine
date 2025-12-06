package com.nosqlmanager.tree;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias para el árbol AVL.
 */
class AVLTreeTest {

    private AVLTree<Integer, String> tree;

    @BeforeEach
    public void setUp() {
        tree = new AVLTree<>();
    }

    @Test
    void testInsertAndSearch() {
        System.out.println("\n[InsertAndSearch] Estado inicial: " + tree.getAllKeys());
        tree.insert(10, "Diez");
        tree.insert(20, "Veinte");
        tree.insert(30, "Treinta");
        System.out.println("[InsertAndSearch] Estado tras inserciones: " + tree.getAllKeys());
        tree.printTree();

        assertEquals(Optional.of("Diez"), tree.search(10));
        assertEquals(Optional.of("Veinte"), tree.search(20));
        assertEquals(Optional.of("Treinta"), tree.search(30));
    }

    @Test
    void testInsertDuplicateUpdatesValue() {
        tree.insert(10, "Valor Inicial");
        tree.insert(10, "Valor Actualizado");

        assertEquals(Optional.of("Valor Actualizado"), tree.search(10));
        assertEquals(1, tree.getSize());
    }

    @Test
    void testSearchNonExistentKey() {
        tree.insert(10, "Diez");

        assertEquals(Optional.empty(), tree.search(99));
    }

    @Test
    void testDelete() {
        tree.insert(10, "Diez");
        tree.insert(20, "Veinte");
        tree.insert(30, "Treinta");
        System.out.println("\n[Delete] Antes de eliminar 20: " + tree.getAllKeys());
        tree.printTree();

        assertTrue(tree.delete(20));
        System.out.println("[Delete] Después de eliminar 20: " + tree.getAllKeys());
        tree.printTree();
        assertEquals(Optional.empty(), tree.search(20));
        assertEquals(2, tree.getSize());
    }

    @Test
    void testDeleteNonExistentKey() {
        tree.insert(10, "Diez");

        assertFalse(tree.delete(99));
        assertEquals(1, tree.getSize());
    }

    @Test
    void testContains() {
        tree.insert(10, "Diez");

        assertTrue(tree.contains(10));
        assertFalse(tree.contains(99));
    }

    @Test
    void testSize() {
        assertEquals(0, tree.getSize());

        tree.insert(10, "Diez");
        assertEquals(1, tree.getSize());

        tree.insert(20, "Veinte");
        tree.insert(30, "Treinta");
        assertEquals(3, tree.getSize());

        tree.delete(20);
        assertEquals(2, tree.getSize());
    }

    @Test
    void testIsEmpty() {
        assertTrue(tree.isEmpty());

        tree.insert(10, "Diez");
        assertFalse(tree.isEmpty());

        tree.delete(10);
        assertTrue(tree.isEmpty());
    }

    @Test
    void testGetAllKeys() {
        tree.insert(30, "Treinta");
        tree.insert(10, "Diez");
        tree.insert(20, "Veinte");
        tree.insert(40, "Cuarenta");

        List<Integer> keys = tree.getAllKeys();

        assertEquals(4, keys.size());
        assertEquals(List.of(10, 20, 30, 40), keys);
    }

    @Test
    void testClear() {
        tree.insert(10, "Diez");
        tree.insert(20, "Veinte");
        tree.insert(30, "Treinta");

        tree.clear();

        assertTrue(tree.isEmpty());
        assertEquals(0, tree.getSize());
    }

    @Test
    void testBalanceAfterInsertions() {
        // Inserción que requiere rotación derecha-derecha
        tree.insert(10, "Diez");
        tree.insert(20, "Veinte");
        tree.insert(30, "Treinta");
        System.out.println("\n[BalanceAfterInsertions] Estado tras inserciones: " + tree.getAllKeys());
        tree.printTree();

        // El árbol debe estar balanceado
        assertTrue(tree.contains(10));
        assertTrue(tree.contains(20));
        assertTrue(tree.contains(30));
    }

    @Test
    void testBalanceAfterDeletions() {
        tree.insert(10, "Diez");
        tree.insert(20, "Veinte");
        tree.insert(30, "Treinta");
        tree.insert(40, "Cuarenta");
        tree.insert(50, "Cincuenta");
        System.out.println("\n[BalanceAfterDeletions] Antes de eliminar: " + tree.getAllKeys());
        tree.printTree();

        tree.delete(10);
        tree.delete(20);
        System.out.println("[BalanceAfterDeletions] Después de eliminar 10 y 20: " + tree.getAllKeys());
        tree.printTree();

        // Verifica que ningún nodo tenga un factor de equilibrio absoluto >= 2
        assertNoNodeWithBalanceFactorAbsGreaterThan1(tree);
        assertEquals(3, tree.getSize());
    }

    /**
     * Verifica recursivamente que ningún nodo del árbol tenga un factor de equilibrio absoluto >= 2.
     */
    private void assertNoNodeWithBalanceFactorAbsGreaterThan1(AVLTree<Integer, String> tree) {
        assertNoNodeWithBalanceFactorAbsGreaterThan1(tree.getRoot());
    }

    private void assertNoNodeWithBalanceFactorAbsGreaterThan1(AVLNode<Integer, String> node) {
        if (node == null) return;
        int balance = getBalance(node);
        assertTrue(Math.abs(balance) <= 1, "Nodo con clave " + node.getKey() + " tiene factor de equilibrio = " + balance);
        assertNoNodeWithBalanceFactorAbsGreaterThan1(node.getLeft());
        assertNoNodeWithBalanceFactorAbsGreaterThan1(node.getRight());
    }

    // Calcula el factor de equilibrio de un nodo
    private int getBalance(AVLNode<Integer, String> node) {
        int leftHeight = (node.getLeft() != null) ? node.getLeft().getHeight() : 0;
        int rightHeight = (node.getRight() != null) ? node.getRight().getHeight() : 0;
        return rightHeight - leftHeight;
    }

    @Test
    void testLargeNumberOfInsertions() {
        for (int i = 1; i <= 1000; i++) {
            tree.insert(i, "Valor" + i);
        }

        assertEquals(1000, tree.getSize());
        assertEquals(Optional.of("Valor500"), tree.search(500));
        assertEquals(Optional.of("Valor1"), tree.search(1));
        assertEquals(Optional.of("Valor1000"), tree.search(1000));
    }
}
