package com.steveperkins.mediagallery;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static String[] args;

    @Override
    public void start(Stage primaryStage) throws Exception{
        final Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("sample.fxml"));
        primaryStage.setScene(new Scene(root));
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

}
