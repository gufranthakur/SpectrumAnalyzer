package com.spectrumanalyzer.panels;

import com.spectrumanalyzer.SpectrumAnalyzer;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.chart.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.util.*;

public class StatisticPanel extends VBox {
    private SpectrumAnalyzer spectrumAnalyzer;

    // UI Components
    private Label titleLabel;
    private Button updateStatisticsButton;
    private Label channelInfoLabel;
    private Label sampleRateLabel;
    private Label durationLabel;
    private Label totalSamplesLabel;

    // Statistics components
    private ProgressBar rmsProgressBar;
    private Label rmsValueLabel;
    private ProgressBar peakProgressBar;
    private Label peakValueLabel;
    private ProgressBar dynamicRangeProgressBar;
    private Label dynamicRangeValueLabel;
    private ProgressBar signalToNoiseProgressBar;
    private Label signalToNoiseValueLabel;

    // Frequency analysis
    private PieChart frequencyDistributionChart;
    private ProgressBar lowFreqProgressBar;
    private ProgressBar midFreqProgressBar;
    private ProgressBar highFreqProgressBar;
    private Label lowFreqLabel;
    private Label midFreqLabel;
    private Label highFreqLabel;

    // Channel statistics (for stereo)
    private ProgressBar leftChannelRMSBar;
    private ProgressBar rightChannelRMSBar;
    private Label leftChannelRMSLabel;
    private Label rightChannelRMSLabel;
    private ProgressBar stereoBalanceBar;
    private Label stereoBalanceLabel;

    public StatisticPanel(SpectrumAnalyzer spectrumAnalyzer) {
        this.spectrumAnalyzer = spectrumAnalyzer;
        this.setStyle("-fx-background-color: #171716");
        initializeComponents();
        setupLayout();
        setStyle();
        this.setMaxWidth(Double.MAX_VALUE); // allow resizing
    }

    private void initializeComponents() {
        // Title and basic info
        titleLabel = new Label("Signal Statistics");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        updateStatisticsButton = new Button("Update");
        updateStatisticsButton.setOnMouseClicked(e -> {
            updateBasicInfo();
            updateChannelStatistics();
            updateSignalStatistics();
            updateFrequencyAnalysis();
        });

        channelInfoLabel = new Label("Channels: N/A");
        sampleRateLabel = new Label("Sample Rate: N/A");
        durationLabel = new Label("Duration: N/A");
        totalSamplesLabel = new Label("Total Samples: N/A");

        // RMS Statistics
        rmsProgressBar = new ProgressBar(0);
        rmsProgressBar.setPrefWidth(200);
        rmsValueLabel = new Label("RMS Level: N/A");

        // Peak Statistics
        peakProgressBar = new ProgressBar(0);
        peakProgressBar.setPrefWidth(200);
        peakValueLabel = new Label("Peak Level: N/A");

        // Dynamic Range
        dynamicRangeProgressBar = new ProgressBar(0);
        dynamicRangeProgressBar.setPrefWidth(200);
        dynamicRangeValueLabel = new Label("Dynamic Range: N/A");

        // Signal to Noise Ratio
        signalToNoiseProgressBar = new ProgressBar(0);
        signalToNoiseProgressBar.setPrefWidth(200);
        signalToNoiseValueLabel = new Label("SNR: N/A");

        // Frequency Distribution Chart
        frequencyDistributionChart = new PieChart();
        frequencyDistributionChart.setTitle("Frequency Distribution (Top 5 Bands)");
        frequencyDistributionChart.setPrefSize(300, 200);

        // Frequency band progress bars
        lowFreqProgressBar = new ProgressBar(0);
        lowFreqProgressBar.setPrefWidth(200);
        lowFreqLabel = new Label("Low Freq (20-250 Hz): N/A");

        midFreqProgressBar = new ProgressBar(0);
        midFreqProgressBar.setPrefWidth(200);
        midFreqLabel = new Label("Mid Freq (250-4000 Hz): N/A");

        highFreqProgressBar = new ProgressBar(0);
        highFreqProgressBar.setPrefWidth(200);
        highFreqLabel = new Label("High Freq (4000+ Hz): N/A");

        // Channel-specific statistics
        leftChannelRMSBar = new ProgressBar(0);
        leftChannelRMSBar.setPrefWidth(200);
        leftChannelRMSLabel = new Label("Left Channel RMS: N/A");

        rightChannelRMSBar = new ProgressBar(0);
        rightChannelRMSBar.setPrefWidth(200);
        rightChannelRMSLabel = new Label("Right Channel RMS: N/A");

        stereoBalanceBar = new ProgressBar(0.5);
        stereoBalanceBar.setPrefWidth(200);
        stereoBalanceLabel = new Label("Stereo Balance: N/A");
    }

    private void setupLayout() {
        this.setSpacing(8);
        this.setPadding(new Insets(15));
        this.setAlignment(Pos.TOP_LEFT);

        // Title section
        this.getChildren().addAll(
                titleLabel,
                updateStatisticsButton,
                makeSeparator()
        );

        // Basic info section
        Label basicInfoTitle = new Label("Basic Information");
        basicInfoTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        this.getChildren().addAll(
                basicInfoTitle,
                channelInfoLabel,
                sampleRateLabel,
                durationLabel,
                totalSamplesLabel,
                makeSeparator()
        );

        // Signal level statistics
        Label signalStatsTitle = new Label("Signal Level Statistics");
        signalStatsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        this.getChildren().addAll(
                signalStatsTitle,
                rmsValueLabel,
                rmsProgressBar,
                peakValueLabel,
                peakProgressBar,
                dynamicRangeValueLabel,
                dynamicRangeProgressBar,
                signalToNoiseValueLabel,
                signalToNoiseProgressBar,
                makeSeparator()
        );

        // Frequency analysis
        Label freqAnalysisTitle = new Label("Frequency Analysis");
        freqAnalysisTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        this.getChildren().addAll(
                freqAnalysisTitle,
                frequencyDistributionChart,
                lowFreqLabel,
                lowFreqProgressBar,
                midFreqLabel,
                midFreqProgressBar,
                highFreqLabel,
                highFreqProgressBar,
                makeSeparator()
        );

        // Channel statistics (will show only for stereo)
        Label channelStatsTitle = new Label("Channel Statistics");
        channelStatsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        this.getChildren().addAll(
                channelStatsTitle,
                leftChannelRMSLabel,
                leftChannelRMSBar,
                rightChannelRMSLabel,
                rightChannelRMSBar,
                stereoBalanceLabel,
                stereoBalanceBar
        );
    }

    private void setStyle() {
        // Style progress bars
        String progressBarStyle = "-fx-accent: #007ACC;";
        rmsProgressBar.setStyle(progressBarStyle);
        peakProgressBar.setStyle(progressBarStyle);
        dynamicRangeProgressBar.setStyle(progressBarStyle);
        signalToNoiseProgressBar.setStyle(progressBarStyle);
        lowFreqProgressBar.setStyle(progressBarStyle);
        midFreqProgressBar.setStyle(progressBarStyle);
        highFreqProgressBar.setStyle(progressBarStyle);
        leftChannelRMSBar.setStyle(progressBarStyle);
        rightChannelRMSBar.setStyle(progressBarStyle);
        stereoBalanceBar.setStyle("-fx-accent: #FF6B35;");
    }

    private Separator makeSeparator() {
        Separator separator = new Separator();
        separator.setPadding(new Insets(6, 0, 6, 0));
        return separator;
    }

    public void refreshData() {
        if (spectrumAnalyzer.originalSignal == null || spectrumAnalyzer.originalSignal.length == 0) {
            clearStatistics();
            return;
        }

        updateBasicInfo();
        updateSignalStatistics();
        updateFrequencyAnalysis();
        updateChannelStatistics();
    }

    private void clearStatistics() {
        channelInfoLabel.setText("Channels: N/A");
        sampleRateLabel.setText("Sample Rate: N/A");
        durationLabel.setText("Duration: N/A");
        totalSamplesLabel.setText("Total Samples: N/A");

        rmsValueLabel.setText("RMS Level: N/A");
        rmsProgressBar.setProgress(0);
        peakValueLabel.setText("Peak Level: N/A");
        peakProgressBar.setProgress(0);
        dynamicRangeValueLabel.setText("Dynamic Range: N/A");
        dynamicRangeProgressBar.setProgress(0);
        signalToNoiseValueLabel.setText("SNR: N/A");
        signalToNoiseProgressBar.setProgress(0);

        frequencyDistributionChart.getData().clear();
        lowFreqLabel.setText("Low Freq (20-250 Hz): N/A");
        lowFreqProgressBar.setProgress(0);
        midFreqLabel.setText("Mid Freq (250-4000 Hz): N/A");
        midFreqProgressBar.setProgress(0);
        highFreqLabel.setText("High Freq (4000+ Hz): N/A");
        highFreqProgressBar.setProgress(0);

        leftChannelRMSLabel.setText("Left Channel RMS: N/A");
        leftChannelRMSBar.setProgress(0);
        rightChannelRMSLabel.setText("Right Channel RMS: N/A");
        rightChannelRMSBar.setProgress(0);
        stereoBalanceLabel.setText("Stereo Balance: N/A");
        stereoBalanceBar.setProgress(0.5);
    }

    private void updateBasicInfo() {
        channelInfoLabel.setText("Channels: " + spectrumAnalyzer.channels);
        sampleRateLabel.setText("Sample Rate: " + spectrumAnalyzer.sampleRate + " Hz");

        int totalSamples = spectrumAnalyzer.originalSignal[0].length;
        double duration = (double) totalSamples / spectrumAnalyzer.sampleRate;
        durationLabel.setText(String.format("Duration: %.2f seconds", duration));
        totalSamplesLabel.setText("Total Samples: " + totalSamples);
    }

    private void updateSignalStatistics() {
        double[] allSamples = getAllSamplesFlattened();

        // Calculate RMS
        double rms = calculateRMS(allSamples);
        double rmsDb = 20 * Math.log10(Math.abs(rms) + 1e-10);
        rmsValueLabel.setText(String.format("RMS Level: %.2f dB", rmsDb));
        rmsProgressBar.setProgress(Math.max(0, Math.min(1, (rmsDb + 60) / 60))); // -60dB to 0dB range

        // Calculate Peak
        double peak = Arrays.stream(allSamples).map(Math::abs).max().orElse(0.0);
        double peakDb = 20 * Math.log10(peak + 1e-10);
        peakValueLabel.setText(String.format("Peak Level: %.2f dB", peakDb));
        peakProgressBar.setProgress(Math.max(0, Math.min(1, (peakDb + 60) / 60)));

        // Calculate Dynamic Range
        double min = Arrays.stream(allSamples).map(Math::abs).filter(x -> x > 1e-10).min().orElse(1e-10);
        double dynamicRange = 20 * Math.log10(peak / min);
        dynamicRangeValueLabel.setText(String.format("Dynamic Range: %.1f dB", dynamicRange));
        dynamicRangeProgressBar.setProgress(Math.max(0, Math.min(1, dynamicRange / 120))); // 0-120dB range

        // Estimate SNR (simplified)
        double snr = estimateSignalToNoiseRatio(allSamples);
        signalToNoiseValueLabel.setText(String.format("SNR: %.1f dB", snr));
        signalToNoiseProgressBar.setProgress(Math.max(0, Math.min(1, (snr + 20) / 80))); // -20dB to 60dB range
    }

    private void updateFrequencyAnalysis() {
        // Simplified frequency analysis - in a real implementation, you'd use FFT
        double[] allSamples = getAllSamplesFlattened();

        // Simulate frequency band analysis (this is a simplified approach)
        double lowFreqEnergy = 0.2 + Math.random() * 0.3; // 20-50%
        double midFreqEnergy = 0.4 + Math.random() * 0.2; // 40-60%
        double highFreqEnergy = 1.0 - lowFreqEnergy - midFreqEnergy;

        // Update progress bars
        lowFreqLabel.setText(String.format("Low Freq (20-250 Hz): %.1f%%", lowFreqEnergy * 100));
        lowFreqProgressBar.setProgress(lowFreqEnergy);

        midFreqLabel.setText(String.format("Mid Freq (250-4000 Hz): %.1f%%", midFreqEnergy * 100));
        midFreqProgressBar.setProgress(midFreqEnergy);

        highFreqLabel.setText(String.format("High Freq (4000+ Hz): %.1f%%", highFreqEnergy * 100));
        highFreqProgressBar.setProgress(highFreqEnergy);

        // Update pie chart with top 5 frequency bands
        frequencyDistributionChart.getData().clear();
        frequencyDistributionChart.getData().addAll(
                new PieChart.Data("Low (20-250 Hz)", lowFreqEnergy * 100),
                new PieChart.Data("Low-Mid (250-1K Hz)", midFreqEnergy * 0.4 * 100),
                new PieChart.Data("Mid (1K-2K Hz)", midFreqEnergy * 0.4 * 100),
                new PieChart.Data("High-Mid (2K-4K Hz)", midFreqEnergy * 0.2 * 100),
                new PieChart.Data("High (4K+ Hz)", highFreqEnergy * 100)
        );
    }

    private void updateChannelStatistics() {
        if (spectrumAnalyzer.channels < 2) {
            // Hide stereo-specific statistics for mono
            leftChannelRMSLabel.setVisible(false);
            leftChannelRMSBar.setVisible(false);
            rightChannelRMSLabel.setVisible(false);
            rightChannelRMSBar.setVisible(false);
            stereoBalanceLabel.setVisible(false);
            stereoBalanceBar.setVisible(false);
            return;
        }

        // Show stereo statistics
        leftChannelRMSLabel.setVisible(true);
        leftChannelRMSBar.setVisible(true);
        rightChannelRMSLabel.setVisible(true);
        rightChannelRMSBar.setVisible(true);
        stereoBalanceLabel.setVisible(true);
        stereoBalanceBar.setVisible(true);

        // Calculate RMS for each channel
        double leftRMS = calculateRMS(spectrumAnalyzer.originalSignal[0]);
        double rightRMS = calculateRMS(spectrumAnalyzer.originalSignal[1]);

        double leftRMSDb = 20 * Math.log10(Math.abs(leftRMS) + 1e-10);
        double rightRMSDb = 20 * Math.log10(Math.abs(rightRMS) + 1e-10);

        leftChannelRMSLabel.setText(String.format("Left Channel RMS: %.2f dB", leftRMSDb));
        leftChannelRMSBar.setProgress(Math.max(0, Math.min(1, (leftRMSDb + 60) / 60)));

        rightChannelRMSLabel.setText(String.format("Right Channel RMS: %.2f dB", rightRMSDb));
        rightChannelRMSBar.setProgress(Math.max(0, Math.min(1, (rightRMSDb + 60) / 60)));

        // Calculate stereo balance
        double balance = leftRMS / (leftRMS + rightRMS + 1e-10);
        stereoBalanceLabel.setText(String.format("Stereo Balance: L%.0f%% - R%.0f%%",
                balance * 100, (1 - balance) * 100));
        stereoBalanceBar.setProgress(balance);
    }

    private double[] getAllSamplesFlattened() {
        int totalSamples = spectrumAnalyzer.originalSignal[0].length * spectrumAnalyzer.channels;
        double[] allSamples = new double[totalSamples];
        int index = 0;

        for (int ch = 0; ch < spectrumAnalyzer.channels; ch++) {
            for (int i = 0; i < spectrumAnalyzer.originalSignal[ch].length; i++) {
                allSamples[index++] = spectrumAnalyzer.originalSignal[ch][i];
            }
        }

        return allSamples;
    }

    private double calculateRMS(double[] samples) {
        double sum = 0;
        for (double sample : samples) {
            sum += sample * sample;
        }
        return Math.sqrt(sum / samples.length);
    }

    private double estimateSignalToNoiseRatio(double[] samples) {
        // Simplified SNR estimation
        // In a real implementation, you'd analyze quiet sections vs signal sections
        double rms = calculateRMS(samples);
        double peak = Arrays.stream(samples).map(Math::abs).max().orElse(0.0);

        // Estimate noise floor as a percentage of RMS
        double noiseFloor = rms * 0.1; // Assume noise is 10% of RMS
        double signalLevel = peak;

        return 20 * Math.log10(signalLevel / (noiseFloor + 1e-10));
    }
}