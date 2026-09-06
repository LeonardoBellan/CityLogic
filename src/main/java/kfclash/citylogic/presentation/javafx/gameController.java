package kfclash.citylogic.presentation.javafx;

import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import java.math.RoundingMode;

import kfclash.citylogic.domain.core.CitySnapshot;
import kfclash.citylogic.domain.map.Dimension;
import kfclash.citylogic.ports.IBuildingState;
import kfclash.citylogic.ports.ICityObserver;

public class gameController implements ICityObserver {

    @FXML private Pane       mapContainer;
    @FXML private StackPane  pauseOverlay;
    @FXML private Label      lblMoney;
    @FXML private Label      lblPopulation;
    @FXML private Label      lblHappiness;
    @FXML private Label      lblDate;

    private TileMapCanvas tileMap;
    private boolean       isPaused   = false;
    private double        dragStartX, dragStartY;
    private double        translateX = 0, translateY = 0;
    private double        scale      = 1.0;
    private String         selectedBuildingType = "house";

    private static final double ZOOM_FACTOR = 1.15;
    private static final double ZOOM_MIN    = 0.2;
    private static final double ZOOM_MAX    = 5.0;

    @FXML
    public void initialize() {

        tileMap = new TileMapCanvas(48);
        mapContainer.getChildren().add(tileMap);
        refreshMap();

        App.askEventPublisher().subscribe(this);
        onMetricsChanged(App.askSimulationEngine().getCurrentSnapshot());

        setupPan();
        setupZoom();
        setupPlacement();
        setupHover();
        applyTransform();

        // ESC key — must be attached after scene is available
        mapContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(e -> {
                    if (e.getCode() == KeyCode.ESCAPE) togglePause();
                });
            }
        });
    }

    @Override
    public void onMetricsChanged(CitySnapshot snapshot) {
        Runnable update = () -> {
            lblMoney.setText("$ " + snapshot.budget().setScale(0, RoundingMode.HALF_UP));
            lblPopulation.setText(Integer.toString(snapshot.population()));
            lblHappiness.setText(String.format("%.0f%%", snapshot.happiness()));
            lblDate.setText("Jan " + (2025 + snapshot.tickCount()));
        };
        if (Platform.isFxApplicationThread()) {
            update.run();
        } else {
            Platform.runLater(update);
        }
    }

    private void refreshMap() {
        Dimension dimensions = App.askGrid().getDimensions();
        int[][] map = new int[dimensions.getHeight()][dimensions.getWidth()];
        for (IBuildingState building : App.askGrid().getAllBuildings()) {
            int tile = tileFor(building.getType());
            int startX = building.getPosition().getX();
            int startY = building.getPosition().getY();
            Dimension footprint = building.getDescription().getFootprint();
            for (int y = startY; y < startY + footprint.getHeight(); y++) {
                for (int x = startX; x < startX + footprint.getWidth(); x++) {
                    if (y >= 0 && y < map.length && x >= 0 && x < map[y].length) {
                        map[y][x] = tile;
                    }
                }
            }
        }
        tileMap.setMap(map);
    }

    private static int tileFor(String buildingType) {
        return switch (buildingType.toLowerCase()) {
            case "park" -> 2;
            case "water" -> 3;
            case "highway" -> 5;
            case "roof" -> 6;
            default -> 1;
        };
    }

    // ── Pause logic ──────────────────────────────────────────────────
    private void togglePause() {
        isPaused = !isPaused;
        pauseOverlay.setVisible(isPaused);

        // Block map interaction while paused
        mapContainer.setMouseTransparent(isPaused);
    }

    @FXML private void onResume() {
        isPaused = false;
        pauseOverlay.setVisible(false);
        mapContainer.setMouseTransparent(false);
    }

    @FXML private void onResetFromPause() {
        scale = 1.0; translateX = 0; translateY = 0;
        applyTransform();
        onResume();
    }

    @FXML private void onMainMenu() {
        try {
            App.setRoot("menu");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Pan ──────────────────────────────────────────────────────────
    private void setupPan() {
        mapContainer.setOnMousePressed(e -> {
            dragStartX = e.getSceneX() - translateX;
            dragStartY = e.getSceneY() - translateY;
            mapContainer.setStyle("-fx-cursor: grabbing; -fx-background-color: #1a1a2e;");
        });
        mapContainer.setOnMouseDragged(e -> {
            translateX = e.getSceneX() - dragStartX;
            translateY = e.getSceneY() - dragStartY;
            applyTransform();
        });
        mapContainer.setOnMouseReleased(e ->
            mapContainer.setStyle("-fx-cursor: default; -fx-background-color: #1a1a2e;"));
    }

    // ── Zoom ─────────────────────────────────────────────────────────
    private void setupZoom() {
        mapContainer.setOnScroll(e -> {
            if (isPaused) return;   // ignore scroll when paused
            double oldScale = scale;
            if (e.getDeltaY() > 0) scale = Math.min(scale * ZOOM_FACTOR, ZOOM_MAX);
            else                    scale = Math.max(scale / ZOOM_FACTOR, ZOOM_MIN);
            double factor = scale / oldScale;
            translateX = e.getX() - factor * (e.getX() - translateX);
            translateY = e.getY() - factor * (e.getY() - translateY);
            applyTransform();
        });
    }

    private void setupPlacement() {
        mapContainer.setOnMouseClicked(event -> {
            if (isPaused || event.getButton() != MouseButton.PRIMARY) {
                return;
            }
            double canvasX = (event.getX() - translateX) / scale;
            double canvasY = (event.getY() - translateY) / scale;
            int[] tile = tileMap.getTileAt(canvasX, canvasY);
            if (tile != null && App.askGameEngine().placeBuilding(
                    tile[0], tile[1], selectedBuildingType)) {
                refreshMap();
            }
        });
    }

    @FXML
    private void selectHouse() {
        selectedBuildingType = "house";
    }

    @FXML
    private void selectFactory() {
        selectedBuildingType = "factory";
    }

    @FXML
    private void selectPark() {
        selectedBuildingType = "park";
    }

    @FXML
    private void advanceTime() {
        if (!isPaused) {
            App.askGameEngine().advanceTime();
            refreshMap();
        }
    }

    // ── Hover ─────────────────────────────────────────────────────────
    private void setupHover() {
        mapContainer.setOnMouseMoved(e -> {
            double canvasX = (e.getX() - translateX) / scale;
            double canvasY = (e.getY() - translateY) / scale;
        });
    }

    // ── Transform ────────────────────────────────────────────────────
    private void applyTransform() {
        tileMap.getTransforms().clear();
        tileMap.getTransforms().addAll(
            new Translate(translateX, translateY),
            new Scale(scale, scale, 0, 0)
        );
    }

    // ── Toolbar buttons ──────────────────────────────────────────────
    @FXML private void onResetView()  { scale=1.0; translateX=0; translateY=0; applyTransform(); }
    @FXML private void onZoomIn()     { scale=Math.min(scale*ZOOM_FACTOR,ZOOM_MAX); applyTransform(); }
    @FXML private void onZoomOut()    { scale=Math.max(scale/ZOOM_FACTOR,ZOOM_MIN); applyTransform(); }
}