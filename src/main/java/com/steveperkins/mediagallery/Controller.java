package com.steveperkins.mediagallery;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * The JavaFX controller bound to "main.fxml", the main UI window.
 */
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

    private String[] args;
    private Stage stage;
    private Gallery gallery;

    /**
     * Called automatically by JavaFX when creating the UI.  Creates event handlers, and checks for any
     * initially-selected file (i.e. passed as a command-line parameter or dragged onto the executable icon).
     *
     * @param location
     * @param resources
     */
    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        // Menu bar events
        fileOpen.setOnAction(actionEvent -> {
            // TODO: Limit the selection to only supported file extensions
            final FileChooser fileChooser = new FileChooser();
            final File file = fileChooser.showOpenDialog(null);
            loadFile(file);
        });
        fileExit.setOnAction(actionEvent -> Platform.exit());
        helpAbout.setOnAction(actionEvent -> {
            // TODO: Show about
        });

        // Drag-n-drop events
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

        // Load the initially-selected file, if there was one
        if (args == null || args.length < 1) {
            status.setText("No file selected");
        } else {
            loadFile(new File(args[0]));
        }
    }

    /**
     * <p>Processes key events, to scroll through the gallery items when arrow keys are pressed.</p>
     *
     * <p>It doesn't seem possible to register key event handlers for the main window from this controller
     * class.  So <code>Main</code> has to register the handler, and pass events here via this method.
     * There could potentially be thread safety issues (i.e. new scrolling operations coming in before
     * the previous operation finishes rendering), but I'm <i>somewhat</i> sure that the synchronous
     * nature of this method and the single-threadedness of <code>Main</code> prevents that.  Still,
     * there's probably a better way to approach this.</p>
     *
     * @param event
     */
    void keyPressedEvent(KeyEvent event) {
        if (gallery != null && gallery.size() > 0) {
            if (event.getCode().equals(KeyCode.RIGHT) || event.getCode().equals(KeyCode.DOWN)) {
                renderNext();
            } else if (event.getCode().equals(KeyCode.LEFT) || event.getCode().equals(KeyCode.UP)) {
                renderPrevious();
            }
        }
    }

    /**
     * <p>Called when a file is explicitly selected by the user (i.e. passed as a command-line parameter,
     * drag-n-dropped onto the executable icon, drag-n-dropped onto the application window after launch,
     * or selected from the File->Open menu item.</p>
     *
     * <p>Populates the gallery with all supported files in the same directory as the explicitly-selected
     * file, and renders that explicitly-selected file.  Or else does nothing if the selected file isn't
     * a supported media item.</p>
     *
     * @param file
     */
    private void loadFile(final File file) {
        if (gallery != null) {
            gallery.clear();
        }
        final GalleryItem item = GalleryItem.create(file);
        if (item == null) return;

        if (gallery == null) {
            gallery = new Gallery(item);
        } else {
            gallery.add(item);
        }
        gallery.addAll(findSiblingItems(item));
        render(item, 1);
    }

    /**
     * <p>Finds all files in the same directory as the parameter item.  The parameter file will be positioned as
     * the first element in the resulting list.  If the parameter file is null, then a non-null empty list will
     * be returned.</p>
     *
     * <p>Note that the returned list will contain <code>null</code> elements for the files that are not supported
     * media items.  The application relies upon the {@link Gallery} methods stripping out <code>null</code>'s.</p>
     *
     * @param item
     * @return
     */
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

    /**
     * Renders the next item in the gallery.
     */
    private void renderNext() {
        if (gallery == null) return;
        final GalleryItem item = gallery.next();
        final int position = gallery.getCursor() + 1;
        render(item, position);
    }

    /**
     * Renders the previous item in the gallery.
     */
    private void renderPrevious() {
        if (gallery == null) return;
        final GalleryItem item = gallery.previous();
        final int position = gallery.getCursor() + 1;
        render(item, position);
    }

    /**
     * Functionality common to {@link Controller#renderNext()} and {@link Controller#renderPrevious()}.
     *
     * @param item
     * @param position
     */
    private void render(final GalleryItem item, final int position) {
        if (item == null) return;
        stage.setTitle("MediaGallery - " + item.getItem().getName());
        // If the currently rendered item is a video, stop its player before proceeding
        if (content.getChildren().size() > 0 && content.getChildren().get(0) instanceof MediaControl) {
            final MediaControl previousMediaControl = (MediaControl) content.getChildren().get(0);
            previousMediaControl.getMediaPlayer().dispose();
        }
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
        } else if (item.isVideo()) {
            try {
                final String videoURL = item.getItem().toURI().toURL().toExternalForm();
                final MediaPlayer mediaPlayer = new MediaPlayer(new Media(videoURL));
                mediaPlayer.setAutoPlay(true);
                final MediaControl mediaControl = new MediaControl(mediaPlayer, true);
                content.getChildren().clear();
                content.getChildren().add(mediaControl);
                status.setText(position + " of " + gallery.size());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Allows {@link Main#start(Stage)} to inject the primary {@link Stage} for the JavaFX window, so
     * that this controller can update the title bar when loading media items.
     *
     * @param stage
     */
    public void setStage(final Stage stage) {
        this.stage = stage;
    }

    /**
     * Allows {@link Main#start(Stage)} to inject the arguments originally passed at application invocation,
     * so that {@link this#initialize(URL, ResourceBundle)} can determine if a filename to load was passed
     * at startup.
     *
     * @param args
     */
    public void setArgs(final String[] args) {
        this.args = args;
    }
}
