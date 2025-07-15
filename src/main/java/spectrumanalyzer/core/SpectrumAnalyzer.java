package spectrumanalyzer.core;


import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.CupertinoLight;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Main application class for the Spectrum Analyzer
 * Initializes the UI components and sets up the main window
 */
public class SpectrumAnalyzer extends Application {

    private Dashboard dashboard;
    private ControlPanel controlPanel;

    @Override
    public void start(Stage primaryStage) {
        Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());

        // Initialize components
        dashboard = new Dashboard();
        controlPanel = new ControlPanel(dashboard);

        // Create main layout
        BorderPane root = createMainLayout();

        // Setup scene and stage
        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setTitle("Spectrum Analyzer");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Start signal generation
        dashboard.startSignalGeneration();
    }

    /**
     * Creates the main layout structure
     * @return BorderPane containing all UI components
     */
    private BorderPane createMainLayout() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        root.setCenter(dashboard.getView());
        root.setLeft(controlPanel.getView());

        return root;
    }

    @Override
    public void stop() {
        if (dashboard != null) {
            dashboard.stopSignalGeneration();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}