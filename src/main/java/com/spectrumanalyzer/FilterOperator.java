package com.spectrumanalyzer;

import com.github.psambit9791.jdsp.filter.Butterworth;
import com.github.psambit9791.jdsp.filter.Chebyshev;
import com.github.psambit9791.jdsp.filter.Bessel;

public class FilterOperator {
    private SpectrumAnalyzer analyzer;

    public FilterOperator(SpectrumAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    public void applyFilter(String filterType, double cutoff, double lowCutoff, double highCutoff, int order, double ripple) {
        if (analyzer.originalSignal == null) return;

        double[] signal = analyzer.processedSignal[0];
        double[] filtered = null;

        try {
            switch (filterType) {
                case "Low Pass":
                    Butterworth blp = new Butterworth(analyzer.sampleRate);
                    filtered = blp.lowPassFilter(signal, order, (int)cutoff);
                    break;

                case "High Pass":
                    Butterworth bhp = new Butterworth(analyzer.sampleRate);
                    filtered = bhp.highPassFilter(signal, order, (int)cutoff);
                    break;

                case "Band Pass":
                    Butterworth bbp = new Butterworth(analyzer.sampleRate);
                    filtered = bbp.bandPassFilter(signal, order, (int)lowCutoff, (int)highCutoff);
                    break;

                case "Band Stop":
                    Butterworth bbs = new Butterworth(analyzer.sampleRate);
                    filtered = bbs.bandStopFilter(signal, order, (int)lowCutoff, (int)highCutoff);
                    break;

                case "Butterworth Low Pass":
                    Butterworth blp2 = new Butterworth(analyzer.sampleRate);
                    filtered = blp2.lowPassFilter(signal, order, (int)cutoff);
                    break;

                case "Butterworth High Pass":
                    Butterworth bhp2 = new Butterworth(analyzer.sampleRate);
                    filtered = bhp2.highPassFilter(signal, order, (int)cutoff);
                    break;

                case "Butterworth Band Pass":
                    Butterworth bbp2 = new Butterworth(analyzer.sampleRate);
                    filtered = bbp2.bandPassFilter(signal, order, (int)lowCutoff, (int)highCutoff);
                    break;

                case "Chebyshev Low Pass":
                    Chebyshev clp = new Chebyshev(analyzer.sampleRate, ripple, 1);
                    filtered = clp.lowPassFilter(signal, order, (int)cutoff);
                    break;

                case "Chebyshev High Pass":
                    Chebyshev chp = new Chebyshev(analyzer.sampleRate, ripple, 1);
                    filtered = chp.highPassFilter(signal, order, (int)cutoff);
                    break;

                case "Chebyshev Band Pass":
                    Chebyshev cbp = new Chebyshev(analyzer.sampleRate, ripple, 1);
                    filtered = cbp.bandPassFilter(signal, order, (int)lowCutoff, (int)highCutoff);
                    break;

                // New Bessel filter cases
                case "Bessel Low Pass":
                    Bessel bslp = new Bessel(analyzer.sampleRate);
                    filtered = bslp.lowPassFilter(signal, order, (int)cutoff);
                    break;

                case "Bessel High Pass":
                    Bessel bshp = new Bessel(analyzer.sampleRate);
                    filtered = bshp.highPassFilter(signal, order, (int)cutoff);
                    break;

                case "Bessel Band Pass":
                    Bessel bsbp = new Bessel(analyzer.sampleRate);
                    filtered = bsbp.bandPassFilter(signal, order, (int)lowCutoff, (int)highCutoff);
                    break;
            }

            if (filtered != null) {
                analyzer.processedSignal[0] = filtered;

                if (analyzer.channels > 1 && analyzer.processedSignal.length > 1) {
                    for (int i = 1; i < analyzer.channels; i++) {
                        analyzer.processedSignal[i] = applyFilterToChannel(filterType, analyzer.processedSignal[i], cutoff, lowCutoff, highCutoff, order, ripple);
                    }
                }
            }
        } catch (Exception e) {
            analyzer.showAlert("Filter application failed: " + e.getMessage());
        }
    }

    private double[] applyFilterToChannel(String filterType, double[] channelSignal, double cutoff, double lowCutoff, double highCutoff, int order, double ripple) {
        try {
            switch (filterType) {
                case "Low Pass":
                case "Butterworth Low Pass":
                    Butterworth blp = new Butterworth(analyzer.sampleRate);
                    return blp.lowPassFilter(channelSignal, order, (int)cutoff);

                case "High Pass":
                case "Butterworth High Pass":
                    Butterworth bhp = new Butterworth(analyzer.sampleRate);
                    return bhp.highPassFilter(channelSignal, order, (int)cutoff);

                case "Band Pass":
                case "Butterworth Band Pass":
                    Butterworth bbp = new Butterworth(analyzer.sampleRate);
                    return bbp.bandPassFilter(channelSignal, order, (int)lowCutoff, (int)highCutoff);

                case "Band Stop":
                    Butterworth bbs = new Butterworth(analyzer.sampleRate);
                    return bbs.bandStopFilter(channelSignal, order, (int)lowCutoff, (int)highCutoff);

                case "Chebyshev Low Pass":
                    Chebyshev clp = new Chebyshev(analyzer.sampleRate, ripple, 1);
                    return clp.lowPassFilter(channelSignal, order, (int)cutoff);

                case "Chebyshev High Pass":
                    Chebyshev chp = new Chebyshev(analyzer.sampleRate, ripple, 1);
                    return chp.highPassFilter(channelSignal, order, (int)cutoff);

                case "Chebyshev Band Pass":
                    Chebyshev cbp = new Chebyshev(analyzer.sampleRate, ripple, 1);
                    return cbp.bandPassFilter(channelSignal, order, (int)lowCutoff, (int)highCutoff);

                // New Bessel filter cases for multi-channel processing
                case "Bessel Low Pass":
                    Bessel bslp = new Bessel(analyzer.sampleRate);
                    return bslp.lowPassFilter(channelSignal, order, (int)cutoff);

                case "Bessel High Pass":
                    Bessel bshp = new Bessel(analyzer.sampleRate);
                    return bshp.highPassFilter(channelSignal, order, (int)cutoff);

                case "Bessel Band Pass":
                    Bessel bsbp = new Bessel(analyzer.sampleRate);
                    return bsbp.bandPassFilter(channelSignal, order, (int)lowCutoff, (int)highCutoff);

                default:
                    return channelSignal;
            }
        } catch (Exception e) {
            return channelSignal;
        }
    }
}