package kfclash.citylogic.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import kfclash.citylogic.application.ApplicationBuildingDescriptionProvider;
import kfclash.citylogic.application.BuildingCatalog;
import kfclash.citylogic.application.CityPersistenceService;
import kfclash.citylogic.domain.core.CityAggregate;
import kfclash.citylogic.domain.core.CitySnapshot;
import kfclash.citylogic.domain.map.Dimension;
import kfclash.citylogic.domain.map.Grid;
import kfclash.citylogic.domain.buildings.BuildingFactory;
import kfclash.citylogic.simulation.engine.SimulationEngine;
import kfclash.citylogic.simulation.tick.SimulationConfig;
import kfclash.citylogic.simulation.tick.TickPhaseFactory;
import kfclash.citylogic.presentation.javafx.CityEventPublisher;

class CityPersistenceServiceTest {
    @TempDir
    Path temporaryDirectory;

    private Grid grid;
    private SimulationEngine simulationEngine;
    private CityPersistenceService persistence;

    @BeforeEach
    void setUp() {
        grid = new Grid(new Dimension(8, 8), new BuildingFactory());
        BuildingCatalog catalog = new BuildingCatalog();
        ApplicationBuildingDescriptionProvider.initDefaultCatalog(catalog);
        CityAggregate city = new CityAggregate(new BigDecimal("5000.00"), 12, 78.0);
        simulationEngine = new SimulationEngine(
                city,
                grid,
                new CityEventPublisher(),
                new TickPhaseFactory(),
                SimulationConfig.defaultConfig());
        persistence = new CityPersistenceService(
                grid, catalog, simulationEngine, new JsonCityRepository());
    }

    @Test
    void savesAndRestoresCitySnapshotAndBuildingPlacements() throws Exception {
        BuildingCatalog catalog = new BuildingCatalog();
        ApplicationBuildingDescriptionProvider.initDefaultCatalog(catalog);
        var factory = grid.constructBuildingAt(2, 3, catalog.getByTypeId("house").orElseThrow());
        factory.setPowered(false);
        simulationEngine.loadState(new CitySnapshot(
                new BigDecimal("4321.00"), 12.5, 99, 64.0, 7));

        Path savePath = temporaryDirectory.resolve("city.json");
        persistence.save(savePath);
        assertTrue(Files.exists(savePath));

        grid.clearBuildings();
        simulationEngine.loadState(new CitySnapshot(
                new BigDecimal("100.00"), 0.0, 0, 50.0, 0));

        persistence.load(savePath);

        assertEquals(new BigDecimal("4321.00"), simulationEngine.getCurrentSnapshot().budget());
        assertEquals(12.5, simulationEngine.getCurrentSnapshot().pollution());
        assertEquals(99, simulationEngine.getCurrentSnapshot().population());
        assertEquals(7, simulationEngine.getCurrentSnapshot().tickCount());
        assertEquals(1, grid.getAllBuildings().size());
        assertFalse(grid.getAllBuildings().get(0).isPowered());
        assertEquals(2, grid.getAllBuildings().get(0).getPosition().getX());
        assertEquals(3, grid.getAllBuildings().get(0).getPosition().getY());
    }
}
