package com.nosqlmanager.gui;

import java.io.File;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.nosqlmanager.manager.DatabaseManager;
import com.nosqlmanager.model.JsonDocument;
import com.nosqlmanager.tree.AVLNode;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Vista principal de la aplicación con interfaz moderna y profesional.
 * Proporciona una interfaz gráfica para gestionar una base de datos NoSQL
 * utilizando árboles AVL como estructura de indexación.
 * 
 * @author NoSQL Manager Team
 * @version 2.0
 */
public class MainView extends Application {

    private DatabaseManager dbManager;
    private TreeVisualizer treeVisualizer;
    private TextArea jsonViewer;
    private VBox nodeDetailsPanel;
    private TextArea logArea;
    private TextField idField;
    private TextArea dataField;
    private Label statusLabel;
    private ObjectMapper objectMapper;
    private VBox dynamicFormPanel;
    private BorderPane mainLayout;
    private VBox logPanel;
    private boolean logVisible = true;
    
    private Label idValueLabel;
    private VBox dataContentBox;
    private Label heightValueLabel;
    private Label balanceValueLabel;
    private Label stateValueLabel;
    
    private double scale = 1.0;
    private double translateX = 0;
    private double translateY = 0;
    private double lastMouseX;
    private double lastMouseY;
    private boolean isPanning = false;

    private static final String DEFAULT_DB_FILE = "database.json";
    private static final double MIN_SCALE = 0.3;
    private static final double MAX_SCALE = 3.0;
    
    private String currentOperation = null;

    /**
     * Inicializa la aplicación y construye la interfaz gráfica.
     * 
     * @param primaryStage Ventana principal de la aplicación
     */
    @Override
    public void start(Stage primaryStage) {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        dbManager = new DatabaseManager(DEFAULT_DB_FILE);

        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #11111b;");

        MenuBar menuBar = createMenuBar(primaryStage);
        mainLayout.setTop(menuBar);

        VBox leftPanel = createLeftPanel();
        mainLayout.setLeft(leftPanel);

        StackPane treePanel = createInteractiveTreePanel();
        mainLayout.setCenter(treePanel);

        VBox rightPanel = createRightPanel();
        mainLayout.setRight(rightPanel);

        logPanel = createCollapsibleLogPanel();
        mainLayout.setBottom(logPanel);

        Scene scene = new Scene(mainLayout, 1600, 900);
        primaryStage.setTitle("◈ NoSQL Database Manager | AVL Tree");
        primaryStage.setScene(scene);
        primaryStage.show();

        treeVisualizer.setTree(dbManager.getIndex());
        treeVisualizer.drawTree(false);

        log("[INFO] Base de datos cargada: " + dbManager.getSize() + " documentos");
    }

    /**
     * Crea la barra de menú con opciones de archivo y vista.
     * 
     * @param stage Ventana principal para diálogos modales
     * @return MenuBar configurado
     */
    private MenuBar createMenuBar(Stage stage) {
        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-background-color: #181825; -fx-border-color: #313244; -fx-border-width: 0 0 1 0;");

        Menu fileMenu = new Menu("Archivo");
        
        MenuItem newDbItem = new MenuItem("Nueva Base de Datos");
        newDbItem.setOnAction(e -> handleNewDatabase());
        
        MenuItem openItem = new MenuItem("Abrir JSON...");
        openItem.setOnAction(e -> handleLoad());
        
        MenuItem saveAsItem = new MenuItem("Guardar Como...");
        saveAsItem.setOnAction(e -> handleSaveAs(stage));
        
        MenuItem exitItem = new MenuItem("Salir");
        exitItem.setOnAction(e -> stage.close());
        
        fileMenu.getItems().addAll(newDbItem, openItem, saveAsItem, exitItem);
        
        Menu viewMenu = new Menu("Vista");
        
        MenuItem toggleLogItem = new MenuItem("Mostrar/Ocultar Log");
        toggleLogItem.setOnAction(e -> toggleLog());
        
        MenuItem resetZoomItem = new MenuItem("Restablecer Zoom");
        resetZoomItem.setOnAction(e -> resetZoom());
        
        viewMenu.getItems().addAll(toggleLogItem, resetZoomItem);
        
        menuBar.getMenus().addAll(fileMenu, viewMenu);
        
        fileMenu.setStyle("-fx-text-fill: #cdd6f4;");
        viewMenu.setStyle("-fx-text-fill: #cdd6f4;");
        
        return menuBar;
    }

    /**
     * Crea el panel izquierdo con selector de operaciones y formularios dinámicos.
     * 
     * @return VBox con el panel de operaciones
     */
    private VBox createLeftPanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(20));
        panel.setPrefWidth(340);
        panel.setStyle("-fx-background-color: #181825;");

        Label title = new Label("⚡ Operaciones CRUD");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setStyle("-fx-text-fill: #cdd6f4;");

        HBox operationSelector = createOperationSelector();

        dynamicFormPanel = new VBox(15);
        dynamicFormPanel.setPadding(new Insets(20));
        dynamicFormPanel.setStyle("-fx-background-color: #1e1e2e; -fx-background-radius: 10;");
        dynamicFormPanel.setMinHeight(300);
        VBox.setVgrow(dynamicFormPanel, Priority.ALWAYS);
        
        Label instructionLabel = new Label("Selecciona una operación");
        instructionLabel.setStyle("-fx-text-fill: #6c7086; -fx-font-size: 14;");
        instructionLabel.setAlignment(Pos.CENTER);
        dynamicFormPanel.getChildren().add(instructionLabel);
        dynamicFormPanel.setAlignment(Pos.CENTER);

        statusLabel = new Label("◆ Documentos: " + dbManager.getSize());
        statusLabel.setStyle("-fx-text-fill: #89dceb; -fx-font-size: 14; -fx-font-weight: bold;");

        panel.getChildren().addAll(title, operationSelector, dynamicFormPanel, statusLabel);

        return panel;
    }

    /**
     * Crea el selector de operaciones con estilo horizontal tipo pestañas.
     * 
     * @return HBox con botones de operación
     */
    private HBox createOperationSelector() {
        HBox selector = new HBox();
        selector.setStyle("-fx-background-color: #1e1e2e; -fx-background-radius: 10; -fx-padding: 5;");
        selector.setSpacing(0);
        selector.setAlignment(Pos.CENTER);

        String[] operations = {"+ Insertar", "◉ Buscar", "✎ Actualizar", "✖ Eliminar", "⟳ Limpiar"};
        String[] ids = {"insert", "search", "update", "delete", "clear"};

        for (int i = 0; i < operations.length; i++) {
            final String opId = ids[i];
            Button btn = new Button(operations[i]);
            btn.setPrefHeight(40);
            btn.setPrefWidth(67);
            btn.setStyle(
                "-fx-background-color: transparent; " +
                "-fx-text-fill: #a6adc8; " +
                "-fx-font-size: 11; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 6; " +
                "-fx-cursor: hand; " +
                "-fx-border-width: 0;"
            );
            
            btn.setOnAction(e -> selectOperation(opId, btn, selector));
            
            btn.setOnMouseEntered(e -> {
                if (!opId.equals(currentOperation)) {
                    btn.setStyle(
                        "-fx-background-color: #313244; " +
                        "-fx-text-fill: #cdd6f4; " +
                        "-fx-font-size: 11; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand; " +
                        "-fx-border-width: 0;"
                    );
                }
            });
            
            btn.setOnMouseExited(e -> {
                if (!opId.equals(currentOperation)) {
                    btn.setStyle(
                        "-fx-background-color: transparent; " +
                        "-fx-text-fill: #a6adc8; " +
                        "-fx-font-size: 11; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand; " +
                        "-fx-border-width: 0;"
                    );
                }
            });
            
            selector.getChildren().add(btn);
        }

        return selector;
    }

    /**
     * Maneja la selección de una operación y actualiza el formulario dinámico.
     * 
     * @param operation ID de la operación seleccionada
     * @param selectedBtn Botón que fue clickeado
     * @param container Contenedor de botones para actualizar estilos
     */
    private void selectOperation(String operation, Button selectedBtn, HBox container) {
        currentOperation = operation;
        
        for (javafx.scene.Node node : container.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                btn.setStyle(
                    "-fx-background-color: transparent; " +
                    "-fx-text-fill: #a6adc8; " +
                    "-fx-font-size: 11; " +
                    "-fx-font-weight: bold; " +
                    "-fx-background-radius: 6; " +
                    "-fx-cursor: hand; " +
                    "-fx-border-width: 0;"
                );
            }
        }
        
        selectedBtn.setStyle(
            "-fx-background-color: #89b4fa; " +
            "-fx-text-fill: #1e1e2e; " +
            "-fx-font-size: 11; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 6; " +
            "-fx-cursor: hand; " +
            "-fx-border-width: 0;"
        );
        
        updateDynamicForm(operation);
    }

    /**
     * Actualiza el panel de formulario según la operación seleccionada.
     * 
     * @param operation ID de la operación
     */
    private void updateDynamicForm(String operation) {
        dynamicFormPanel.getChildren().clear();
        dynamicFormPanel.setAlignment(Pos.TOP_LEFT);

        switch (operation) {
            case "insert":
                createInsertForm();
                break;
            case "search":
                createSearchForm();
                break;
            case "update":
                createUpdateForm();
                break;
            case "delete":
                createDeleteForm();
                break;
            case "clear":
                createClearForm();
                break;
        }
    }

    private void createInsertForm() {
        Label idLabel = new Label("ID del documento:");
        idLabel.setStyle("-fx-text-fill: #a6adc8; -fx-font-weight: bold;");
        
        idField = new TextField();
        idField.setPromptText("Ej: 1, 2, 3...");
        idField.setStyle("-fx-background-color: #313244; -fx-text-fill: #cdd6f4; -fx-prompt-text-fill: #6c7086; -fx-background-radius: 5;");

        Label dataLabel = new Label("Datos (JSON):");
        dataLabel.setStyle("-fx-text-fill: #a6adc8; -fx-font-weight: bold;");
        
        dataField = createSyntaxHighlightedTextArea();
        dataField.setPromptText("{\n  \"nombre\": \"Juan\",\n  \"edad\": 25\n}");
        dataField.setPrefRowCount(10);
        VBox.setVgrow(dataField, Priority.ALWAYS);

        Button executeBtn = createExecuteButton("✓ Insertar Documento", "#a6e3a1");
        executeBtn.setOnAction(e -> handleInsert());

        dynamicFormPanel.getChildren().addAll(idLabel, idField, dataLabel, dataField, executeBtn);
    }

    private void createSearchForm() {
        Label idLabel = new Label("ID a buscar:");
        idLabel.setStyle("-fx-text-fill: #a6adc8; -fx-font-weight: bold;");
        
        idField = new TextField();
        idField.setPromptText("Ej: 42");
        idField.setStyle("-fx-background-color: #313244; -fx-text-fill: #cdd6f4; -fx-prompt-text-fill: #6c7086; -fx-background-radius: 5;");
        idField.setOnAction(e -> handleSearch());

        Button executeBtn = createExecuteButton("◉ Buscar por ID", "#89b4fa");
        executeBtn.setOnAction(e -> handleSearch());
        
        Button searchByFieldBtn = createExecuteButton("⊕ Buscar por Campo", "#74c7ec");
        searchByFieldBtn.setOnAction(e -> handleSearchByField());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        dynamicFormPanel.getChildren().addAll(idLabel, idField, executeBtn, searchByFieldBtn, spacer);
    }

    private void createUpdateForm() {
        Label idLabel = new Label("ID del documento:");
        idLabel.setStyle("-fx-text-fill: #a6adc8; -fx-font-weight: bold;");
        
        idField = new TextField();
        idField.setPromptText("Ej: 1");
        idField.setStyle("-fx-background-color: #313244; -fx-text-fill: #cdd6f4; -fx-prompt-text-fill: #6c7086; -fx-background-radius: 5;");

        Label dataLabel = new Label("Nuevos datos (JSON):");
        dataLabel.setStyle("-fx-text-fill: #a6adc8; -fx-font-weight: bold;");
        
        dataField = createSyntaxHighlightedTextArea();
        dataField.setPromptText("{\n  \"nombre\": \"Juan Actualizado\"\n}");
        dataField.setPrefRowCount(10);
        VBox.setVgrow(dataField, Priority.ALWAYS);

        Button executeBtn = createExecuteButton("✓ Actualizar Documento", "#f9e2af");
        executeBtn.setOnAction(e -> handleUpdate());

        dynamicFormPanel.getChildren().addAll(idLabel, idField, dataLabel, dataField, executeBtn);
    }

    private void createDeleteForm() {
        Label idLabel = new Label("ID a eliminar:");
        idLabel.setStyle("-fx-text-fill: #a6adc8; -fx-font-weight: bold;");
        
        idField = new TextField();
        idField.setPromptText("Ej: 5");
        idField.setStyle("-fx-background-color: #313244; -fx-text-fill: #cdd6f4; -fx-prompt-text-fill: #6c7086; -fx-background-radius: 5;");
        idField.setOnAction(e -> handleDelete());

        Button executeBtn = createExecuteButton("✖ Eliminar Documento", "#f38ba8");
        executeBtn.setOnAction(e -> handleDelete());

        Label warningLabel = new Label("⚠ ADVERTENCIA: Esta acción es permanente");
        warningLabel.setStyle("-fx-text-fill: #fab387; -fx-font-size: 12;");
        warningLabel.setWrapText(true);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        dynamicFormPanel.getChildren().addAll(idLabel, idField, executeBtn, warningLabel, spacer);
    }

    private void createClearForm() {
        Label warningLabel = new Label("⚠ ADVERTENCIA");
        warningLabel.setStyle("-fx-text-fill: #f38ba8; -fx-font-size: 16; -fx-font-weight: bold;");
        
        Label infoLabel = new Label("Esta acción eliminará TODOS los documentos de la base de datos.");
        infoLabel.setStyle("-fx-text-fill: #a6adc8; -fx-font-size: 13;");
        infoLabel.setWrapText(true);

        Button executeBtn = createExecuteButton("⟳ Limpiar Base de Datos", "#f38ba8");
        executeBtn.setOnAction(e -> handleClear());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        dynamicFormPanel.getChildren().addAll(warningLabel, infoLabel, new Label(""), executeBtn, spacer);
        dynamicFormPanel.setAlignment(Pos.CENTER);
    }

    /**
     * Crea un área de texto con validación de sintaxis JSON.
     * 
     * @return TextArea configurado
     */
    private TextArea createSyntaxHighlightedTextArea() {
        TextArea area = new TextArea();
        area.setStyle(
            "-fx-background-color: #1e1e2e; " +
            "-fx-text-fill: #cdd6f4; " +
            "-fx-control-inner-background: #1e1e2e; " +
            "-fx-font-family: 'Consolas', 'Monaco', monospace; " +
            "-fx-font-size: 13; " +
            "-fx-highlight-fill: #45475a; " +
            "-fx-highlight-text-fill: #cdd6f4; " +
            "-fx-background-radius: 5;"
        );
        
        area.textProperty().addListener((obs, oldVal, newVal) -> {
            applySyntaxHighlight(area, newVal);
        });
        
        return area;
    }

    private void applySyntaxHighlight(TextArea area, String text) {
        boolean hasError = false;
        try {
            if (!text.trim().isEmpty()) {
                objectMapper.readTree(text);
            }
        } catch (Exception e) {
            hasError = true;
        }
        
        if (hasError && !text.trim().isEmpty()) {
            area.setStyle(
                "-fx-background-color: #1e1e2e; " +
                "-fx-text-fill: #f38ba8; " +
                "-fx-control-inner-background: #1e1e2e; " +
                "-fx-font-family: 'Consolas', 'Monaco', monospace; " +
                "-fx-font-size: 13; " +
                "-fx-border-color: #f38ba8; " +
                "-fx-border-width: 2; " +
                "-fx-background-radius: 5; " +
                "-fx-border-radius: 5;"
            );
        } else {
            area.setStyle(
                "-fx-background-color: #1e1e2e; " +
                "-fx-text-fill: #a6e3a1; " +
                "-fx-control-inner-background: #1e1e2e; " +
                "-fx-font-family: 'Consolas', 'Monaco', monospace; " +
                "-fx-font-size: 13; " +
                "-fx-border-color: #45475a; " +
                "-fx-border-width: 1; " +
                "-fx-background-radius: 5; " +
                "-fx-border-radius: 5;"
            );
        }
    }

    private Button createExecuteButton(String text, String color) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: #1e1e2e; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 12 20; " +
            "-fx-background-radius: 8; " +
            "-fx-font-size: 14; " +
            "-fx-cursor: hand;",
            color
        ));
        
        btn.setOnMouseEntered(e -> btn.setStyle(String.format(
            "-fx-background-color: derive(%s, 20%%); " +
            "-fx-text-fill: #1e1e2e; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 12 20; " +
            "-fx-background-radius: 8; " +
            "-fx-font-size: 14; " +
            "-fx-cursor: hand;",
            color
        )));
        
        btn.setOnMouseExited(e -> btn.setStyle(String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: #1e1e2e; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 12 20; " +
            "-fx-background-radius: 8; " +
            "-fx-font-size: 14; " +
            "-fx-cursor: hand;",
            color
        )));
        
        return btn;
    }

    /**
     * Crea el panel central con el visualizador del árbol AVL interactivo.
     * Incluye funcionalidad de zoom y paneo.
     * 
     * @return StackPane con el árbol y controles
     */
    private StackPane createInteractiveTreePanel() {
        treeVisualizer = new TreeVisualizer();
        treeVisualizer.setPrefSize(3000, 2000);

        StackPane wrapper = new StackPane(treeVisualizer);
        wrapper.setStyle("-fx-background-color: #1e1e2e;");
        
        ScrollPane scrollPane = new ScrollPane(wrapper);
        scrollPane.setStyle("-fx-background-color: #1e1e2e; -fx-background: #1e1e2e;");
        scrollPane.setPannable(true);
        scrollPane.setHvalue(0.5);
        scrollPane.setVvalue(0.1);

        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.isControlDown()) {
                event.consume();
                
                double deltaY = event.getDeltaY();
                double scaleFactor = (deltaY > 0) ? 1.1 : 0.9;
                
                scale *= scaleFactor;
                scale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale));
                
                treeVisualizer.setScaleX(scale);
                treeVisualizer.setScaleY(scale);
            }
        });

        wrapper.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.SECONDARY || event.getButton() == MouseButton.MIDDLE) {
                isPanning = true;
                lastMouseX = event.getSceneX();
                lastMouseY = event.getSceneY();
                event.consume();
            }
        });

        wrapper.setOnMouseDragged(event -> {
            if (isPanning) {
                double deltaX = event.getSceneX() - lastMouseX;
                double deltaY = event.getSceneY() - lastMouseY;
                
                translateX += deltaX;
                translateY += deltaY;
                
                treeVisualizer.setTranslateX(translateX);
                treeVisualizer.setTranslateY(translateY);
                
                lastMouseX = event.getSceneX();
                lastMouseY = event.getSceneY();
                event.consume();
            }
        });

        wrapper.setOnMouseReleased(event -> {
            if (event.getButton() == MouseButton.SECONDARY || event.getButton() == MouseButton.MIDDLE) {
                isPanning = false;
                event.consume();
            }
        });

        treeVisualizer.setOnNodeClick((key, doc) -> {
            showDocumentDetails(key, doc);
            log("[SELECT] Nodo seleccionado: " + key);
        });

        VBox zoomControls = createZoomControls();
        
        StackPane container = new StackPane();
        container.getChildren().addAll(scrollPane, zoomControls);
        StackPane.setAlignment(zoomControls, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(zoomControls, new Insets(15));

        return container;
    }

    /**
     * Crea los controles de zoom flotantes.
     * 
     * @return VBox con botones de zoom
     */
    private VBox createZoomControls() {
        VBox controls = new VBox(5);
        controls.setMaxWidth(60);
        controls.setMaxHeight(150);
        controls.setStyle(
            "-fx-background-color: rgba(24, 24, 37, 0.95); " +
            "-fx-padding: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-border-color: #313244; " +
            "-fx-border-radius: 8; " +
            "-fx-border-width: 1;"
        );

        Label zoomLabel = new Label("Zoom");
        zoomLabel.setStyle("-fx-text-fill: #a6adc8; -fx-font-size: 10; -fx-font-weight: bold;");
        zoomLabel.setAlignment(Pos.CENTER);
        zoomLabel.setMaxWidth(Double.MAX_VALUE);

        Button zoomInBtn = new Button("+");
        Button zoomOutBtn = new Button("-");
        Button resetBtn = new Button("⟲");
        
        for (Button btn : new Button[]{zoomInBtn, zoomOutBtn, resetBtn}) {
            btn.setPrefSize(40, 30);
            btn.setMaxSize(40, 30);
            btn.setStyle(
                "-fx-background-color: #313244; " +
                "-fx-text-fill: #cdd6f4; " +
                "-fx-font-size: 13; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 5; " +
                "-fx-cursor: hand;"
            );
            
            btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #45475a; " +
                "-fx-text-fill: #cdd6f4; " +
                "-fx-font-size: 14; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 6; " +
                "-fx-cursor: hand;"
            ));
            
            btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: #313244; " +
                "-fx-text-fill: #cdd6f4; " +
                "-fx-font-size: 14; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 6; " +
                "-fx-cursor: hand;"
            ));
        }

        zoomInBtn.setOnAction(e -> zoomIn());
        zoomOutBtn.setOnAction(e -> zoomOut());
        resetBtn.setOnAction(e -> resetZoom());

        controls.getChildren().addAll(zoomLabel, zoomInBtn, zoomOutBtn, resetBtn);
        controls.setAlignment(Pos.CENTER);

        return controls;
    }

    private void zoomIn() {
        scale *= 1.2;
        scale = Math.min(scale, MAX_SCALE);
        treeVisualizer.setScaleX(scale);
        treeVisualizer.setScaleY(scale);
    }

    private void zoomOut() {
        scale *= 0.8;
        scale = Math.max(scale, MIN_SCALE);
        treeVisualizer.setScaleX(scale);
        treeVisualizer.setScaleY(scale);
    }

    private void resetZoom() {
        scale = 1.0;
        translateX = 0;
        translateY = 0;
        treeVisualizer.setScaleX(scale);
        treeVisualizer.setScaleY(scale);
        treeVisualizer.setTranslateX(translateX);
        treeVisualizer.setTranslateY(translateY);
    }

    /**
     * Crea el panel derecho con visor de JSON y detalles del nodo.
     * 
     * @return VBox con paneles estructurados
     */
    private VBox createRightPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setPrefWidth(380);
        panel.setStyle("-fx-background-color: #181825;");

        // Node Details Section - Panel estructurado
        VBox detailsSection = createNodeDetailsPanel();

        // JSON Viewer Section
        Label jsonTitle = new Label("{ } Contenido JSON");
        jsonTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        jsonTitle.setStyle("-fx-text-fill: #cdd6f4;");

        jsonViewer = new TextArea();
        jsonViewer.setEditable(false);
        jsonViewer.setWrapText(true);
        jsonViewer.setStyle(
            "-fx-background-color: #1e1e2e; " +
            "-fx-text-fill: #a6e3a1; " +
            "-fx-control-inner-background: #1e1e2e; " +
            "-fx-font-family: 'Consolas', 'Monaco', monospace; " +
            "-fx-font-size: 13; " +
            "-fx-background-radius: 8;"
        );
        jsonViewer.setText("Haz clic en un nodo del arbol\npara ver su contenido aqui.");

        VBox.setVgrow(jsonViewer, Priority.ALWAYS);

        panel.getChildren().addAll(detailsSection, jsonTitle, jsonViewer);
        return panel;
    }

    /**
     * Crea el panel estructurado de detalles del nodo.
     * 
     * @return VBox con la estructura visual del panel de detalles
     */
    private VBox createNodeDetailsPanel() {
        VBox section = new VBox(0);
        section.setStyle("-fx-background-color: #1e1e2e; -fx-background-radius: 10;");
        section.setPadding(new Insets(20));

        // Titulo
        Label title = new Label("Detalles del Nodo");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: #89dceb;");
        VBox.setMargin(title, new Insets(0, 0, 20, 0));

        // ID Section
        Label idLabel = new Label("ID");
        idLabel.setStyle("-fx-text-fill: #6c7086; -fx-font-size: 12;");
        
        idValueLabel = new Label("-");
        idValueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        idValueLabel.setStyle("-fx-text-fill: #cdd6f4;");
        VBox.setMargin(idValueLabel, new Insets(2, 0, 15, 0));

        // Data Section
        Label dataLabel = new Label("Data");
        dataLabel.setStyle("-fx-text-fill: #6c7086; -fx-font-size: 12;");
        
        dataContentBox = new VBox(2);
        dataContentBox.setStyle("-fx-background-color: #313244; -fx-background-radius: 6; -fx-padding: 10;");
        VBox.setMargin(dataContentBox, new Insets(5, 0, 15, 0));
        
        Label dataPlaceholder = new Label("Selecciona un nodo");
        dataPlaceholder.setStyle("-fx-text-fill: #6c7086; -fx-font-size: 13;");
        dataContentBox.getChildren().add(dataPlaceholder);

        // Height and Balance Factor - Side by side
        HBox metricsRow = new HBox(30);
        metricsRow.setAlignment(Pos.CENTER_LEFT);
        
        VBox heightBox = new VBox(2);
        Label heightLabel = new Label("Altura");
        heightLabel.setStyle("-fx-text-fill: #6c7086; -fx-font-size: 12;");
        heightValueLabel = new Label("-");
        heightValueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        heightValueLabel.setStyle("-fx-text-fill: #a6e3a1;");
        heightBox.getChildren().addAll(heightLabel, heightValueLabel);
        
        VBox balanceBox = new VBox(2);
        Label balanceLabel = new Label("Factor Balance");
        balanceLabel.setStyle("-fx-text-fill: #6c7086; -fx-font-size: 12;");
        balanceValueLabel = new Label("-");
        balanceValueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        balanceValueLabel.setStyle("-fx-text-fill: #f38ba8;");
        balanceBox.getChildren().addAll(balanceLabel, balanceValueLabel);
        
        metricsRow.getChildren().addAll(heightBox, balanceBox);
        VBox.setMargin(metricsRow, new Insets(0, 0, 15, 0));

        // Estado Section
        Label stateLabel = new Label("Estado");
        stateLabel.setStyle("-fx-text-fill: #6c7086; -fx-font-size: 12;");
        
        stateValueLabel = new Label("-");
        stateValueLabel.setStyle(
            "-fx-background-color: #313244; " +
            "-fx-text-fill: #cdd6f4; " +
            "-fx-padding: 6 12; " +
            "-fx-background-radius: 4; " +
            "-fx-font-size: 13;"
        );
        VBox.setMargin(stateValueLabel, new Insets(5, 0, 0, 0));

        section.getChildren().addAll(
            title, 
            idLabel, idValueLabel, 
            dataLabel, dataContentBox, 
            metricsRow, 
            stateLabel, stateValueLabel
        );

        nodeDetailsPanel = section;
        return section;
    }

    /**
     * Resetea el panel de detalles del nodo a su estado inicial.
     * 
     * @param message Mensaje opcional a mostrar en el panel de datos
     */
    private void resetNodeDetails(String message) {
        idValueLabel.setText("-");
        dataContentBox.getChildren().clear();
        Label msgLabel = new Label(message);
        msgLabel.setStyle("-fx-text-fill: #6c7086; -fx-font-size: 13;");
        dataContentBox.getChildren().add(msgLabel);
        heightValueLabel.setText("-");
        balanceValueLabel.setText("-");
        balanceValueLabel.setStyle("-fx-text-fill: #f38ba8; -fx-font-size: 20; -fx-font-weight: bold;");
        stateValueLabel.setText("-");
        stateValueLabel.setStyle(
            "-fx-background-color: #313244; " +
            "-fx-text-fill: #cdd6f4; " +
            "-fx-padding: 6 12; " +
            "-fx-background-radius: 4; " +
            "-fx-font-size: 13;"
        );
    }

    /**
     * Crea el panel de log colapsable en la parte inferior.
     * 
     * @return VBox con log y controles
     */
    private VBox createCollapsibleLogPanel() {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(12, 20, 12, 20));
        panel.setStyle("-fx-background-color: #11111b; -fx-border-color: #313244; -fx-border-width: 1 0 0 0;");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("▣ Log de Operaciones");
        title.setStyle("-fx-text-fill: #a6adc8; -fx-font-weight: bold; -fx-font-size: 13;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button collapseBtn = new Button("Ocultar");
        collapseBtn.setStyle(
            "-fx-background-color: #313244; " +
            "-fx-text-fill: #cdd6f4; " +
            "-fx-font-size: 11; " +
            "-fx-padding: 6 12; " +
            "-fx-background-radius: 5; " +
            "-fx-cursor: hand;"
        );
        
        header.getChildren().addAll(title, spacer, collapseBtn);

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefRowCount(4);
        logArea.setStyle(
            "-fx-background-color: #1e1e2e; " +
            "-fx-text-fill: #6c7086; " +
            "-fx-control-inner-background: #1e1e2e; " +
            "-fx-font-family: 'Consolas', monospace; " +
            "-fx-font-size: 12; " +
            "-fx-background-radius: 5;"
        );

        collapseBtn.setOnAction(e -> {
            if (logVisible) {
                logArea.setVisible(false);
                logArea.setManaged(false);
                collapseBtn.setText("Mostrar");
                logVisible = false;
            } else {
                logArea.setVisible(true);
                logArea.setManaged(true);
                collapseBtn.setText("Ocultar");
                logVisible = true;
            }
        });

        panel.getChildren().addAll(header, logArea);
        return panel;
    }

    private void toggleLog() {
        if (logVisible) {
            mainLayout.setBottom(null);
            logVisible = false;
        } else {
            mainLayout.setBottom(logPanel);
            logVisible = true;
        }
    }
    
    private void handleNewDatabase() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Nueva Base de Datos");
        confirm.setHeaderText("Crear una nueva base de datos");
        confirm.setContentText("Esto limpiará todos los datos actuales.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            dbManager.clear();
            treeVisualizer.setTree(dbManager.getIndex());
            treeVisualizer.drawTree(false);
            updateStatus();
            jsonViewer.setText("Nueva base de datos creada.");
            resetNodeDetails("Base de datos vacia");
            log("[INFO] Nueva base de datos creada");
        }
    }

    private void handleInsert() {
        String idStr = idField.getText().trim();
        String jsonData = dataField.getText().trim();

        if (idStr.isEmpty()) {
            showError("El ID no puede estar vacío");
            return;
        }

        Integer id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            showError("El ID debe ser un número entero");
            return;
        }

        try {
            JsonNode data;
            if (jsonData.isEmpty()) {
                data = objectMapper.createObjectNode();
            } else {
                data = objectMapper.readTree(jsonData);
            }

            JsonDocument doc = new JsonDocument(id, data);
            dbManager.save(doc);

            treeVisualizer.setTree(dbManager.getIndex());
            treeVisualizer.animateInsert(id);

            updateStatus();
            log("[INSERT] Documento insertado: " + id);
            showDocumentDetails(id, doc);

            idField.clear();
            dataField.clear();

        } catch (Exception e) {
            showError("JSON inválido: " + e.getMessage());
            log("[ERROR] Error al insertar: " + e.getMessage());
        }
    }

    private void handleSearch() {
        String idStr = idField.getText().trim();

        if (idStr.isEmpty()) {
            showError("El ID no puede estar vacío");
            return;
        }

        Integer id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            showError("El ID debe ser un número entero");
            return;
        }

        Optional<JsonDocument> found = dbManager.findById(id);
        if (found.isPresent()) {
            treeVisualizer.animateSearch(id);
            showDocumentDetails(id, found.get());
            log("[SEARCH] Documento encontrado: " + id);
        } else {
            showError("Documento no encontrado: " + id);
            log("[SEARCH] Documento no encontrado: " + id);
        }
    }

    private void handleSearchByField() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Buscar por campo");
        dialog.setHeaderText("Ingresa el nombre del campo y valor (campo:valor)");
        dialog.setContentText("Ejemplo: ciudad:Bogotá");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && result.get().contains(":")) {
            String[] parts = result.get().split(":", 2);
            var docs = dbManager.findByField(parts[0], parts[1]);
            log("[SEARCH] Búsqueda por campo '" + parts[0] + "': " + docs.size() + " resultados");

            if (!docs.isEmpty()) {
                StringBuilder sb = new StringBuilder("Resultados:\n\n");
                for (JsonDocument doc : docs) {
                    sb.append("ID: ").append(doc.getId()).append("\n");
                    try {
                        sb.append(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(doc.getData()));
                    } catch (Exception e) {
                        sb.append(doc.getData().toString());
                    }
                    sb.append("\n\n");
                }
                jsonViewer.setText(sb.toString());
                resetNodeDetails("Busqueda: " + docs.size() + " resultados");
            } else {
                jsonViewer.setText("No se encontraron resultados.");
                resetNodeDetails("Sin resultados");
            }
        }
    }

    private void handleUpdate() {
        String idStr = idField.getText().trim();
        String jsonData = dataField.getText().trim();

        if (idStr.isEmpty()) {
            showError("El ID no puede estar vacío");
            return;
        }

        Integer id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            showError("El ID debe ser un número entero");
            return;
        }

        try {
            JsonNode data = objectMapper.readTree(jsonData);
            JsonDocument doc = new JsonDocument(id, data);

            if (dbManager.update(doc)) {
                treeVisualizer.setTree(dbManager.getIndex());
                treeVisualizer.animateInsert(id);
                showDocumentDetails(id, doc);
                log("[UPDATE] Documento actualizado: " + id);
                idField.clear();
                dataField.clear();
            } else {
                showError("Documento no existe: " + id);
                log("[ERROR] No se pudo actualizar, documento no existe: " + id);
            }

        } catch (Exception e) {
            showError("JSON inválido: " + e.getMessage());
            log("[ERROR] Error al actualizar: " + e.getMessage());
        }
    }

    private void handleDelete() {
        String idStr = idField.getText().trim();

        if (idStr.isEmpty()) {
            showError("El ID no puede estar vacío");
            return;
        }

        Integer id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            showError("El ID debe ser un número entero");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("Eliminar documento");
        confirm.setContentText("ID: " + id);

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            treeVisualizer.animateDelete(id);

            if (dbManager.deleteById(id)) {
                treeVisualizer.setTree(dbManager.getIndex());
                updateStatus();
                jsonViewer.setText("Documento eliminado: " + id);
                resetNodeDetails("Documento eliminado");
                log("[DELETE] Documento eliminado: " + id);
                idField.clear();
            } else {
                showError("Documento no encontrado: " + id);
                log("[ERROR] No se pudo eliminar, documento no existe: " + id);
            }
        }
    }

    private void handleClear() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar limpieza");
        confirm.setHeaderText("Eliminar TODOS los documentos");
        confirm.setContentText("Esta acción no se puede deshacer.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            dbManager.clear();
            treeVisualizer.setTree(dbManager.getIndex());
            treeVisualizer.drawTree(false);
            updateStatus();
            jsonViewer.setText("Base de datos limpiada.");
            resetNodeDetails("Base de datos vacia");
            log("[CLEAR] Base de datos limpiada");
        }
    }

    private void handleLoad() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Abrir base de datos");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            dbManager = new DatabaseManager(file.getAbsolutePath());
            treeVisualizer.setTree(dbManager.getIndex());
            treeVisualizer.drawTree(true);
            updateStatus();
            log("[LOAD] Base de datos cargada desde: " + file.getName());
        }
    }

    private void handleSaveAs(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar base de datos como");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        fileChooser.setInitialFileName("database.json");

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                var docs = dbManager.getAllDocuments();
                objectMapper.writeValue(file, docs);
                log("[SAVE] Base de datos guardada en: " + file.getName());
                showInfo("Base de datos guardada correctamente.");
            } catch (Exception e) {
                showError("Error al guardar: " + e.getMessage());
                log("[ERROR] Error al guardar: " + e.getMessage());
            }
        }
    }

    /**
     * Muestra los detalles completos de un nodo del árbol AVL.
     * 
     * @param id ID del documento
     * @param doc Documento asociado al nodo
     */
    private void showDocumentDetails(Integer id, JsonDocument doc) {
        try {
            AVLNode<Integer, JsonDocument> node = findNode(dbManager.getIndex().getRoot(), id);
            
            idValueLabel.setText(String.valueOf(id));
            
            dataContentBox.getChildren().clear();
            try {
                JsonNode data = doc.getData();
                if (data.isObject()) {
                    data.fields().forEachRemaining(entry -> {
                        String value = entry.getValue().isTextual() ? 
                            entry.getValue().asText() : 
                            entry.getValue().toString();
                        Label fieldLabel = new Label(entry.getKey() + "  " + value);
                        fieldLabel.setStyle("-fx-text-fill: #cdd6f4; -fx-font-size: 13;");
                        dataContentBox.getChildren().add(fieldLabel);
                    });
                } else {
                    Label dataLabel = new Label(data.toString());
                    dataLabel.setStyle("-fx-text-fill: #cdd6f4; -fx-font-size: 13;");
                    dataContentBox.getChildren().add(dataLabel);
                }
            } catch (Exception e) {
                Label errorLabel = new Label("Error al parsear data");
                errorLabel.setStyle("-fx-text-fill: #f38ba8; -fx-font-size: 13;");
                dataContentBox.getChildren().add(errorLabel);
            }
            
            if (node != null) {
                heightValueLabel.setText(String.valueOf(node.getHeight()));
                
                int balanceFactor = getBalanceFactor(node);
                balanceValueLabel.setText(String.valueOf(balanceFactor));
                
                if (balanceFactor >= -1 && balanceFactor <= 1) {
                    balanceValueLabel.setStyle("-fx-text-fill: #a6e3a1; -fx-font-size: 20; -fx-font-weight: bold;");
                } else {
                    balanceValueLabel.setStyle("-fx-text-fill: #f38ba8; -fx-font-size: 20; -fx-font-weight: bold;");
                }
                
                String state = getNodeState(balanceFactor);
                stateValueLabel.setText(state);
                
                String stateColor;
                if (state.equals("Balanceado")) {
                    stateColor = "#a6e3a1";
                } else if (state.equals("Aceptable")) {
                    stateColor = "#f9e2af";
                } else {
                    stateColor = "#f38ba8";
                }
                stateValueLabel.setStyle(
                    "-fx-background-color: #313244; " +
                    "-fx-text-fill: " + stateColor + "; " +
                    "-fx-padding: 6 12; " +
                    "-fx-background-radius: 4; " +
                    "-fx-font-size: 13; " +
                    "-fx-font-weight: bold;"
                );
            } else {
                heightValueLabel.setText("-");
                balanceValueLabel.setText("-");
                stateValueLabel.setText("-");
            }
            
            String jsonStr = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(doc.getData());
            jsonViewer.setText(jsonStr);
            
        } catch (Exception e) {
            idValueLabel.setText("Error");
            dataContentBox.getChildren().clear();
            Label errorLabel = new Label(e.getMessage());
            errorLabel.setStyle("-fx-text-fill: #f38ba8; -fx-font-size: 12;");
            dataContentBox.getChildren().add(errorLabel);
            jsonViewer.setText("Error al mostrar documento: " + e.getMessage());
        }
    }

    /**
     * Busca un nodo específico en el árbol AVL.
     * 
     * @param node Nodo actual
     * @param key Clave a buscar
     * @return Nodo encontrado o null
     */
    private AVLNode<Integer, JsonDocument> findNode(AVLNode<Integer, JsonDocument> node, Integer key) {
        if (node == null) return null;
        
        int cmp = key.compareTo(node.getKey());
        if (cmp == 0) return node;
        if (cmp < 0) return findNode(node.getLeft(), key);
        return findNode(node.getRight(), key);
    }

    /**
     * Calcula el factor de balance de un nodo.
     * 
     * @param node Nodo a evaluar
     * @return Factor de balance (altura derecha - altura izquierda)
     */
    private int getBalanceFactor(AVLNode<Integer, JsonDocument> node) {
        if (node == null) return 0;
        int leftHeight = node.getLeft() != null ? node.getLeft().getHeight() : 0;
        int rightHeight = node.getRight() != null ? node.getRight().getHeight() : 0;
        return rightHeight - leftHeight;
    }

    /**
     * Determina el estado de balance de un nodo.
     * 
     * @param balanceFactor Factor de balance del nodo
     * @return Descripcion del estado
     */
    private String getNodeState(int balanceFactor) {
        if (balanceFactor > 1) return "Desbalanceado (Der pesado)";
        if (balanceFactor < -1) return "Desbalanceado (Izq pesado)";
        if (balanceFactor == 0) return "Perfecto";
        return "Aceptable";
    }

    /**
     * Calcula el nivel de un nodo en el árbol.
     * 
     * @param node Nodo raíz
     * @param key Clave a buscar
     * @param level Nivel actual
     * @return Nivel del nodo o -1 si no existe
     */
    private int getNodeLevel(AVLNode<Integer, JsonDocument> node, Integer key, int level) {
        if (node == null) return -1;
        
        int cmp = key.compareTo(node.getKey());
        if (cmp == 0) return level;
        if (cmp < 0) return getNodeLevel(node.getLeft(), key, level + 1);
        return getNodeLevel(node.getRight(), key, level + 1);
    }

    private void updateStatus() {
        statusLabel.setText("◆ Documentos: " + dbManager.getSize());
    }

    private void log(String message) {
        String timestamp = java.time.LocalTime.now().toString().substring(0, 8);
        logArea.appendText("[" + timestamp + "] " + message + "\n");
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
