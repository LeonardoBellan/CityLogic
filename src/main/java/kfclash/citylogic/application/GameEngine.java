package kfclash.citylogic.application;

import java.math.BigDecimal;
import java.util.Objects;

import kfclash.citylogic.domain.buildings.BuildingDescription;
import kfclash.citylogic.domain.core.CitySnapshot;
import kfclash.citylogic.simulation.engine.SimulationEngine;
import kfclash.citylogic.ports.IGridCommandPort;
import kfclash.citylogic.ports.IGridReadPort;
import kfclash.citylogic.ports.IPolicyStrategy;

/**
 * Application-facing facade that orchestrates placement, demolition, time progression,
 * and policy changes for the UI layer.
 */
public class GameEngine {
    private final IGridCommandPort mapCommander;
    private final IGridReadPort gridReader;
    private final SimulationEngine simulationEngine;
    private final BuildingCatalog catalog;
    private final PlacementValidator validator;

    public GameEngine(IGridCommandPort mapCommander,
            IGridReadPort gridReader,
            SimulationEngine simulationEngine,
            BuildingCatalog catalog,
            PlacementValidator validator) {
        this.mapCommander = Objects.requireNonNull(mapCommander, "mapCommander cannot be null");
        this.gridReader = Objects.requireNonNull(gridReader, "gridReader cannot be null");
        this.simulationEngine = Objects.requireNonNull(simulationEngine, "simulationEngine cannot be null");
        this.catalog = Objects.requireNonNull(catalog, "catalog cannot be null");
        this.validator = Objects.requireNonNull(validator, "validator cannot be null");
    }

    public boolean placeBuilding(int x, int y, String typeId) {
        if (typeId == null || typeId.isBlank()) {
            return false;
        }
        BuildingDescription description = catalog.getByTypeId(typeId)
                .orElse(null);
        if (description == null || !validator.canPlace(x, y, typeId, gridReader)) {
            return false;
        }
        CitySnapshot currentSnapshot = simulationEngine.getCurrentSnapshot();
        if (currentSnapshot.budget().compareTo(BigDecimal.valueOf(description.getConstructionCost())) < 0) {
            return false;
        }
        mapCommander.constructBuildingAt(x, y, description);
        simulationEngine.loadState(withBudget(
                currentSnapshot,
                currentSnapshot.budget().subtract(BigDecimal.valueOf(description.getConstructionCost()))));
        return true;
    }

    public boolean demolishBuilding(int x, int y) {
        return demolishBuilding(x, y, 0.5);
    }

    public boolean demolishBuilding(int x, int y, double refundRate) {
        if (refundRate < 0.0 || refundRate > 1.0) {
            throw new IllegalArgumentException("refundRate must be between 0 and 1");
        }
        var removed = mapCommander.removeBuildingAt(x, y);
        if (removed == null) {
            return false;
        }
        BigDecimal refund = BigDecimal.valueOf(removed.getDescription().getConstructionCost())
                .multiply(BigDecimal.valueOf(refundRate));
        CitySnapshot currentSnapshot = simulationEngine.getCurrentSnapshot();
        simulationEngine.loadState(withBudget(
                currentSnapshot,
                currentSnapshot.budget().add(refund)));
        return true;
    }

    public void advanceTime() {
        simulationEngine.advanceTick();
    }

    public void setCityPolicy(IPolicyStrategy policy) {
        simulationEngine.activatePolicy(policy);
    }

    public void clearCityPolicy(String policyName) {
        simulationEngine.deactivatePolicy(policyName);
    }

    private static CitySnapshot withBudget(CitySnapshot snapshot, BigDecimal budget) {
        return new CitySnapshot(
                budget,
                snapshot.pollution(),
                snapshot.population(),
                snapshot.happiness(),
                snapshot.tickCount());
    }
}
