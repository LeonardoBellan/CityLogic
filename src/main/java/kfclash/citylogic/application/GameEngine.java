package kfclash.citylogic.application;

import java.util.Objects;

import kfclash.citylogic.domain.buildings.BuildingDescription;
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
        mapCommander.constructBuildingAt(x, y, description);
        return true;
    }

    public boolean demolishBuilding(int x, int y) {
        return mapCommander.removeBuildingAt(x, y) != null;
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
}
