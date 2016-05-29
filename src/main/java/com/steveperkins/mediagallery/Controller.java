package com.steveperkins.mediagallery;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private MenuItem fileOpen;

    @FXML
    private MenuItem fileExit;

    @FXML
    private MenuItem helpAbout;

    @FXML
    private Label status;

    @FXML
    private StackPane content;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        fileOpen.setOnAction(actionEvent -> {
            final FileChooser fileChooser = new FileChooser();
            final File file = fileChooser.showOpenDialog(null);
            loadImage(file);
        });
        fileExit.setOnAction(actionEvent -> Platform.exit());
        helpAbout.setOnAction(actionEvent -> {
            // TODO: Show about
        });

        if (Main.getArgs().length < 1) {
            status.setText("No file selected");
        } else {
            loadImage(new File(Main.getArgs()[0]));
        }
    }

    private void loadImage(final File file) {
        if (file == null) return;
        try {
            final String imageURL = file.toURI().toURL().toExternalForm();
            final ImageView imageView = new ImageView(new Image(imageURL));
            imageView.setPreserveRatio(true);
            imageView.fitWidthProperty().bind(content.widthProperty());
            imageView.fitHeightProperty().bind(content.heightProperty());
            content.getChildren().clear();
            content.getChildren().add(imageView);
            status.setText("1 of 1");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
