package com.spectrumanalyzer;

import javafx.scene.chart.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.Slider;
import javafx.geometry.Bounds;
import com.github.psambit9791.jdsp.transform.DiscreteFourier;

import java.util.Arrays;

public class DashboardPanel extends VBox {
    private SpectrumAnalyzer analyzer;
    public LineChart<Number, Number> timeChart;
    public LineChart<Number, Number> frequencyChart;

    // Zoom and pan functionality
    private LineChart<Number, Number> selectedChart = null;
    private boolean ctrlPressed = false;
    private double lastMouseX = 0;
    private double lastMouseY = 0;
    private boolean isDragging = false;

    // Global zoom rate control - can be adjusted at runtime
    public double zoomRate = 0.8; // Default 30% zoom increment (0.7/1.4 factors)

    // Horizontal zoom functionality
    public Slider horizontalZoomSlider;
    private double baseXRange = 0; // Store the original X range
    private double baseXCenter = 0; // Store the original X center

    public DashboardPanel(SpectrumAnalyzer analyzer) {
        this.analyzer = analyzer;
        setupUI();
        setupZoomAndPan();
    }

    private void setupUI() {
        VBox timeBox = new VBox();
        VBox frequencyBox = new VBox();

        // Create horizontal zoom slider
        horizontalZoomSlider = createHorizontalZoomSlider();

        NumberAxis timeXAxis = new NumberAxis();
        NumberAxis timeYAxis = new NumberAxis();
        timeXAxis.setLabel("Time (s)");
        timeYAxis.setLabel("Amplitude");
        timeChart = new LineChart<>(timeXAxis, timeYAxis);
        timeChart.setTitle("Time Domain");
        timeChart.setCreateSymbols(false);
        timeChart.setLegendVisible(true);
        VBox.setVgrow(timeChart, Priority.ALWAYS);

        NumberAxis freqXAxis = new NumberAxis();
        NumberAxis freqYAxis = new NumberAxis();
        freqXAxis.setLabel("Frequency (Hz)");
        freqYAxis.setLabel("Magnitude (dB)");
        frequencyChart = new LineChart<>(freqXAxis, freqYAxis);
        frequencyChart.setTitle("Frequency Domain");
        frequencyChart.setCreateSymbols(false);
        frequencyChart.setLegendVisible(true);
        VBox.setVgrow(frequencyChart, Priority.ALWAYS);

        // Add slider and charts to boxes
        timeBox.getChildren().addAll(timeChart);
        frequencyBox.getChildren().add(frequencyChart);

        getChildren().addAll(timeBox, frequencyBox);

        HBox.setHgrow(timeBox, Priority.ALWAYS);
        HBox.setHgrow(frequencyBox, Priority.ALWAYS);
    }

    private Slider createHorizontalZoomSlider() {
        Slider slider = new Slider(0.1, 10.0, 1.0); // Min: 0.1x, Max: 5x, Default: 1x
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(1.0);
        slider.setMinorTickCount(4);
        slider.setPrefWidth(300);

        // Add listener for real-time zoom
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            applyHorizontalZoom(newVal.doubleValue());
        });

        return slider;
    }

    private void applyHorizontalZoom(double zoomFactor) {
        if (selectedChart == null) return;

        NumberAxis xAxis = (NumberAxis) selectedChart.getXAxis();

        // Store base range if not set (first time or after reset)
        if (baseXRange == 0) {
            baseXRange = xAxis.getUpperBound() - xAxis.getLowerBound();
            baseXCenter = (xAxis.getUpperBound() + xAxis.getLowerBound()) / 2;
        }

        // Calculate new range based on zoom factor
        double newRange = baseXRange / zoomFactor; // Smaller range = more zoomed in

        // Apply new bounds centered on the base center
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(baseXCenter - newRange / 2);
        xAxis.setUpperBound(baseXCenter + newRange / 2);
    }

    private void setupZoomAndPan() {
        // Make the panel focusable to receive key events
        this.setFocusTraversable(true);

        // Setup for time domain chart
        setupChartInteraction(timeChart);

        // Setup for frequency domain chart
        setupChartInteraction(frequencyChart);

        // Global key event handling
        this.setOnKeyPressed(this::handleKeyPressed);
        this.setOnKeyReleased(this::handleKeyReleased);

        // Request focus when clicked
        this.setOnMouseClicked(e -> this.requestFocus());
    }

    private void setupChartInteraction(LineChart<Number, Number> chart) {
        // Chart selection on click and double-click reset
        chart.setOnMouseClicked(this::handleChartClick);

        // Mouse wheel zoom
        chart.setOnScroll(this::handleScroll);

        // Mouse drag for panning
        chart.setOnMousePressed(this::handleMousePressed);
        chart.setOnMouseDragged(this::handleMouseDragged);
        chart.setOnMouseReleased(this::handleMouseReleased);
    }

    private void handleChartClick(MouseEvent event) {
        LineChart<Number, Number> clickedChart = (LineChart<Number, Number>) event.getSource();

        // Double-click to reset zoom
        if (event.getClickCount() == 2) {
            resetChartZoom(clickedChart);
            event.consume();
            return;
        }

        // Single click to select chart (no visual feedback)
        selectedChart = clickedChart;

        // Reset base values for horizontal zoom
        NumberAxis xAxis = (NumberAxis) selectedChart.getXAxis();
        baseXRange = xAxis.getUpperBound() - xAxis.getLowerBound();
        baseXCenter = (xAxis.getUpperBound() + xAxis.getLowerBound()) / 2;

        this.requestFocus();
        event.consume();
    }

    private void deselectChart() {
        selectedChart = null;
        // Reset horizontal zoom slider and base values
        horizontalZoomSlider.setValue(1.0);
        baseXRange = 0;
    }



    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.CONTROL) {
            ctrlPressed = true;
        }
    }

    private void handleKeyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.CONTROL) {
            ctrlPressed = false;
        }
    }

    private void handleScroll(ScrollEvent event) {
        if (!ctrlPressed || selectedChart == null) {
            return;
        }

        LineChart<Number, Number> chart = (LineChart<Number, Number>) event.getSource();
        if (chart != selectedChart) {
            return;
        }

        NumberAxis xAxis = (NumberAxis) chart.getXAxis();
        NumberAxis yAxis = (NumberAxis) chart.getYAxis();

        double deltaY = event.getDeltaY();
        double zoomFactor = deltaY > 0 ? (1.0 - zoomRate) : (1.0 + zoomRate); // Use configurable zoom rate

        // Get current bounds
        double xLower = xAxis.getLowerBound();
        double xUpper = xAxis.getUpperBound();
        double yLower = yAxis.getLowerBound();
        double yUpper = yAxis.getUpperBound();

        // Calculate zoom center (mouse position)
        double mouseXRatio = event.getX() / chart.getWidth();
        double mouseYRatio = 1.0 - (event.getY() / chart.getHeight()); // Invert Y

        // Calculate new bounds
        double xRange = xUpper - xLower;
        double yRange = yUpper - yLower;
        double newXRange = xRange * zoomFactor;
        double newYRange = yRange * zoomFactor;

        double xCenter = xLower + xRange * mouseXRatio;
        double yCenter = yLower + yRange * mouseYRatio;

        double newXLower = xCenter - newXRange * mouseXRatio;
        double newXUpper = xCenter + newXRange * (1 - mouseXRatio);
        double newYLower = yCenter - newYRange * mouseYRatio;
        double newYUpper = yCenter + newYRange * (1 - mouseYRatio);

        // Apply new bounds
        xAxis.setAutoRanging(false);
        yAxis.setAutoRanging(false);
        xAxis.setLowerBound(newXLower);
        xAxis.setUpperBound(newXUpper);
        yAxis.setLowerBound(newYLower);
        yAxis.setUpperBound(newYUpper);

        event.consume();
    }

    private void handleMousePressed(MouseEvent event) {
        if (!ctrlPressed || selectedChart == null) {
            return;
        }

        LineChart<Number, Number> chart = (LineChart<Number, Number>) event.getSource();
        if (chart != selectedChart) {
            return;
        }

        lastMouseX = event.getX();
        lastMouseY = event.getY();
        isDragging = true;
        event.consume();
    }

    private void handleMouseDragged(MouseEvent event) {
        if (!ctrlPressed || !isDragging || selectedChart == null) {
            return;
        }

        LineChart<Number, Number> chart = (LineChart<Number, Number>) event.getSource();
        if (chart != selectedChart) {
            return;
        }

        NumberAxis xAxis = (NumberAxis) chart.getXAxis();
        NumberAxis yAxis = (NumberAxis) chart.getYAxis();

        double deltaX = event.getX() - lastMouseX;
        double deltaY = event.getY() - lastMouseY;

        // Calculate pan amounts based on axis ranges and chart size
        double xRange = xAxis.getUpperBound() - xAxis.getLowerBound();
        double yRange = yAxis.getUpperBound() - yAxis.getLowerBound();

        double xPan = -(deltaX / chart.getWidth()) * xRange;
        double yPan = (deltaY / chart.getHeight()) * yRange; // Invert Y

        // Apply pan
        xAxis.setLowerBound(xAxis.getLowerBound() + xPan);
        xAxis.setUpperBound(xAxis.getUpperBound() + xPan);
        yAxis.setLowerBound(yAxis.getLowerBound() + yPan);
        yAxis.setUpperBound(yAxis.getUpperBound() + yPan);

        lastMouseX = event.getX();
        lastMouseY = event.getY();
        event.consume();
    }

    private void handleMouseReleased(MouseEvent event) {
        isDragging = false;
    }

    // Method to reset zoom for a specific chart
    private void resetChartZoom(LineChart<Number, Number> chart) {
        NumberAxis xAxis = (NumberAxis) chart.getXAxis();
        NumberAxis yAxis = (NumberAxis) chart.getYAxis();
        xAxis.setAutoRanging(true);
        yAxis.setAutoRanging(true);

        // Reset horizontal zoom slider
        horizontalZoomSlider.setValue(1.0);
        baseXRange = 0; // Will be recalculated on next selection
    }

    // Method to reset zoom for selected chart
    public void resetZoom() {
        if (selectedChart != null) {
            resetChartZoom(selectedChart);
        }
    }

    // Method to reset zoom for all charts
    public void resetAllZoom() {
        NumberAxis timeXAxis = (NumberAxis) timeChart.getXAxis();
        NumberAxis timeYAxis = (NumberAxis) timeChart.getYAxis();
        NumberAxis freqXAxis = (NumberAxis) frequencyChart.getXAxis();
        NumberAxis freqYAxis = (NumberAxis) frequencyChart.getYAxis();

        timeXAxis.setAutoRanging(true);
        timeYAxis.setAutoRanging(true);
        freqXAxis.setAutoRanging(true);
        freqYAxis.setAutoRanging(true);

        // Reset horizontal zoom slider
        horizontalZoomSlider.setValue(1.0);
        baseXRange = 0;
    }

    public void updatePlots() {
        System.out.println("updatePlots() called");
        if (analyzer.processedSignal == null) {
            System.out.println("processedSignal is null");
            return;
        }

        double[] firstChannel = analyzer.processedSignal[0];
        System.out.println("First few signal values: " + firstChannel[0] + ", " + firstChannel[1] + ", " + firstChannel[2]);
        System.out.println("Sample rate: " + analyzer.sampleRate);

        updateTimeDomainPlot();
        updateFrequencyDomainPlot();
    }

    private void updateTimeDomainPlot() {
        timeChart.getData().clear();

        // Add original signal if available
        if (analyzer.originalSignal != null && analyzer.originalSignal.length > 0) {
            XYChart.Series<Number, Number> originalSeries = new XYChart.Series<>();
            originalSeries.setName("Original Signal");

            double[] signal = analyzer.originalSignal[0];
            int maxPoints = 2000; // Limit to 2000 points
            int step = Math.max(1, signal.length / maxPoints);

            for (int i = 0; i < signal.length; i += step) {
                double time = (double) i / analyzer.sampleRate;
                originalSeries.getData().add(new XYChart.Data<>(time, signal[i]));
            }

            timeChart.getData().add(originalSeries);

            // Style the original series (default color - usually red/orange)
            originalSeries.getNode().setStyle("-fx-stroke: #ff6b35; -fx-stroke-width: 1px;");
        }

        // Add current/processed signal
        XYChart.Series<Number, Number> currentSeries = new XYChart.Series<>();
        currentSeries.setName(analyzer.originalSignal != null ? "Current Signal" : "Signal");

        double[] signal = analyzer.processedSignal[0];
        int maxPoints = 2000; // Limit to 2000 points
        int step = Math.max(1, signal.length / maxPoints);

        for (int i = 0; i < signal.length; i += step) {
            double time = (double) i / analyzer.sampleRate;
            currentSeries.getData().add(new XYChart.Data<>(time, signal[i]));
        }

        timeChart.getData().add(currentSeries);

        // Style the current series in blue
        currentSeries.getNode().setStyle("-fx-stroke: #0066cc; -fx-stroke-width: 2px;");
    }

    private void updateFrequencyDomainPlot() {
        frequencyChart.getData().clear();

        // Add original signal spectrum if available
        if (analyzer.originalSignal != null && analyzer.originalSignal.length > 0) {
            XYChart.Series<Number, Number> originalSeries = createFrequencyDomainSeries(analyzer.originalSignal[0], "Original Spectrum");
            frequencyChart.getData().add(originalSeries);

            // Style the original spectrum
            originalSeries.getNode().setStyle("-fx-stroke: #ff6b35; -fx-stroke-width: 1px;");
        }

        // Add current signal spectrum
        XYChart.Series<Number, Number> currentSeries = createFrequencyDomainSeries(
                analyzer.processedSignal[0],
                analyzer.originalSignal != null ? "Current Spectrum" : "Spectrum"
        );
        frequencyChart.getData().add(currentSeries);

        // Style the current spectrum in blue
        currentSeries.getNode().setStyle("-fx-stroke: #0066cc; -fx-stroke-width: 2px;");
    }

    private XYChart.Series<Number, Number> createFrequencyDomainSeries(double[] signal, String seriesName) {
        // Use power-of-2 FFT size for better performance
        int fftSize = Integer.highestOneBit(Math.min(8192, signal.length));
        if (fftSize < signal.length && fftSize * 2 <= 8192) {
            fftSize *= 2; // Use next power of 2 if it fits
        }

        double[] chunk = new double[fftSize];
        System.arraycopy(signal, 0, chunk, 0, Math.min(fftSize, signal.length));

        // Zero-pad if signal is shorter than fftSize
        Arrays.fill(chunk, Math.min(fftSize, signal.length), fftSize, 0.0);

        // Apply windowing to reduce spectral leakage
        applyHanningWindow(chunk);

        DiscreteFourier dft = new DiscreteFourier(chunk);
        dft.transform();

        double[] magnitude = dft.getMagnitude(true);

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(seriesName);

        // Only plot positive frequencies (Nyquist theorem)
        int halfLength = magnitude.length / 2;
        double freqResolution = (double) analyzer.sampleRate / magnitude.length;

        // Skip DC component (i=0) and limit points for performance
        int maxPoints = 1000;
        int step = Math.max(1, halfLength / maxPoints);

        for (int i = 1; i < halfLength; i += step) { // Start from 1 to skip DC
            double frequency = i * freqResolution;

            // FIXED: Proper magnitude calculation
            double magnitudeValue = magnitude[i];

            // Apply proper scaling for FFT normalization
            magnitudeValue = magnitudeValue / (fftSize / 2.0); // Normalize by FFT size

            // Apply window correction factor (for Hanning window)
            magnitudeValue = magnitudeValue / 0.5; // Hanning window correction

            // Convert to dB with proper noise floor
            double magnitudeDb;
            if (magnitudeValue > 0) {
                magnitudeDb = 20 * Math.log10(magnitudeValue);
            } else {
                magnitudeDb = -120; // Set noise floor to -120 dB
            }

            series.getData().add(new XYChart.Data<>(frequency, magnitudeDb));
        }

        return series;
    }

    // Improved windowing with proper normalization
    private void applyHanningWindow(double[] data) {
        int N = data.length;
        for (int i = 0; i < N; i++) {
            double window = 0.5 * (1 - Math.cos(2 * Math.PI * i / (N - 1)));
            data[i] *= window;
        }
    }

    // Alternative: More accurate frequency domain analysis method
    private XYChart.Series<Number, Number> createAccurateFrequencyDomainSeries(double[] signal, String seriesName) {
        // Ensure we have enough samples for meaningful analysis
        int minFFTSize = 512;
        int fftSize = Math.max(minFFTSize, Integer.highestOneBit(signal.length));
        if (fftSize < signal.length && fftSize * 2 <= 16384) {
            fftSize *= 2;
        }

        double[] paddedSignal = new double[fftSize];
        System.arraycopy(signal, 0, paddedSignal, 0, Math.min(signal.length, fftSize));

        // Apply Hanning window
        applyHanningWindow(paddedSignal);

        // Perform FFT
        DiscreteFourier dft = new DiscreteFourier(paddedSignal);
        dft.transform();

        double[] magnitude = dft.getMagnitude(true);

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(seriesName);

        int halfLength = magnitude.length / 2;
        double freqResolution = (double) analyzer.sampleRate / magnitude.length;

        // Calculate RMS scaling factor for proper amplitude representation
        double windowRMS = Math.sqrt(0.375); // RMS of Hanning window
        double scaleFactor = 2.0 / (fftSize * windowRMS); // Proper scaling

        int maxPoints = 1000;
        int step = Math.max(1, halfLength / maxPoints);

        for (int i = 1; i < halfLength; i += step) {
            double frequency = i * freqResolution;

            // Apply proper scaling
            double scaledMagnitude = magnitude[i] * scaleFactor;

            // Convert to dB
            double magnitudeDb = scaledMagnitude > 1e-12 ?
                    20 * Math.log10(scaledMagnitude) : -120;

            series.getData().add(new XYChart.Data<>(frequency, magnitudeDb));
        }

        return series;
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