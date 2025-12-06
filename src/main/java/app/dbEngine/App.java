package app.dbEngine;

import app.dbEngine.gui.MainView;

/**
 * Punto de entrada de la aplicación.
 * Lanza la interfaz gráfica JavaFX para el gestor de base de datos NoSQL.
 */
public class App {
    public static void main(String[] args) {
        System.out.println();
        System.out.println("Ejecución de la aplicación...");
        System.out.println();
        MainView.main(args);
    }
}
