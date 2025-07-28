package com.spectrumanalyzer.panels;

import com.spectrumanalyzer.SpectrumAnalyzer;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.layout.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.*;
import javafx.concurrent.Task;
import javafx.application.Platform;
import com.github.psambit9791.jdsp.transform.DiscreteFourier;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardPanel extends VBox {
    private SpectrumAnalyzer analyzer;
    public LineChart<Number, Number> timeChart;
    public LineChart<Number, Number> frequencyChart;

    // Background processing
    private ExecutorService executorService;
    public ProgressBar progressBar;
    public Label statusLabel;
    private volatile boolean processingInProgress = false;

    // Zoom and pan functionality
    private LineChart<Number, Number> selectedChart = null;
    public double zoomRate = 0.8;

    // Horizontal zoom functionality
    public Slider horizontalZoomSlider;
    public Slider horizontalMoveSlider;
    private double baseXRange = 0;
    private double baseXCenter = 0;

    // Performance settings
    private static final int MAX_TIME_POINTS = 1000; // Reduced for better performance
    private static final int MAX_FREQ_POINTS = 500;  // Reduced for better performance
    private static final int MAX_FFT_SIZE = 4096;    // Reduced for faster FFT

    // Color constants for consistent styling
    private static final String PROCESSED_COLOR = "#0066cc"; // Blue

    public DashboardPanel(SpectrumAnalyzer analyzer) {
        this.analyzer = analyzer;
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "SpectrumAnalyzer-Background");
            t.setDaemon(true);
            return t;
        });

        setupUI();
        setupZoomAndPan();
    }

    private void setupUI() {
        // Progress indicator
        progressBar = new ProgressBar();
        progressBar.setPrefWidth(200);
        progressBar.setVisible(false);

        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-text-fill: #ffffff;");

        VBox timeBox = new VBox();
        VBox frequencyBox = new VBox();

        // Create horizontal zoom slider
        horizontalZoomSlider = createHorizontalZoomSlider();
        horizontalMoveSlider = createHorizontalMoveSlider();

        NumberAxis timeXAxis = new NumberAxis();
        NumberAxis timeYAxis = new NumberAxis();
        timeXAxis.setLabel("Time (s)");
        timeYAxis.setLabel("Amplitude");
        timeChart = new LineChart<>(timeXAxis, timeYAxis);
        timeChart.setTitle("Time Domain");
        timeChart.setCreateSymbols(false);
        timeChart.setAnimated(false); // Disable animations for better performance
        timeChart.setLegendVisible(true); // Ensure legend is visible
        VBox.setVgrow(timeChart, Priority.ALWAYS);

        NumberAxis freqXAxis = new NumberAxis();
        NumberAxis freqYAxis = new NumberAxis();
        freqXAxis.setLabel("Frequency (Hz)");
        freqYAxis.setLabel("Magnitude (dB)");
        frequencyChart = new LineChart<>(freqXAxis, freqYAxis);
        frequencyChart.setTitle("Frequency Domain");
        frequencyChart.setCreateSymbols(false);
        frequencyChart.setAnimated(false); // Disable animations for better performance
        frequencyChart.setLegendVisible(true); // Ensure legend is visible
        VBox.setVgrow(frequencyChart, Priority.ALWAYS);

        timeBox.getChildren().addAll(timeChart);
        frequencyBox.getChildren().add(frequencyChart);

        getChildren().addAll(timeBox, frequencyBox);

        HBox.setHgrow(timeBox, Priority.ALWAYS);
        HBox.setHgrow(frequencyBox, Priority.ALWAYS);
    }

    private Slider createHorizontalZoomSlider() {
        Slider slider = new Slider(0.1, 10.0, 1.0);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(1.0);
        slider.setMinorTickCount(4);
        slider.setPrefWidth(300);

        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            applyHorizontalZoom(newVal.doubleValue());
        });

        return slider;
    }

    private Slider createHorizontalMoveSlider() {
        Slider slider = new Slider(-1.0, 1.0, 0.0);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(0.5);
        slider.setMinorTickCount(4);
        slider.setPrefWidth(300);

        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            applyHorizontalPan(newVal.doubleValue());
        });

        return slider;
    }

    private void applyHorizontalZoom(double zoomFactor) {
        if (selectedChart == null) return;

        NumberAxis xAxis = (NumberAxis) selectedChart.getXAxis();

        if (baseXRange == 0) {
            baseXRange = xAxis.getUpperBound() - xAxis.getLowerBound();
            baseXCenter = (xAxis.getUpperBound() + xAxis.getLowerBound()) / 2;
        }

        double newRange = baseXRange / zoomFactor;

        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(baseXCenter - newRange / 2);
        xAxis.setUpperBound(baseXCenter + newRange / 2);
    }

    private void applyHorizontalPan(double positionFactor) {
        if (selectedChart == null || baseXRange == 0) return;

        NumberAxis xAxis = (NumberAxis) selectedChart.getXAxis();

        double maxShift = baseXRange / 2.0;
        double offset = maxShift * positionFactor;

        double newCenter = baseXCenter + offset;
        double newRange = xAxis.getUpperBound() - xAxis.getLowerBound();

        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(newCenter - newRange / 2);
        xAxis.setUpperBound(newCenter + newRange / 2);
    }

    private void setupZoomAndPan() {
        this.setFocusTraversable(true);
        setupChartInteraction(timeChart);
        setupChartInteraction(frequencyChart);
        this.setOnMouseClicked(e -> this.requestFocus());
    }

    private void setupChartInteraction(LineChart<Number, Number> chart) {
        chart.setOnMouseClicked(this::handleChartClick);
    }

    private void handleChartClick(MouseEvent event) {
        LineChart<Number, Number> clickedChart = (LineChart<Number, Number>) event.getSource();

        if (event.getClickCount() == 2) {
            resetChartZoom(clickedChart);
            event.consume();
            return;
        }

        selectedChart = clickedChart;

        NumberAxis xAxis = (NumberAxis) selectedChart.getXAxis();
        baseXRange = xAxis.getUpperBound() - xAxis.getLowerBound();
        baseXCenter = (xAxis.getUpperBound() + xAxis.getLowerBound()) / 2;

        this.requestFocus();
        event.consume();
    }

    private void resetChartZoom(LineChart<Number, Number> chart) {
        NumberAxis xAxis = (NumberAxis) chart.getXAxis();
        NumberAxis yAxis = (NumberAxis) chart.getYAxis();
        xAxis.setAutoRanging(true);
        yAxis.setAutoRanging(true);

        horizontalZoomSlider.setValue(1.0);
        horizontalMoveSlider.setValue(0.0);
        baseXRange = 0;
    }

    public void resetAllZoom() {
        NumberAxis timeXAxis = (NumberAxis) timeChart.getXAxis();
        NumberAxis timeYAxis = (NumberAxis) timeChart.getYAxis();
        NumberAxis freqXAxis = (NumberAxis) frequencyChart.getXAxis();
        NumberAxis freqYAxis = (NumberAxis) frequencyChart.getYAxis();

        timeXAxis.setAutoRanging(true);
        timeYAxis.setAutoRanging(true);
        freqXAxis.setAutoRanging(true);
        freqYAxis.setAutoRanging(true);

        horizontalZoomSlider.setValue(1.0);
        horizontalMoveSlider.setValue(0.0);
        baseXRange = 0;
    }

    // Main method to update plots with background processing
    public void updatePlots() {
        if (processingInProgress) {
            System.out.println("Processing already in progress, skipping update");
            return;
        }

        if (analyzer.processedSignal == null) {
            System.out.println("processedSignal is null");
            return;
        }

        processingInProgress = true;

        // Show progress indicator
        Platform.runLater(() -> {
            progressBar.setVisible(true);
            statusLabel.setText("Processing...");
            progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        });

        // Create background task
        Task<PlotData> task = new Task<>() {
            @Override
            protected PlotData call(){
                updateProgress(0, 100);
                updateMessage("Preparing time domain data...");

                // Prepare time domain data
                PlotData plotData = new PlotData();
                plotData.prepareTimeDomainData(analyzer);
                updateProgress(33, 100);

                updateMessage("Computing FFT...");
                // Prepare frequency domain data
                plotData.prepareFrequencyDomainData(analyzer);
                updateProgress(66, 100);

                updateMessage("Finalizing...");
                updateProgress(100, 100);

                return plotData;
            }
        };

        // Update progress bar
        task.progressProperty().addListener((obs, oldProgress, newProgress) -> {
            Platform.runLater(() -> {
                if (newProgress.doubleValue() >= 0) {
                    progressBar.setProgress(newProgress.doubleValue() / 100.0);
                }
            });
        });

        task.messageProperty().addListener((obs, oldMessage, newMessage) -> {
            Platform.runLater(() -> statusLabel.setText(newMessage));
        });

        // Handle task completion
        task.setOnSucceeded(e -> {
            PlotData result = task.getValue();
            Platform.runLater(() -> {
                updateChartsWithData(result);
                progressBar.setVisible(false);
                statusLabel.setText("Ready");
                processingInProgress = false;
            });
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                progressBar.setVisible(false);
                statusLabel.setText("Error occurred");
                processingInProgress = false;
            });
            task.getException().printStackTrace();
        });

        // Execute task
        executorService.submit(task);
    }

    // Data class to hold prepared plot data
    private static class PlotData {
        XYChart.Series<Number, Number> originalTimeSeries;
        XYChart.Series<Number, Number> currentTimeSeries;
        XYChart.Series<Number, Number> originalFreqSeries;
        XYChart.Series<Number, Number> currentFreqSeries;

        void prepareTimeDomainData(SpectrumAnalyzer analyzer) {
            // Always show original signal if it exists
            if (analyzer.originalSignal != null && analyzer.originalSignal.length > 0) {
                originalTimeSeries = new XYChart.Series<>();
                originalTimeSeries.setName("Original Signal");

                double[] signal = analyzer.originalSignal[0];
                int step = Math.max(1, signal.length / MAX_TIME_POINTS);

                for (int i = 0; i < signal.length; i += step) {
                    double time = (double) i / analyzer.sampleRate;
                    originalTimeSeries.getData().add(new XYChart.Data<>(time, signal[i]));
                }
            }

            // Check if we should show processed signal (if it's different from original)
            if (analyzer.processedSignal != null && analyzer.processedSignal.length > 0) {
                boolean filterApplied = analyzer.originalSignal == null ||
                        arraysEqual(analyzer.originalSignal[0], analyzer.processedSignal[0]);

                if (filterApplied) {
                    currentTimeSeries = new XYChart.Series<>();
                    currentTimeSeries.setName("Filtered Signal");

                    double[] signal = analyzer.processedSignal[0];
                    int step = Math.max(1, signal.length / MAX_TIME_POINTS);

                    for (int i = 0; i < signal.length; i += step) {
                        double time = (double) i / analyzer.sampleRate;
                        currentTimeSeries.getData().add(new XYChart.Data<>(time, signal[i]));
                    }

                    // Debug: Check if signals are actually different
                    System.out.println("Filter applied - signals are different");
                    System.out.println("Original first 3 values: " + analyzer.originalSignal[0][0] + ", " +
                            analyzer.originalSignal[0][1] + ", " + analyzer.originalSignal[0][2]);
                    System.out.println("Processed first 3 values: " + analyzer.processedSignal[0][0] + ", " +
                            analyzer.processedSignal[0][1] + ", " + analyzer.processedSignal[0][2]);
                } else {
                    currentTimeSeries = null;
                    System.out.println("No filter applied - signals are identical");
                }
            }
        }

        private boolean arraysEqual(double[] a, double[] b) {
            if (a == null || b == null) return a != b;
            if (a.length != b.length) return true;

            // Use a more lenient comparison for floating point
            for (int i = 0; i < Math.min(100, a.length); i++) { // Check first 100 samples for efficiency
                if (Math.abs(a[i] - b[i]) > 1e-6) {
                    return true;
                }
            }
            return false;
        }

        void prepareFrequencyDomainData(SpectrumAnalyzer analyzer) {
            // Always show original spectrum if it exists
            if (analyzer.originalSignal != null && analyzer.originalSignal.length > 0) {
                originalFreqSeries = createOptimizedFrequencyDomainSeries(analyzer.originalSignal[0],
                        analyzer.sampleRate, "Original Spectrum");
            }

            // Check if we should show processed spectrum (if it's different from original)
            if (analyzer.processedSignal != null && analyzer.processedSignal.length > 0) {
                boolean filterApplied = analyzer.originalSignal == null ||
                        arraysEqual(analyzer.originalSignal[0], analyzer.processedSignal[0]);

                if (filterApplied) {
                    currentFreqSeries = createOptimizedFrequencyDomainSeries(analyzer.processedSignal[0],
                            analyzer.sampleRate, "Filtered Spectrum");
                } else {
                    currentFreqSeries = null;
                }
            }
        }

        private XYChart.Series<Number, Number> createOptimizedFrequencyDomainSeries(double[] signal, double sampleRate, String seriesName) {
            // Use smaller FFT size for better performance
            int fftSize = Math.min(MAX_FFT_SIZE, Integer.highestOneBit(signal.length));
            if (fftSize < signal.length && fftSize * 2 <= MAX_FFT_SIZE) {
                fftSize *= 2;
            }

            double[] chunk = new double[fftSize];
            System.arraycopy(signal, 0, chunk, 0, Math.min(fftSize, signal.length));
            Arrays.fill(chunk, Math.min(fftSize, signal.length), fftSize, 0.0);

            // Apply windowing
            applyHanningWindow(chunk);

            // Compute FFT
            DiscreteFourier dft = new DiscreteFourier(chunk);
            dft.transform();
            double[] magnitude = dft.getMagnitude(true);

            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(seriesName);

            // Only positive frequencies with decimation
            int halfLength = magnitude.length / 2;
            double freqResolution = sampleRate / magnitude.length;
            int step = Math.max(1, halfLength / MAX_FREQ_POINTS);

            for (int i = 1; i < halfLength; i += step) {
                double frequency = i * freqResolution;
                double magnitudeValue = magnitude[i] / (fftSize / 2.0) / 0.5; // Normalize and correct for windowing

                double magnitudeDb = magnitudeValue > 0 ? 20 * Math.log10(magnitudeValue) : -120;
                series.getData().add(new XYChart.Data<>(frequency, magnitudeDb));
            }

            return series;
        }

        private void applyHanningWindow(double[] data) {
            int N = data.length;
            for (int i = 0; i < N; i++) {
                double window = 0.5 * (1 - Math.cos(2 * Math.PI * i / (N - 1)));
                data[i] *= window;
            }
        }
    }

    // Update charts on UI thread with pre-computed data
    private void updateChartsWithData(PlotData plotData) {
        // Clear existing data
        timeChart.getData().clear();
        frequencyChart.getData().clear();

        // Time domain chart - add original first (series0 = orange)
        if (plotData.originalTimeSeries != null) {
            timeChart.getData().add(plotData.originalTimeSeries); // series0 (orange)
        }
        // Add filtered second (series1 = blue)
        if (plotData.currentTimeSeries != null) {
            timeChart.getData().add(plotData.currentTimeSeries); // series1 (blue)
        }

        // Frequency domain chart - same order
        if (plotData.originalFreqSeries != null) {
            frequencyChart.getData().add(plotData.originalFreqSeries); // series0 (orange)
        }
        if (plotData.currentFreqSeries != null) {
            frequencyChart.getData().add(plotData.currentFreqSeries); // series1 (blue)
        }

        Platform.runLater(() -> {
            // Fix line stroke color
            for (XYChart.Series<Number, Number> series : timeChart.getData()) {
                if ("Filtered Signal".equals(series.getName()) && series.getNode() != null) {
                    series.getNode().setStyle("-fx-stroke: " + PROCESSED_COLOR + " !important;");
                }
            }

            for (XYChart.Series<Number, Number> series : frequencyChart.getData()) {
                if ("Filtered Spectrum".equals(series.getName()) && series.getNode() != null) {
                    series.getNode().setStyle("-fx-stroke: " + PROCESSED_COLOR + " !important;");
                }
            }

            // Fix legend symbol color
            for (Node legend : timeChart.lookupAll(".chart-legend-item")) {
                if (legend instanceof Label label && label.getText().equals("Filtered Signal")) {
                    Node symbol = label.getGraphic();
                    if (symbol != null) {
                        symbol.setStyle("-fx-background-color: " + PROCESSED_COLOR + ", white;");
                    }
                }
            }

            for (Node legend : frequencyChart.lookupAll(".chart-legend-item")) {
                if (legend instanceof Label label && label.getText().equals("Filtered Spectrum")) {
                    Node symbol = label.getGraphic();
                    if (symbol != null) {
                        symbol.setStyle("-fx-background-color: " + PROCESSED_COLOR + ", white;");
                    }
                }
            }
        });

    }

    public void showChartMode(boolean showTime, boolean showFrequency) {
        getChildren().clear();
        if (showTime) {
            VBox.setVgrow(timeChart, Priority.ALWAYS);
            HBox.setHgrow(timeChart, Priority.ALWAYS);
            timeChart.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            getChildren().add(timeChart);
        }

        if (showFrequency) {
            VBox.setVgrow(frequencyChart, Priority.ALWAYS);
            HBox.setHgrow(frequencyChart, Priority.ALWAYS);
            frequencyChart.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            getChildren().add(frequencyChart);
        }
    }

}