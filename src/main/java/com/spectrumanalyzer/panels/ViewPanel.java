package com.spectrumanalyzer.panels;

import com.spectrumanalyzer.SpectrumAnalyzer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ViewPanel extends VBox {

    private SpectrumAnalyzer analyzer;
    private Button resetViewsButton;
    private Button exportButton;
    private Slider zoomSlider;
    private RadioButton viewTimeDomainButton, viewFrequencyDomainButton, viewBothDomainButton;

    public ViewPanel(SpectrumAnalyzer analyzer) {
        this.analyzer = analyzer;
        setMaxWidth(Double.MAX_VALUE);
        setPadding(new Insets(10));
        setSpacing(10);
        setupUI();
    }

    private void setupUI() {
        Label titleLabel = new Label("View Settings");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        resetViewsButton = new Button("Reset Views");
        resetViewsButton.setStyle("-fx-background-color: #e37a09;");
        resetViewsButton.setOnMouseClicked(e -> analyzer.dashboardPanel.resetAllZoom());

        // Export button
        exportButton = new Button("Export Signal Data");
        exportButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        exportButton.setMaxWidth(Double.MAX_VALUE);
        exportButton.setOnAction(e -> exportSignalData());

        zoomSlider = new Slider(1.0, 10.0, 4.0);
        zoomSlider.setShowTickLabels(true);
        zoomSlider.setShowTickMarks(true);
        zoomSlider.setMajorTickUnit(1);
        zoomSlider.setMinorTickCount(1);
        zoomSlider.setBlockIncrement(1);
        zoomSlider.setSnapToTicks(true);
        zoomSlider.setMaxWidth(Double.MAX_VALUE);

        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            analyzer.dashboardPanel.zoomRate = newVal.doubleValue() / 10.0;
        });

        viewTimeDomainButton = new RadioButton("View Time Domain");
        viewFrequencyDomainButton = new RadioButton("View Frequency Domain");
        viewBothDomainButton = new RadioButton("View both domains");

        ToggleGroup viewGroup = new ToggleGroup();
        viewTimeDomainButton.setToggleGroup(viewGroup);
        viewFrequencyDomainButton.setToggleGroup(viewGroup);
        viewBothDomainButton.setToggleGroup(viewGroup);

        for (RadioButton rb : new RadioButton[]{viewTimeDomainButton, viewFrequencyDomainButton, viewBothDomainButton}) {
            rb.setMaxWidth(Double.MAX_VALUE);
        }

        viewTimeDomainButton.setOnMouseClicked(e -> analyzer.dashboardPanel.showChartMode(true, false));
        viewFrequencyDomainButton.setOnMouseClicked(e -> analyzer.dashboardPanel.showChartMode(false, true));
        viewBothDomainButton.setOnMouseClicked(e -> analyzer.dashboardPanel.showChartMode(true, true));

        getChildren().addAll(
                titleLabel,
                new Label("Horizontal Zoom"),
                analyzer.dashboardPanel.horizontalZoomSlider,
                new Label("Horizontal Movement"),
                analyzer.dashboardPanel.horizontalMoveSlider,
                resetViewsButton,
                viewBothDomainButton,
                viewTimeDomainButton,
                viewFrequencyDomainButton,
                exportButton // Added export button here
        );
    }

    private void exportSignalData() {
        if (analyzer.processedSignal == null) {
            showAlert("No Data", "No processed signal data available to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Signal Data");

        // Set initial filename with timestamp
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        fileChooser.setInitialFileName("signal_data_" + timestamp + ".csv");

        // Add file extension filters
        FileChooser.ExtensionFilter csvFilter = new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv");
        FileChooser.ExtensionFilter txtFilter = new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().addAll(csvFilter, txtFilter);
        fileChooser.setSelectedExtensionFilter(csvFilter);

        // Show save dialog
        Stage stage = (Stage) this.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                exportToFile(file);
                showAlert("Export Successful", "Signal data exported successfully to:\n" + file.getAbsolutePath());
            } catch (IOException ex) {
                showAlert("Export Error", "Failed to export signal data:\n" + ex.getMessage());
            }
        }
    }

    private void exportToFile(File file) throws IOException {
        String separator = file.getName().toLowerCase().endsWith(".csv") ? "," : "\t";

        try (FileWriter writer = new FileWriter(file)) {
            // Write header with metadata
            writer.write("# Spectrum Analyzer Signal Export\n");
            writer.write("# Export Date: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\n");
            writer.write("# Sample Rate: " + analyzer.sampleRate + " Hz\n");
            writer.write("# Channels: " + analyzer.channels + "\n");

            if (analyzer.audioProperties != null && !analyzer.audioProperties.isEmpty()) {
                writer.write("# Audio Properties:\n");
                for (String key : analyzer.audioProperties.keySet()) {
                    writer.write("# " + key + ": " + analyzer.audioProperties.get(key) + "\n");
                }
            }
            writer.write("#\n");

            // Write column headers
            if (analyzer.channels == 1) {
                writer.write("Sample_Index" + separator + "Amplitude\n");
            } else {
                writer.write("Sample_Index");
                for (int ch = 0; ch < analyzer.channels; ch++) {
                    writer.write(separator + "Channel_" + (ch + 1));
                }
                writer.write("\n");
            }

            // Write data
            double[][] data = analyzer.processedSignal;
            int samples = data[0].length;

            for (int i = 0; i < samples; i++) {
                writer.write(String.valueOf(i)); // Sample index

                for (int ch = 0; ch < analyzer.channels && ch < data.length; ch++) {
                    writer.write(separator + String.format("%.6f", data[ch][i]));
                }
                writer.write("\n");

                // Progress feedback for large files
                if (i % 10000 == 0 && i > 0) {
                    System.out.println("Exported " + i + "/" + samples + " samples...");
                }
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}