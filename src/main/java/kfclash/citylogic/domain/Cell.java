package kfclash.citylogic.domain;

public class Cell {
    private final int x;
    private final int y;
    private BuildingInstance currentBuilding;

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
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
