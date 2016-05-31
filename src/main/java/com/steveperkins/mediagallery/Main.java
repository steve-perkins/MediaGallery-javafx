package com.steveperkins.mediagallery;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * The main class and entry point for the JavaFX application.
 */
public class Main extends Application {

    private static String[] args;
    private static Controller controller;

    public static void main(final String[] args) {
        Main.args = args;
        launch(args);
    }

    /**
     * <p>Called automatically by JavaFX (via the {@link Main#launch(String...)} invocation in
     * {@link Main#main(String[])}, to create the UI.</p>
     *
     * <p>Loads the main FXML file and controller class, and registers a handler so that arrow key events
     * can be passed to {@link Controller#keyPressedEvent(KeyEvent)} for scrolling through the gallery.</p>
     *
     * @param primaryStage
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception{
        final Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("main.fxml"));
        final Scene scene = new Scene(root);
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (controller != null) controller.keyPressedEvent(event);
        });
        primaryStage.setScene(scene);
        primaryStage.setTitle("MediaGallery");
        primaryStage.show();
    }

    /**
     * The arguments originally passed to this application at invocation are stored in a static field, and
     * make accessible to {@link Controller#initialize(URL, ResourceBundle)} so that it can load any
     * initially-selected file.
     *
     * @return
     */
    static String[] getArgs() {
        return args;
    }

    /**
     * {@link Controller#initialize(URL, ResourceBundle)} injects its object instance here via this static
     * setter, so that the key event handler registered in {@link Main#start(Stage)} can pass key events
     * to the controller.
     *
     * @param controller
     */
    static void setController(final Controller controller) {
        Main.controller = controller;
    }
}
