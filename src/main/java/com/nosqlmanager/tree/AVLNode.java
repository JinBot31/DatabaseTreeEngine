package com.nosqlmanager.tree;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Representa un nodo en el árbol AVL.
 * @param <K> Tipo de la clave (debe ser comparable)
 * @param <V> Tipo del valor asociado a la clave
 */
@Data
@RequiredArgsConstructor
public class AVLNode<K extends Comparable<K>, V> {
    private AVLNode<K, V> left;
    private AVLNode<K, V> right;
    
    @NonNull
    private K key;
    @NonNull
    private V value;
    private int height = 1;
}
