package spectrumanalyzer.core;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Control panel for managing filters and display options
 * Provides interface for real-time signal processing control
 */
public class ControlPanel {

    private VBox mainView;
    private Dashboard dashboard;

    // Filter controls
    private ToggleGroup filterGroup;
    private RadioButton noFilterBtn, lowPassBtn, highPassBtn, bandPassBtn, butterworthBtn;
    private Slider cutoffSlider, lowCutoffSlider, highCutoffSlider;
    private Label cutoffLabel, lowCutoffLabel, highCutoffLabel;

    // View controls
    private ToggleGroup viewGroup;
    private RadioButton timeViewBtn, freqViewBtn, bothViewBtn;

    // Apply button
    private Button applyBtn;

    public ControlPanel(Dashboard dashboard) {
        this.dashboard = dashboard;
        initializeControls();
        setupLayout();
        setupEventHandlers();
    }

    /**
     * Initializes all control components
     * Creates buttons, sliders, and labels for the control panel
     */
    private void initializeControls() {
        // Filter radio buttons
        filterGroup = new ToggleGroup();
        noFilterBtn = new RadioButton("No Filter");
        lowPassBtn = new RadioButton("Low Pass");
        highPassBtn = new RadioButton("High Pass");
        bandPassBtn = new RadioButton("Band Pass");
        butterworthBtn = new RadioButton("Butterworth");

        noFilterBtn.setToggleGroup(filterGroup);
        lowPassBtn.setToggleGroup(filterGroup);
        highPassBtn.setToggleGroup(filterGroup);
        bandPassBtn.setToggleGroup(filterGroup);
        butterworthBtn.setToggleGroup(filterGroup);

        noFilterBtn.setSelected(true);

        // Frequency sliders
        cutoffSlider = new Slider(10, 400, 100);
        lowCutoffSlider = new Slider(10, 200, 50);
        highCutoffSlider = new Slider(100, 400, 150);

        cutoffSlider.setShowTickLabels(true);
        cutoffSlider.setShowTickMarks(true);
        cutoffSlider.setMajorTickUnit(50);

        lowCutoffSlider.setShowTickLabels(true);
        lowCutoffSlider.setShowTickMarks(true);
        lowCutoffSlider.setMajorTickUnit(50);

        highCutoffSlider.setShowTickLabels(true);
        highCutoffSlider.setShowTickMarks(true);
        highCutoffSlider.setMajorTickUnit(50);

        // Labels
        cutoffLabel = new Label("Cutoff: 100 Hz");
        lowCutoffLabel = new Label("Low: 50 Hz");
        highCutoffLabel = new Label("High: 150 Hz");

        // View controls
        viewGroup = new ToggleGroup();
        timeViewBtn = new RadioButton("Time Domain");
        freqViewBtn = new RadioButton("Frequency Domain");
        bothViewBtn = new RadioButton("Both Views");

        timeViewBtn.setToggleGroup(viewGroup);
        freqViewBtn.setToggleGroup(viewGroup);
        bothViewBtn.setToggleGroup(viewGroup);

        bothViewBtn.setSelected(true);

        // Apply button
        applyBtn = new Button("Apply Settings");
        applyBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        applyBtn.setPrefWidth(150);
    }

    /**
     * Sets up the layout structure for the control panel
     * Arranges components in logical groups with proper spacing
     */
    private void setupLayout() {
        mainView = new VBox(15);
        mainView.setPadding(new Insets(20));
        mainView.setAlignment(Pos.TOP_LEFT);
        mainView.setMaxWidth(280);


        // Title
        Text title = new Text("Control Panel");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        // Filter section
        VBox filterSection = createFilterSection();

        // View section
        VBox viewSection = createViewSection();

        // Apply button
        VBox buttonSection = new VBox(10);
        buttonSection.setAlignment(Pos.CENTER);
        buttonSection.getChildren().add(applyBtn);

        mainView.getChildren().addAll(title, filterSection, viewSection, buttonSection);
    }

    /**
     * Creates the filter control section
     * @return VBox containing filter radio buttons and parameter controls
     */
    private VBox createFilterSection() {
        VBox filterSection = new VBox(10);

        Text filterTitle = new Text("Signal Filters");
        filterTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        VBox filterButtons = new VBox(5);
        filterButtons.getChildren().addAll(noFilterBtn, lowPassBtn, highPassBtn, bandPassBtn, butterworthBtn);

        Separator separator1 = new Separator();

        // Cutoff frequency control
        VBox cutoffControl = new VBox(5);
        cutoffControl.getChildren().addAll(cutoffLabel, cutoffSlider);

        // Band pass controls
        VBox bandPassControl = new VBox(5);
        bandPassControl.getChildren().addAll(lowCutoffLabel, lowCutoffSlider, highCutoffLabel, highCutoffSlider);

        filterSection.getChildren().addAll(filterTitle, filterButtons, separator1, cutoffControl, bandPassControl);

        return filterSection;
    }

    /**
     * Creates the view control section
     * @return VBox containing view mode radio buttons
     */
    private VBox createViewSection() {
        VBox viewSection = new VBox(10);

        Separator separator2 = new Separator();

        Text viewTitle = new Text("Display Options");
        viewTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        VBox viewButtons = new VBox(5);
        viewButtons.getChildren().addAll(timeViewBtn, freqViewBtn, bothViewBtn);

        viewSection.getChildren().addAll(separator2, viewTitle, viewButtons);

        return viewSection;
    }

    /**
     * Sets up event handlers for all interactive components
     * Connects UI controls to dashboard functionality
     */
    private void setupEventHandlers() {
        // Filter selection handlers
        noFilterBtn.setOnAction(e -> updateFilterState());
        lowPassBtn.setOnAction(e -> updateFilterState());
        highPassBtn.setOnAction(e -> updateFilterState());
        bandPassBtn.setOnAction(e -> updateFilterState());
        butterworthBtn.setOnAction(e -> updateFilterState());

        // Slider handlers
        cutoffSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            cutoffLabel.setText(String.format("Cutoff: %.0f Hz", newVal.doubleValue()));
        });

        lowCutoffSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            lowCutoffLabel.setText(String.format("Low: %.0f Hz", newVal.doubleValue()));
            // Ensure low cutoff is always less than high cutoff
            if (newVal.doubleValue() >= highCutoffSlider.getValue()) {
                highCutoffSlider.setValue(newVal.doubleValue() + 10);
            }
        });

        highCutoffSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            highCutoffLabel.setText(String.format("High: %.0f Hz", newVal.doubleValue()));
            // Ensure high cutoff is always greater than low cutoff
            if (newVal.doubleValue() <= lowCutoffSlider.getValue()) {
                lowCutoffSlider.setValue(newVal.doubleValue() - 10);
            }
        });

        // View mode handlers
        timeViewBtn.setOnAction(e -> updateViewMode());
        freqViewBtn.setOnAction(e -> updateViewMode());
        bothViewBtn.setOnAction(e -> updateViewMode());

        // Apply button handler
        applyBtn.setOnAction(e -> applySettings());

        // Initialize filter state
        updateFilterState();
    }

    /**
     * Updates filter state based on current selection
     * Enables/disables relevant parameter controls
     */
    private void updateFilterState() {
        boolean isBandPass = bandPassBtn.isSelected();
        boolean hasFilterWithCutoff = lowPassBtn.isSelected() || highPassBtn.isSelected() || butterworthBtn.isSelected();

        // Enable/disable controls based on filter type
        cutoffSlider.setDisable(!hasFilterWithCutoff);
        cutoffLabel.setDisable(!hasFilterWithCutoff);

        lowCutoffSlider.setDisable(!isBandPass);
        lowCutoffLabel.setDisable(!isBandPass);
        highCutoffSlider.setDisable(!isBandPass);
        highCutoffLabel.setDisable(!isBandPass);

        // Auto-apply filter changes
        applySettings();
    }

    /**
     * Updates view mode in the dashboard
     * Changes the display layout based on user selection
     */
    private void updateViewMode() {
        Dashboard.ViewMode mode = Dashboard.ViewMode.BOTH;

        if (timeViewBtn.isSelected()) {
            mode = Dashboard.ViewMode.TIME_DOMAIN;
        } else if (freqViewBtn.isSelected()) {
            mode = Dashboard.ViewMode.FREQUENCY_DOMAIN;
        }

        dashboard.setViewMode(mode);
    }

    /**
     * Applies current settings to the dashboard
     * Updates filter parameters and triggers signal processing
     */
    private void applySettings() {
        // Apply filter settings
        Dashboard.FilterType filterType = Dashboard.FilterType.NONE;

        if (lowPassBtn.isSelected()) {
            filterType = Dashboard.FilterType.LOW_PASS;
        } else if (highPassBtn.isSelected()) {
            filterType = Dashboard.FilterType.HIGH_PASS;
        } else if (bandPassBtn.isSelected()) {
            filterType = Dashboard.FilterType.BAND_PASS;
        } else if (butterworthBtn.isSelected()) {
            filterType = Dashboard.FilterType.BUTTERWORTH;
        }

        dashboard.setFilter(filterType);
        dashboard.setCutoffFrequency(cutoffSlider.getValue());
        dashboard.setBandPassRange(lowCutoffSlider.getValue(), highCutoffSlider.getValue());

        // Update view mode
        updateViewMode();

        // Visual feedback
        applyBtn.setStyle("-fx-background-color: #45a049; -fx-text-fill: white; -fx-font-weight: bold;");

        // Reset button color after short delay
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(200), e -> {
                    applyBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
                })
        );
        timeline.play();
    }
    /**
     * Gets the main view component for the control panel
     * @return VBox containing all control panel components
     */
    public VBox getView() {
        return mainView;
    }
}