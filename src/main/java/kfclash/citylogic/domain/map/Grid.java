package kfclash.citylogic.domain.map;

import kfclash.citylogic.domain.buildings.BuildingDescription;
import kfclash.citylogic.domain.buildings.BuildingFactory;
import kfclash.citylogic.domain.buildings.BuildingInstance;
import kfclash.citylogic.ports.IBuildingState;
import kfclash.citylogic.ports.IGridCommandPort;
import kfclash.citylogic.ports.IGridReadPort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Grid implements IGridReadPort, IGridCommandPort {
    private final Dimension dimensions;
    private final Cell[][] map;
    private final BuildingFactory factory;

    private final Map<String, BuildingInstance> activeBuildings = new HashMap<>();

    public Grid(Dimension dimensions, BuildingFactory factory) {
        if (dimensions == null) {
            throw new IllegalArgumentException("Dimensions cannot be null");
        }
        if (factory == null) {
            throw new IllegalArgumentException("BuildingFactory cannot be null");
        }

        this.dimensions = dimensions;
        this.factory = factory;
        this.map = new Cell[dimensions.getWidth()][dimensions.getHeight()];
        for (int x = 0; x < dimensions.getWidth(); x++) {
            for (int y = 0; y < dimensions.getHeight(); y++) {
                this.map[x][y] = new Cell(x, y);
            }
        }
    }

    public Dimension getDimensions() {
        return dimensions;
    }

    public Cell getCell(int x, int y) {
        if (!isWithinBounds(x, y)) {
            return null;
        }
        return map[x][y];
    }

    @Override
    public String getTerrainAt(int x, int y) {
        if (!isWithinBounds(x, y)) {
            return null;
        }
        return "land";
    }

    @Override
    public Optional<IBuildingState> getBuildingById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(activeBuildings.get(id));
    }

    @Override
    public List<IBuildingState> getAllBuildings() {
        return List.copyOf(activeBuildings.values());
    }

    @Override
    public List<IBuildingState> getAdjacentBuildings(String id, int radius) {
        if (id == null || radius < 0) {
            return Collections.emptyList();
        }

        Optional<BuildingInstance> originBuilding = getBuildingInstanceById(id);
        if (!originBuilding.isPresent()) {
            return Collections.emptyList();
        }

        Point origin = originBuilding.get().getPosition();
        List<IBuildingState> adjacent = new ArrayList<>();
        for (IBuildingState building : getAllBuildings()) {
            if (building.getId().equals(id)) {
                continue;
            }
            if (!(building instanceof BuildingInstance)) {
                continue;
            }
            BuildingInstance other = (BuildingInstance) building;
            int dx = Math.abs(other.getPosition().getX() - origin.getX());
            int dy = Math.abs(other.getPosition().getY() - origin.getY());
            if (Math.max(dx, dy) <= radius) {
                adjacent.add(other);
            }
        }
        return Collections.unmodifiableList(adjacent);
    }

    @Override
    public boolean isAreaFree(int x, int y, Dimension footprint) {
        return validateSpatialPlacement(x, y, footprint);
    }

    public boolean validateSpatialPlacement(int x, int y, Dimension footprint) {
        if (footprint == null) {
            return false;
        }
        if (!isWithinBounds(x, y)) {
            return false;
        }
        if (!isWithinBounds(x + footprint.getWidth() - 1, y + footprint.getHeight() - 1)) {
            return false;
        }

        for (int offsetX = 0; offsetX < footprint.getWidth(); offsetX++) {
            for (int offsetY = 0; offsetY < footprint.getHeight(); offsetY++) {
                Cell cell = map[x + offsetX][y + offsetY];
                if (cell.isOccupied()) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public BuildingInstance constructBuildingAt(int x, int y, BuildingDescription desc) {
        
        // Validate input and spatial placement
        if (desc == null) {
            throw new IllegalArgumentException("BuildingDescription cannot be null");
        }
        if (!validateSpatialPlacement(x, y, desc.getFootprint())) {
            throw new IllegalArgumentException("Cannot construct building at the requested position");
        }


        // Place building
        BuildingInstance building = factory.createBuilding(desc, x, y);
        Dimension footprint = desc.getFootprint();

        for (int offsetX = 0; offsetX < footprint.getWidth(); offsetX++) {
            for (int offsetY = 0; offsetY < footprint.getHeight(); offsetY++) {
                map[x + offsetX][y + offsetY].setBuilding(building);
            }
        }
        
        // Add building to active building map
        activeBuildings.put(building.getId(), building);
        return building;
    }

    @Override
    public BuildingInstance removeBuildingAt(int x, int y) {

        // Validate input
        if (!isWithinBounds(x, y)) {
            return null;
        }
        Cell startCell = map[x][y];
        if (!startCell.isOccupied()) {
            return null;
        }

        // Remove building
        BuildingInstance building = startCell.getBuilding();
        Dimension footprint = building.getDescription().getFootprint();
        int originX = building.getPosition().getX();
        int originY = building.getPosition().getY();

        for (int offsetX = 0; offsetX < footprint.getWidth(); offsetX++) {
            for (int offsetY = 0; offsetY < footprint.getHeight(); offsetY++) {
                int cellX = originX + offsetX;
                int cellY = originY + offsetY;
                if (isWithinBounds(cellX, cellY)) {
                    Cell cell = map[cellX][cellY];
                    if (cell.getBuilding() == building) {
                        cell.clear();
                    }
                }
            }
        }

        // Remove building from active building map
        activeBuildings.remove(building.getId());
        return building;
    }

    private Optional<BuildingInstance> getBuildingInstanceById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        for (IBuildingState buildingState : getAllBuildings()) {
            if (id.equals(buildingState.getId()) && buildingState instanceof BuildingInstance) {
                return Optional.of((BuildingInstance) buildingState);
            }
        }
        return Optional.empty();
    }

    private boolean isWithinBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < dimensions.getWidth() && y < dimensions.getHeight();
    }
}

