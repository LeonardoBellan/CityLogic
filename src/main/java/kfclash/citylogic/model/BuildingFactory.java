package kfclash.citylogic.model;

public class BuildingFactory {
    public BuildingInstance createBuilding(BuildingDescription description, int x, int y) {
        return new BuildingInstance(description, x, y);
    }
}
