package kfclash.citylogic.ports;

import kfclash.citylogic.domain.buildings.BuildingDescription;
import kfclash.citylogic.domain.core.ResourceDelta;
import kfclash.citylogic.domain.map.Point;

public interface IBuildingState {
    String getId();
    String getType();
    boolean isPowered();
    Point getPosition(); 
    BuildingDescription getDescription();
    ResourceDelta getBaseProduction();
    ResourceDelta getCurrentProduction();
}
