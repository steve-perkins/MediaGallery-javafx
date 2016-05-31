package com.steveperkins.mediagallery;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

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

    private Gallery gallery;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        fileOpen.setOnAction(actionEvent -> {
            final FileChooser fileChooser = new FileChooser();
            final File file = fileChooser.showOpenDialog(null);
            loadFile(file);
        });
        fileExit.setOnAction(actionEvent -> Platform.exit());
        helpAbout.setOnAction(actionEvent -> {
            // TODO: Show about
        });
        content.addEventHandler(DragEvent.DRAG_OVER, event -> {
            if (event.getDragboard().hasFiles() && GalleryItem.create(event.getDragboard().getFiles().get(0)) != null) {
                event.acceptTransferModes(TransferMode.LINK);
            } else {
                event.consume();
            }
        });
        content.addEventHandler(DragEvent.DRAG_DROPPED, event -> {
            if (event.getDragboard().hasFiles()) {
                loadFile(event.getDragboard().getFiles().get(0));
            }
        });

        if (Main.getArgs().length < 1) {
            status.setText("No file selected");
        } else {
            loadFile(new File(Main.getArgs()[0]));
        }
    }

    private void loadFile(final File file) {
        final GalleryItem item = GalleryItem.create(file);
        if (item == null) return;

        if (gallery == null) {
            gallery = new Gallery(item);
        } else {
            gallery.add(item);
        }
        gallery.addAll(findSiblingItems(item));
        renderNext();
    }

    private List<GalleryItem> findSiblingItems(final GalleryItem item) {
        final List<GalleryItem> returnValue = new ArrayList<>(Arrays.asList(item));
        if (item == null) return returnValue;
        returnValue.addAll(
            Arrays.stream(item.getItem().getParentFile().listFiles())
                .filter(sibling -> item.getItem().isFile() && !item.getItem().equals(sibling))
                .map(GalleryItem::create)
                .collect(Collectors.toList())
        );
        return returnValue;
    }

    private void renderNext() {
        if (gallery == null) return;
        final int position = gallery.getCursor() + 1;
        final GalleryItem item = gallery.next();
        if (item == null) return;
        if (item.isImage()) {
            try {
                final String imageURL = item.getItem().toURI().toURL().toExternalForm();
                final ImageView imageView = new ImageView(new Image(imageURL));
                imageView.setPreserveRatio(true);
                imageView.fitWidthProperty().bind(content.widthProperty());
                imageView.fitHeightProperty().bind(content.heightProperty());
                content.getChildren().clear();
                content.getChildren().add(imageView);
                status.setText(position + " of " + gallery.size());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }
}
