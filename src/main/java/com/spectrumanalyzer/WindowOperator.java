package com.spectrumanalyzer;

import com.github.psambit9791.jdsp.windows.*;
import com.spectrumanalyzer.SpectrumAnalyzer;
import java.util.Arrays;

public class WindowOperator {
    private SpectrumAnalyzer analyzer;

    public WindowOperator(SpectrumAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    public void applyWindow(String windowType, double kaiserBeta) {
        if (analyzer.processedSignal == null) {
            analyzer.showAlert("No signal to apply window to");
            return;
        }

        try {
            double[][] windowedSignal = applyWindowFunction(analyzer.processedSignal, windowType, kaiserBeta);
            if (windowedSignal != null) {
                analyzer.processedSignal = windowedSignal;
            } else {
                analyzer.showAlert("Failed to apply window function");
            }
        } catch (Exception e) {
            analyzer.showAlert("Error applying window: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private double[][] applyWindowFunction(double[][] signal, String windowType, double kaiserBeta) {
        int channels = signal.length;
        int length = signal[0].length;

        double[] window = generateWindow(windowType, length, kaiserBeta);
        if (window == null) return null;

        double[][] windowedSignal = new double[channels][length];
        for (int ch = 0; ch < channels; ch++) {
            for (int i = 0; i < length; i++) {
                windowedSignal[ch][i] = signal[ch][i] * window[i];
            }
        }

        return windowedSignal;
    }

    private double[] generateWindow(String windowType, int length, double kaiserBeta) {
        try {
            return switch (windowType) {
                case "Rectangular" -> generateRectangularWindow(length);
                case "Hanning" -> new Hanning(length).getWindow();
                case "Hamming" -> new Hamming(length).getWindow();
                case "Blackman" -> new Blackman(length).getWindow();
                case "Kaiser" -> new Kaiser(length, kaiserBeta).getWindow();
                default -> {
                    System.err.println("Unknown window type: " + windowType);
                    yield null;
                }
            };
        } catch (Exception e) {
            System.err.println("Error generating " + windowType + " window: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private double[] generateRectangularWindow(int length) {
        double[] window = new double[length];
        Arrays.fill(window, 1.0);
        return window;
    }

}
