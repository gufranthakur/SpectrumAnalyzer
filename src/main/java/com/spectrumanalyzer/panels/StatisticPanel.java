package com.spectrumanalyzer.panels;

import com.spectrumanalyzer.SpectrumAnalyzer;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.chart.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.util.*;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.TransformType;

public class StatisticPanel extends VBox {
    private SpectrumAnalyzer spectrumAnalyzer;

    // UI Components
    private Label titleLabel;
    private Button updateStatisticsButton;

    // Original Signal Statistics
    private Label originalTitleLabel;
    private Label originalChannelsLabel;
    private Label originalSampleRateLabel;
    private Label originalDurationLabel;
    private Label originalRMSLabel;
    private Label originalPeakLabel;

    // Filtered Signal Statistics
    private Label filteredTitleLabel;
    private Label filteredChannelsLabel;
    private Label filteredSampleRateLabel;
    private Label filteredDurationLabel;
    private Label filteredRMSLabel;
    private Label filteredPeakLabel;

    // Top 5 Frequencies
    private Label frequenciesTitleLabel;
    private VBox frequenciesContainer;

    public StatisticPanel(SpectrumAnalyzer spectrumAnalyzer) {
        this.spectrumAnalyzer = spectrumAnalyzer;
        this.setStyle("-fx-background-color: #171716");
        initializeComponents();
        setupLayout();
        setStyle();
        this.setMaxWidth(Double.MAX_VALUE);
    }

    private void initializeComponents() {
        // Title and update button
        titleLabel = new Label("Signal Statistics");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        updateStatisticsButton = new Button("Update Statistics");
        updateStatisticsButton.setOnAction(e -> refreshData());

        // Original Signal Section
        originalTitleLabel = new Label("Original Signal");
        originalTitleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #00BFFF;");

        originalChannelsLabel = new Label("Channels: N/A");
        originalChannelsLabel.setStyle("-fx-font-size: 16px;");
        originalSampleRateLabel = new Label("Sample Rate: N/A");
        originalSampleRateLabel.setStyle("-fx-font-size: 16px;");
        originalDurationLabel = new Label("Duration: N/A");
        originalDurationLabel.setStyle("-fx-font-size: 16px;");
        originalRMSLabel = new Label("RMS Level: N/A");
        originalRMSLabel.setStyle("-fx-font-size: 16px;");
        originalPeakLabel = new Label("Peak Level: N/A");
        originalPeakLabel.setStyle("-fx-font-size: 16px;");

        // Filtered Signal Section
        filteredTitleLabel = new Label("Filtered Signal");
        filteredTitleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #FF6B35;");

        filteredChannelsLabel = new Label("Channels: N/A");
        filteredChannelsLabel.setStyle("-fx-font-size: 16px;");
        filteredSampleRateLabel = new Label("Sample Rate: N/A");
        filteredSampleRateLabel.setStyle("-fx-font-size: 16px;");
        filteredDurationLabel = new Label("Duration: N/A");
        filteredDurationLabel.setStyle("-fx-font-size: 16px;");
        filteredRMSLabel = new Label("RMS Level: N/A");
        filteredRMSLabel.setStyle("-fx-font-size: 16px;");
        filteredPeakLabel = new Label("Peak Level: N/A");
        filteredPeakLabel.setStyle("-fx-font-size: 16px;");

        // Top 5 Frequencies Section
        frequenciesTitleLabel = new Label("Top 5 Frequencies");
        frequenciesTitleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #00FF7F;");

        frequenciesContainer = new VBox();
        frequenciesContainer.setSpacing(3);
    }


    private void setupLayout() {
        this.setSpacing(10);
        this.setPadding(new Insets(15));
        this.setAlignment(Pos.TOP_LEFT);

        // Title section
        this.getChildren().addAll(
                titleLabel,
                updateStatisticsButton,
                makeSeparator()
        );

        // Original signal section
        this.getChildren().addAll(
                originalTitleLabel,
                originalChannelsLabel,
                originalSampleRateLabel,
                originalDurationLabel,
                originalRMSLabel,
                originalPeakLabel,
                makeSeparator()
        );

        // Filtered signal section
        this.getChildren().addAll(
                filteredTitleLabel,
                filteredChannelsLabel,
                filteredSampleRateLabel,
                filteredDurationLabel,
                filteredRMSLabel,
                filteredPeakLabel,
                makeSeparator()
        );

        // Top frequencies section
        this.getChildren().addAll(
                frequenciesTitleLabel,
                frequenciesContainer
        );
    }

    private void setStyle() {
        // Style all labels
        String labelStyle = "-fx-text-fill: #E0E0E0; -fx-font-size: 12px;";

        originalChannelsLabel.setStyle(labelStyle);
        originalSampleRateLabel.setStyle(labelStyle);
        originalDurationLabel.setStyle(labelStyle);
        originalRMSLabel.setStyle(labelStyle);
        originalPeakLabel.setStyle(labelStyle);

        filteredChannelsLabel.setStyle(labelStyle);
        filteredSampleRateLabel.setStyle(labelStyle);
        filteredDurationLabel.setStyle(labelStyle);
        filteredRMSLabel.setStyle(labelStyle);
        filteredPeakLabel.setStyle(labelStyle);

        // Style button
        updateStatisticsButton.setStyle(
                "-fx-background-color: #007ACC; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 5px;"
        );
    }

    private Separator makeSeparator() {
        Separator separator = new Separator();
        separator.setPadding(new Insets(5, 0, 5, 0));
        separator.setStyle("-fx-background-color: #404040;");
        return separator;
    }

    public void refreshData() {
        updateOriginalSignalStats();
        updateFilteredSignalStats();
        updateTopFrequencies();
    }

    private void updateOriginalSignalStats() {
        if (spectrumAnalyzer.originalSignal == null || spectrumAnalyzer.originalSignal.length == 0) {
            clearOriginalStats();
            return;
        }

        // Basic info
        originalChannelsLabel.setText("Channels: " + spectrumAnalyzer.channels);
        originalSampleRateLabel.setText("Sample Rate: " + spectrumAnalyzer.sampleRate + " Hz");

        int samples = spectrumAnalyzer.originalSignal[0].length;
        double duration = (double) samples / spectrumAnalyzer.sampleRate;
        originalDurationLabel.setText(String.format("Duration: %.2f seconds", duration));

        // Audio statistics
        double[] mixedSignal = getMixedDownSignal(spectrumAnalyzer.originalSignal);
        double rms = calculateRMS(mixedSignal);
        double peak = Arrays.stream(mixedSignal).map(Math::abs).max().orElse(0.0);

        double rmsDb = 20 * Math.log10(Math.abs(rms) + 1e-10);
        double peakDb = 20 * Math.log10(peak + 1e-10);

        originalRMSLabel.setText(String.format("RMS Level: %.2f dB", rmsDb));
        originalPeakLabel.setText(String.format("Peak Level: %.2f dB", peakDb));
    }

    private void updateFilteredSignalStats() {
        if (spectrumAnalyzer.processedSignal == null || spectrumAnalyzer.processedSignal.length == 0) {
            clearFilteredStats();
            return;
        }

        // Basic info (same as original for channels and sample rate)
        filteredChannelsLabel.setText("Channels: " + spectrumAnalyzer.channels);
        filteredSampleRateLabel.setText("Sample Rate: " + spectrumAnalyzer.sampleRate + " Hz");

        int samples = spectrumAnalyzer.processedSignal[0].length;
        double duration = (double) samples / spectrumAnalyzer.sampleRate;
        filteredDurationLabel.setText(String.format("Duration: %.2f seconds", duration));

        // Audio statistics
        double[] mixedSignal = getMixedDownSignal(spectrumAnalyzer.processedSignal);
        double rms = calculateRMS(mixedSignal);
        double peak = Arrays.stream(mixedSignal).map(Math::abs).max().orElse(0.0);

        double rmsDb = 20 * Math.log10(Math.abs(rms) + 1e-10);
        double peakDb = 20 * Math.log10(peak + 1e-10);

        filteredRMSLabel.setText(String.format("RMS Level: %.2f dB", rmsDb));
        filteredPeakLabel.setText(String.format("Peak Level: %.2f dB", peakDb));
    }

    private void updateTopFrequencies() {
        frequenciesContainer.getChildren().clear();

        // Use processed signal if available, otherwise original
        double[][] signalToAnalyze = (spectrumAnalyzer.processedSignal != null && spectrumAnalyzer.processedSignal.length > 0)
                ? spectrumAnalyzer.processedSignal
                : spectrumAnalyzer.originalSignal;

        if (signalToAnalyze == null || signalToAnalyze.length == 0) {
            Label noDataLabel = new Label("No signal data available");
            noDataLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 12px;");
            frequenciesContainer.getChildren().add(noDataLabel);
            return;
        }

        List<FrequencyPeak> topFrequencies = findTopFrequencies(signalToAnalyze);

        for (int i = 0; i < topFrequencies.size(); i++) {
            FrequencyPeak peak = topFrequencies.get(i);
            Label freqLabel = new Label(String.format("%d. %.1f Hz (%.2f dB)",
                    i + 1, peak.frequency, peak.magnitude));
            freqLabel.setStyle("-fx-text-fill: #E0E0E0; -fx-font-size: 12px;");
            frequenciesContainer.getChildren().add(freqLabel);
        }
    }

    private void clearOriginalStats() {
        originalChannelsLabel.setText("Channels: N/A");
        originalSampleRateLabel.setText("Sample Rate: N/A");
        originalDurationLabel.setText("Duration: N/A");
        originalRMSLabel.setText("RMS Level: N/A");
        originalPeakLabel.setText("Peak Level: N/A");
    }

    private void clearFilteredStats() {
        filteredChannelsLabel.setText("Channels: N/A");
        filteredSampleRateLabel.setText("Sample Rate: N/A");
        filteredDurationLabel.setText("Duration: N/A");
        filteredRMSLabel.setText("RMS Level: N/A");
        filteredPeakLabel.setText("Peak Level: N/A");
    }

    private double[] getMixedDownSignal(double[][] signal) {
        int sampleCount = signal[0].length;
        double[] monoSignal = new double[sampleCount];

        for (int i = 0; i < sampleCount; i++) {
            double sum = 0;
            for (int ch = 0; ch < signal.length; ch++) {
                sum += signal[ch][i];
            }
            monoSignal[i] = sum / signal.length;
        }

        return monoSignal;
    }

    private double calculateRMS(double[] samples) {
        double sum = 0;
        for (double sample : samples) {
            sum += sample * sample;
        }
        return Math.sqrt(sum / samples.length);
    }

    private void applyHanningWindow(double[] data) {
        int N = data.length;
        for (int i = 0; i < N; i++) {
            double window = 0.5 * (1 - Math.cos(2 * Math.PI * i / (N - 1)));
            data[i] *= window;
        }
    }

    private List<FrequencyPeak> findTopFrequencies(double[][] signal) {
        double[] monoSignal = getMixedDownSignal(signal);

        // Use power-of-2 frame size for FFT
        int frameSize = 4096;
        double overlapRatio = 0.5;
        int hopSize = (int) (frameSize * (1 - overlapRatio));

        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);

        // Accumulate magnitudes across all frames
        double[] totalMagnitudes = new double[frameSize / 2];
        int numFrames = 0;

        for (int start = 0; start + frameSize <= monoSignal.length; start += hopSize) {
            double[] frame = Arrays.copyOfRange(monoSignal, start, start + frameSize);
            applyHanningWindow(frame);

            Complex[] fftResult = fft.transform(frame, TransformType.FORWARD);

            for (int i = 1; i < frameSize / 2; i++) { // Skip DC component
                totalMagnitudes[i] += fftResult[i].abs();
            }
            numFrames++;
        }

        // Average and find peaks
        List<FrequencyPeak> peaks = new ArrayList<>();
        double freqPerBin = (double) spectrumAnalyzer.sampleRate / frameSize;

        for (int i = 1; i < totalMagnitudes.length; i++) {
            if (numFrames > 0) {
                totalMagnitudes[i] /= numFrames;
            }

            double frequency = i * freqPerBin;
            double magnitudeDb = 20 * Math.log10(totalMagnitudes[i] + 1e-10);

            peaks.add(new FrequencyPeak(frequency, magnitudeDb));
        }

        // Sort by magnitude and return top 5
        peaks.sort((a, b) -> Double.compare(b.magnitude, a.magnitude));
        return peaks.subList(0, Math.min(5, peaks.size()));
    }

    // Helper class for frequency peaks
    private static class FrequencyPeak {
        public final double frequency;
        public final double magnitude;

        public FrequencyPeak(double frequency, double magnitude) {
            this.frequency = frequency;
            this.magnitude = magnitude;
        }
    }
}