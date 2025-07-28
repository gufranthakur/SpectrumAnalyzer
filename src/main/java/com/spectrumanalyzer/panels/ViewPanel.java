package com.spectrumanalyzer.panels;

import com.spectrumanalyzer.SpectrumAnalyzer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ViewPanel extends VBox {

    private SpectrumAnalyzer analyzer;
    private Button resetViewsButton;
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
                viewFrequencyDomainButton
        );
    }

}
