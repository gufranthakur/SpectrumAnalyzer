module spectrumanalyzer.core {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires atlantafx.base;

    opens spectrumanalyzer.core to javafx.fxml;
    exports spectrumanalyzer.core;
}