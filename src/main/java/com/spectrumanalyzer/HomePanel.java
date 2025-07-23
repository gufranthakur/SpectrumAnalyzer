package com.spectrumanalyzer;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import java.io.File;

public class HomePanel extends VBox {
    private SpectrumAnalyzer analyzer;
    private Label fileLabel;
    private Button openButton;

    public HomePanel(SpectrumAnalyzer analyzer) {
        this.analyzer = analyzer;
        setupUI();
    }

    private void setupUI() {
        setAlignment(Pos.CENTER);
        setSpacing(20);

        Label titleLabel = new Label("Spectrum Analyzer");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        fileLabel = new Label("No file selected");
        fileLabel.setStyle("-fx-font-size: 14px;");

        openButton = new Button("Open WAV File");
        openButton.setOnAction(e -> openFile());
        openButton.setStyle("-fx-background-color: #38ab03;");

        VBox infoBox = new VBox(10);
        infoBox.setAlignment(Pos.CENTER);

        getChildren().addAll(fileLabel, openButton, infoBox);
    }

    private void openFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select WAV File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("WAV Files", "*.wav")
        );

        File selectedFile = fileChooser.showOpenDialog(getScene().getWindow());
        if (selectedFile != null) {
            fileLabel.setText("Selected: " + selectedFile.getName());
            analyzer.loadAudioFile(selectedFile.getAbsolutePath());
        }
    }
}