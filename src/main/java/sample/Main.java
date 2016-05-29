package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static Main instance;
    private static String[] args;
    private Stage stage;

    public Main() {
        instance = this;
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        final Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("sample.fxml"));
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("MediaGallery");
        primaryStage.show();
    }

    public static void main(String[] args) {
        Main.args = args;
        launch(args);
    }

    static Main getInstance() {
        return instance;
    }

    static String[] getArgs() {
        return args;
    }

    Stage getStage() {
        return stage;
    }

}
