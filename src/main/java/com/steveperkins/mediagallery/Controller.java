package com.steveperkins.mediagallery;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
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
    private CheckMenuItem optionsAutoplay;
    @FXML
    private CheckMenuItem optionsLoop;
    @FXML
    private MenuItem helpAbout;
    @FXML
    private Label status;
    @FXML
    private StackPane content;
    @FXML
    private Button beginningButton;
    @FXML
    private Button backButton;
    @FXML
    private Button forwardButton;
    @FXML
    private Button endButton;
    @FXML
    private Button sizeButton;
    @FXML
    private Slider sizeSlider;

    private String[] args;
    private Stage stage;
    private Gallery gallery = new Gallery();
    private boolean fitsize = true;
    private ChangeListener<? super Number> sizeSliderListener;

    /**
     * Called automatically by JavaFX when creating the UI.
     *
     * @param location
     * @param resources
     */
    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        initializeMenuBar();
        initializeStatusBar();
        initializeDragAndDrop();

        // Load the initially-selected file, if there was one
        if (args != null && args.length > 0) {
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
        if (!gallery.isEmpty()) {
            if (event.getCode().equals(KeyCode.RIGHT) || event.getCode().equals(KeyCode.DOWN)) {
                renderNext();
            } else if (event.getCode().equals(KeyCode.LEFT) || event.getCode().equals(KeyCode.UP)) {
                renderPrevious();
            }
        }
    }

    /**
     * Registers event handlers for the menu bar actions.
     */
    private void initializeMenuBar() {
        fileOpen.setOnAction(actionEvent -> {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("All supported files",
                            GalleryItem.allExtensions.stream().map(ext -> "*" + ext).collect(Collectors.toList())),
                    new FileChooser.ExtensionFilter("Images",
                            GalleryItem.imageExtensions.stream().map(ext -> "*" + ext).collect(Collectors.toList())),
                    new FileChooser.ExtensionFilter("Audio/Video",
                            GalleryItem.videoExtensions.stream().map(ext -> "*" + ext).collect(Collectors.toList()))
            );
            final File file = fileChooser.showOpenDialog(null);
            loadFile(file);
        });
        fileExit.setOnAction(actionEvent -> Platform.exit());
        helpAbout.setOnAction(actionEvent -> {
            final Alert dialog = new Alert(
                    Alert.AlertType.NONE,
                    "MediaGallery\nby Steve Perkins\n\nhttps://gitlab.com/steve-perkins/MediaGallery-javafx\n\n",
                    ButtonType.CLOSE
            );
            dialog.setTitle("About");
            ((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons().add(new Image(getClass().getResource("/icon.png").toString()));
            dialog.showAndWait();
        });
    }

    /**
     * Initializes the controls and status label on the status bar.
     */
    private void initializeStatusBar() {
        beginningButton.setOnAction(event -> {
            renderFirst();
            content.requestFocus();
        });
        backButton.setOnAction(event -> {
            renderPrevious();
            content.requestFocus();
        });
        forwardButton.setOnAction(event -> {
            renderNext();
            content.requestFocus();
        });
        endButton.setOnAction(event -> {
            renderLast();
            content.requestFocus();
        });
        sizeButton.setOnAction(event -> {
            if (content.getChildren().size() < 1 || !(content.getChildren().get(0) instanceof ImageView)) return;
            if (fitsize) {
                // Switch to actual size
                resizeImage(1.0);
                final ImageView actualSizeImageView = new ImageView(new Image(getClass().getResourceAsStream("/actualsizebutton.png")));
                sizeButton.setGraphic(actualSizeImageView);
                fitsize = false;
            } else {
                // Switch to window fit size
                final ImageView imageView = (ImageView) content.getChildren().get(0);
                imageView.fitWidthProperty().unbind();
                imageView.fitHeightProperty().unbind();
                imageView.fitWidthProperty().bind(content.widthProperty());
                imageView.fitHeightProperty().bind(content.heightProperty());
                imageView.setViewport(null);
                final ImageView fitSizeImageView = new ImageView(new Image(getClass().getResourceAsStream("/fitsizebutton.png")));
                sizeButton.setGraphic(fitSizeImageView);
                fitsize = true;
            }
            // TODO: reset slider
            content.requestFocus();
        });
        sizeSliderListener = (observable, oldValue, newValue) -> {
            double ratio = 1 + (sizeSlider.getValue() / 100);
            ratio = ratio == 0 ? 0.01 : ratio;
            resizeImage(ratio);
        };
        sizeSlider.setOnMouseReleased(event -> content.requestFocus());
        sizeSlider.setOnKeyReleased(event -> content.requestFocus());
    }

    /**
     * Registers event handlers for loading files by drag-n-dropping them onto the window's main content area.
     */
    private void initializeDragAndDrop() {
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

        gallery.add(item);
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
     * Renders the item at the beginning of the gallery list.
     */
    private void renderFirst() {
        if (gallery == null) return;
        final GalleryItem item = gallery.first();
        render(item, 1);
    }

    /**
     * Renders the item at the end of the gallery list.
     */
    private void renderLast() {
        if (gallery == null) return;
        final GalleryItem item = gallery.last();
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

        if (content.getChildren().size() > 0 && content.getChildren().get(0) instanceof ImageView) {
            // Stop the status bar slider from resizing any previous image (this will be a no-op if there is no
            // existing listener)
            sizeSlider.valueProperty().removeListener(sizeSliderListener);

        } else if (content.getChildren().size() > 0 && content.getChildren().get(0) instanceof MediaControl) {
            // If the currently rendered item is a video, stop its player before proceeding
            final MediaControl previousMediaControl = (MediaControl) content.getChildren().get(0);
            previousMediaControl.getMediaPlayer().dispose();
        }

        if (item.isImage()) {
            renderImage(item.getItem(), position);
        } else if (item.isVideo()) {
            renderVideo(item.getItem(), position);
        }
    }

    /**
     * TODO
     *
     * @param item
     * @param position
     */
    private void renderImage(final File item, final int position) {
        try {
            final String imageURL = item.toURI().toURL().toExternalForm();
            final ImageView imageView = new ImageView(new Image(imageURL));
            imageView.setPreserveRatio(true);
            imageView.fitWidthProperty().bind(content.widthProperty());
            imageView.fitHeightProperty().bind(content.heightProperty());
            content.getChildren().clear();
            content.getChildren().add(imageView);
            sizeSlider.valueProperty().addListener(sizeSliderListener);
            status.setText(position + " of " + gallery.size());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * TODO
     *
     * @param item
     * @param position
     */
    private void renderVideo(final File item, final int position) {
        try {
            final String videoURL = item.toURI().toURL().toExternalForm();
            final MediaPlayer mediaPlayer = new MediaPlayer(new Media(videoURL));
            mediaPlayer.setAutoPlay(optionsAutoplay.isSelected());
            final MediaControl mediaControl = new MediaControl(mediaPlayer, optionsLoop.isSelected());
            content.getChildren().clear();
            content.getChildren().add(mediaControl);
            status.setText(position + " of " + gallery.size());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * TODO
     *
     * @param ratio
     */
    private void resizeImage(final double ratio) {
        if (content.getChildren().size() < 1 || !(content.getChildren().get(0) instanceof ImageView)) return;

        // Clear the current ImageView
        final ImageView imageView = (ImageView) content.getChildren().get(0);
        imageView.fitWidthProperty().unbind();
        imageView.fitHeightProperty().unbind();
        imageView.setViewport(null);
        content.getChildren().clear();

        // Calculate position and size
        final double imageWidth = imageView.getImage().getWidth() * ratio;
        final double imageHeight = imageView.getImage().getHeight() * ratio;
        final double contentWidth = content.getWidth();
        System.out.println(imageWidth + ", " + contentWidth + ", " + ratio);
//        if (imageWidth < contentWidth) {
//            imageView.setFitWidth(imageWidth);
//        } else {
//            final double contentImageRatio = contentWidth / imageWidth;
//            double x = (contentWidth - imageWidth) * contentImageRatio * -1;
//            imageView.setViewport(new Rectangle2D(x, content.getLayoutY(), content.getWidth() / ratio, content.getHeight() / ratio));
//        }


        final double imageToContentRatio = imageWidth / contentWidth;
//        System.out.println(imageToContentRatio);
        if (imageToContentRatio < 1) {
            imageView.setViewport(new Rectangle2D(0, 0, imageWidth, imageHeight));
        } else {
            imageView.setViewport(new Rectangle2D(0, 0, imageWidth, imageHeight));
        }





        // Re-add the updated ImageView
        content.getChildren().add(imageView);
        fitsize = false;
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
