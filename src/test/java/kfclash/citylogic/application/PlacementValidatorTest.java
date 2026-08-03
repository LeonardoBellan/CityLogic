package kfclash.citylogic.application;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import kfclash.citylogic.domain.buildings.BuildingDescription;
import kfclash.citylogic.domain.map.Dimension;
import kfclash.citylogic.ports.IBuildingState;
import kfclash.citylogic.ports.IGridReadPort;

class PlacementValidatorTest {

    private static final class FakeGrid implements IGridReadPort {
        private final boolean areaFree;

        private FakeGrid(boolean areaFree) {
            this.areaFree = areaFree;
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
            return List.of();
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
        BuildingCatalog catalog = BuildingCatalog.getInstance();
        catalog.register(new BuildingDescription("House", 10, 1, new Dimension(1, 1)));
        PlacementValidator validator = new PlacementValidator(catalog);

        assertFalse(validator.canPlace(0, 0, "   ", null));
        assertFalse(validator.canPlace(0, 0, "House", null));
    }

    @Test
    void canPlaceReturnsFalseForUnknownTypeId() {
        BuildingCatalog catalog = BuildingCatalog.getInstance();
        PlacementValidator validator = new PlacementValidator(catalog);

        assertFalse(validator.canPlace(0, 0, "missing", new FakeGrid(true)));
    }

    @Test
    void canPlaceUsesGridAvailabilityForKnownDescription() {
        BuildingCatalog catalog = BuildingCatalog.getInstance();
        BuildingDescription description = new BuildingDescription("House", 10, 1, new Dimension(2, 1));
        catalog.register(description);
        PlacementValidator validator = new PlacementValidator(catalog);

        assertTrue(validator.canPlace(0, 0, description.getTypeId(), new FakeGrid(true)));
        assertFalse(validator.canPlace(0, 0, description.getTypeId(), new FakeGrid(false)));
    }

    @Test
    void canPlaceWithDescriptionObjectReturnsFalseForNullInputs() {
        BuildingCatalog catalog = BuildingCatalog.getInstance();
        PlacementValidator validator = new PlacementValidator(catalog);

        assertFalse(validator.canPlace(0, 0, (BuildingDescription) null, new FakeGrid(true)));
        assertFalse(validator.canPlace(0, 0, new BuildingDescription("House", 10, 1, new Dimension(1, 1)), null));
    }
}
