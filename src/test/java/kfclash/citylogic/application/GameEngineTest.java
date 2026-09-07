package kfclash.citylogic.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import kfclash.citylogic.ports.IPolicyStrategy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import kfclash.citylogic.domain.buildings.BuildingDescription;
import kfclash.citylogic.domain.core.CityAggregate;
import kfclash.citylogic.domain.core.CitySnapshot;
import kfclash.citylogic.domain.map.Dimension;
import kfclash.citylogic.simulation.engine.SimulationEngine;
import kfclash.citylogic.ports.IBuildingState;
import kfclash.citylogic.ports.ICityEventPublisher;
import kfclash.citylogic.ports.ICityObserver;
import kfclash.citylogic.ports.IGridCommandPort;
import kfclash.citylogic.ports.IGridReadPort;
import kfclash.citylogic.simulation.tick.SimulationConfig;
import kfclash.citylogic.simulation.tick.TickPhaseFactory;

class GameEngineTest {

    private static class RecordingGrid implements IGridCommandPort, IGridReadPort {
        private final List<String> constructed = new ArrayList<>();
        private int removals = 0;
        private IBuildingState buildingToRemove;

        @Override
        public IBuildingState constructBuildingAt(int x, int y, BuildingDescription desc) {
            constructed.add(desc.getTypeId());
            return new StubBuildingState(desc.getTypeId(), desc);
        }

        @Override
        public IBuildingState removeBuildingAt(int x, int y) {
            removals++;
            IBuildingState removed = buildingToRemove;
            buildingToRemove = null;
            return removed;
        }

        @Override
        public String getTerrainAt(int x, int y) {
            return "land";
        }

        @Override
        public Optional<IBuildingState> getBuildingById(String id) {
            return Optional.empty();
        }

        @Override
        public List<IBuildingState> getAllBuildings() {
            BuildingDescription road = new BuildingDescription("Road", 10, 1, new Dimension(1, 1));
            return List.of(new StubBuildingState("road-1", road, 1, 0));
        }

        @Override
        public List<IBuildingState> getAdjacentBuildings(String id, int radius) {
            return List.of();
        }

        @Override
        public boolean isAreaFree(int x, int y, Dimension footprint) {
            return x == 0 && y == 0;
        }
    }

    private static class RecordingPublisher implements ICityEventPublisher {
        @Override
        public void publish(CitySnapshot snapshot) {
            // no-op for this test
        }

        @Override
        public void subscribe(ICityObserver observer) {
            // no-op
        }

        @Override
        public void unsubscribe(ICityObserver observer) {
            // no-op
        }
    }

    private static class StubBuildingState implements IBuildingState {
        private final String id;
        private final BuildingDescription description;
        private final int x;
        private final int y;

        private StubBuildingState(String id, BuildingDescription description) {
            this(id, description, 0, 0);
        }

        private StubBuildingState(String id, BuildingDescription description, int x, int y) {
            this.id = id;
            this.description = description;
            this.x = x;
            this.y = y;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getType() {
            return description.getTypeId();
        }

        @Override
        public boolean isPowered() {
            return true;
        }

        @Override
        public kfclash.citylogic.domain.map.Point getPosition() {
            return new kfclash.citylogic.domain.map.Point(x, y);
        }

        @Override
        public BuildingDescription getDescription() {
            return description;
        }

        @Override
        public kfclash.citylogic.domain.core.ResourceDelta getBaseProduction() {
            return kfclash.citylogic.domain.core.ResourceDelta.zero();
        }

        @Override
        public kfclash.citylogic.domain.core.ResourceDelta getCurrentProduction() {
            return kfclash.citylogic.domain.core.ResourceDelta.zero();
        }
    }

    @Test
    void placeBuildingUsesCatalogAndValidator() {
        RecordingGrid grid = new RecordingGrid();
        BuildingCatalog catalog = new BuildingCatalog();
        BuildingDescription description = new BuildingDescription(
                "Test House", 120, 10, new Dimension(1, 1));
        catalog.register(description);

        CityAggregate city = new CityAggregate(new BigDecimal("1000"), 0, 50.0);
        SimulationEngine simulation = new SimulationEngine(
                city,
                grid,
                new RecordingPublisher(),
                new TickPhaseFactory(),
                SimulationConfig.defaultConfig());
        GameEngine engine = new GameEngine(grid, grid, simulation, catalog,
                new PlacementValidator(catalog));

        assertTrue(engine.placeBuilding(0, 0, description.getTypeId()));
        assertFalse(engine.placeBuilding(1, 1, description.getTypeId()));
        assertEquals(1, grid.constructed.size());
    }

        @Test
        void placeBuildingDeductsConstructionCost() {
        RecordingGrid grid = new RecordingGrid();
        BuildingCatalog catalog = new BuildingCatalog();
        BuildingDescription description = new BuildingDescription(
            "Test House", 120, 10, new Dimension(1, 1));
        catalog.register(description);
        CityAggregate city = new CityAggregate(new BigDecimal("1000"), 0, 50.0);
        SimulationEngine simulation = new SimulationEngine(
            city, grid, new RecordingPublisher(), new TickPhaseFactory(),
            SimulationConfig.defaultConfig());
        GameEngine engine = new GameEngine(grid, grid, simulation, catalog,
            new PlacementValidator(catalog));

        assertTrue(engine.placeBuilding(0, 0, description.getTypeId()));
        assertEquals(new BigDecimal("880"), simulation.getCurrentSnapshot().budget());
        }

        @Test
        void placeBuildingRejectsInsufficientFundsBeforeConstruction() {
        RecordingGrid grid = new RecordingGrid();
        BuildingCatalog catalog = new BuildingCatalog();
        BuildingDescription description = new BuildingDescription(
            "Expensive House", 120, 10, new Dimension(1, 1));
        catalog.register(description);
        CityAggregate city = new CityAggregate(new BigDecimal("100"), 0, 50.0);
        SimulationEngine simulation = new SimulationEngine(
            city, grid, new RecordingPublisher(), new TickPhaseFactory(),
            SimulationConfig.defaultConfig());
        GameEngine engine = new GameEngine(grid, grid, simulation, catalog,
            new PlacementValidator(catalog));

        assertFalse(engine.placeBuilding(0, 0, description.getTypeId()));
        assertTrue(grid.constructed.isEmpty());
        assertEquals(new BigDecimal("100"), simulation.getCurrentSnapshot().budget());
        }

    @Test
    void advanceTimeDelegatesToSimulationEngine() {
        RecordingGrid grid = new RecordingGrid();
        BuildingCatalog catalog = new BuildingCatalog();
        CityAggregate city = new CityAggregate(new BigDecimal("1000"), 0, 50.0);
        SimulationEngine simulation = new SimulationEngine(
                city,
                grid,
                new RecordingPublisher(),
                new TickPhaseFactory(),
                SimulationConfig.defaultConfig());
        GameEngine engine = new GameEngine(grid, grid, simulation, catalog,
                new PlacementValidator(catalog));

        engine.advanceTime();

        assertEquals(1, simulation.getCurrentSnapshot().tickCount());
    }

    @Test
    void demolishBuildingDelegatesToGridAndReturnsTrueWhenRemoved() {
        RecordingGrid grid = new RecordingGrid();
        BuildingCatalog catalog = new BuildingCatalog();
        CityAggregate city = new CityAggregate(new BigDecimal("1000"), 0, 50.0);
        SimulationEngine simulation = new SimulationEngine(
                city,
                grid,
                new RecordingPublisher(),
                new TickPhaseFactory(),
                SimulationConfig.defaultConfig());
        GameEngine engine = new GameEngine(grid, grid, simulation, catalog,
                new PlacementValidator(catalog));

        assertFalse(engine.demolishBuilding(0, 0));
        assertEquals(1, grid.removals);
    }

        @Test
        void demolishBuildingRefundsHalfOfConstructionCostByDefault() {
        RecordingGrid grid = new RecordingGrid();
        BuildingCatalog catalog = new BuildingCatalog();
        BuildingDescription description = new BuildingDescription(
            "Test House", 120, 10, new Dimension(1, 1));
        catalog.register(description);
        grid.buildingToRemove = new StubBuildingState("house-1", description);
        CityAggregate city = new CityAggregate(new BigDecimal("1000"), 0, 50.0);
        SimulationEngine simulation = new SimulationEngine(
            city, grid, new RecordingPublisher(), new TickPhaseFactory(),
            SimulationConfig.defaultConfig());
        GameEngine engine = new GameEngine(grid, grid, simulation, catalog,
            new PlacementValidator(catalog));

        assertTrue(engine.demolishBuilding(0, 0));
        assertEquals(new BigDecimal("1060.0"), simulation.getCurrentSnapshot().budget());
        }

    @Test
    void policiesAreForwardedToSimulationEngine() {
        RecordingGrid grid = new RecordingGrid();
        BuildingCatalog catalog = new BuildingCatalog();
        CityAggregate city = new CityAggregate(new BigDecimal("1000"), 0, 50.0);
        SimulationEngine simulation = new SimulationEngine(
                city,
                grid,
                new RecordingPublisher(),
                new TickPhaseFactory(),
                SimulationConfig.defaultConfig());
        GameEngine engine = new GameEngine(grid, grid, simulation, catalog,
                new PlacementValidator(catalog));

        IPolicyStrategy policy = new IPolicyStrategy() {
            @Override
            public String getName() {
                return "Test Policy";
            }

            @Override
            public kfclash.citylogic.domain.core.ResourceDelta calculateModifier(IBuildingState building, IGridReadPort grid) {
                return kfclash.citylogic.domain.core.ResourceDelta.zero();
            }
        };

        engine.setCityPolicy(policy);
        engine.clearCityPolicy(policy.getName());

        assertTrue(simulation.getActivePolicyNames().isEmpty());
    }
}
