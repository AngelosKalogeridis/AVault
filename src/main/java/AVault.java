import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

// --- JAUDIOTAGGER IMPORTS ---
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagOptionSingleton;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AVault extends Application {

    // Common UI
    private TextField urlField;
    private TextField outputField;
    private TextArea logArea;
    private Button mainActionBtn;
    private Button cancelButton;
    private Button analyzeButton;
    private Button refreshButton;
    private Button updateButton;
    private ProgressBar progressBar;
    private Label statusLabel;
    private VBox root;

    // Mode Toggle
    private ToggleGroup mediaTypeGroup;
    private ToggleButton audioModeRadio;
    private ToggleButton videoModeRadio;
    private VBox audioConfigBox;
    private VBox videoConfigBox;

    // Audio UI
    private ComboBox<String> formatCombo;
    private CheckBox playlistCheckBox;
    private ComboBox<SubtitleTrack> subtitleBox;
    private ToggleGroup lyricsMode;
    private RadioButton noLyricsRadio;
    private RadioButton separateLyricsRadio;
    private RadioButton embedLyricsRadio;

    // Video UI
    private ComboBox<String> videoFormatBox;
    private ComboBox<String> resolutionBox;
    private ComboBox<SubtitleTrack> videoSubtitleBox;

    // State
    private WorkerTask<?> currentTask;
    private volatile Process currentProcess;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        java.util.logging.Logger.getLogger("org.jaudiotagger").setLevel(java.util.logging.Level.OFF);

        primaryStage.setTitle("AVault — Audio & Video Downloader");

        String GLOBAL_THEME_RED =
                "-fx-base: #2b2b2b; " +
                "-fx-accent: #c0392b; " +
                "-fx-control-inner-background: #333333; " +
                "-fx-text-fill: #cccccc; " +
                "-fx-text-inner-color: #cccccc; " +
                "-fx-focus-color: #e74c3c; " +
                "-fx-faint-focus-color: #c0392b22; " +
                "-fx-font-family: 'Segoe UI', sans-serif;";

        String GLOBAL_THEME_BLUE =
                "-fx-base: #2b2b2b; " +
                "-fx-accent: #0078d7; " +
                "-fx-control-inner-background: #333333; " +
                "-fx-text-fill: #cccccc; " +
                "-fx-text-inner-color: #cccccc; " +
                "-fx-focus-color: #2980b9; " +
                "-fx-faint-focus-color: #0078d722; " +
                "-fx-font-family: 'Segoe UI', sans-serif;";

        String GLOBAL_THEME = GLOBAL_THEME_RED;

        String BTN_RED    = "-fx-base: #c0392b; -fx-font-weight: bold; -fx-cursor: hand;";
        String BTN_BLUE   = "-fx-base: #0078d7; -fx-font-weight: bold; -fx-cursor: hand;";
        String BTN_ORANGE = "-fx-base: #d35400; -fx-font-weight: bold; -fx-cursor: hand;";
        String BTN_GRAY   = "-fx-base: #4a4a4a; -fx-font-weight: bold; -fx-cursor: hand;";

        // --- 1. Header ---
        Label header = new Label("AVault");
        header.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");

        // --- 2. URL Input & Analyze ---
        Label urlLabel = new Label("Video URL:");
        urlLabel.setStyle("-fx-text-fill: white;");

        urlField = new TextField();
        urlField.setPromptText("Paste link here...");
        urlField.setPrefHeight(30);
        HBox.setHgrow(urlField, Priority.ALWAYS);

        analyzeButton = new Button("Analyze");
        analyzeButton.setStyle(BTN_ORANGE);
        analyzeButton.setOnAction(e -> startAnalysis());

        HBox urlBox = new HBox(10, urlField, analyzeButton);
        urlBox.setAlignment(Pos.CENTER_LEFT);
        urlBox.setMaxWidth(Double.MAX_VALUE);

        // --- 3. Mode Selection ---
        mediaTypeGroup = new ToggleGroup();

        audioModeRadio = new ToggleButton("🎵  Audio  (.mp3 / .wav / .flac)");
        audioModeRadio.setToggleGroup(mediaTypeGroup);
        audioModeRadio.setSelected(true);
        audioModeRadio.setPrefHeight(36);
        audioModeRadio.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(audioModeRadio, Priority.ALWAYS);

        videoModeRadio = new ToggleButton("🎬  Video  (.mp4 / .mkv)");
        videoModeRadio.setToggleGroup(mediaTypeGroup);
        videoModeRadio.setPrefHeight(36);
        videoModeRadio.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(videoModeRadio, Priority.ALWAYS);

        updateModeButtonStyles(false);

        HBox typeBox = new HBox(0, audioModeRadio, videoModeRadio);
        typeBox.setAlignment(Pos.CENTER_LEFT);
        typeBox.setPadding(new Insets(10));
        typeBox.setMaxWidth(Double.MAX_VALUE);
        typeBox.setStyle(
            "-fx-background-color: #1e1e1e; " +
            "-fx-background-radius: 7; " +
            "-fx-border-color: #c0392b; " +
            "-fx-border-width: 0 0 2 0; " +
            "-fx-border-radius: 7;"
        );

        // --- 4. AUDIO CONFIG BOX ---
        audioConfigBox = buildAudioConfigBox();
        audioConfigBox.managedProperty().bind(audioConfigBox.visibleProperty());
        audioConfigBox.setMaxWidth(Double.MAX_VALUE);
        audioConfigBox.setStyle(
            "-fx-border-color: #c0392b; " +
            "-fx-border-width: 0 0 0 3; " +
            "-fx-padding: 0 0 0 10; " +
            "-fx-border-radius: 3;"
        );

        // --- 5. VIDEO CONFIG BOX ---
        videoConfigBox = buildVideoConfigBox();
        videoConfigBox.managedProperty().bind(videoConfigBox.visibleProperty());
        videoConfigBox.setVisible(false);
        videoConfigBox.setOpacity(0);
        videoConfigBox.setMaxWidth(Double.MAX_VALUE);
        videoConfigBox.setStyle(
            "-fx-border-color: #0078d7; " +
            "-fx-border-width: 0 0 0 3; " +
            "-fx-padding: 0 0 0 10; " +
            "-fx-border-radius: 3;"
        );

        StackPane configStack = new StackPane(audioConfigBox, videoConfigBox);
        configStack.setAlignment(Pos.TOP_LEFT);
        configStack.setMaxWidth(Double.MAX_VALUE);

       
        mediaTypeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) { oldVal.setSelected(true); return; }
            boolean isVideo = newVal == videoModeRadio;

            updateModeButtonStyles(isVideo);
            switchConfigPanels(isVideo, audioConfigBox, videoConfigBox);

            if (isVideo) {
                mainActionBtn.setText("DOWNLOAD VIDEO");
                mainActionBtn.setStyle(BTN_BLUE + "-fx-font-size: 13px; -fx-pref-height: 36px;");
                header.setText("Video & Subtitle Downloader");
                animateHeaderColor(header, "#2980b9");
                typeBox.setStyle(
                    "-fx-background-color: #1a2535; " +
                    "-fx-background-radius: 7; " +
                    "-fx-border-color: #0078d7; " +
                    "-fx-border-width: 0 0 2 0; " +
                    "-fx-border-radius: 7;"
                );
                progressBar.setStyle("-fx-accent: #0078d7;");
                analyzeButton.setStyle(BTN_BLUE);
                root.setStyle(GLOBAL_THEME_BLUE);
                logArea.setText("[MODE] Switched to ▶ Video mode\n");
            } else {
                updateAudioActionText();
                mainActionBtn.setStyle(BTN_RED + "-fx-font-size: 13px; -fx-pref-height: 36px;");
                header.setText("Audio & Lyrics Converter");
                animateHeaderColor(header, "#e74c3c");
                typeBox.setStyle(
                    "-fx-background-color: #1e1e1e; " +
                    "-fx-background-radius: 7; " +
                    "-fx-border-color: #c0392b; " +
                    "-fx-border-width: 0 0 2 0; " +
                    "-fx-border-radius: 7;"
                );
                progressBar.setStyle("-fx-accent: #e74c3c;");
                analyzeButton.setStyle(BTN_ORANGE);
                root.setStyle(GLOBAL_THEME_RED);
                logArea.setText("[MODE] Switched to 🎵 Audio mode\n");
            }
            resetAnalysisState();
            Platform.runLater(() -> urlField.requestFocus());
        });

        // --- 6. Output Directory ---
        Label dirLabel = new Label("Save to:");
        dirLabel.setStyle("-fx-text-fill: white;");

        outputField = new TextField(System.getProperty("user.home") + File.separator + "Downloads");
        outputField.setEditable(false);
        Button browseButton = new Button("Browse");
        browseButton.setOnAction(e -> chooseDirectory(primaryStage));

        HBox dirBox = new HBox(10, outputField, browseButton);
        HBox.setHgrow(outputField, Priority.ALWAYS);
        dirBox.setAlignment(Pos.CENTER_LEFT);
        dirBox.setMaxWidth(Double.MAX_VALUE);

        // --- 7. Action Buttons ---
        mainActionBtn = new Button("CONVERT TO MP3");
        mainActionBtn.setStyle(BTN_RED + "-fx-font-size: 13px; -fx-pref-height: 36px;");
        mainActionBtn.setMaxWidth(Double.MAX_VALUE);
        mainActionBtn.setOnAction(e -> startDownload());
        HBox.setHgrow(mainActionBtn, Priority.ALWAYS);

        cancelButton = new Button("Cancel");
        cancelButton.setStyle(BTN_GRAY + "-fx-pref-height: 36px;");
        cancelButton.setPrefWidth(90);
        cancelButton.setDisable(true);
        cancelButton.setOnAction(e -> cancelProcess());

        refreshButton = new Button("Reset");
        refreshButton.setStyle(BTN_GRAY + "-fx-pref-height: 36px;");
        refreshButton.setPrefHeight(36);
        refreshButton.setOnAction(e -> resetApp());

        updateButton = new Button("Update yt-dlp");
        updateButton.setStyle(BTN_GRAY + "-fx-pref-height: 36px;");
        updateButton.setPrefHeight(36);
        updateButton.setOnAction(e -> updateYtDlp());

        HBox actionBox = new HBox(10, mainActionBtn, cancelButton, refreshButton, updateButton);
        actionBox.setMaxWidth(Double.MAX_VALUE);

        // --- 8. Feedback ---
        progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(15);
        progressBar.setStyle("-fx-accent: #e74c3c;");

        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-text-fill: #bbb; -fx-font-style: italic;");

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(150);
        logArea.setMaxWidth(Double.MAX_VALUE);
        logArea.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(logArea, Priority.ALWAYS);
        logArea.setStyle("-fx-control-inner-background: #1e1e1e; -fx-text-fill: #cccccc; -fx-font-family: 'Consolas', monospace; -fx-font-size: 11px;");
        logArea.setWrapText(true);

        // --- 9. Layout Setup ---
        root = new VBox(10);
        root.setPadding(new Insets(15));
        root.setFillWidth(true);
        root.setMaxWidth(Double.MAX_VALUE);
        root.setMaxHeight(Double.MAX_VALUE);
        root.setStyle(GLOBAL_THEME);

        root.getChildren().addAll(
                header,
                urlLabel, urlBox,
                typeBox,
                configStack,
                dirLabel, dirBox,
                actionBox,
                statusLabel, progressBar, logArea
        );

        Scene scene = new Scene(root, 600, 740);
        root.prefWidthProperty().bind(scene.widthProperty());
        root.prefHeightProperty().bind(scene.heightProperty());
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            cancelProcess();
            Platform.exit();
            System.exit(0);
        });

        primaryStage.show();
        
        // --- Init State ---
        logArea.setText("[MODE] Started in 🎵 Audio mode\n");
        onLyricsModeChanged();
    }

    // --- THEME / ANIMATION HELPERS ---

    private void updateModeButtonStyles(boolean isVideo) {
        String activeAudio =
            "-fx-background-color: #c0392b; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 12px; " +
            "-fx-background-radius: 6 0 0 6; " +
            "-fx-cursor: hand;";
        String inactiveAudio =
            "-fx-background-color: #2e2e2e; " +
            "-fx-text-fill: #777; " +
            "-fx-font-weight: normal; " +
            "-fx-font-size: 12px; " +
            "-fx-background-radius: 6 0 0 6; " +
            "-fx-cursor: hand;";
        String activeVideo =
            "-fx-background-color: #0078d7; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 12px; " +
            "-fx-background-radius: 0 6 6 0; " +
            "-fx-cursor: hand;";
        String inactiveVideo =
            "-fx-background-color: #2e2e2e; " +
            "-fx-text-fill: #777; " +
            "-fx-font-weight: normal; " +
            "-fx-font-size: 12px; " +
            "-fx-background-radius: 0 6 6 0; " +
            "-fx-cursor: hand;";

        audioModeRadio.setStyle(isVideo ? inactiveAudio : activeAudio);
        videoModeRadio.setStyle(isVideo ? activeVideo   : inactiveVideo);
    }

    private void switchConfigPanels(boolean toVideo, VBox audioBox, VBox videoBox) {
        VBox outgoing = toVideo ? audioBox  : videoBox;
        VBox incoming = toVideo ? videoBox  : audioBox;

        FadeTransition fadeOut = new FadeTransition(Duration.millis(160), outgoing);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            outgoing.setVisible(false);
            incoming.setOpacity(0.0);
            incoming.setVisible(true);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(220), incoming);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }

    private void animateHeaderColor(Label label, String targetHex) {
        FadeTransition ft = new FadeTransition(Duration.millis(120), label);
        ft.setFromValue(1.0);
        ft.setToValue(0.3);
        ft.setOnFinished(e -> {
            label.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + targetHex + ";");
            FadeTransition ftIn = new FadeTransition(Duration.millis(160), label);
            ftIn.setFromValue(0.3);
            ftIn.setToValue(1.0);
            ftIn.play();
        });
        ft.play();
    }

    // --- SUB-UI BUILDERS ---
    private VBox buildAudioConfigBox() {
        VBox box = new VBox(10);
        
        lyricsMode = new ToggleGroup();
        noLyricsRadio = new RadioButton("No Lyrics (Audio Only)");
        noLyricsRadio.setToggleGroup(lyricsMode);
        noLyricsRadio.setStyle("-fx-text-fill: white;");
        noLyricsRadio.setSelected(true);
        noLyricsRadio.setOnAction(e -> onLyricsModeChanged());

        separateLyricsRadio = new RadioButton("Separate Lyrics File (.lrc)");
        separateLyricsRadio.setToggleGroup(lyricsMode);
        separateLyricsRadio.setStyle("-fx-text-fill: white;");
        separateLyricsRadio.setOnAction(e -> onLyricsModeChanged());

        embedLyricsRadio = new RadioButton("Embed Lyrics in MP3");
        embedLyricsRadio.setToggleGroup(lyricsMode);
        embedLyricsRadio.setStyle("-fx-text-fill: white;");
        embedLyricsRadio.setOnAction(e -> onLyricsModeChanged());

        HBox modeRow = new HBox(15, noLyricsRadio, separateLyricsRadio, embedLyricsRadio);

        subtitleBox = new ComboBox<>();
        subtitleBox.setPromptText("Click Analyze to fetch tracks");
        subtitleBox.setMaxWidth(Double.MAX_VALUE);
        subtitleBox.setDisable(true);
        subtitleBox.setConverter(getSubtitleConverter());
        subtitleBox.setOnAction(e -> {
            SubtitleTrack t = subtitleBox.getValue();
            if (t != null) logArea.appendText("[INFO] Lyrics track selected: " + t + "\n");
        });

        Label formatLabel = new Label("Format:");
        formatLabel.setStyle("-fx-text-fill: white;");
        formatCombo = new ComboBox<>();
        formatCombo.getItems().addAll("mp3", "wav", "flac");
        formatCombo.setValue("mp3");
        formatCombo.setOnAction(e -> {
            String fmt = formatCombo.getValue();
            boolean isMp3 = "mp3".equals(fmt);
            separateLyricsRadio.setDisable(!isMp3);
            embedLyricsRadio.setDisable(!isMp3);
            if (!isMp3) {
                noLyricsRadio.setSelected(true);
                onLyricsModeChanged();
            }
            updateAudioActionText();
            logArea.appendText("[INFO] Audio format set to: " + fmt.toUpperCase() + "\n");
        });

        playlistCheckBox = new CheckBox("Download full playlist");
        playlistCheckBox.setStyle("-fx-text-fill: white;");
        playlistCheckBox.setOnAction(e ->
            logArea.appendText("[INFO] Playlist mode: " + (playlistCheckBox.isSelected() ? "ON" : "OFF") + "\n")
        );

        HBox formatRow = new HBox(20, formatLabel, formatCombo, playlistCheckBox);
        formatRow.setAlignment(Pos.CENTER_LEFT);

        Label lyricsLabel = new Label("🎵  Lyrics Options");
        lyricsLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 12px;");
        box.getChildren().addAll(lyricsLabel, modeRow, subtitleBox, formatRow);
        return box;
    }

    private VBox buildVideoConfigBox() {
        VBox box = new VBox(10);
        
        Label formatLabel = new Label("Container Format:");
        formatLabel.setStyle("-fx-text-fill: #5dade2;");
        videoFormatBox = new ComboBox<>();
        videoFormatBox.getItems().addAll("mp4", "mkv");
        videoFormatBox.setValue("mp4");
        videoFormatBox.setOnAction(e -> {
            if (videoFormatBox.getValue() != null)
                logArea.appendText("[INFO] Video container set to: " + videoFormatBox.getValue().toUpperCase() + "\n");
        });

        Label resLabel = new Label("🎬  Video Quality");
        resLabel.setStyle("-fx-text-fill: #2980b9; -fx-font-weight: bold; -fx-font-size: 12px;");
        resolutionBox = new ComboBox<>();
        resolutionBox.setPromptText("Click Analyze to fetch resolutions");
        resolutionBox.setMaxWidth(Double.MAX_VALUE);
        resolutionBox.setDisable(true);
        resolutionBox.setOnAction(e -> {
            if (resolutionBox.getValue() != null)
                logArea.appendText("[INFO] Resolution selected: " + resolutionBox.getValue() + "\n");
        });

        Label subLabel = new Label("Embed Subtitles:");
        subLabel.setStyle("-fx-text-fill: #5dade2;");
        videoSubtitleBox = new ComboBox<>();
        videoSubtitleBox.setPromptText("Click Analyze to fetch tracks");
        videoSubtitleBox.setMaxWidth(Double.MAX_VALUE);
        videoSubtitleBox.setDisable(true);
        videoSubtitleBox.setConverter(getSubtitleConverter());
        videoSubtitleBox.setOnAction(e -> {
            SubtitleTrack t = videoSubtitleBox.getValue();
            if (t != null) logArea.appendText("[INFO] Subtitle track selected: " + t + "\n");
        });

        HBox formatRow = new HBox(15, formatLabel, videoFormatBox);
        formatRow.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().addAll(formatRow, resLabel, resolutionBox, subLabel, videoSubtitleBox);
        return box;
    }

    // --- UI HELPERS ---
    private StringConverter<SubtitleTrack> getSubtitleConverter() {
        return new StringConverter<>() {
            @Override public String toString(SubtitleTrack object) { return object == null ? "None" : object.toString(); }
            @Override public SubtitleTrack fromString(String string) { return null; }
        };
    }

    private void updateAudioActionText() {
        if (mainActionBtn != null && audioModeRadio.isSelected()) {
            mainActionBtn.setText("CONVERT TO " + formatCombo.getValue().toUpperCase());
        }
    }

    private void onLyricsModeChanged() {
        if (noLyricsRadio.isSelected()) {
            subtitleBox.setDisable(true);
            logArea.appendText("[INFO] Lyrics mode: Audio only (no lyrics)\n");
        } else if (separateLyricsRadio.isSelected()) {
            if (!subtitleBox.getItems().isEmpty()) subtitleBox.setDisable(false);
            logArea.appendText("[INFO] Lyrics mode: Separate .lrc file\n");
        } else if (embedLyricsRadio.isSelected()) {
            if (!subtitleBox.getItems().isEmpty()) subtitleBox.setDisable(false);
            logArea.appendText("[INFO] Lyrics mode: Embed lyrics into MP3\n");
        }
    }

    private void chooseDirectory(Stage stage) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Output Folder");
        File selectedDirectory = chooser.showDialog(stage);
        if (selectedDirectory != null) {
            outputField.setText(selectedDirectory.getAbsolutePath());
            logArea.appendText("[INFO] Output folder: " + selectedDirectory.getAbsolutePath() + "\n");
        }
    }

    private boolean isValidUrl(String url) { return url.startsWith("http://") || url.startsWith("https://"); }

    // --- STATE MANAGEMENT ---
    private void resetApp() {
        urlField.clear(); unbindAll(); progressBar.setProgress(0);
        statusLabel.setText("Ready"); resetAnalysisState();
        noLyricsRadio.setSelected(true); formatCombo.setValue("mp3"); playlistCheckBox.setSelected(false);
        videoFormatBox.setValue("mp4");
        setUiLocked(false);
        logArea.clear();
        logArea.appendText("[INFO] App reset.\n");
        Platform.runLater(() -> urlField.requestFocus());
    }

    private void resetAnalysisState() {
        subtitleBox.getItems().clear(); subtitleBox.setDisable(true); subtitleBox.setPromptText("Click Analyze to fetch tracks");
        resolutionBox.getItems().clear(); resolutionBox.setDisable(true); resolutionBox.setPromptText("Click Analyze to fetch resolutions");
        videoSubtitleBox.getItems().clear(); videoSubtitleBox.setDisable(true); videoSubtitleBox.setPromptText("Click Analyze to fetch tracks");
    }

    private void cancelProcess() {
        if (currentTask != null && currentTask.isRunning()) currentTask.cancel(true);
        if (currentProcess != null) { currentProcess.destroyForcibly(); currentProcess = null; }
        unbindAll(); statusLabel.setText("Cancelled."); progressBar.setProgress(0);
        setUiLocked(false); logArea.appendText("\n[INFO] Operation cancelled by user.\n");
    }

    private void setUiLocked(boolean locked) {
        mainActionBtn.setDisable(locked); cancelButton.setDisable(!locked); analyzeButton.setDisable(locked);
        refreshButton.setDisable(locked); updateButton.setDisable(locked); urlField.setDisable(locked);
        audioModeRadio.setDisable(locked); videoModeRadio.setDisable(locked); formatCombo.setDisable(locked);
        playlistCheckBox.setDisable(locked); videoFormatBox.setDisable(locked); outputField.setDisable(locked);

        if (!locked) {
            if (audioModeRadio.isSelected()) onLyricsModeChanged();
            else {
                resolutionBox.setDisable(resolutionBox.getItems().isEmpty());
                videoSubtitleBox.setDisable(videoSubtitleBox.getItems().isEmpty());
            }
        } else {
            subtitleBox.setDisable(true); resolutionBox.setDisable(true); videoSubtitleBox.setDisable(true);
        }
    }

    private void unbindAll() {
        if (statusLabel.textProperty().isBound()) statusLabel.textProperty().unbind();
        if (progressBar.progressProperty().isBound()) progressBar.progressProperty().unbind();
    }

    // --- CORE ROUTING: ANALYZE ---
    private void startAnalysis() {
        String url = urlField.getText().trim();
        if (url.isEmpty() || !isValidUrl(url)) { statusLabel.setText("Error: Please enter a valid URL."); return; }

        setUiLocked(true); progressBar.setProgress(-1);

        if (audioModeRadio.isSelected()) {
            statusLabel.setText("Analyzing video for subtitles/lyrics...");
            AudioAnalysisTask task = new AudioAnalysisTask(url);
            currentTask = task;
            
            task.setOnSucceeded(e -> {
                setUiLocked(false); progressBar.setProgress(0);
                List<SubtitleTrack> tracks = task.getValue();
                subtitleBox.setDisable(false);
                subtitleBox.setItems(FXCollections.observableArrayList(tracks));
                if (tracks.size() > 1) {
                    subtitleBox.getSelectionModel().select(1);
                    logArea.appendText("[INFO] Found " + (tracks.size() - 1) + " subtitle track(s).\n");
                } else subtitleBox.getSelectionModel().select(0);
                statusLabel.setText("Analysis complete.");
            });
            task.setOnFailed(this::handleTaskFailure);
            new Thread(task).start();
        } else {
            statusLabel.setText("Analyzing video resolutions & subtitles...");
            VideoAnalysisTask task = new VideoAnalysisTask(url);
            currentTask = task;
            
            task.setOnSucceeded(e -> {
                setUiLocked(false); progressBar.setProgress(0);
                VideoAnalysisResult res = task.getValue();
               
                // Set Resolutions
                if (res.resolutions.isEmpty()) statusLabel.setText("No resolutions found.");
                else {
                    resolutionBox.getItems().clear();
                    for (Integer h : res.resolutions) resolutionBox.getItems().add(h + "p");
                    resolutionBox.setValue(resolutionBox.getItems().get(0));
                    resolutionBox.setDisable(false);
                    statusLabel.setText("Ready to download.");
                }

                // Set Subtitles
                videoSubtitleBox.setDisable(false);
                videoSubtitleBox.setItems(FXCollections.observableArrayList(res.subtitles));
                if (res.subtitles.size() > 1) {
                    videoSubtitleBox.getSelectionModel().select(1);
                } else videoSubtitleBox.getSelectionModel().select(0);
            });
            task.setOnFailed(this::handleTaskFailure);
            new Thread(task).start();
        }
    }

    // --- CORE ROUTING: DOWNLOAD ---
    private void startDownload() {
        String url = urlField.getText().trim(); String outDir = outputField.getText().trim();
        if (url.isEmpty() || !isValidUrl(url) || outDir.isEmpty()) { statusLabel.setText("Error: Valid URL and Output Directory required."); return; }

        setUiLocked(true); unbindAll(); progressBar.setProgress(0);

        if (audioModeRadio.isSelected()) {
            SubtitleTrack selectedTrack = subtitleBox.getValue();
            LyricsMode mode = separateLyricsRadio.isSelected() ? LyricsMode.SEPARATE : embedLyricsRadio.isSelected() ? LyricsMode.EMBED : LyricsMode.NONE;
            if (mode != LyricsMode.NONE && (selectedTrack == null || selectedTrack.code.equals("none"))) {
                setUiLocked(false); statusLabel.setText("Error: Lyrics mode enabled but no track selected."); return;
            }
            AudioConversionTask task = new AudioConversionTask(url, outDir, selectedTrack, mode, formatCombo.getValue(), playlistCheckBox.isSelected());
            bindAndStartTask(task, outDir);
            
        } else {
            String selectedRes = resolutionBox.getValue();
            if (selectedRes == null) { setUiLocked(false); statusLabel.setText("Error: Select a resolution first."); return; }
            int targetHeight = Integer.parseInt(selectedRes.split("p")[0]);
            SubtitleTrack selectedSub = videoSubtitleBox.getValue();
            VideoConversionTask task = new VideoConversionTask(url, outDir, targetHeight, videoFormatBox.getValue(), selectedSub);
            bindAndStartTask(task, outDir);
        }
    }

    private void bindAndStartTask(WorkerTask<Boolean> task, String outDir) {
        currentTask = task;
        progressBar.progressProperty().bind(task.progressProperty());
        statusLabel.textProperty().bind(task.messageProperty());
        task.setOnSucceeded(e -> {
            unbindAll(); progressBar.setProgress(1.0); setUiLocked(false);
            if (task.getValue()) {
                statusLabel.setText("✅ Success! File saved to: " + outDir);
                logArea.appendText("\n[DONE] Finished processing to: " + outDir + "\n");
            } else statusLabel.setText("Finished with warnings.");
        });
        task.setOnCancelled(e -> cancelProcess());
        task.setOnFailed(this::handleTaskFailure);
        Thread thread = new Thread(task); thread.setDaemon(true); thread.start();
    }

    private void handleTaskFailure(javafx.concurrent.WorkerStateEvent e) {
        unbindAll(); statusLabel.setText("❌ Error occurred."); progressBar.setProgress(0); setUiLocked(false);
        logArea.appendText("\n[ERROR] " + currentTask.getException().getMessage() + "\n");
        currentTask.getException().printStackTrace();
    }

    // --- UPDATER ---
    private void updateYtDlp() {
        setUiLocked(true); logArea.appendText("[INFO] Checking for yt-dlp updates...\n");
        Task<Void> updateTask = new Task<>() {
            @Override protected Void call() throws Exception {
                List<String> command = new WorkerTask<Void>(){ @Override protected Void call(){return null;} }.getBaseCommand();
                command.clear();
                String appPath = new File(AVault.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
                File bundledTool = new File(appPath, "yt-dlp.exe");
                command.add(bundledTool.exists() ? bundledTool.getAbsolutePath() : "yt-dlp");
                command.add("-U");
                Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        final String finalLine = line;
                        Platform.runLater(() -> { logArea.appendText(finalLine + "\n"); logArea.setScrollTop(Double.MAX_VALUE); });
                    }
                }
                process.waitFor(); return null;
            }
        };
        updateTask.setOnSucceeded(e -> { setUiLocked(false); logArea.appendText("[INFO] yt-dlp update complete.\n"); });
        updateTask.setOnFailed(e -> { setUiLocked(false); logArea.appendText("[ERROR] Failed to update: " + updateTask.getException().getMessage() + "\n"); });
        new Thread(updateTask).start();
    }

    // --- DATA CLASSES & BASE TASK ---
    public enum LyricsMode { NONE, SEPARATE, EMBED }

    public static class SubtitleTrack {
        String code; String name; boolean isAuto;
        public SubtitleTrack(String code, String name, boolean isAuto) { this.code = code; this.name = name; this.isAuto = isAuto; }
        @Override public String toString() {
            if (code.equals("none")) return "None";
            return String.format("[%s] %s %s", code, name, isAuto ? "(Auto)" : "");
        }
    }

    public static class VideoAnalysisResult {
        List<Integer> resolutions; List<SubtitleTrack> subtitles;
        public VideoAnalysisResult(List<Integer> r, List<SubtitleTrack> s) { this.resolutions = r; this.subtitles = s; }
    }

    abstract class WorkerTask<V> extends Task<V> {
        protected List<String> getBaseCommand() {
            List<String> command = new ArrayList<>();
            String appPath;
            try { appPath = new File(AVault.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent(); } 
            catch (Exception e) { appPath = System.getProperty("user.dir"); }
            File bundledYt = new File(appPath, "yt-dlp.exe"); File bundledFf = new File(appPath, "ffmpeg.exe");
            if (bundledYt.exists()) {
                command.add(bundledYt.getAbsolutePath());
                if (bundledFf.exists()) { command.add("--ffmpeg-location"); command.add(bundledFf.getAbsolutePath()); }
            } else command.add("yt-dlp"); 
            return command;
        }

        protected List<SubtitleTrack> fetchSubtitles(String url) throws Exception {
            List<String> command = getBaseCommand();
            command.add("--list-subs"); command.add(url);
            Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
            List<SubtitleTrack> tracks = new ArrayList<>();
            tracks.add(new SubtitleTrack("none", "None", false));
            Pattern subPattern = Pattern.compile("^(\\w{2,}(-\\w+)?)\\s+(.+?)\\s+(?:vtt|ttml|srv)");
            boolean readingAuto = false;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("Available automatic captions")) readingAuto = true;
                    else if (line.contains("Available subtitles")) readingAuto = false;
                    if (readingAuto) continue; // Skip auto subs for clarity, same as audio logic
                    Matcher m = subPattern.matcher(line);
                    if (m.find()) tracks.add(new SubtitleTrack(m.group(1), m.group(3).trim(), false));
                }
            }
            process.waitFor();
            return tracks;
        }
    }

    // --- AUDIO TASKS ---
    class AudioAnalysisTask extends WorkerTask<List<SubtitleTrack>> {
        private final String url;
        public AudioAnalysisTask(String url) { this.url = url; }
        @Override protected List<SubtitleTrack> call() throws Exception {
            Platform.runLater(() -> logArea.appendText("[INFO] Fetching subtitle data...\n"));
            return fetchSubtitles(url);
        }
    }

    class AudioConversionTask extends WorkerTask<Boolean> {
        private final String url, outputDir, format;
        private final SubtitleTrack subTrack;
        private final LyricsMode mode;
        private final boolean downloadPlaylist;

        public AudioConversionTask(String url, String outputDir, SubtitleTrack subTrack, LyricsMode mode, String format, boolean downloadPlaylist) {
            this.url = url; this.outputDir = outputDir; this.subTrack = subTrack; this.mode = mode; this.format = format; this.downloadPlaylist = downloadPlaylist;
        }

        @Override protected Boolean call() throws Exception {
            updateMessage("Initializing Audio Download..."); updateProgress(0, 100);
            List<String> command = getBaseCommand();
            command.add("-x"); command.add("--audio-format"); command.add(format);
            command.add("--audio-quality"); command.add("mp3".equals(format) ? "320K" : "0");
            command.add("--add-metadata"); command.add("--parse-metadata"); command.add("%(upload_date>%Y)s:%(meta_year)s");

            if ("mp3".equals(format)) { command.add("--embed-thumbnail"); command.add("--postprocessor-args"); command.add("ffmpeg:-ar 48000 -metadata comment= -metadata description="); } 
            else if ("flac".equals(format)) { command.add("--embed-thumbnail"); command.add("--postprocessor-args"); command.add("ffmpeg:-metadata comment= -metadata description="); } 
            else if ("wav".equals(format)) { command.add("--postprocessor-args"); command.add("ffmpeg:-write_id3v2 1 -id3v2_version 3 -metadata comment="); }

            command.add("--no-overwrites"); if (!downloadPlaylist) command.add("--no-playlist");

            if (mode != LyricsMode.NONE && subTrack != null && !subTrack.code.equals("none")) {
                Platform.runLater(() -> logArea.appendText("[INFO] Lyrics enabled: " + subTrack.name + "\n"));
                command.add(subTrack.isAuto ? "--write-auto-sub" : "--write-sub");
                command.add("--sub-lang"); command.add(subTrack.code);
                command.add("--convert-subs"); command.add("lrc");
            }

            command.add("-o"); command.add(outputDir + File.separator + "%(title)s.%(ext)s"); command.add(url);
            currentProcess = new ProcessBuilder(command).redirectErrorStream(true).start();
            String downloadedMp3Path = null;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(currentProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (isCancelled()) { currentProcess.destroyForcibly(); return false; }
                    final String finalLine = line;
                    Platform.runLater(() -> { logArea.appendText(finalLine + "\n"); logArea.setScrollTop(Double.MAX_VALUE); });
                    if (line.contains("[ExtractAudio] Destination:")) {
                        String[] parts = line.split("Destination:");
                        if (parts.length > 1) downloadedMp3Path = parts[1].trim();
                    }
                    if (line.contains("[download]") && line.contains("%")) {
                        try {
                            for (String part : line.split("\\s+")) {
                                if (part.contains("%")) { updateProgress(Double.parseDouble(part.replace("%", "")), 100.0); updateMessage("Downloading Audio: " + part); break; }
                            }
                        } catch (Exception ignored) {}
                    } else if (line.contains("[ExtractAudio]") || line.contains("[ffmpeg]")) {
                        updateProgress(-1, 1); updateMessage("Converting to " + format.toUpperCase() + "...");
                    }
                }
            }
            if (currentProcess.waitFor() != 0) throw new RuntimeException("Download failed.");

            if (mode == LyricsMode.EMBED && subTrack != null && !subTrack.code.equals("none")) {
                updateMessage("Embedding lyrics into MP3...");
                File[] files = new File(outputDir).listFiles();
                File newestMp3 = null, newestLrc = null; long lastMp3 = 0, lastLrc = 0;
                if (files != null) {
                    for (File f : files) {
                        if (f.getName().endsWith(".mp3") && f.lastModified() > lastMp3) { newestMp3 = f; lastMp3 = f.lastModified(); }
                        if (f.getName().endsWith(".lrc") && f.lastModified() > lastLrc) { newestLrc = f; lastLrc = f.lastModified(); }
                    }
                }
                String mp3 = newestMp3 != null ? newestMp3.getAbsolutePath() : downloadedMp3Path;
                String lrc = newestLrc != null ? newestLrc.getAbsolutePath() : null;
                if (mp3 != null && lrc != null) {
                    try {
                        TagOptionSingleton.getInstance().setAndroid(true);
                        AudioFile af = AudioFileIO.read(new File(mp3));
                        Tag tag = af.getTagOrCreateAndSetDefault();
                        tag.setField(FieldKey.LYRICS, Files.readString(Paths.get(lrc), StandardCharsets.UTF_8));
                        af.commit(); new File(lrc).delete();
                        Platform.runLater(() -> logArea.appendText("[SUCCESS] Lyrics embedded.\n"));
                    } catch (Exception e) { Platform.runLater(() -> logArea.appendText("[ERROR] Embed failed: " + e.getMessage() + "\n")); }
                }
            }
            return true;
        }
    }

    // --- VIDEO TASKS ---
    class VideoAnalysisTask extends WorkerTask<VideoAnalysisResult> {
        private final String url;
        public VideoAnalysisTask(String url) { this.url = url; }
        @Override protected VideoAnalysisResult call() throws Exception {
            Platform.runLater(() -> logArea.appendText("[INFO] Fetching video resolutions...\n"));
            List<String> command = getBaseCommand();
            command.add("--print"); command.add("%(formats.:.height)s"); command.add("--no-warnings"); command.add(url);
            Process process = new ProcessBuilder(command).start();
            Set<Integer> uniqueHeights = new HashSet<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line; Pattern p = Pattern.compile("\\d+");
                while ((line = reader.readLine()) != null) {
                    Matcher m = p.matcher(line);
                    while (m.find()) { try { uniqueHeights.add(Integer.parseInt(m.group())); } catch (Exception ignored) {} }
                }
            }
            process.waitFor();
            List<Integer> sortedHeights = new ArrayList<>(uniqueHeights);
            sortedHeights.sort(Collections.reverseOrder());
            Platform.runLater(() -> logArea.appendText("[INFO] Found resolutions: " + sortedHeights + "\n"));

            Platform.runLater(() -> logArea.appendText("[INFO] Fetching subtitle data...\n"));
            List<SubtitleTrack> subs = fetchSubtitles(url);
            
            return new VideoAnalysisResult(sortedHeights, subs);
        }
    }

    class VideoConversionTask extends WorkerTask<Boolean> {
        private final String url, outputDir, format;
        private final int targetHeight;
        private final SubtitleTrack subTrack;

        public VideoConversionTask(String url, String outputDir, int targetHeight, String format, SubtitleTrack subTrack) {
            this.url = url; this.outputDir = outputDir; this.targetHeight = targetHeight; this.format = format; this.subTrack = subTrack;
        }

        @Override protected Boolean call() throws Exception {
            updateMessage("Initializing Video Download..."); updateProgress(0, 100);
            List<String> command = getBaseCommand();
            
            command.add("--user-agent"); 
            command.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            command.add("--no-mtime"); command.add("--force-overwrites");
            command.add("-o"); command.add(outputDir + File.separator + "%(title)s.%(ext)s");
            command.add("-f"); command.add(targetHeight > 0 ? "bestvideo[height<=" + targetHeight + "]+bestaudio/best[height<=" + targetHeight + "]" : "bestvideo+bestaudio/best");

            boolean hasSubs = subTrack != null && !subTrack.code.equals("none");
            if (hasSubs) {
                Platform.runLater(() -> logArea.appendText("[INFO] Subtitles enabled: " + subTrack.name + "\n"));
                command.add(subTrack.isAuto ? "--write-auto-sub" : "--write-sub");
                command.add("--sub-lang"); command.add(subTrack.code);
                command.add("--embed-subs");
                command.add("--convert-subs"); command.add("srt");
            }

            command.add("--add-metadata"); command.add("--embed-thumbnail"); command.add("--convert-thumbnails"); command.add("jpg");
            command.add("--parse-metadata"); command.add("%(upload_date)s:(?P<meta_date>^\\d{4})");
            command.add("--merge-output-format"); command.add(format);
            command.add(url);

            currentProcess = new ProcessBuilder(command).redirectErrorStream(true).start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(currentProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (isCancelled()) { currentProcess.destroyForcibly(); return false; }
                    final String finalLine = line;
                    Platform.runLater(() -> { logArea.appendText(finalLine + "\n"); logArea.setScrollTop(Double.MAX_VALUE); });

                    if (line.contains("[download]") && line.contains("%")) {
                        try {
                            for (String part : line.split("\\s+")) {
                                if (part.contains("%")) { updateProgress(Double.parseDouble(part.replace("%", "")), 100.0); updateMessage("Downloading Video: " + part); break; }
                            }
                        } catch (Exception ignored) {}
                    } else if (line.contains("429") || line.contains("Too many requests")) {
                        updateMessage("Error: YouTube blocking subs (429)...");
                    } else if (line.contains("[Merger]") || line.contains("Merging")) {
                        updateMessage("Merging video/audio/subs..."); updateProgress(-1, 1);
                    }
                }
            }

            boolean success = currentProcess.waitFor() == 0;
            
            if (success && hasSubs) {
                updateMessage("Cleaning up subtitle sidecar files...");
                File dir = new File(outputDir);
                File[] leftovers = dir.listFiles(f -> {
                    String n = f.getName().toLowerCase();
                    return n.endsWith(".vtt") || n.endsWith(".srt") || n.endsWith(".ass")
                        || n.endsWith(".ttml") || n.endsWith(".srv3") || n.endsWith(".srv2") || n.endsWith(".srv1");
                });
                if (leftovers != null) {
                    for (File f : leftovers) {
                        boolean deleted = f.delete();
                        final String msg = deleted
                            ? "[INFO] Removed sidecar: " + f.getName() + "\n"
                            : "[WARN] Could not remove: " + f.getName() + "\n";
                        Platform.runLater(() -> logArea.appendText(msg));
                    }
                }
            }

            return success;
        }
    }
}
