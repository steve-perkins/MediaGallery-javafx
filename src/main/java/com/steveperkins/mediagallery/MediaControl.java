/*
 * Copyright (c) 2008, 2014, Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.steveperkins.mediagallery;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * TODO: Document
 */
public class MediaControl extends BorderPane {

    private final MediaPlayer mediaPlayer;
    private final MediaView mediaView;
    private final Pane mediaViewPane;
    private final Slider timeSlider;
    private final Label playTime;
    private final Slider volumeSlider;
    private final HBox mediaBar;

    private Duration duration;
    private Stage newStage;
    private boolean stopRequested = false;
    private boolean atEndOfMedia = false;
    private boolean fullScreen = false;

    /**
     * TODO: Document
     *
     * @param mediaPlayer
     */
    public MediaControl(final MediaPlayer mediaPlayer) {
        this(mediaPlayer, false);
    }

    /**
     * TODO: Document
     *
     * @param mediaPlayer
     * @param repeat
     */
    public MediaControl(final MediaPlayer mediaPlayer, final boolean repeat) {
        // Main media player and controls bar layout
        this.mediaPlayer = mediaPlayer;
        mediaView = new MediaView(mediaPlayer);
        mediaViewPane = new Pane();
        mediaViewPane.getChildren().add(mediaView);
        mediaViewPane.setStyle("-fx-background-color: black;");
        mediaBar = new HBox(5.0);
        mediaBar.setPadding(new Insets(5, 10, 5, 10));
        mediaBar.setAlignment(Pos.CENTER_LEFT);
        setStyle("-fx-background-color: #bfc2c7;");
        setCenter(mediaViewPane);
        BorderPane.setAlignment(mediaBar, Pos.CENTER);

        // Play button layout and events
        final Button playButton = new Button();
        playButton.setMinWidth(Control.USE_PREF_SIZE);
        final Image PlayButtonImage = new Image(MediaControl.class.getResourceAsStream("/playbutton.png"));
        final Image PauseButtonImage = new Image(MediaControl.class.getResourceAsStream("/pausebutton.png"));
        final ImageView imageViewPlay = new ImageView(PlayButtonImage);
        final ImageView imageViewPause = new ImageView(PauseButtonImage);
        playButton.setGraphic(imageViewPlay);
        playButton.setOnAction((ActionEvent e) -> {
            updateValues();
            MediaPlayer.Status status = mediaPlayer.getStatus();
            if (status == MediaPlayer.Status.UNKNOWN || status == MediaPlayer.Status.HALTED) {
                return;
            }
            if (status == MediaPlayer.Status.PAUSED
                    || status == MediaPlayer.Status.READY
                    || status == MediaPlayer.Status.STOPPED) {
                // rewind the movie if we're sitting at the end
                if (atEndOfMedia) {
                    mediaPlayer.seek(mediaPlayer.getStartTime());
                    atEndOfMedia = false;
                    playButton.setGraphic(imageViewPlay);
                    updateValues();
                }
                mediaPlayer.play();
                playButton.setGraphic(imageViewPause);
            } else {
                mediaPlayer.pause();
            }
        });
        mediaPlayer.currentTimeProperty().addListener((ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) -> {
            updateValues();
        });
        mediaPlayer.setOnPlaying(() -> {
            if (stopRequested) {
                mediaPlayer.pause();
                stopRequested = false;
            } else {
                playButton.setGraphic(imageViewPause);
            }
        });
        mediaPlayer.setOnPaused(() -> {
            playButton.setGraphic(imageViewPlay);
        });
        mediaPlayer.setOnReady(() -> {
            duration = mediaPlayer.getMedia().getDuration();
            updateValues();
        });
        mediaPlayer.setCycleCount(repeat ? MediaPlayer.INDEFINITE : 1);
        mediaPlayer.setOnEndOfMedia(() -> {
            if (!repeat) {
                playButton.setGraphic(imageViewPlay);
                //playButton.setText(">");
                stopRequested = true;
                atEndOfMedia = true;
            }
        });
        mediaBar.getChildren().add(playButton);

        // Time label
        final Label timeLabel = new Label("Time");
        timeLabel.setMinWidth(Control.USE_PREF_SIZE);
        mediaBar.getChildren().add(timeLabel);

        // Time slider
        timeSlider = new Slider();
        timeSlider.setMinWidth(30);
        timeSlider.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(timeSlider, Priority.ALWAYS);
        timeSlider.valueProperty().addListener((Observable ov) -> {
            if (timeSlider.isValueChanging()) {
                // multiply duration by percentage calculated by slider position
                if (duration != null) {
                    mediaPlayer.seek(duration.multiply(timeSlider.getValue() / 100.0));
                }
                updateValues();

            }
        });
        mediaBar.getChildren().add(timeSlider);

        // Time label
        playTime = new Label();
        playTime.setMinWidth(Control.USE_PREF_SIZE);
        mediaBar.getChildren().add(playTime);

        //Fullscreen button
        final Button buttonFullScreen = new Button("Full Screen");
        buttonFullScreen.setMinWidth(Control.USE_PREF_SIZE);
        buttonFullScreen.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!fullScreen) {
                    newStage = new Stage();
                    newStage.fullScreenProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
                        onFullScreen();
                    });
                    final BorderPane borderPane = new BorderPane() {
                        @Override
                        protected void layoutChildren() {
                            if (mediaView != null && getBottom() != null) {
                                mediaView.setFitWidth(getWidth());
                                mediaView.setFitHeight(getHeight() - getBottom().prefHeight(-1));
                            }
                            super.layoutChildren();
                            if (mediaView != null) {
                                if (getCenter() != null) { //if smaller pane has content
                                    mediaView.setTranslateX((((Pane) getCenter()).getWidth() - mediaView.prefWidth(-1)) / 2);
                                    mediaView.setTranslateY((((Pane) getCenter()).getHeight() - mediaView.prefHeight(-1)) / 2);
                                }
                            }
                        }
                    };
                    setCenter(null);
                    setBottom(null);
                    borderPane.setCenter(mediaViewPane);
                    borderPane.setBottom(mediaBar);

                    Scene newScene = new Scene(borderPane);
                    newStage.setScene(newScene);
                    //Workaround for disposing stage when exit fullscreen
                    newStage.setX(-100000);
                    newStage.setY(-100000);

                    newStage.setFullScreen(true);
                    fullScreen = true;
                    newStage.show();
                } else {
                    fullScreen = false;
                    newStage.setFullScreen(false);
                }
            }
        });
        mediaBar.getChildren().add(buttonFullScreen);

        // Volume label
        Label volumeLabel = new Label("Vol");
        volumeLabel.setMinWidth(Control.USE_PREF_SIZE);
        mediaBar.getChildren().add(volumeLabel);

        // Volume slider
        volumeSlider = new Slider();
        volumeSlider.setPrefWidth(70);
        volumeSlider.setMinWidth(30);
        volumeSlider.setMaxWidth(Region.USE_PREF_SIZE);
        volumeSlider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (volumeSlider.isValueChanging()) {
                mediaPlayer.setVolume(volumeSlider.getValue() / 100.0);
            }
        });
        mediaBar.getChildren().add(volumeSlider);

        setBottom(mediaBar);
    }

    /**
     * TODO: Document
     *
     * @return
     */
    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    @Override
    protected void layoutChildren() {
        if (mediaView != null && getBottom() != null) {
            mediaView.setFitWidth(getWidth());
            mediaView.setFitHeight(getHeight() - getBottom().prefHeight(-1));
        }
        super.layoutChildren();
        if (mediaView != null && getCenter() != null) {
            mediaView.setTranslateX((((Pane) getCenter()).getWidth() - mediaView.prefWidth(-1)) / 2);
            mediaView.setTranslateY((((Pane) getCenter()).getHeight() - mediaView.prefHeight(-1)) / 2);
        }
    }

    @Override
    protected double computeMinWidth(double height) {
        return mediaBar.prefWidth(-1);
    }

    @Override
    protected double computeMinHeight(double width) {
        return 200;
    }

    @Override
    protected double computePrefWidth(double height) {
        return Math.max(mediaPlayer.getMedia().getWidth(), mediaBar.prefWidth(height));
    }

    @Override
    protected double computePrefHeight(double width) {
        return mediaPlayer.getMedia().getHeight() + mediaBar.prefHeight(width);
    }

    @Override
    protected double computeMaxWidth(double height) {
        return Double.MAX_VALUE;
    }

    @Override
    protected double computeMaxHeight(double width) {
        return Double.MAX_VALUE;
    }

    /**
     * TODO: Document
     */
    private void onFullScreen() {
        if (!newStage.isFullScreen()) {
            fullScreen = false;
            BorderPane smallBP = (BorderPane) newStage.getScene().getRoot();
            smallBP.setCenter(null);
            setCenter(mediaViewPane);

            smallBP.setBottom(null);
            setBottom(mediaBar);
            Platform.runLater(() -> {
                newStage.close();
            });
        }
    }

    /**
     * TODO: Document
     */
    private void updateValues() {
        if (playTime != null && timeSlider != null && volumeSlider != null && duration != null) {
            Platform.runLater(() -> {
                Duration currentTime = mediaPlayer.getCurrentTime();
                playTime.setText(formatTime(currentTime, duration));
                timeSlider.setDisable(duration.isUnknown());
                if (!timeSlider.isDisabled() && duration.greaterThan(Duration.ZERO) && !timeSlider.isValueChanging()) {
                    timeSlider.setValue(currentTime.divide(duration).toMillis() * 100.0);
                }
                if (!volumeSlider.isValueChanging()) {
                    volumeSlider.setValue((int) Math.round(mediaPlayer.getVolume() * 100));
                }
            });
        }
    }

    /**
     * TODO: Document
     *
     * @param elapsed
     * @param duration
     * @return
     */
    private String formatTime(Duration elapsed, Duration duration) {
        int intElapsed = (int) Math.floor(elapsed.toSeconds());
        int elapsedHours = intElapsed / (60 * 60);
        if (elapsedHours > 0) {
            intElapsed -= elapsedHours * 60 * 60;
        }
        int elapsedMinutes = intElapsed / 60;
        int elapsedSeconds = intElapsed - elapsedHours * 60 * 60 - elapsedMinutes * 60;

        if (duration.greaterThan(Duration.ZERO)) {
            int intDuration = (int) Math.floor(duration.toSeconds());
            int durationHours = intDuration / (60 * 60);
            if (durationHours > 0) {
                intDuration -= durationHours * 60 * 60;
            }
            int durationMinutes = intDuration / 60;
            int durationSeconds = intDuration - durationHours * 60 * 60 - durationMinutes * 60;

            if (durationHours > 0) {
                return String.format("%d:%02d:%02d/%d:%02d:%02d",
                        elapsedHours, elapsedMinutes, elapsedSeconds,
                        durationHours, durationMinutes, durationSeconds);
            } else {
                return String.format("%02d:%02d/%02d:%02d",
                        elapsedMinutes, elapsedSeconds,
                        durationMinutes, durationSeconds);
            }
        } else {
            if (elapsedHours > 0) {
                return String.format("%d:%02d:%02d",
                        elapsedHours, elapsedMinutes, elapsedSeconds);
            } else {
                return String.format("%02d:%02d",
                        elapsedMinutes, elapsedSeconds);
            }
        }
    }
}
