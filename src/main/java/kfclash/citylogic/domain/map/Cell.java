package kfclash.citylogic.domain.map;

import kfclash.citylogic.domain.buildings.BuildingInstance;

public class Cell {
    private final int x;
    private final int y;
    private final Point position;
    private int pollutionLevel;
    private BuildingInstance currentBuilding;

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        this.position = new Point(x, y);
        this.pollutionLevel = 0;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Point getPosition() {
        return position;
    }

    public int getPollutionLevel() {
        return pollutionLevel;
    }

    public void setPollutionLevel(int pollutionLevel) {
        this.pollutionLevel = pollutionLevel;
    }

    public BuildingInstance getBuilding() {
        return currentBuilding;
    }

    public boolean isOccupied() {
        return currentBuilding != null;
    }

    public void setBuilding(BuildingInstance building) {
        this.currentBuilding = building;
    }

    public void clear() {
        this.currentBuilding = null;
    }
}
