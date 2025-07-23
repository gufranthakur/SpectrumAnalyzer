module com.spectrumanalyzer {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.github.psambit9791.jdsp;
    requires atlantafx.base;


    opens com.spectrumanalyzer to javafx.fxml;
    exports com.spectrumanalyzer;
}