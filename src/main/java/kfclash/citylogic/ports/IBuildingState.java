package kfclash.citylogic.ports;

import java.util.List;

import kfclash.citylogic.domain.buildings.BuildingDescription;
import kfclash.citylogic.domain.map.Point;
import kfclash.citylogic.domain.map.Resource;

public interface IBuildingState {
    String getId();
    String getType();
    boolean isPowered();
    Point getPosition(); 
    BuildingDescription getDescription();
    List<Resource> getCurrentProduction();
}
