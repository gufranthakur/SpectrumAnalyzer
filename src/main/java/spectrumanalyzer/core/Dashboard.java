package spectrumanalyzer.core;


import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Dashboard class handles signal visualization and statistics display
 * Manages time domain and frequency domain charts with real-time updates
 */
public class Dashboard {

    private BorderPane mainContainer;
    private VBox mainView;
    private LineChart<Number, Number> timeChart;
    private LineChart<Number, Number> frequencyChart;
    private VBox statisticsPanel;
    private AnimationTimer animationTimer;

    private boolean showBothSignals = true;

    // Signal data
    private double[] timeData = new double[512];
    private double[] frequencyData = new double[256];
    private double[] originalSignal = new double[512];
    private double sampleRate = 1000.0;

    // Filter parameters
    private FilterType currentFilter = FilterType.NONE;
    private double cutoffFrequency = 100.0;
    private double lowCutoff = 50.0;
    private double highCutoff = 150.0;

    // View mode
    private ViewMode currentView = ViewMode.BOTH;

    public enum FilterType {
        NONE, LOW_PASS, HIGH_PASS, BAND_PASS, BUTTERWORTH
    }

    public enum ViewMode {
        TIME_DOMAIN, FREQUENCY_DOMAIN, BOTH
    }

    public Dashboard() {
        initializeCharts();
        initializeStatistics();
        setupView();
        generateInitialSignal();
    }

    /**
     * Initializes time and frequency domain charts
     * Sets up axes and chart properties for real-time display
     */
    private void initializeCharts() {
        // Time domain chart
        NumberAxis timeXAxis = new NumberAxis();
        NumberAxis timeYAxis = new NumberAxis();
        timeXAxis.setLabel("Time (ms)");
        timeYAxis.setLabel("Amplitude");
        timeChart = new LineChart<>(timeXAxis, timeYAxis);
        timeChart.setTitle("Time Domain");
        timeChart.setLegendVisible(false);
        timeChart.setAnimated(false);
        timeChart.setCreateSymbols(false);
        timeChart.setLegendVisible(true);
        setupZoomPan(timeChart);

        // Frequency domain chart
        NumberAxis freqXAxis = new NumberAxis();
        NumberAxis freqYAxis = new NumberAxis();
        freqXAxis.setLabel("Frequency (Hz)");
        freqYAxis.setLabel("Magnitude");
        frequencyChart = new LineChart<>(freqXAxis, freqYAxis);
        frequencyChart.setTitle("Frequency Domain");
        frequencyChart.setLegendVisible(false);
        frequencyChart.setAnimated(false);
        frequencyChart.setCreateSymbols(false);
        setupZoomPan(frequencyChart);
    }

    /**
     * Initializes the statistics text area
     * Sets up formatting and initial content
     */
    private void initializeStatistics() {
        statisticsPanel = new VBox(5);
        statisticsPanel.setMaxWidth(300);
    }

    /**
     * Sets up the main view layout based on current view mode
     * Arranges charts and statistics panel
     */
    private void setupView() {
        mainContainer = new BorderPane();

        mainView = new VBox(10);
        updateViewLayout();
    }

    private void setupZoomPan(LineChart<Number, Number> chart) {
        chart.setOnScroll(event -> {
            if (event.isControlDown()) {
                double zoomFactor = event.getDeltaY() > 0 ? 0.9 : 1.0;
                NumberAxis xAxis = (NumberAxis) chart.getXAxis();
                NumberAxis yAxis = (NumberAxis) chart.getYAxis();

                double xRange = xAxis.getUpperBound() - xAxis.getLowerBound();
                double yRange = yAxis.getUpperBound() - yAxis.getLowerBound();

                double xCenter = (xAxis.getUpperBound() + xAxis.getLowerBound()) / 2;
                double yCenter = (yAxis.getUpperBound() + yAxis.getLowerBound()) / 2;

                double newXRange = xRange * zoomFactor;
                double newYRange = yRange * zoomFactor;

                xAxis.setAutoRanging(false);
                yAxis.setAutoRanging(false);

                xAxis.setLowerBound(xCenter - newXRange / 2);
                xAxis.setUpperBound(xCenter + newXRange / 2);
                yAxis.setLowerBound(yCenter - newYRange / 2);
                yAxis.setUpperBound(yCenter + newYRange / 2);

                event.consume();
            }
        });


        chart.setOnMousePressed(event -> {
            if (event.isSecondaryButtonDown()) {
                chart.setUserData(new double[]{event.getX(), event.getY()});
            }
        });

        chart.setOnMouseDragged(event -> {
            if (event.isSecondaryButtonDown() && chart.getUserData() != null) {
                double[] startPoint = (double[]) chart.getUserData();
                double deltaX = event.getX() - startPoint[0];
                double deltaY = event.getY() - startPoint[1];

                NumberAxis xAxis = (NumberAxis) chart.getXAxis();
                NumberAxis yAxis = (NumberAxis) chart.getYAxis();

                double xRange = xAxis.getUpperBound() - xAxis.getLowerBound();
                double yRange = yAxis.getUpperBound() - yAxis.getLowerBound();

                double xShift = -deltaX / chart.getWidth() * xRange;
                double yShift = deltaY / chart.getHeight() * yRange;

                xAxis.setLowerBound(xAxis.getLowerBound() + xShift);
                xAxis.setUpperBound(xAxis.getUpperBound() + xShift);
                yAxis.setLowerBound(yAxis.getLowerBound() + yShift);
                yAxis.setUpperBound(yAxis.getUpperBound() + yShift);

                chart.setUserData(new double[]{event.getX(), event.getY()});
            }
        });

        chart.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                NumberAxis xAxis = (NumberAxis) chart.getXAxis();
                NumberAxis yAxis = (NumberAxis) chart.getYAxis();
                xAxis.setAutoRanging(true);
                yAxis.setAutoRanging(true);
            }
        });
    }

    /**
     * Updates the view layout based on current view mode
     * Reorganizes components for different display modes
     */
    private void updateViewLayout() {
        mainView.getChildren().clear();

        switch (currentView) {
            case TIME_DOMAIN:
                VBox.setVgrow(timeChart, javafx.scene.layout.Priority.ALWAYS);
                timeChart.setMaxHeight(Double.MAX_VALUE);
                mainView.getChildren().add(timeChart);
                break;

            case FREQUENCY_DOMAIN:
                VBox.setVgrow(frequencyChart, javafx.scene.layout.Priority.ALWAYS);
                frequencyChart.setMaxHeight(Double.MAX_VALUE);
                mainView.getChildren().add(frequencyChart);
                break;

            case BOTH:
                VBox chartsBox = new VBox(10);
                chartsBox.getChildren().addAll(timeChart, frequencyChart);
                mainView.getChildren().add(chartsBox);
                break;
        }

        VBox.setVgrow(statisticsPanel, javafx.scene.layout.Priority.NEVER);
        mainContainer.setCenter(mainView);
        mainContainer.setRight(statisticsPanel);


    }


    /**
     * Generates initial composite signal with multiple frequency components
     * Creates a realistic signal for demonstration purposes
     */
    private void generateInitialSignal() {
        for (int i = 0; i < timeData.length; i++) {
            double t = i / sampleRate;
            // Composite signal with multiple frequencies
            originalSignal[i] = 2.0 * Math.sin(2 * Math.PI * 50 * t) +
                    1.5 * Math.sin(2 * Math.PI * 120 * t) +
                    0.8 * Math.sin(2 * Math.PI * 200 * t) +
                    0.3 * ThreadLocalRandom.current().nextGaussian();
        }
        System.arraycopy(originalSignal, 0, timeData, 0, timeData.length);

    }

    /**
     * Applies the current filter to the signal
     * Processes the original signal based on selected filter type and parameters
     */
    private void applyFilter() {
        System.arraycopy(originalSignal, 0, timeData, 0, timeData.length);

        switch (currentFilter) {
            case LOW_PASS:
                applyLowPassFilter();
                break;
            case HIGH_PASS:
                applyHighPassFilter();
                break;
            case BAND_PASS:
                applyBandPassFilter();
                break;
            case BUTTERWORTH:
                applyButterworthFilter();
                break;
        }
    }

    /**
     * Applies simple low-pass filter using moving average
     * Attenuates high-frequency components above cutoff
     */
    private void applyLowPassFilter() {
        double rc = 1.0 / (2 * Math.PI * cutoffFrequency);
        double dt = 1.0 / sampleRate;
        double alpha = dt / (rc + dt);

        for (int i = 1; i < timeData.length; i++) {
            timeData[i] = alpha * timeData[i] + (1 - alpha) * timeData[i - 1];
        }
    }


    /**
     * Applies high-pass filter using difference equation
     * Attenuates low-frequency components below cutoff
     */
    private void applyHighPassFilter() {
        double rc = 1.0 / (2 * Math.PI * cutoffFrequency);
        double dt = 1.0 / sampleRate;
        double alpha = rc / (rc + dt);

        double[] filtered = new double[timeData.length];
        filtered[0] = timeData[0];

        for (int i = 1; i < timeData.length; i++) {
            filtered[i] = alpha * (filtered[i - 1] + timeData[i] - timeData[i - 1]);
        }

        System.arraycopy(filtered, 0, timeData, 0, timeData.length);
    }


    /**
     * Applies band-pass filter combining low and high pass
     * Allows frequencies within specified band to pass through
     */
    private void applyBandPassFilter() {
        // High-pass stage
        double rcHigh = 1.0 / (2 * Math.PI * lowCutoff);
        double dt = 1.0 / sampleRate;
        double alphaHigh = rcHigh / (rcHigh + dt);

        double[] temp = new double[timeData.length];
        temp[0] = timeData[0];

        for (int i = 1; i < timeData.length; i++) {
            temp[i] = alphaHigh * (temp[i - 1] + timeData[i] - timeData[i - 1]);
        }

        // Low-pass stage
        double rcLow = 1.0 / (2 * Math.PI * highCutoff);
        double alphaLow = dt / (rcLow + dt);

        for (int i = 1; i < timeData.length; i++) {
            temp[i] = alphaLow * temp[i] + (1 - alphaLow) * temp[i - 1];
        }

        System.arraycopy(temp, 0, timeData, 0, timeData.length);
    }


    /**
     * Applies Butterworth filter approximation
     * Implements second-order filter with smooth frequency response
     */
    private void applyButterworthFilter() {
        double[] filtered = new double[timeData.length];
        double a = Math.exp(-2.0 * Math.PI * cutoffFrequency / sampleRate);
        double b = 1.0 - a;

        filtered[0] = timeData[0];
        for (int i = 1; i < timeData.length; i++) {
            filtered[i] = a * filtered[i-1] + b * timeData[i];
        }
        System.arraycopy(filtered, 0, timeData, 0, timeData.length);
    }

    /**
     * Computes FFT approximation for frequency domain analysis
     * Calculates magnitude spectrum from time domain signal
     */
    private void computeFFT() {
        // Simple magnitude spectrum calculation
        for (int k = 0; k < frequencyData.length; k++) {
            double real = 0, imag = 0;
            for (int n = 0; n < timeData.length; n++) {
                double angle = -2 * Math.PI * k * n / timeData.length;
                real += timeData[n] * Math.cos(angle);
                imag += timeData[n] * Math.sin(angle);
            }
            frequencyData[k] = Math.sqrt(real * real + imag * imag);
        }
    }

    /**
     * Updates chart displays with current signal data
     * Refreshes both time and frequency domain visualizations
     */
    private void updateCharts() {
        Platform.runLater(() -> {
            updateTimeChart();
            updateFrequencyChart();
            updateStatistics();
        });
    }

    /**
     * Updates time domain chart with current signal data
     * Plots amplitude vs time for the processed signal
     */
    private void updateTimeChart() {
        timeChart.getData().clear();

        // Original signal series
        XYChart.Series<Number, Number> originalSeries = new XYChart.Series<>();
        originalSeries.setName("Original");
        for (int i = 0; i < originalSignal.length; i++) {
            originalSeries.getData().add(new XYChart.Data<>(i * 1000.0 / sampleRate, originalSignal[i]));
        }

        // Filtered signal series
        XYChart.Series<Number, Number> filteredSeries = new XYChart.Series<>();
        filteredSeries.setName("Filtered");
        for (int i = 0; i < timeData.length; i++) {
            filteredSeries.getData().add(new XYChart.Data<>(i * 1000.0 / sampleRate, timeData[i]));
        }

        timeChart.getData().addAll(originalSeries, filteredSeries);

        // Set colors
        originalSeries.getNode().setStyle("-fx-stroke: #2196F3;");
        filteredSeries.getNode().setStyle("-fx-stroke: #FF5722;");
    }

    /**
     * Updates frequency domain chart with FFT results
     * Plots magnitude spectrum showing frequency components
     */
    private void updateFrequencyChart() {
        frequencyChart.getData().clear();
        XYChart.Series<Number, Number> series = new XYChart.Series<>();

        for (int i = 0; i < frequencyData.length; i++) {
            double frequency = i * sampleRate / (2 * frequencyData.length);
            series.getData().add(new XYChart.Data<>(frequency, frequencyData[i]));
        }

        frequencyChart.getData().add(series);
    }

    /**
     * Updates statistics display with signal analysis results
     * Shows top frequencies, filter info, and signal characteristics
     */
    private void updateStatistics() {
        statisticsPanel.getChildren().clear();

        // Filter Info
        statisticsPanel.getChildren().add(makeTitle("Filter Info"));
        statisticsPanel.getChildren().add(makeStyledLabel("Filter: " + currentFilter));
        if (currentFilter == FilterType.BAND_PASS) {
            statisticsPanel.getChildren().add(makeStyledLabel(String.format("Band: %.1f - %.1f Hz", lowCutoff, highCutoff)));
        } else if (currentFilter != FilterType.NONE) {
            statisticsPanel.getChildren().add(makeStyledLabel(String.format("Cutoff: %.1f Hz", cutoffFrequency)));
        }

        statisticsPanel.getChildren().add(makeSeparator());

        // Top Frequencies
        statisticsPanel.getChildren().add(makeTitle("Top Frequencies"));
        List<FrequencyPeak> peaks = findTopFrequencies(5);
        statisticsPanel.getChildren().add(makePieChart(peaks));

        statisticsPanel.getChildren().add(makeSeparator());

        // Signal Quality
        statisticsPanel.getChildren().add(makeTitle("Signal Quality"));
        double rms = calculateRMS();
        double peak = findPeakAmplitude();
        double snr = calculateSNR();
        double thd = calculateTHD();
        double dynamicRange = 20 * Math.log10(peak / 0.001);
        statisticsPanel.getChildren().add(makeProgressBar("SNR (dB)", snr, 100));
        statisticsPanel.getChildren().add(makeProgressBar("THD (%)", thd, 100));
        statisticsPanel.getChildren().add(makeProgressBar("Dynamic Range (dB)", dynamicRange, 100));

        statisticsPanel.getChildren().add(makeSeparator());

        // Frequency Analysis
        statisticsPanel.getChildren().add(makeTitle("Frequency Analysis"));
        statisticsPanel.getChildren().add(makeStyledLabel("Fundamental: " + String.format("%.1f Hz", findFundamentalFreq())));
        statisticsPanel.getChildren().add(makeStyledLabel("Bandwidth: " + String.format("%.1f Hz", calculateBandwidth())));
        statisticsPanel.getChildren().add(makeStyledLabel("Spectral Centroid: " + String.format("%.1f Hz", calculateSpectralCentroid())));

        statisticsPanel.getChildren().add(makeSeparator());
        // System Info
        statisticsPanel.getChildren().add(makeTitle("System Info"));
        statisticsPanel.getChildren().add(makeStyledLabel("Samples: " + timeData.length));
        statisticsPanel.getChildren().add(makeStyledLabel("Sample Rate: " + String.format("%.0f Hz", sampleRate)));
        statisticsPanel.getChildren().add(makeStyledLabel("Resolution: " + String.format("%.2f Hz", sampleRate / timeData.length)));
        statisticsPanel.getChildren().add(makeStyledLabel("Update Rate: 10 Hz"));
    }

    private Label makeStyledLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 14; -fx-padding: 2 0 2 0;");
        return label;
    }

    private VBox makeProgressBar(String labelText, double value, double max) {
        VBox box = new VBox(2);
        Label label = new Label(labelText + ": " + String.format("%.2f", value));
        ProgressBar bar = new ProgressBar(Math.min(value / max, 1.0));
        bar.setPrefWidth(200);
        bar.setPrefHeight(5);
        box.getChildren().addAll(label, bar);
        return box;
    }

    private PieChart makePieChart(List<FrequencyPeak> peaks) {
        PieChart chart = new PieChart();
        for (FrequencyPeak peak : peaks) {
            chart.getData().add(new PieChart.Data(String.format("%.1f Hz", peak.frequency), peak.magnitude));
        }
        chart.setLegendVisible(false);
        chart.setLabelsVisible(true);
        chart.setPrefSize(300, 300);
        return chart;
    }

    private Label makeTitle(String titleText) {
        Label label = new Label(titleText);
        label.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-padding: 6 0 2 0;");
        return label;
    }

    private Separator makeSeparator() {
        Separator separator = new Separator();
        separator.setPadding(new Insets(6, 0, 6, 0));
        return separator;
    }


    /**
     * Finds top frequency peaks in the spectrum
     * @param count number of peaks to find
     * @return sorted list of frequency peaks by magnitude
     */
    private List<FrequencyPeak> findTopFrequencies(int count) {
        List<FrequencyPeak> peaks = new ArrayList<>();

        for (int i = 1; i < frequencyData.length - 1; i++) {
            if (frequencyData[i] > frequencyData[i-1] && frequencyData[i] > frequencyData[i+1]) {
                double frequency = i * sampleRate / (2 * frequencyData.length);
                peaks.add(new FrequencyPeak(frequency, frequencyData[i]));
            }
        }

        peaks.sort((a, b) -> Double.compare(b.magnitude, a.magnitude));
        return peaks.subList(0, Math.min(count, peaks.size()));
    }

    /**
     * Calculates RMS value of the current signal
     * @return root mean square amplitude
     */
    private double calculateRMS() {
        double sum = 0;
        for (double sample : timeData) {
            sum += sample * sample;
        }
        return Math.sqrt(sum / timeData.length);
    }

    /**
     * Finds peak amplitude in the current signal
     * @return maximum absolute amplitude
     */
    private double findPeakAmplitude() {
        double peak = 0;
        for (double sample : timeData) {
            peak = Math.max(peak, Math.abs(sample));
        }
        return peak;
    }

    /**
     * Starts real-time signal processing and display updates
     * Begins animation timer for continuous refresh
     */
    public void startSignalGeneration() {
        animationTimer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 100_000_000) { // Update every 100ms
                    applyFilter();
                    computeFFT();
                    updateCharts();
                    lastUpdate = now;
                }
            }
        };
        animationTimer.start();
    }
    private double calculateMean() {
        double sum = 0;
        for (double sample : timeData) {
            sum += sample;
        }
        return sum / timeData.length;
    }

    private double calculateStandardDeviation() {
        double mean = calculateMean();
        double sum = 0;
        for (double sample : timeData) {
            sum += (sample - mean) * (sample - mean);
        }
        return Math.sqrt(sum / timeData.length);
    }

    private double calculateSNR() {
        double signal = calculateRMS();
        double noise = calculateStandardDeviation() * 0.1; // Approximate noise
        return 20 * Math.log10(signal / noise);
    }

    private double calculateTHD() {
        List<FrequencyPeak> peaks = findTopFrequencies(5);
        if (peaks.size() < 2) return 0;

        double fundamental = peaks.get(0).magnitude;
        double harmonics = 0;
        for (int i = 1; i < peaks.size(); i++) {
            harmonics += peaks.get(i).magnitude * peaks.get(i).magnitude;
        }
        return Math.sqrt(harmonics) / fundamental * 100;
    }

    private double findFundamentalFreq() {
        List<FrequencyPeak> peaks = findTopFrequencies(1);
        return peaks.isEmpty() ? 0 : peaks.get(0).frequency;
    }

    private double calculateBandwidth() {
        double maxMag = 0;
        for (double mag : frequencyData) {
            maxMag = Math.max(maxMag, mag);
        }
        double threshold = maxMag * 0.707; // -3dB point

        int lowIndex = 0, highIndex = frequencyData.length - 1;
        for (int i = 0; i < frequencyData.length; i++) {
            if (frequencyData[i] > threshold) {
                lowIndex = i;
                break;
            }
        }
        for (int i = frequencyData.length - 1; i >= 0; i--) {
            if (frequencyData[i] > threshold) {
                highIndex = i;
                break;
            }
        }

        return (highIndex - lowIndex) * sampleRate / (2 * frequencyData.length);
    }

    private double calculateSpectralCentroid() {
        double weightedSum = 0;
        double magnitudeSum = 0;

        for (int i = 0; i < frequencyData.length; i++) {
            double frequency = i * sampleRate / (2 * frequencyData.length);
            weightedSum += frequency * frequencyData[i];
            magnitudeSum += frequencyData[i];
        }

        return magnitudeSum > 0 ? weightedSum / magnitudeSum : 0;
    }
    /**
     * Stops signal processing and updates
     * Cleans up animation timer resources
     */
    public void stopSignalGeneration() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }





    // Getters and setters for external control
    public BorderPane getView() { return mainContainer; }

    public void setFilter(FilterType filter) {
        this.currentFilter = filter;
    }

    public void setCutoffFrequency(double frequency) {
        this.cutoffFrequency = frequency;
    }

    public void setBandPassRange(double low, double high) {
        this.lowCutoff = low;
        this.highCutoff = high;
    }

    public void setViewMode(ViewMode mode) {
        this.currentView = mode;
        Platform.runLater(this::updateViewLayout);
    }

    /**
     * Helper class to represent frequency peaks
     * Stores frequency and magnitude pairs for analysis
     */
    private static class FrequencyPeak {
        final double frequency;
        final double magnitude;

        FrequencyPeak(double frequency, double magnitude) {
            this.frequency = frequency;
            this.magnitude = magnitude;
        }
    }
}