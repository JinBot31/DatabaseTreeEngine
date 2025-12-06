package com.nosqlmanager.model;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa un documento JSON almacenado en el gestor.
 * Cada documento tiene una clave principal única (id numérico) y un contenido JSON.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JsonDocument {

    /**
     * Clave principal única del documento (numérica).
     */
    private Integer id;

    /**
     * Contenido del documento en formato JSON.
     */
    private JsonNode data;
}
