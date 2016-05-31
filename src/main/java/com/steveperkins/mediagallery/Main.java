package com.steveperkins.mediagallery;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class Main extends Application {

    private static String[] args;
    private static Controller controller;

    @Override
    public void start(Stage primaryStage) throws Exception{
        final Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("sample.fxml"));
        final Scene scene = new Scene(root);
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (controller != null) controller.keyPressedEvent(event);
        });
        primaryStage.setScene(scene);
        primaryStage.setTitle("MediaGallery");
        primaryStage.show();
    }

    public static void main(final String[] args) {
        Main.args = args;
        launch(args);
    }

    static String[] getArgs() {
        return args;
    }

    static void setController(final Controller controller) {
        Main.controller = controller;
    }
}
