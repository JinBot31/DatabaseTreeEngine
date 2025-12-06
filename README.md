# DatabaseTreeEngine

A lightweight JSON document manager with AVL tree indexing and a modern JavaFX interface. Enables CRUD operations on a database persisted in `database.json` file, real-time AVL tree visualization, and a table view of data.

## Features

- Self-balancing AVL tree for efficient operations (insert, search, update, delete).
- JSON file persistence (automatic read/write).
- JavaFX Interface:
  - Tree View: gray nodes with animations and details on click.
  - Data View: table with ID and JSON content (pretty-print).
  - Details panel: ID, Data, Height, and Balance Factor.
  - Log panel hidden by default (can be shown/hidden).
- Search capabilities:
  - By ID.
  - By field (contains) and exact match (equals).
- Unit tests for AVL tree and `DatabaseManager`.

## Requirements

- Java 21
- Maven 3.9+

Optional (Linux):
- OpenJFX runtime if running outside Maven.

## Project Structure
```
DatabaseTreeEngine/
├─ database.json                 # Sample data
├─ pom.xml
├─ src/
│  ├─ main/java/app/dbEngine/
│  │  ├─ App.java                # Entry point (CLI that launches JavaFX)
│  │  ├─ gui/
│  │  │  ├─ MainView.java        # Main UI (menus, panels, handlers)
│  │  │  └─ TreeRender.java      # AVL tree visualizer (JavaFX Pane)
│  │  ├─ controller/DatabaseController.java     # Index/persistence orchestrator
│  │  ├─ model/JsonDocument.java          # JSON document model
│  │  ├─ repository/JsonRepository.java   # CRUD contract
│  │  ├─ storage/JsonFileStorage.java     # File persistence
│  │  └─ tree/
│  │     ├─ AVLNode.java
│  │     └─ AVLTree.java
│  └─ test/java/app/dbEngine/
│     ├─ controllerr/DatabaseControllerrTest.java
│     └─ tree/AVLTreeTest.java
└─ target/ ...
```

## How to Run

Using Maven (recommended):
```bash
# Compile
mvn -q clean package

# Run tests
mvn -q -DskipTests=false test

# Run the application
mvn -q javafx:run
```

If you prefer running with `java` directly (requires OpenJFX in classpath):
```bash
# Package
mvn -q clean package

# Run (example; adjust JavaFX module paths for your system)
java \
  --module-path /path/to/javafx/lib \
  --add-modules javafx.controls,javafx.fxml \
  -cp target/classes:$(dependency:list -DincludeArtifactIds=jackson-databind -DoutputAbsoluteArtifactFilename=true | grep "\.jar$" | tr '\n' ':') \
  com.nosqlmanager.App
```

**Note:** The manual command above is just a guide; use `mvn javafx:run` if you don't want to configure OpenJFX.

## Application Usage

### File Menu:
- **New Database:** Clears the index and restarts.
- **Open JSON:** Loads a `.json` file as database.
- **Save As:** Exports documents to a `.json` file.

### View Menu:
- **View Tree:** Displays the AVL tree (zoom, pan with middle/right mouse button).
- **View Data:** Shows the document table.
- The log panel starts hidden; you can show it from its button in the header (if visible) or integrate a toggle in the menu.

### Left Panel (Operations):
- **Insert:** ID + JSON.
- **Search:** By ID or by field (format: `field:value`).
- **Update:** ID + JSON.
- **Delete:** By ID.
- **Clear:** Empties the database.

### Right Panel (Details + JSON):
- Shows attributes of the selected node (ID, Data, Height, Balance Factor) and pretty-printed JSON.

## Test Data

The `database.json` file includes test users with uniform schema:
```json
{
  "id": 153,
  "data": {
    "nombre": "marta",
    "codigo": 999,
    "edad": 32,
    "email": "marta153@gmail.com"
  }
}
```
