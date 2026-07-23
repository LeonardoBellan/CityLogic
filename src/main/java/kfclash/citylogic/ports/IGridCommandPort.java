package kfclash.citylogic.ports;

import kfclash.citylogic.domain.buildings.BuildingDescription;

public interface IGridCommandPort {
    IBuildingState constructBuildingAt(int x, int y, BuildingDescription desc);
    IBuildingState removeBuildingAt(int x, int y);
}
