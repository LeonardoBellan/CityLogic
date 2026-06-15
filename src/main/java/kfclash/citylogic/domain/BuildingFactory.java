package kfclash.citylogic.domain;

import kfclash.citylogic.application.BuildingCatalog;

public class BuildingFactory {
    public BuildingInstance createBuilding(BuildingDescription description, int x, int y) {
        BuildingDescription shared = BuildingCatalog.getInstance().intern(description);
        return new BuildingInstance(shared, x, y);
    }
}
