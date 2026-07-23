package kfclash.citylogic.domain;

import kfclash.citylogic.ports.IBuildingState;
import kfclash.citylogic.ports.IGridCommandPort;
import kfclash.citylogic.ports.IGridReadPort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class MapManager implements IGridReadPort, IGridCommandPort {
    private final Dimension dimensions;
    private final Cell[][] map;
    private final BuildingFactory factory;

    public MapManager(Dimension dimensions, BuildingFactory factory) {
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
        if (id == null) {
            return Optional.empty();
        }
        return getAllBuildings().stream()
                .filter(building -> id.equals(building.getId()))
                .findFirst();
    }

    @Override
    public List<IBuildingState> getAllBuildings() {
        Set<BuildingInstance> buildingSet = new LinkedHashSet<>();
        for (int x = 0; x < dimensions.getWidth(); x++) {
            for (int y = 0; y < dimensions.getHeight(); y++) {
                Cell cell = map[x][y];
                if (cell.isOccupied()) {
                    buildingSet.add(cell.getBuilding());
                }
            }
        }
        return Collections.unmodifiableList(new ArrayList<>(buildingSet));
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
        if (desc == null) {
            throw new IllegalArgumentException("BuildingDescription cannot be null");
        }
        if (!validateSpatialPlacement(x, y, desc.getFootprint())) {
            throw new IllegalArgumentException("Cannot construct building at the requested position");
        }

        BuildingInstance building = factory.createBuilding(desc, x, y);
        Dimension footprint = desc.getFootprint();
        for (int offsetX = 0; offsetX < footprint.getWidth(); offsetX++) {
            for (int offsetY = 0; offsetY < footprint.getHeight(); offsetY++) {
                map[x + offsetX][y + offsetY].setBuilding(building);
            }
        }

        return building;
    }

    @Override
    public BuildingInstance removeBuildingAt(int x, int y) {
        if (!isWithinBounds(x, y)) {
            return null;
        }

        Cell startCell = map[x][y];
        if (!startCell.isOccupied()) {
            return null;
        }

        BuildingInstance building = startCell.getBuilding();
        Dimension footprint = building.getDescription().getFootprint();
        int originX = building.getX();
        int originY = building.getY();

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

