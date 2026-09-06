package kfclash.citylogic.application;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import kfclash.citylogic.domain.buildings.BuildingDescription;
import kfclash.citylogic.domain.buildings.BuildingFactory;
import kfclash.citylogic.domain.core.CitySnapshot;
import kfclash.citylogic.domain.map.Dimension;
import kfclash.citylogic.domain.map.Grid;
import kfclash.citylogic.persistence.BuildingSaveData;
import kfclash.citylogic.persistence.CityRepository;
import kfclash.citylogic.persistence.CitySaveData;
import kfclash.citylogic.persistence.CitySnapshotSaveData;
import kfclash.citylogic.ports.IBuildingState;
import kfclash.citylogic.simulation.engine.SimulationEngine;

/** Application service coordinating repository I/O with domain state restoration. */
public final class CityPersistenceService {
    private final Grid grid;
    private final BuildingCatalog catalog;
    private final SimulationEngine simulationEngine;
    private final CityRepository repository;

    public CityPersistenceService(
            Grid grid,
            BuildingCatalog catalog,
            SimulationEngine simulationEngine,
            CityRepository repository) {
        this.grid = Objects.requireNonNull(grid, "grid cannot be null");
        this.catalog = Objects.requireNonNull(catalog, "catalog cannot be null");
        this.simulationEngine = Objects.requireNonNull(simulationEngine, "simulationEngine cannot be null");
        this.repository = Objects.requireNonNull(repository, "repository cannot be null");
    }

    public void save(Path path) throws IOException {
        Dimension dimensions = grid.getDimensions();
        List<BuildingSaveData> buildings = grid.getAllBuildings().stream()
                .map(this::toSaveData)
                .toList();
        repository.save(path, new CitySaveData(
            toSaveData(simulationEngine.getCurrentSnapshot()),
                dimensions.getWidth(),
                dimensions.getHeight(),
                buildings));
    }

    public void load(Path path) throws IOException {
        CitySaveData saveData = repository.load(path);
        validateDimensions(saveData);

        // Validate every placement before mutating the live grid.
        Grid restoredGrid = new Grid(
                new Dimension(saveData.gridWidth(), saveData.gridHeight()),
                new BuildingFactory());
        for (BuildingSaveData buildingData : saveData.buildings()) {
            BuildingDescription description = catalog.getByTypeId(buildingData.typeId())
                    .orElseThrow(() -> new IOException(
                            "Unknown building type in save: " + buildingData.typeId()));
            try {
                var restored = restoredGrid.constructBuildingAt(
                        buildingData.x(), buildingData.y(), description);
                restored.setPowered(buildingData.powered());
            } catch (IllegalArgumentException error) {
                throw new IOException("Invalid building placement in save", error);
            }
        }

        grid.clearBuildings();
        for (BuildingSaveData buildingData : saveData.buildings()) {
            BuildingDescription description = catalog.getByTypeId(buildingData.typeId()).orElseThrow();
            var restored = grid.constructBuildingAt(
                    buildingData.x(), buildingData.y(), description);
            restored.setPowered(buildingData.powered());
        }
        simulationEngine.loadState(new CitySnapshot(
            saveData.snapshot().budget(),
            saveData.snapshot().pollution(),
            saveData.snapshot().population(),
            saveData.snapshot().happiness(),
            saveData.snapshot().tickCount()));
    }

    private BuildingSaveData toSaveData(IBuildingState building) {
        return new BuildingSaveData(
                building.getDescription().getTypeId(),
                building.getPosition().getX(),
                building.getPosition().getY(),
                building.isPowered());
    }

    private CitySnapshotSaveData toSaveData(CitySnapshot snapshot) {
        return new CitySnapshotSaveData(
                snapshot.budget(),
                snapshot.pollution(),
                snapshot.population(),
                snapshot.happiness(),
                snapshot.tickCount());
    }

    private void validateDimensions(CitySaveData saveData) throws IOException {
        Dimension current = grid.getDimensions();
        if (current.getWidth() != saveData.gridWidth()
                || current.getHeight() != saveData.gridHeight()) {
            throw new IOException(String.format(
                    "Save grid is %dx%d but the current grid is %dx%d",
                    saveData.gridWidth(), saveData.gridHeight(),
                    current.getWidth(), current.getHeight()));
        }
    }
}
