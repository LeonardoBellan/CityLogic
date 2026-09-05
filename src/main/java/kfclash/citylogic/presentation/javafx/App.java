package kfclash.citylogic.presentation.javafx;

import java.math.BigDecimal;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import kfclash.citylogic.application.ApplicationBuildingDescriptionProvider;
import kfclash.citylogic.application.BuildingCatalog;
import kfclash.citylogic.application.GameEngine;
import kfclash.citylogic.application.PlacementValidator;
import kfclash.citylogic.domain.buildings.BuildingFactory;
import kfclash.citylogic.domain.core.CityAggregate;
import kfclash.citylogic.domain.map.Dimension;
import kfclash.citylogic.domain.map.Grid;
import kfclash.citylogic.simulation.engine.SimulationEngine;
import kfclash.citylogic.simulation.tick.SimulationConfig;
import kfclash.citylogic.simulation.tick.TickPhaseFactory;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    private static Grid grid;
    private static GameEngine gameEngine;
    private static SimulationEngine simulationEngine;
    private static CityEventPublisher eventPublisher;

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

    private static void initializeApplication() {
        grid = new Grid(new Dimension(12, 12), new BuildingFactory());

        BuildingCatalog catalog = new BuildingCatalog();
        ApplicationBuildingDescriptionProvider.initDefaultCatalog(catalog);

        CityAggregate cityState = new CityAggregate(
                new BigDecimal("50000.00"), 4200, 74.0);
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
    }

    @Override
    public void start(Stage stage) throws IOException {
        initializeApplication();
        scene = new Scene(loadFXML("menu"), 640, 480);
        stage.setTitle("Simcity lite");
        stage.setResizable(true);
        stage.setMinWidth(640);
        stage.setMinHeight(480);
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}