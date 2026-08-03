package kfclash.citylogic.application;

import java.util.Objects;

import kfclash.citylogic.domain.buildings.BuildingDescription;
import kfclash.citylogic.domain.map.Dimension;
import kfclash.citylogic.ports.IGridReadPort;

/**
 * Lightweight domain service used by the application facade to validate placement.
 */
public class PlacementValidator {
    private final BuildingCatalog catalog;

    public PlacementValidator(BuildingCatalog catalog) {
        this.catalog = Objects.requireNonNull(catalog, "catalog cannot be null");
    }

    public boolean canPlace(int x, int y, String typeId, IGridReadPort grid) {
        if (grid == null || typeId == null || typeId.isBlank()) {
            return false;
        }
        BuildingDescription description = catalog.getByTypeId(typeId)
                .orElse(null);
        if (description == null) {
            return false;
        }
        return grid.isAreaFree(x, y, description.getFootprint());
    }

    public boolean canPlace(int x, int y, BuildingDescription description, IGridReadPort grid) {
        if (description == null || grid == null) {
            return false;
        }
        return grid.isAreaFree(x, y, description.getFootprint());
    }
}
