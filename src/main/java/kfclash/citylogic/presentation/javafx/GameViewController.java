package kfclash.citylogic.presentation.javafx;

import java.util.HashMap;
import java.util.Map;

import kfclash.citylogic.domain.core.CitySnapshot;
import kfclash.citylogic.domain.core.ResourceDelta;
import kfclash.citylogic.domain.map.Point;
import kfclash.citylogic.domain.policies.EnvironmentalTaxPolicy;
import kfclash.citylogic.domain.policies.GreenSubsidiesPolicy;
import kfclash.citylogic.domain.policies.IndustrialExpansionPolicy;
import kfclash.citylogic.ports.ICityObserver;
import kfclash.citylogic.ports.IPolicyStrategy;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

/** Rich dashboard controller backed by the citylogic application facade. */
public class GameViewController implements ICityObserver {
    @FXML private Label budgetLabel;
    @FXML private Label budgetDeltaLabel;
    @FXML private Label populationLabel;
    @FXML private Label populationDeltaLabel;
    @FXML private Label happinessLabel;
    @FXML private ProgressBar happinessBar;
    @FXML private Label pollutionLabel;
    @FXML private ProgressBar pollutionBar;
    @FXML private Label tickLabel;
    @FXML private Label statusMessageLabel;
    @FXML private ToggleGroup toolToggleGroup;
    @FXML private ToggleButton selectToolBtn;
    @FXML private ToggleButton demolishToolBtn;
    @FXML private ToggleButton houseBtn;
    @FXML private ToggleButton commercialBtn;
    @FXML private ToggleButton factoryBtn;
    @FXML private ToggleButton parkBtn;
    @FXML private ToggleButton powerPlantBtn;
    @FXML private ToggleButton roadBtn;
    @FXML private ToggleButton playPauseBtn;
    @FXML private ToggleButton stepTickBtn;
    @FXML private Slider speedSlider;
    @FXML private Label speedLabel;
    @FXML private CityMapCanvas mapCanvas;
    @FXML private Label inspectorTitle;
    @FXML private Label inspectorCoords;
    @FXML private Label inspectorFootprint;
    @FXML private Label inspectorPower;
    @FXML private Label inspectorProduction;
    @FXML private CheckBox envTaxCheck;
    @FXML private CheckBox greenSubsidyCheck;
    @FXML private CheckBox industrialCheck;
    @FXML private ListView<String> logListView;

    private final Map<String, IPolicyStrategy> policies = new HashMap<>();
    private String activeTool = "select";
    private Point selectedPoint;
    private boolean playing;
    private long lastTick;
    private AnimationTimer ticker;

    @FXML
    private void initialize() {
        logListView.getItems().clear();
        mapCanvas.init(App.askGrid());
        App.askEventPublisher().subscribe(this);
        setupTools();
        setupCanvas();
        setupPolicies();
        setupTicker();
        updateMetrics(App.askSimulationEngine().getCurrentSnapshot(), ResourceDelta.zero());
        updateInspector();
        addLog("CityLogic dashboard ready.");
    }

    private void setupCanvas() {
        mapCanvas.setOnMouseMoved(event -> mapCanvas.setHoverPoint(
                mapCanvas.getGridCoordinatesFromPixel(event.getX(), event.getY())));
        mapCanvas.setOnMouseExited(event -> mapCanvas.setHoverPoint(null));
        mapCanvas.setOnMouseClicked(event -> {
            Point point = mapCanvas.getGridCoordinatesFromPixel(event.getX(), event.getY());
            if (point != null) handleGridClick(point);
        });
    }

    private void handleGridClick(Point point) {
        if ("select".equals(activeTool)) {
            selectedPoint = point;
            mapCanvas.setSelectedPoint(point);
            updateInspector();
            return;
        }
        if ("demolish".equals(activeTool)) {
            if (App.askGameEngine().demolishBuilding(point.getX(), point.getY())) {
                addLog("Demolished building at (" + point.getX() + ", " + point.getY() + ")");
                mapCanvas.redraw();
                updateInspector();
                setStatus("Building demolished", false);
            } else {
                setStatus("No building at this location", true);
            }
            return;
        }
        if (App.askGameEngine().placeBuilding(point.getX(), point.getY(), activeTool)) {
            selectedPoint = point;
            mapCanvas.setSelectedPoint(point);
            mapCanvas.redraw();
            updateInspector();
            addLog("Constructed " + activeTool + " at (" + point.getX() + ", " + point.getY() + ")");
            setStatus("Construction successful", false);
        } else {
            setStatus("Cannot build here or insufficient funds", true);
        }
    }

    private void setupTools() {
        if (toolToggleGroup == null) toolToggleGroup = new ToggleGroup();
        selectToolBtn.setToggleGroup(toolToggleGroup);
        demolishToolBtn.setToggleGroup(toolToggleGroup);
        houseBtn.setToggleGroup(toolToggleGroup);
        commercialBtn.setToggleGroup(toolToggleGroup);
        factoryBtn.setToggleGroup(toolToggleGroup);
        parkBtn.setToggleGroup(toolToggleGroup);
        powerPlantBtn.setToggleGroup(toolToggleGroup);
        roadBtn.setToggleGroup(toolToggleGroup);
        selectToolBtn.setSelected(true);
        toolToggleGroup.selectedToggleProperty().addListener((observable, oldValue, selected) -> {
            if (selected == selectToolBtn) activeTool = "select";
            else if (selected == demolishToolBtn) activeTool = "demolish";
            else if (selected == houseBtn) activeTool = "house";
            else if (selected == commercialBtn) activeTool = "commercial";
            else if (selected == factoryBtn) activeTool = "factory";
            else if (selected == parkBtn) activeTool = "park";
            else if (selected == powerPlantBtn) activeTool = "power_plant";
            else if (selected == roadBtn) activeTool = "road";
            mapCanvas.setActiveTool(activeTool);
            setStatus("Tool: " + activeTool, false);
        });
    }

    private void setupPolicies() {
        policies.put("tax", new EnvironmentalTaxPolicy());
        policies.put("green", new GreenSubsidiesPolicy());
        policies.put("industrial", new IndustrialExpansionPolicy());
        bindPolicy(envTaxCheck, policies.get("tax"));
        bindPolicy(greenSubsidyCheck, policies.get("green"));
        bindPolicy(industrialCheck, policies.get("industrial"));
    }

    private void bindPolicy(CheckBox checkbox, IPolicyStrategy policy) {
        checkbox.selectedProperty().addListener((observable, oldValue, selected) -> {
            if (selected) {
                App.askGameEngine().setCityPolicy(policy);
                addLog("Policy activated: " + policy.getName());
            } else {
                App.askGameEngine().clearCityPolicy(policy.getName());
                addLog("Policy deactivated: " + policy.getName());
            }
        });
    }

    private void setupTicker() {
        speedSlider.valueProperty().addListener((observable, oldValue, value) ->
                speedLabel.setText(String.format("%.1fx", value.doubleValue())));
        stepTickBtn.setOnAction(event -> executeTick());
        playPauseBtn.setOnAction(event -> {
            playing = !playing;
            playPauseBtn.setText(playing ? "⏸ Pause" : "▶ Play");
        });
        ticker = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!playing) return;
                long interval = (long) (1_000_000_000L / Math.max(0.5, speedSlider.getValue()));
                if (now - lastTick >= interval) {
                    lastTick = now;
                    executeTick();
                }
            }
        };
        ticker.start();
    }

    private void executeTick() {
        CitySnapshot before = App.askSimulationEngine().getCurrentSnapshot();
        try {
            App.askGameEngine().advanceTime();
            CitySnapshot after = App.askSimulationEngine().getCurrentSnapshot();
            updateMetrics(after, deltaBetween(before, after));
            mapCanvas.redraw();
            addLog("Advanced to tick " + after.tickCount());
        } catch (RuntimeException error) {
            playing = false;
            setStatus(error.getMessage() == null ? "Simulation failed" : error.getMessage(), true);
        }
    }

    @Override
    public void onMetricsChanged(CitySnapshot snapshot) {
        updateMetrics(snapshot, ResourceDelta.zero());
    }

    private void updateMetrics(CitySnapshot snapshot, ResourceDelta delta) {
        Runnable update = () -> {
            budgetLabel.setText("$" + snapshot.budget().setScale(0));
            budgetDeltaLabel.setText(String.format("%+.0f/tick", delta.budgetDelta().doubleValue()));
            populationLabel.setText(Integer.toString(snapshot.population()));
            populationDeltaLabel.setText(String.format("%+d", delta.populationDelta()));
            happinessLabel.setText(String.format("%.1f%%", snapshot.happiness()));
            happinessBar.setProgress(snapshot.happiness() / 100.0);
            pollutionLabel.setText(String.format("%.1f ppm", snapshot.pollution()));
            pollutionBar.setProgress(Math.min(1.0, snapshot.pollution() / 150.0));
            tickLabel.setText("Tick #" + snapshot.tickCount());
        };
        if (Platform.isFxApplicationThread()) update.run(); else Platform.runLater(update);
    }

    private void updateInspector() {
        if (selectedPoint == null) {
            inspectorTitle.setText("No Tile Selected");
            inspectorCoords.setText("-");
            inspectorFootprint.setText("-");
            inspectorPower.setText("-");
            inspectorProduction.setText("-");
            return;
        }
        var building = App.askGrid().getCell(selectedPoint.getX(), selectedPoint.getY()).getBuilding();
        inspectorCoords.setText("(" + selectedPoint.getX() + ", " + selectedPoint.getY() + ")");
        if (building == null) {
            inspectorTitle.setText("Empty Tile");
            inspectorFootprint.setText("-");
            inspectorPower.setText("-");
            inspectorProduction.setText("-");
            return;
        }
        inspectorTitle.setText(building.getDescription().getName());
        inspectorFootprint.setText(building.getDescription().getFootprint().getWidth() + " x "
                + building.getDescription().getFootprint().getHeight());
        inspectorPower.setText(building.isPowered() ? "ONLINE" : "SHUTDOWN");
        inspectorProduction.setText(building.getBaseProduction().toString());
    }

    private static ResourceDelta deltaBetween(CitySnapshot before, CitySnapshot after) {
        return new ResourceDelta(after.budget().subtract(before.budget()),
                after.pollution() - before.pollution(),
                after.population() - before.population(),
                after.happiness() - before.happiness());
    }

    private void addLog(String message) {
        logListView.getItems().add(message);
        if (logListView.getItems().size() > 100) logListView.getItems().remove(0);
        logListView.scrollTo(logListView.getItems().size() - 1);
    }

    private void setStatus(String message, boolean error) {
        statusMessageLabel.setText(message);
        statusMessageLabel.setStyle(error ? "-fx-text-fill: #f87171;" : "-fx-text-fill: #94a3b8;");
    }

}
