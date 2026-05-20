package test.jfx;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

public class gameController {

    @FXML private Pane       mapContainer;
    @FXML private StackPane  pauseOverlay;
    @FXML private Label      lblMoney;
    @FXML private Label      lblPopulation;
    @FXML private Label      lblHappiness;
    @FXML private Label      lblDate;


    //TODO:trovare il modo di non dover dichiarare questo qui e testare il change map tipo load controller per simulare il caricamento di una mappa da file
    CityEngine cityEngine = new CityEngine();

    private TileMapCanvas tileMap;
    private boolean       isPaused   = false;
    private double        dragStartX, dragStartY;
    private double        translateX = 0, translateY = 0;
    private double        scale      = 1.0;

    private static final double ZOOM_FACTOR = 1.15;
    private static final double ZOOM_MIN    = 0.2;
    private static final double ZOOM_MAX    = 5.0;

    @FXML
    public void initialize() {

        tileMap = new TileMapCanvas(48);
        mapContainer.getChildren().add(tileMap);

        
        cityEngine.pcs.addPropertyChangeListener(e -> {
            if ("map".equals(e.getPropertyName())) {
                tileMap.setMap((int[][]) e.getNewValue());
            }
        });
        cityEngine.changeMap(new int[][] {
            {5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5},
            {5,4,4,4,4,4,4,4,4,0,4,4,4,4,4,4,4,4,5},
            {5,4,1,1,4,1,1,4,4,0,4,4,1,1,4,1,1,4,5},
            {5,4,1,6,4,6,1,4,4,0,4,4,1,6,4,6,1,4,5},
            {5,4,1,1,4,1,1,4,4,0,4,4,1,1,4,1,1,4,5},
            {5,4,4,4,4,4,4,4,4,0,4,4,4,4,4,4,4,4,5},
            {5,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,5},
            {5,4,4,4,4,4,4,4,4,0,4,4,4,4,4,4,4,4,5},
            {5,4,2,2,2,4,1,4,4,0,4,4,1,4,2,2,2,4,5},
            {5,4,2,2,2,4,6,4,4,0,4,4,6,4,2,2,2,4,5},
            {5,4,2,2,2,4,1,4,4,0,4,4,1,4,2,2,2,4,5},
            {5,4,4,4,4,4,4,4,4,0,4,4,4,4,4,4,4,4,5},
            {5,0,0,0,0,0,0,0,3,3,3,0,0,0,0,0,0,0,5},
            {5,4,4,4,4,4,4,4,3,3,3,4,4,4,4,4,4,4,5},
            {5,4,1,6,4,1,1,4,3,3,3,4,1,1,4,6,1,4,5},
            {5,4,1,1,4,1,6,4,4,4,4,4,6,1,4,1,1,4,5},
            {5,4,4,4,4,4,4,4,4,0,4,4,4,4,4,4,4,4,5},
            {5,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,5},
            {5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5},
        });

        setupPan();
        setupZoom();
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