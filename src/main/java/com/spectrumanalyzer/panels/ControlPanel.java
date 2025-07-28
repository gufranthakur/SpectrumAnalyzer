package com.spectrumanalyzer.panels;

import com.spectrumanalyzer.SpectrumAnalyzer;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ControlPanel extends VBox {
    private SpectrumAnalyzer analyzer;
    private Accordion filterAccordion;
    private ToggleGroup toggleGroup;
    private TextField cutoffField;
    private TextField lowCutoffField;
    private TextField highCutoffField;
    private TextField rippleField;
    private Slider orderSlider;

    private ComboBox<String> cutOffUnitBox, lowCutOffUnitBox, highCutoffUnitBox;

    private Button resetViewsButton;

    private Slider zoomSlider;
    private RadioButton viewTimeDomainButton, viewFrequencyDomainButton, viewBothDomainButton;

    // Windowing components
    private ToggleGroup windowToggleGroup;
    private RadioButton rectangularButton, hanningButton, hammingButton, blackmanButton, kaiserButton;
    private TextField kaiserBetaField;

    public ControlPanel(SpectrumAnalyzer analyzer) {
        this.analyzer = analyzer;
        this.setStyle("-fx-background-color: #171716");
        setMaxWidth(Double.MAX_VALUE);
        setPadding(new Insets(10));
        setSpacing(10);
        setupUI();
    }

    private void setupUI() {
        Label titleLabel = new Label("Filter Controls");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        filterAccordion = new Accordion();
        TitledPane basicFiltersPane = createBasicFiltersPane();
        TitledPane butterworthPane = createButterworthPane();
        TitledPane chebyshevPane = createChebyshevPane();
        TitledPane besselPane = createBesselPane(); // Added Bessel pane
        filterAccordion.getPanes().addAll(basicFiltersPane, butterworthPane, chebyshevPane, besselPane);
        VBox.setVgrow(filterAccordion, Priority.NEVER);

        VBox parameterBox = createParameterBox();

        Button applyButton = new Button("Apply Filter");
        applyButton.setStyle("-fx-background-color: #38ab03;");

        Button resetButton = new Button("Reset Signal");
        resetButton.setStyle("-fx-background-color: #e37a09;");

        applyButton.setOnAction(e -> applySelectedFilter());
        resetButton.setOnAction(e -> resetSignal());

        HBox buttonBox = new HBox(10, applyButton, resetButton);
        buttonBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(applyButton, Priority.ALWAYS);
        HBox.setHgrow(resetButton, Priority.ALWAYS);

        // Create windowing section
        VBox windowingBox = createWindowingBox();

        VBox settingsBox = createViewsBox();

        getChildren().addAll(
                titleLabel,
                filterAccordion,
                makeSeparator(),
                parameterBox,
                buttonBox,
                analyzer.dashboardPanel.statusLabel,
                analyzer.dashboardPanel.progressBar,
                makeSeparator(),
                windowingBox,
                makeSeparator(),
                settingsBox
        );

        // Add this inside setupUI()
        setupFilterToggleListeners();
        setupWindowToggleListeners();
    }

    private VBox createWindowingBox() {
        VBox windowingBox = new VBox(10);
        windowingBox.setPadding(new Insets(10));

        Label titleLabel = new Label("Windowing");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Create toggle group for windowing
        windowToggleGroup = new ToggleGroup();

        // Create radio buttons
        rectangularButton = new RadioButton("Rectangular");
        hanningButton = new RadioButton("Hanning");
        hammingButton = new RadioButton("Hamming");
        blackmanButton = new RadioButton("Blackman");
        kaiserButton = new RadioButton("Kaiser");

        // Set all buttons to the same toggle group and style
        RadioButton[] windowButtons = {rectangularButton, hanningButton, hammingButton,
                blackmanButton, kaiserButton};

        for (RadioButton rb : windowButtons) {
            rb.setToggleGroup(windowToggleGroup);
            rb.setMaxWidth(Double.MAX_VALUE);
            rb.setStyle("-fx-background-color: #171716;");
        }

        // Set Rectangular as default
        rectangularButton.setSelected(true);

        // Create layout with 3 rows of 2 buttons each
        HBox row1 = new HBox(10, rectangularButton, hanningButton);
        HBox row2 = new HBox(10, hammingButton, blackmanButton);
        HBox row3 = new HBox(10, kaiserButton);

        // Make buttons expand to fill available space
        for (HBox row : new HBox[]{row1, row2, row3}) {
            row.setMaxWidth(Double.MAX_VALUE);
            for (Node node : row.getChildren()) {
                HBox.setHgrow(node, Priority.ALWAYS);
            }
        }

        // Parameter fields for Kaiser and Gaussian windows
        kaiserBetaField = new TextField("8.6");
        kaiserBetaField.setPrefWidth(85);
        kaiserBetaField.setDisable(true); // Initially disabled

        // Apply window button
        Button applyWindowButton = new Button("Apply Window");
        applyWindowButton.setStyle("-fx-background-color: #0373ab;");
        applyWindowButton.setMaxWidth(Double.MAX_VALUE);
        applyWindowButton.setOnAction(e -> applySelectedWindow());

        windowingBox.getChildren().addAll(
                titleLabel,
                row1,
                row2,
                row3,
                createStyledLabel("Kaiser Beta:"),
                kaiserBetaField,
                applyWindowButton
        );

        return windowingBox;
    }

    private void setupWindowToggleListeners() {
        windowToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateWindowParameterFieldState(((RadioButton) newVal).getText());
            }
        });
    }

    private void updateWindowParameterFieldState(String windowType) {
        // Disable all parameter fields first
        kaiserBetaField.setDisable(true);

        // Enable specific fields based on window type
        switch (windowType) {
            case "Kaiser":
                kaiserBetaField.setDisable(false);
                break;
        }
    }

    private String getSelectedWindowType() {
        RadioButton selected = (RadioButton) windowToggleGroup.getSelectedToggle();
        if (selected != null) return selected.getText();
        return "Rectangular"; // Default
    }

    private void applySelectedWindow() {
        if (analyzer.originalSignal == null) {
            analyzer.showAlert("Please load an audio file first");
            return;
        }

        try {
            String windowType = getSelectedWindowType();
            double kaiserBeta = Double.parseDouble(kaiserBetaField.getText());

            // Apply window using WindowOperator (you'll need to create this class)
            analyzer.windowOperator.applyWindow(windowType, kaiserBeta);
            analyzer.dashboardPanel.updatePlots();
        } catch (NumberFormatException e) {
            analyzer.showAlert("Invalid parameter values for window");
        }
    }

    private TitledPane createBasicFiltersPane() {
        VBox content = new VBox(10);

        content.setPadding(new Insets(10));
        toggleGroup = new ToggleGroup();

        RadioButton lowPass = new RadioButton("Low Pass");
        RadioButton highPass = new RadioButton("High Pass");
        RadioButton bandPass = new RadioButton("Band Pass");
        RadioButton bandStop = new RadioButton("Band Stop");

        for (RadioButton rb : new RadioButton[]{lowPass, highPass, bandPass, bandStop}) {
            rb.setToggleGroup(toggleGroup);
            rb.setMaxWidth(Double.MAX_VALUE);
        }

        content.getChildren().addAll(lowPass, highPass, bandPass, bandStop);
        return new TitledPane("Basic Filters", content);
    }

    private TitledPane createButterworthPane() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        RadioButton butterLow = new RadioButton("Butterworth Low Pass");
        RadioButton butterHigh = new RadioButton("Butterworth High Pass");
        RadioButton butterBand = new RadioButton("Butterworth Band Pass");

        for (RadioButton rb : new RadioButton[]{butterLow, butterHigh, butterBand}) {
            rb.setToggleGroup(toggleGroup);
            rb.setMaxWidth(Double.MAX_VALUE);
        }

        content.getChildren().addAll(butterLow, butterHigh, butterBand);
        return new TitledPane("Butterworth Filters", content);
    }

    private TitledPane createChebyshevPane() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        RadioButton chebyLow = new RadioButton("Chebyshev Low Pass");
        RadioButton chebyHigh = new RadioButton("Chebyshev High Pass");
        RadioButton chebyBand = new RadioButton("Chebyshev Band Pass");

        for (RadioButton rb : new RadioButton[]{chebyLow, chebyHigh, chebyBand}) {
            rb.setToggleGroup(toggleGroup);
            rb.setMaxWidth(Double.MAX_VALUE);
        }

        content.getChildren().addAll(chebyLow, chebyHigh, chebyBand);
        return new TitledPane("Chebyshev Filters", content);
    }

    // New method to create Bessel filter pane
    private TitledPane createBesselPane() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        RadioButton besselLow = new RadioButton("Bessel Low Pass");
        RadioButton besselHigh = new RadioButton("Bessel High Pass");
        RadioButton besselBand = new RadioButton("Bessel Band Pass");

        for (RadioButton rb : new RadioButton[]{besselLow, besselHigh, besselBand}) {
            rb.setToggleGroup(toggleGroup);
            rb.setMaxWidth(Double.MAX_VALUE);
        }

        content.getChildren().addAll(besselLow, besselHigh, besselBand);
        return new TitledPane("Bessel Filters", content);
    }

    private VBox createParameterBox() {
        VBox paramBox = new VBox(10);
        paramBox.setPadding(new Insets(10));

        Label titleLabel = new Label("Parameters");
        titleLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        cutOffUnitBox = new ComboBox<>();
        cutOffUnitBox.getItems().addAll("Hz", "kHz", "MHz");
        cutOffUnitBox.setValue("Hz");

        lowCutOffUnitBox = new ComboBox<>();
        lowCutOffUnitBox.getItems().addAll("Hz", "kHz", "MHz");
        lowCutOffUnitBox.setValue("Hz");

        highCutoffUnitBox = new ComboBox<>();
        highCutoffUnitBox.getItems().addAll("Hz", "kHz", "MHz");
        highCutoffUnitBox.setValue("Hz");

        cutoffField = new TextField("1000");
        cutoffField.setPrefWidth(85);
        lowCutoffField = new TextField("500");
        lowCutoffField.setPrefWidth(85);
        highCutoffField = new TextField("2000");
        highCutoffField.setPrefWidth(85);
        rippleField = new TextField("1.0");
        rippleField.setPrefWidth(85);


        orderSlider = new Slider(1, 25, 7);
        orderSlider.setShowTickLabels(true);
        orderSlider.setShowTickMarks(true);
        orderSlider.setMajorTickUnit(1);
        orderSlider.setMinorTickCount(1);
        orderSlider.setBlockIncrement(1);
        orderSlider.setSnapToTicks(true);
        orderSlider.setMaxWidth(Double.MAX_VALUE);

        paramBox.getChildren().addAll(
                titleLabel,
                createStyledLabel("Cut off:"),
                createHBox(cutoffField, cutOffUnitBox),
                createStyledLabel("Low Cut off:"),
                createHBox(lowCutoffField, lowCutOffUnitBox),
                createStyledLabel("High Cut off:"),
                createHBox(highCutoffField, highCutoffUnitBox),
                labeledSlider("Filter Order:", orderSlider)
        );

        return paramBox;
    }

    private HBox createHBox(Node node, Node node2) {
        HBox box = new HBox(5);
        box.getChildren().addAll(node, node2);
        return box;
    }

    private Label createStyledLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 14px;");
        return label;
    }

    private VBox createViewsBox() {
        VBox box = new VBox(10);

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
        viewTimeDomainButton.setStyle("-fx-background-color: #171716;");
        viewFrequencyDomainButton.setToggleGroup(viewGroup);
        viewFrequencyDomainButton.setStyle("-fx-background-color: #171716;");
        viewBothDomainButton.setToggleGroup(viewGroup);
        viewBothDomainButton.setStyle("-fx-background-color: #171716;");

        for (RadioButton rb : new RadioButton[]{viewTimeDomainButton, viewFrequencyDomainButton, viewBothDomainButton}) {
            rb.setMaxWidth(Double.MAX_VALUE);
        }

        viewTimeDomainButton.setOnMouseClicked(e -> analyzer.dashboardPanel.showChartMode(true, false));
        viewFrequencyDomainButton.setOnMouseClicked(e -> analyzer.dashboardPanel.showChartMode(false, true));
        viewBothDomainButton.setOnMouseClicked(e -> analyzer.dashboardPanel.showChartMode(true, true));

        box.getChildren().addAll(
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

        return box;
    }

    private String getSelectedFilterType() {
        RadioButton selected = (RadioButton) toggleGroup.getSelectedToggle();
        if (selected != null) return selected.getText();

        return null;
    }

    private void applySelectedFilter() {
        if (analyzer.originalSignal == null) {
            analyzer.showAlert("Please load an audio file first");
            return;
        }

        resetSignal();

        try {
            double cutoff = Double.parseDouble(cutoffField.getText());
            double lowCutoff = Double.parseDouble(lowCutoffField.getText());
            double highCutoff = Double.parseDouble(highCutoffField.getText());
            int order = (int) orderSlider.getValue();
            double ripple = Double.parseDouble(rippleField.getText());

            // Convert units
            cutoff *= getUnitMultiplier(cutOffUnitBox.getValue());
            lowCutoff *= getUnitMultiplier(lowCutOffUnitBox.getValue());
            highCutoff *= getUnitMultiplier(highCutoffUnitBox.getValue());

            String filterType = getSelectedFilterType();
            if (filterType != null) {
                analyzer.filterOperator.applyFilter(filterType, cutoff, lowCutoff, highCutoff, order, ripple);
                analyzer.dashboardPanel.updatePlots();
            } else {
                analyzer.showAlert("Please select a filter type");
            }
        } catch (NumberFormatException e) {
            analyzer.showAlert("Invalid parameter values");
        }
    }

    private double getUnitMultiplier(String unit) {
        return switch (unit) {
            case "Hz" -> 1.0;
            case "kHz" -> 1_000.0;
            case "MHz" -> 1_000_000.0;
            default -> 1.0;
        };
    }


    private void resetSignal() {
        if (analyzer.originalSignal != null) {
            analyzer.processedSignal = analyzer.originalSignal.clone();
            analyzer.dashboardPanel.updatePlots();
        }
    }

    private void setupFilterToggleListeners() {
        ToggleGroup[] allGroups = {toggleGroup};

        for (ToggleGroup group : allGroups) {
            group.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    updateParameterFieldState(((RadioButton) newVal).getText());
                }
            });
        }
    }

    private void updateParameterFieldState(String filterType) {
        // Reset all to enabled first
        cutoffField.setDisable(false);
        lowCutoffField.setDisable(false);
        highCutoffField.setDisable(false);
        rippleField.setDisable(false);

        // Disable based on what is not needed
        if (filterType.contains("Band Pass") || filterType.contains("Band Stop")) {
            cutoffField.setDisable(true);
        } else {
            lowCutoffField.setDisable(true);
            highCutoffField.setDisable(true);
        }

        // Ripple only relevant for Chebyshev
        rippleField.setDisable(!filterType.contains("Chebyshev"));
    }


    private Separator makeSeparator() {
        Separator separator = new Separator();
        separator.setPadding(new Insets(6, 0, 6, 0));
        return separator;
    }

    private VBox labeledSlider(String label, Slider slider) {
        VBox box = new VBox(5, new Label(label), slider);
        slider.setMaxWidth(Double.MAX_VALUE);
        return box;
    }
}