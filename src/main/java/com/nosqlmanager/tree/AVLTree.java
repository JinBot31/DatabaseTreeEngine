package com.nosqlmanager.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Implementación de un árbol AVL autobalanceado.
 * Permite operaciones eficientes de inserción, búsqueda, actualización y eliminación.
 *
 * @param <K> Tipo de la clave (debe ser comparable)
 * @param <V> Tipo del valor asociado a la clave
 */
@Data
@NoArgsConstructor
public class AVLTree<K extends Comparable<K>, V> {
    private AVLNode<K, V> root;
    private int size = 0;

    /**
     * Obtiene la altura de un nodo.
     * @param node Nodo del cual obtener la altura
     * @return Altura del nodo, 0 si es null
     */
    private int height(AVLNode<K, V> node) {
        return node == null ? 0 : node.getHeight();
    }

    /**
     * Calcula el factor de balance de un nodo.
     * @param node Nodo del cual calcular el balance
     * @return Factor de balance (altura izquierda - altura derecha)
     */
    private int getBalance(AVLNode<K, V> node) {
        return node == null ? 0 : height(node.getLeft()) - height(node.getRight());
    }

    /**
     * Actualiza la altura de un nodo basándose en sus hijos.
     * @param node Nodo a actualizar
     */
    private void updateHeight(AVLNode<K, V> node) {
        if (node != null) {
            node.setHeight(1 + Math.max(height(node.getLeft()), height(node.getRight())));
        }
    }

    /**
     * Rotación simple a la derecha.
     * @param y Nodo raíz de la rotación
     * @return Nueva raíz después de la rotación
     */
    private AVLNode<K, V> rotateRight(AVLNode<K, V> y) {
        AVLNode<K, V> x = y.getLeft();
        AVLNode<K, V> T2 = x.getRight();

        // Realizar rotación
        x.setRight(y);
        y.setLeft(T2);

        // Actualizar alturas
        updateHeight(y);
        updateHeight(x);

        return x;
    }

    /**
     * Rotación simple a la izquierda.
     * @param x Nodo raíz de la rotación
     * @return Nueva raíz después de la rotación
     */
    private AVLNode<K, V> rotateLeft(AVLNode<K, V> x) {
        AVLNode<K, V> y = x.getRight();
        AVLNode<K, V> T2 = y.getLeft();

        // Realizar rotación
        y.setLeft(x);
        x.setRight(T2);

        // Actualizar alturas
        updateHeight(x);
        updateHeight(y);

        return y;
    }

    /**
     * Inserta o actualiza un par clave-valor en el árbol.
     * @param key Clave a insertar
     * @param value Valor asociado a la clave
     */
    public void insert(K key, V value) {
        root = insertNode(root, key, value);
    }

    /**
     * Método recursivo para insertar un nodo y balancear el árbol.
     * @param node Nodo actual
     * @param key Clave a insertar
     * @param value Valor asociado
     * @return Nodo actualizado después de la inserción y balanceo
     */
    private AVLNode<K, V> insertNode(AVLNode<K, V> node, K key, V value) {
        // Inserción: claves menores a la izquierda, mayores a la derecha, igual actualiza valor
        if (node == null) {
            size++;
            return new AVLNode<>(key, value);
        }

        int comparison = key.compareTo(node.getKey());

        if (comparison < 0) {
            node.setLeft(insertNode(node.getLeft(), key, value));
        } else if (comparison > 0) {
            node.setRight(insertNode(node.getRight(), key, value));
        } else {
            // Clave igual: actualizar valor
            node.setValue(value);
            return node;
        }

        updateHeight(node);

        int balance = getBalance(node);

        // Caso Izquierda-Izquierda
        if (balance > 1 && key.compareTo(node.getLeft().getKey()) < 0) {
            return rotateRight(node);
        }

        // Caso Derecha-Derecha
        if (balance < -1 && key.compareTo(node.getRight().getKey()) > 0) {
            return rotateLeft(node);
        }

        // Caso Izquierda-Derecha
        if (balance > 1 && key.compareTo(node.getLeft().getKey()) > 0) {
            node.setLeft(rotateLeft(node.getLeft()));
            return rotateRight(node);
        }

        // Caso Derecha-Izquierda
        if (balance < -1 && key.compareTo(node.getRight().getKey()) < 0) {
            node.setRight(rotateRight(node.getRight()));
            return rotateLeft(node);
        }

        return node;
    }

    /**
     * Busca un valor por su clave.
     * @param key Clave a buscar
     * @return Optional con el valor si existe, vacío si no
     */
    public Optional<V> search(K key) {
        AVLNode<K, V> node = searchNode(root, key);
        return node == null ? Optional.empty() : Optional.of(node.getValue());
    }

    /**
     * Método recursivo para buscar un nodo por clave.
     * @param node Nodo actual
     * @param key Clave a buscar
     * @return Nodo encontrado o null
     */
    private AVLNode<K, V> searchNode(AVLNode<K, V> node, K key) {
        if (node == null) {
            return null;
        }

        int comparison = key.compareTo(node.getKey());

        if (comparison < 0) {
            return searchNode(node.getLeft(), key);
        } else if (comparison > 0) {
            return searchNode(node.getRight(), key);
        } else {
            return node;
        }
    }

    /**
     * Elimina un nodo por su clave.
     * @param key Clave a eliminar
     * @return true si se eliminó, false si no existía
     */
    public boolean delete(K key) {
        if (!contains(key)) {
            return false;
        }
        root = deleteNode(root, key);
        size--;
        return true;
    }

    /**
     * Método recursivo para eliminar un nodo y balancear el árbol.
     * @param node Nodo actual
     * @param key Clave a eliminar
     * @return Nodo actualizado después de la eliminación y balanceo
     */
    private AVLNode<K, V> deleteNode(AVLNode<K, V> node, K key) {
        if (node == null) {
            return null;
        }

        int comparison = key.compareTo(node.getKey());

        if (comparison < 0) {
            node.setLeft(deleteNode(node.getLeft(), key));
        } else if (comparison > 0) {
            node.setRight(deleteNode(node.getRight(), key));
        } else {

            // Nodo a eliminar encontrado

            // Caso 1: Nodo sin hijos o con un solo hijo
            if (node.getLeft() == null || node.getRight() == null) {
                AVLNode<K, V> temp = node.getLeft() != null ? node.getLeft() : node.getRight();

                if (temp == null) {
                    // Sin hijos
                    return null;
                } else {
                    // Un hijo
                    return temp;
                }
            } else {
                // Caso 2: Nodo con dos hijos
                // El sucesor es el hijo más izquierdo del subárbol derecho
                AVLNode<K, V> successor = getMinNode(node.getRight());

                // Copiar contenido del sucesor al nodo actual
                node.setKey(successor.getKey());
                node.setValue(successor.getValue());
                node.setRight(deleteNode(node.getRight(), successor.getKey()));
            }
        }

        // Actualizar altura
        updateHeight(node);

        // Obtener factor de balance
        int balance = getBalance(node);

        // Balancear el árbol

        // Caso Izquierda-Izquierda
        if (balance > 1 && getBalance(node.getLeft()) >= 0) {
            return rotateRight(node);
        }

        // Caso Izquierda-Derecha
        if (balance > 1 && getBalance(node.getLeft()) < 0) {
            node.setLeft(rotateLeft(node.getLeft()));
            return rotateRight(node);
        }

        // Caso Derecha-Derecha
        if (balance < -1 && getBalance(node.getRight()) <= 0) {
            return rotateLeft(node);
        }

        // Caso Derecha-Izquierda
        if (balance < -1 && getBalance(node.getRight()) > 0) {
            node.setRight(rotateRight(node.getRight()));
            return rotateLeft(node);
        }

        return node;
    }

    /**
     * Obtiene el nodo con la clave mínima en un subárbol.
     * @param node Raíz del subárbol
     * @return Nodo con la clave mínima
     */
    private AVLNode<K, V> getMinNode(AVLNode<K, V> node) {
        AVLNode<K, V> current = node;
        while (current.getLeft() != null) {
            current = current.getLeft();
        }
        return current;
    }

    /**
     * Verifica si una clave existe en el árbol.
     * @param key Clave a verificar
     * @return true si existe, false si no
     */
    public boolean contains(K key) {
        return searchNode(root, key) != null;
    }

    /**
     * Obtiene todas las claves del árbol en orden.
     * @return Lista de claves ordenadas
     */
    public List<K> getAllKeys() {
        List<K> keys = new ArrayList<>();
        inorderTraversal(root, keys);
        return keys;
    }

    /**
     * Recorrido inorden para obtener claves ordenadas.
     * @param node Nodo actual
     * @param keys Lista donde se acumulan las claves
     */
    private void inorderTraversal(AVLNode<K, V> node, List<K> keys) {
        if (node != null) {
            inorderTraversal(node.getLeft(), keys);
            keys.add(node.getKey());
            inorderTraversal(node.getRight(), keys);
        }
    }

        /**
     * Imprime el árbol por niveles, mostrando la estructura tipo array binario.
     * Cada nodo se muestra como (clave,altura).
     */
    public void printTree() {
        List<String> array = new ArrayList<>();
        fillArray(root, 0, array);
        System.out.println("[AVLTree] Representación tipo array binario:");
        for (int i = 0; i < array.size(); i++) {
            System.out.printf("[%d]: %s\n", i, array.get(i));
        }
    }

    // Rellena el array con la estructura del árbol, usando índices de heap binario
    private void fillArray(AVLNode<K, V> node, int index, List<String> array) {
        if (node == null) return;
        while (array.size() <= index) array.add("null");
        array.set(index, String.format("(%s,h=%d)", node.getKey(), node.getHeight()));
        fillArray(node.getLeft(), 2 * index + 1, array);
        fillArray(node.getRight(), 2 * index + 2, array);
    }
    
    /**
     * Verifica si el árbol está vacío.
     * @return true si está vacío, false si no
     */
    public boolean isEmpty() {
        return root == null;
    }

    /**
     * Limpia el árbol, eliminando todos los nodos.
     */
    public void clear() {
        root = null;
        size = 0;
    }
}
