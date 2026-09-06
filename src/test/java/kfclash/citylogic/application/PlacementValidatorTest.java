package kfclash.citylogic.application;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import kfclash.citylogic.domain.buildings.BuildingDescription;
import kfclash.citylogic.domain.buildings.BuildingInstance;
import kfclash.citylogic.domain.map.Dimension;
import kfclash.citylogic.ports.IBuildingState;
import kfclash.citylogic.ports.IGridReadPort;

class PlacementValidatorTest {

    private static final class FakeGrid implements IGridReadPort {
        private final boolean areaFree;
        private final List<IBuildingState> buildings;

        private FakeGrid(boolean areaFree) {
            this(areaFree, List.of());
        }

        private FakeGrid(boolean areaFree, List<IBuildingState> buildings) {
            this.areaFree = areaFree;
            this.buildings = buildings;
        }

        @Override
        public String getTerrainAt(int x, int y) {
            return "land";
        }

        @Override
        public Optional<IBuildingState> getBuildingById(String id) {
            return Optional.empty();
        }

        @Override
        public List<IBuildingState> getAllBuildings() {
            return buildings;
        }

        @Override
        public List<IBuildingState> getAdjacentBuildings(String id, int radius) {
            return List.of();
        }

        @Override
        public boolean isAreaFree(int x, int y, Dimension footprint) {
            return areaFree;
        }
    }

    @Test
    void canPlaceReturnsFalseForNullGridOrBlankTypeId() {
        BuildingCatalog catalog = new BuildingCatalog();
        catalog.register(new BuildingDescription("House", 10, 1, new Dimension(1, 1)));
        PlacementValidator validator = new PlacementValidator(catalog);

        assertFalse(validator.canPlace(0, 0, "   ", null));
        assertFalse(validator.canPlace(0, 0, "House", null));
    }

    @Test
    void canPlaceReturnsFalseForUnknownTypeId() {
        BuildingCatalog catalog = new BuildingCatalog();
        PlacementValidator validator = new PlacementValidator(catalog);

        assertFalse(validator.canPlace(0, 0, "missing", new FakeGrid(true)));
    }

    @Test
    void canPlaceUsesGridAvailabilityForKnownDescription() {
        BuildingCatalog catalog = new BuildingCatalog();
        BuildingDescription description = new BuildingDescription("Factory", 10, 1, new Dimension(2, 1));
        catalog.register(description);
        PlacementValidator validator = new PlacementValidator(catalog);

        assertTrue(validator.canPlace(0, 0, description.getTypeId(), new FakeGrid(true)));
        assertFalse(validator.canPlace(0, 0, description.getTypeId(), new FakeGrid(false)));
    }

    @Test
    void canPlaceWithDescriptionObjectReturnsFalseForNullInputs() {
        BuildingCatalog catalog = new BuildingCatalog();
        PlacementValidator validator = new PlacementValidator(catalog);

        assertFalse(validator.canPlace(0, 0, (BuildingDescription) null, new FakeGrid(true)));
        assertFalse(validator.canPlace(0, 0, new BuildingDescription("House", 10, 1, new Dimension(1, 1)), null));
    }

    @Test
    void residentialBuildingRequiresNearbyRoad() {
        BuildingCatalog catalog = new BuildingCatalog();
        BuildingDescription house = new BuildingDescription("House", 10, 1, new Dimension(1, 1));
        BuildingDescription road = new BuildingDescription("Road", 10, 1, new Dimension(1, 1));
        catalog.register(house);
        PlacementValidator validator = new PlacementValidator(catalog);

        IBuildingState nearbyRoad = new BuildingInstance(road, 1, 0);
        IBuildingState distantRoad = new BuildingInstance(road, 4, 4);

        assertTrue(validator.canPlace(0, 0, house.getTypeId(),
                new FakeGrid(true, List.of(nearbyRoad))));
        assertFalse(validator.canPlace(0, 0, house.getTypeId(),
                new FakeGrid(true, List.of(distantRoad))));
    }

    @Test
    void commercialBuildingUsesTheSameRoadAccessRule() {
        BuildingCatalog catalog = new BuildingCatalog();
        BuildingDescription commercial = new BuildingDescription("Commercial", 10, 1, new Dimension(1, 1));
        BuildingDescription road = new BuildingDescription("Road", 10, 1, new Dimension(1, 1));
        catalog.register(commercial);
        PlacementValidator validator = new PlacementValidator(catalog);

        assertTrue(validator.canPlace(2, 2, commercial.getTypeId(),
                new FakeGrid(true, List.of(new BuildingInstance(road, 2, 3)))));
    }
}
