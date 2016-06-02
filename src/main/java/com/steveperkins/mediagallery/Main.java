package com.steveperkins.mediagallery;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * The main class and entry point for the JavaFX application.
 */
public class Main extends Application {

    private static String[] args;

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
    public void start(Stage primaryStage) throws Exception {
        final FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main.fxml"));
        Controller controller = new Controller();
        controller.setArgs(args);
        controller.setStage(primaryStage);
        loader.setController(controller);

        final Parent root = loader.load();
        final Scene scene = new Scene(root);
        scene.addEventHandler(KeyEvent.KEY_PRESSED, controller::keyPressedEvent);
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image(getClass().getResource("/icon.png").toString()));
        primaryStage.setTitle("MediaGallery");
        primaryStage.show();
    }

}
