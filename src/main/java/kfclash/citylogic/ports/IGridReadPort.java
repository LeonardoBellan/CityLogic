package kfclash.citylogic.ports;

import kfclash.citylogic.domain.Dimension;
import java.util.List;
import java.util.Optional;

public interface IGridReadPort {
    String getTerrainAt(int x, int y);
    Optional<IBuildingState> getBuildingById(String id);
    List<IBuildingState> getAllBuildings();
    List<IBuildingState> getAdjacentBuildings(String id, int radius);
    boolean isAreaFree(int x, int y, Dimension footprint);
}
