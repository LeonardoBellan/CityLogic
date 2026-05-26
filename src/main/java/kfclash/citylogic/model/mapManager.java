package kfclash.citylogic.model;

public class mapManager {
    private final Dimension dimensions;
    private final Cell[][] map;
    private final BuildingFactory factory;

    public mapManager(Dimension dimensions, BuildingFactory factory) {
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

    private boolean isWithinBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < dimensions.getWidth() && y < dimensions.getHeight();
    }
}

