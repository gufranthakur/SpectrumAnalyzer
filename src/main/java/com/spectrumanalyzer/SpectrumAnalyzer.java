package com.spectrumanalyzer;

import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.CupertinoLight;
import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import com.github.psambit9791.jdsp.io.WAV;
import java.util.Hashtable;

public class SpectrumAnalyzer extends Application {
    public double[][] originalSignal;
    public double[][] processedSignal;
    public Hashtable<String, Long> audioProperties;
    public int sampleRate;
    public int channels;

    public HomePanel homePanel;
    public ControlPanel controlPanel;
    public DashboardPanel dashboardPanel;
    public StatisticPanel statisticPanel;
    public FilterOperator filterOperator;

    public BorderPane rootPane;
    public SplitPane splitPane;

    @Override
    public void start(Stage primaryStage) {
        initializeComponents();
        setupUI();
        Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());

        Scene scene = new Scene(rootPane, 1200, 600);
        primaryStage.setTitle("Spectrum Analyzer");
        primaryStage.setScene(scene);
        primaryStage.show();
        //Platform.runLater(() -> primaryStage.setFullScreen(true));
    }

    private void initializeComponents() {
        filterOperator = new FilterOperator(this);
        homePanel = new HomePanel(this);
        dashboardPanel = new DashboardPanel(this);
        controlPanel = new ControlPanel(this);
        statisticPanel = new StatisticPanel(this);
    }

    private void setupUI() {
        rootPane = new BorderPane();
        splitPane = new SplitPane();

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        ScrollPane controlScrollPane = new ScrollPane(controlPanel);
        controlScrollPane.setFitToWidth(true); // This is the key line
        controlScrollPane.setFitToHeight(false); // Optional — keep false if you want natural height growth
        controlScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // No horizontal scroll
        controlScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        ScrollPane statisticScrollPane = new ScrollPane(statisticPanel);
        statisticScrollPane.setFitToWidth(true); // This is the key line
        statisticScrollPane.setFitToHeight(false); // Optional — keep false if you want natural height growth
        statisticScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // No horizontal scroll
        statisticScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        Tab homeTab = new Tab("Home", homePanel);
        Tab controlTab = new Tab("Controls", controlScrollPane);
        Tab statsTab = new Tab("Statistics", statisticScrollPane);

        tabPane.getTabs().addAll(homeTab, controlTab, statsTab);
        tabPane.setPrefWidth(300); // optional width

        splitPane.setDividerPosition(0, 0.15);
        splitPane.getItems().addAll(tabPane, dashboardPanel);

        rootPane.setCenter(splitPane);
    }


    public void loadAudioFile(String filename) {
        try {
            WAV objRead = new WAV();
            objRead.readWAV(filename);
            audioProperties = objRead.getProperties();
            double[][] rawData = objRead.getData("int");

            if (rawData == null || rawData.length == 0) {
                throw new RuntimeException("No audio data found in file");
            }

            sampleRate = getSampleRate();
            channels = getChannels();

            // rawData is [samples][channels], transpose to [channels][samples]
            originalSignal = new double[channels][rawData.length];
            processedSignal = new double[channels][rawData.length];

            for (int sample = 0; sample < rawData.length; sample++) {
                for (int ch = 0; ch < channels; ch++) {
                    originalSignal[ch][sample] = rawData[sample][ch];
                    processedSignal[ch][sample] = rawData[sample][ch];
                }
            }

            // Debug first few values
            System.out.println("First few values from channel 0: " + originalSignal[0][0] + ", " + originalSignal[0][1] + ", " + originalSignal[0][2]);
            if (channels > 1) {
                System.out.println("First few values from channel 1: " + originalSignal[1][0] + ", " + originalSignal[1][1] + ", " + originalSignal[1][2]);
            }

            System.out.println("Loaded: " + channels + " channels, " + rawData.length + " samples");
            dashboardPanel.updatePlots();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading file: " + e.getMessage());
        }
    }

    private int getSampleRate() {
        Long sampleRate = audioProperties.get("SampleRate");
        if (sampleRate != null) {
            return sampleRate.intValue();
        }
        return 44100; // default
    }

    private int getChannels() {
        Long channels = audioProperties.get("Channels");
        if (channels != null) {
            return channels.intValue();
        }
        return 1; // default
    }

    public void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}