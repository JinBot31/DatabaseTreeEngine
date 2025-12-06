package com.nosqlmanager.gui;

import java.util.HashMap;
import java.util.Map;

import com.nosqlmanager.model.JsonDocument;
import com.nosqlmanager.tree.AVLNode;
import com.nosqlmanager.tree.AVLTree;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Componente visual que dibuja el árbol AVL de forma interactiva.
 * Los nodos se pueden clickear para ver el documento JSON asociado.
 */
public class TreeVisualizer extends Pane {

    private static final double NODE_RADIUS = 25;
    private static final double VERTICAL_SPACING = 85;
    private static final double INITIAL_HORIZONTAL_SPACING = 500;
    private static final Duration ANIMATION_DURATION = Duration.millis(500);

    private AVLTree<Integer, JsonDocument> tree;
    private Map<Integer, Point2D> nodePositions = new HashMap<>();
    private NodeClickHandler clickHandler;
    private Integer highlightedKey = null;

    /**
     * Interfaz para manejar clics en nodos
     */
    public interface NodeClickHandler {
        void onNodeClick(Integer key, JsonDocument document);
    }

    public TreeVisualizer() {
        this.setStyle("-fx-background-color: #1e1e2e;");
        this.setMinSize(800, 500);
    }

    public void setTree(AVLTree<Integer, JsonDocument> tree) {
        this.tree = tree;
    }

    public void setOnNodeClick(NodeClickHandler handler) {
        this.clickHandler = handler;
    }

    public void setHighlightedKey(Integer key) {
        this.highlightedKey = key;
        drawTree(false);
    }

    /**
     * Dibuja el árbol completo con animación
     */
    public void drawTree(boolean animate) {
        this.getChildren().clear();
        nodePositions.clear();

        if (tree == null || tree.getRoot() == null) {
            drawEmptyMessage();
            return;
        }

        double centerX = this.getWidth() / 2;
        if (centerX < 400) centerX = 400;
        calculatePositions(tree.getRoot(), centerX, 50, INITIAL_HORIZONTAL_SPACING);

        drawLines(tree.getRoot(), animate);

        drawNodes(tree.getRoot(), animate);
    }

    private void drawEmptyMessage() {
        Text text = new Text("🌳 Árbol vacío - Agrega documentos para comenzar");
        text.setFill(Color.web("#cdd6f4"));
        text.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        text.setX(this.getWidth() / 2 - 200);
        text.setY(this.getHeight() / 2);
        this.getChildren().add(text);
    }

    /**
     * Calcula las posiciones de cada nodo recursivamente
     */
    private void calculatePositions(AVLNode<Integer, JsonDocument> node, double x, double y, double hSpacing) {
        if (node == null) return;

        nodePositions.put(node.getKey(), new Point2D(x, y));

        double nextHSpacing = hSpacing * 0.52;
        if (nextHSpacing < 70) nextHSpacing = 70;

        if (node.getLeft() != null) {
            calculatePositions(node.getLeft(), x - hSpacing, y + VERTICAL_SPACING, nextHSpacing);
        }
        if (node.getRight() != null) {
            calculatePositions(node.getRight(), x + hSpacing, y + VERTICAL_SPACING, nextHSpacing);
        }
    }

    /**
     * Dibuja las líneas que conectan los nodos
     */
    private void drawLines(AVLNode<Integer, JsonDocument> node, boolean animate) {
        if (node == null) return;

        Point2D pos = nodePositions.get(node.getKey());

        if (node.getLeft() != null) {
            Point2D leftPos = nodePositions.get(node.getLeft().getKey());
            Line line = createLine(pos, leftPos, animate);
            this.getChildren().add(line);
            drawLines(node.getLeft(), animate);
        }

        if (node.getRight() != null) {
            Point2D rightPos = nodePositions.get(node.getRight().getKey());
            Line line = createLine(pos, rightPos, animate);
            this.getChildren().add(line);
            drawLines(node.getRight(), animate);
        }
    }

    private Line createLine(Point2D from, Point2D to, boolean animate) {
        Line line = new Line(from.getX(), from.getY(), to.getX(), to.getY());
        line.setStroke(Color.web("#585b70"));
        line.setStrokeWidth(2);

        if (animate) {
            line.setOpacity(0);
            FadeTransition fade = new FadeTransition(ANIMATION_DURATION, line);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        }

        return line;
    }

    /**
     * Dibuja los nodos del árbol
     */
    private void drawNodes(AVLNode<Integer, JsonDocument> node, boolean animate) {
        if (node == null) return;

        Point2D pos = nodePositions.get(node.getKey());
        createNodeVisual(node, pos.getX(), pos.getY(), animate);

        drawNodes(node.getLeft(), animate);
        drawNodes(node.getRight(), animate);
    }

    private void createNodeVisual(AVLNode<Integer, JsonDocument> node, double x, double y, boolean animate) {
        Integer key = node.getKey();
        boolean isHighlighted = key.equals(highlightedKey);

        // Círculo del nodo
        Circle circle = new Circle(x, y, NODE_RADIUS);
        circle.setFill(isHighlighted ? Color.web("#f38ba8") : Color.web("#89b4fa"));
        circle.setStroke(isHighlighted ? Color.web("#f5c2e7") : Color.web("#cdd6f4"));
        circle.setStrokeWidth(3);
        circle.setCursor(javafx.scene.Cursor.HAND);

        // Texto con la clave
        Text keyText = new Text(String.valueOf(key));
        keyText.setFill(Color.web("#1e1e2e"));
        keyText.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        keyText.setX(x - keyText.getLayoutBounds().getWidth() / 2);
        keyText.setY(y + 5);

        // Texto con la altura (factor de balance)
        int balance = getBalance(node);
        Text balanceText = new Text("h:" + node.getHeight() + " b:" + balance);
        balanceText.setFill(Color.web("#a6adc8"));
        balanceText.setFont(Font.font("Arial", FontWeight.NORMAL, 9));
        balanceText.setX(x - balanceText.getLayoutBounds().getWidth() / 2);
        balanceText.setY(y + NODE_RADIUS + 15);

        // Eventos de clic
        circle.setOnMouseClicked(e -> {
            if (clickHandler != null) {
                clickHandler.onNodeClick(key, node.getValue());
            }
        });

        circle.setOnMouseEntered(e -> {
            circle.setScaleX(1.1);
            circle.setScaleY(1.1);
            circle.setFill(Color.web("#f9e2af"));
        });

        circle.setOnMouseExited(e -> {
            circle.setScaleX(1.0);
            circle.setScaleY(1.0);
            circle.setFill(isHighlighted ? Color.web("#f38ba8") : Color.web("#89b4fa"));
        });

        // Animación de entrada
        if (animate) {
            circle.setScaleX(0);
            circle.setScaleY(0);
            keyText.setOpacity(0);
            balanceText.setOpacity(0);

            ScaleTransition scale = new ScaleTransition(ANIMATION_DURATION, circle);
            scale.setFromX(0);
            scale.setFromY(0);
            scale.setToX(1);
            scale.setToY(1);

            FadeTransition fadeKey = new FadeTransition(ANIMATION_DURATION, keyText);
            fadeKey.setFromValue(0);
            fadeKey.setToValue(1);

            FadeTransition fadeBalance = new FadeTransition(ANIMATION_DURATION, balanceText);
            fadeBalance.setFromValue(0);
            fadeBalance.setToValue(1);

            ParallelTransition parallel = new ParallelTransition(scale, fadeKey, fadeBalance);
            parallel.play();
        }

        this.getChildren().addAll(circle, keyText, balanceText);
    }

    private int getBalance(AVLNode<Integer, JsonDocument> node) {
        int leftHeight = node.getLeft() != null ? node.getLeft().getHeight() : 0;
        int rightHeight = node.getRight() != null ? node.getRight().getHeight() : 0;
        return rightHeight - leftHeight;
    }

    /**
     * Anima la inserción de un nuevo nodo
     */
    public void animateInsert(Integer key) {
        highlightedKey = key;
        drawTree(true);

        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> {
            highlightedKey = null;
            drawTree(false);
        });
        pause.play();
    }

    /**
     * Anima la eliminación de un nodo
     */
    public void animateDelete(Integer key) {
        highlightedKey = key;
        drawTree(true);

        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.millis(300));
        pause.setOnFinished(e -> {
            highlightedKey = null;
            drawTree(true);
        });
        pause.play();
    }

    /**
     * Anima la búsqueda de un nodo
     */
    public void animateSearch(Integer key) {
        highlightedKey = key;
        drawTree(false);

        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> {
            highlightedKey = null;
            drawTree(false);
        });
        pause.play();
    }
}
