package kfclash.citylogic.application;

import java.util.Objects;

import kfclash.citylogic.domain.buildings.BuildingDescription;
import kfclash.citylogic.domain.map.Point;
import kfclash.citylogic.domain.policies.BuildingTypes;
import kfclash.citylogic.ports.IBuildingState;
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
        if (!grid.isAreaFree(x, y, description.getFootprint())) {
            return false;
        }
        return satisfiesTypeSpecificRules(x, y, description, grid);
    }

    public boolean canPlace(int x, int y, BuildingDescription description, IGridReadPort grid) {
        if (description == null || grid == null) {
            return false;
        }
        if (!grid.isAreaFree(x, y, description.getFootprint())) {
            return false;
        }
        return satisfiesTypeSpecificRules(x, y, description, grid);
    }

    private boolean satisfiesTypeSpecificRules(
            int x, int y, BuildingDescription description, IGridReadPort grid) {
        String category = categoryOf(description);
        if (BuildingTypes.RESIDENTIAL.equals(category)
                || BuildingTypes.COMMERCIAL.equals(category)) {
            return hasNearbyRoad(x, y, description.getFootprint(), grid);
        }
        return true;
    }

    private boolean hasNearbyRoad(int x, int y, Dimension footprint, IGridReadPort grid) {
        for (IBuildingState building : grid.getAllBuildings()) {
            if (!BuildingTypes.is(building, BuildingTypes.ROAD)) {
                continue;
            }
            Point position = building.getPosition();
            Dimension roadFootprint = building.getDescription().getFootprint();
            if (rectanglesAreAdjacent(
                    x, y, footprint,
                    position.getX(), position.getY(), roadFootprint)) {
                return true;
            }
        }
        return false;
    }

    private static boolean rectanglesAreAdjacent(
            int firstX, int firstY, Dimension first,
            int secondX, int secondY, Dimension second) {
        int firstMaxX = firstX + first.getWidth() - 1;
        int firstMaxY = firstY + first.getHeight() - 1;
        int secondMaxX = secondX + second.getWidth() - 1;
        int secondMaxY = secondY + second.getHeight() - 1;

        int horizontalGap = Math.max(
                Math.max(secondX - firstMaxX, firstX - secondMaxX), 0);
        int verticalGap = Math.max(
                Math.max(secondY - firstMaxY, firstY - secondMaxY), 0);
        return Math.max(horizontalGap, verticalGap) <= 1;
    }

    private static String categoryOf(BuildingDescription description) {
        String normalized = description.getName().trim()
                .toUpperCase(java.util.Locale.ROOT)
                .replaceAll("[\\s-]+", "_");
        return switch (normalized) {
            case "HOUSE", "RESIDENTIAL" -> BuildingTypes.RESIDENTIAL;
            case "FACTORY", "INDUSTRIAL" -> BuildingTypes.INDUSTRIAL;
            case "SHOP", "COMMERCIAL" -> BuildingTypes.COMMERCIAL;
            case "PARK" -> BuildingTypes.PARK;
            case "POWERPLANT", "POWER_PLANT" -> BuildingTypes.POWER_PLANT;
            case "ROAD" -> BuildingTypes.ROAD;
            default -> "";
        };
    }
}
