package com.spectrumanalyzer.panels;

import com.github.psambit9791.jdsp.io.WAV;
import com.github.psambit9791.wavfile.WavFileException;
import com.spectrumanalyzer.SpectrumAnalyzer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;

public class HomePanel extends VBox {
    private SpectrumAnalyzer analyzer;
    private Label fileLabel;
    private Button openButton;

    public HomePanel(SpectrumAnalyzer analyzer) {
        this.analyzer = analyzer;
        setupUI();
    }

    private void setupUI() {
        // Dark vertical gradient background
        BackgroundFill backgroundFill = new BackgroundFill(
                new LinearGradient(
                        0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.rgb(20, 20, 20)),
                        new Stop(1, Color.rgb(40, 40, 40))
                ),
                CornerRadii.EMPTY, Insets.EMPTY
        );
        setBackground(new Background(backgroundFill));

        setAlignment(Pos.CENTER);
        setSpacing(20);
        setPadding(new Insets(40, 20, 40, 20));

        // Spacer above content
        Region topSpacer = new Region();
        VBox.setVgrow(topSpacer, Priority.ALWAYS);

        Label titleLabel = new Label("Spectrum Analyzer");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");

        fileLabel = new Label("No file selected");
        fileLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #bbbbbb;");

        openButton = new Button("Open WAV File");
        openButton.setStyle("-fx-background-color: #38ab03; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8px 16px;");
        openButton.setOnAction(e -> openFile());

        VBox buttonBox = new VBox(12, openButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Spacer below content
        Region bottomSpacer = new Region();
        VBox.setVgrow(bottomSpacer, Priority.ALWAYS);

        Label footerLabel = new Label("Developed at Rhosigma");
        footerLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #888888;; -fx-font-weight: bold");
        footerLabel.setPadding(new Insets(10, 0, 10, 0));

        getChildren().addAll(topSpacer, titleLabel, fileLabel, buttonBox, bottomSpacer, footerLabel);
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
