package kfclash.citylogic.presentation.javafx;

import java.math.BigDecimal;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;

import kfclash.citylogic.application.ApplicationBuildingDescriptionProvider;
import kfclash.citylogic.application.BuildingCatalog;
import kfclash.citylogic.application.CityPersistenceService;
import kfclash.citylogic.application.GameEngine;
import kfclash.citylogic.application.PlacementValidator;
import kfclash.citylogic.domain.buildings.BuildingFactory;
import kfclash.citylogic.domain.core.CityAggregate;
import kfclash.citylogic.domain.map.Dimension;
import kfclash.citylogic.domain.map.Grid;
import kfclash.citylogic.simulation.engine.SimulationEngine;
import kfclash.citylogic.simulation.tick.SimulationConfig;
import kfclash.citylogic.simulation.tick.TickPhaseFactory;
import kfclash.citylogic.persistence.JsonCityRepository;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    private static Grid grid;
    private static BuildingCatalog catalog;
    private static GameEngine gameEngine;
    private static SimulationEngine simulationEngine;
    private static CityEventPublisher eventPublisher;
    private static CityPersistenceService persistenceService;

    static Grid askGrid() {
        return grid;
    }

    static GameEngine askGameEngine() {
        return gameEngine;
    }

    static SimulationEngine askSimulationEngine() {
        return simulationEngine;
    }

    static CityEventPublisher askEventPublisher() {
        return eventPublisher;
    }

    static void saveGame(int slot) throws IOException {
        persistenceService.save(savePath(slot));
    }

    static void loadGame(int slot) throws IOException {
        persistenceService.load(savePath(slot));
    }

    private static void initializeApplication() {
        grid = new Grid(new Dimension(12, 12), new BuildingFactory());

        catalog = new BuildingCatalog();
        ApplicationBuildingDescriptionProvider.initDefaultCatalog(catalog);

        CityAggregate cityState = new CityAggregate(
                new BigDecimal("10000.00"), 4200, 74.0);
        eventPublisher = new CityEventPublisher();
        simulationEngine = new SimulationEngine(
                cityState,
                grid,
                eventPublisher,
                new TickPhaseFactory(),
                SimulationConfig.defaultConfig());
        gameEngine = new GameEngine(
                grid,
                grid,
                simulationEngine,
                catalog,
                new PlacementValidator(catalog));
        persistenceService = new CityPersistenceService(
            grid, catalog, simulationEngine, new JsonCityRepository());
    }

    @Override
    public void start(Stage stage) throws IOException {
        initializeApplication();
        
        scene = new Scene(loadFXML("GameView"), 1100, 720); 
        
        stage.setTitle("CityLogic - Municipal Simulation");
        stage.setResizable(true);
        stage.setMinWidth(1100);
        stage.setMinHeight(720);
        stage.setScene(scene);
        
        stage.setMaximized(true); 
        
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    private static Path savePath(int slot) {
        if (slot < 1 || slot > 3) {
            throw new IllegalArgumentException("Save slot must be between 1 and 3");
        }
        return Path.of("saves", "citylogic-save-" + slot + ".json");
    }

    public static void main(String[] args) {
        launch();
    }

}