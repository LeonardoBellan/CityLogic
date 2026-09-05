package kfclash.citylogic.domain.map;

import kfclash.citylogic.domain.buildings.BuildingDescription;
import kfclash.citylogic.domain.buildings.BuildingFactory;
import kfclash.citylogic.domain.buildings.BuildingInstance;
import kfclash.citylogic.domain.map.Cell;
import kfclash.citylogic.domain.map.Dimension;
import kfclash.citylogic.domain.map.Grid;
import kfclash.citylogic.ports.IBuildingState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GridTest {

    private Grid grid;
    private BuildingFactory factory;
    private Dimension mapDimension;
    private BuildingDescription buildingDescription;

    @BeforeEach
    public void setUp() {
        factory = new BuildingFactory();
        mapDimension = new Dimension(10, 10);
        grid = new Grid(mapDimension, factory);
        Dimension footprint = new Dimension(2, 2);
        buildingDescription = new BuildingDescription("Test Building", 500, 100, footprint);
    }

    @Test
    public void testGridCreation() {
        assertEquals(10, grid.getDimensions().getWidth());
        assertEquals(10, grid.getDimensions().getHeight());
    }

    @Test
    public void testGridWithNullDimensions() {
        assertThrows(IllegalArgumentException.class, () -> new Grid(null, factory));
    }

    @Test
    public void testGridWithNullFactory() {
        assertThrows(IllegalArgumentException.class, () -> new Grid(mapDimension, null));
    }

    @Test
    public void testGetCellWithinBounds() {
        Cell cell = grid.getCell(5, 5);
        assertNotNull(cell);
        assertEquals(5, cell.getX());
        assertEquals(5, cell.getY());
    }

    @Test
    public void testGetCellOutOfBounds() {
        Cell cell = grid.getCell(15, 15);
        assertNull(cell);
    }

    @Test
    public void testGetCellNegativeCoordinates() {
        Cell cell = grid.getCell(-1, -1);
        assertNull(cell);
    }

    @Test
    public void testValidateSpatialPlacementValid() {
        Dimension footprint = new Dimension(2, 2);
        assertTrue(grid.validateSpatialPlacement(0, 0, footprint));
        assertTrue(grid.validateSpatialPlacement(5, 5, footprint));
        assertTrue(grid.validateSpatialPlacement(8, 8, footprint));
    }

    @Test
    public void testValidateSpatialPlacementOutOfBounds() {
        Dimension footprint = new Dimension(2, 2);
        assertFalse(grid.validateSpatialPlacement(9, 9, footprint));
        assertFalse(grid.validateSpatialPlacement(10, 10, footprint));
        assertFalse(grid.validateSpatialPlacement(100, 100, footprint));
    }

    @Test
    public void testValidateSpatialPlacementNegativeCoordinates() {
        Dimension footprint = new Dimension(2, 2);
        assertFalse(grid.validateSpatialPlacement(-1, 0, footprint));
        assertFalse(grid.validateSpatialPlacement(0, -1, footprint));
    }

    @Test
    public void testValidateSpatialPlacementWithNullFootprint() {
        assertFalse(grid.validateSpatialPlacement(0, 0, null));
    }

    @Test
    public void testConstructBuildingAt() {
        BuildingInstance building = grid.constructBuildingAt(0, 0, buildingDescription);

        assertNotNull(building);
        assertEquals(0, building.getPosition().getX());
        assertEquals(0, building.getPosition().getY());
        assertEquals(buildingDescription, building.getDescription());

        // Verify cells are occupied
        assertTrue(grid.getCell(0, 0).isOccupied());
        assertTrue(grid.getCell(1, 0).isOccupied());
        assertTrue(grid.getCell(0, 1).isOccupied());
        assertTrue(grid.getCell(1, 1).isOccupied());
    }

    @Test
    public void testGetTerrainAtReturnsLand() {
        assertEquals("land", grid.getTerrainAt(0, 0));
    }

    @Test
    public void testGetTerrainAtOutOfBoundsReturnsNull() {
        assertNull(grid.getTerrainAt(-1, 0));
        assertNull(grid.getTerrainAt(10, 10));
    }

    @Test
    public void testGetBuildingByIdReturnsExpectedBuilding() {
        BuildingInstance building = grid.constructBuildingAt(0, 0, buildingDescription);
        assertTrue(grid.getBuildingById(building.getId()).isPresent());
        assertEquals(building.getId(), grid.getBuildingById(building.getId()).get().getId());
    }

    @Test
    public void testGetAllBuildingsDoesNotDuplicateBuildings() {
        BuildingInstance building = grid.constructBuildingAt(0, 0, buildingDescription);

        List<IBuildingState> allBuildings = grid.getAllBuildings();
        assertEquals(1, allBuildings.size());
        assertEquals(building.getId(), allBuildings.get(0).getId());
    }

    @Test
    public void testGetAdjacentBuildingsWithinRadius() {
        BuildingInstance origin = grid.constructBuildingAt(0, 0, buildingDescription);
        Dimension footprint = new Dimension(1, 1);
        BuildingDescription neighborDesc = new BuildingDescription("Neighbor", 100, 20, footprint);
        BuildingInstance neighbor = grid.constructBuildingAt(2, 0, neighborDesc);

        List<IBuildingState> adjacent = grid.getAdjacentBuildings(origin.getId(), 2);
        assertEquals(1, adjacent.size());
        assertEquals(neighbor.getId(), adjacent.get(0).getId());
    }

    @Test
    public void testGetAdjacentBuildingsWithInvalidInputReturnsEmptyList() {
        assertTrue(grid.getAdjacentBuildings(null, 1).isEmpty());
        assertTrue(grid.getAdjacentBuildings("missing", 1).isEmpty());
        assertTrue(grid.getAdjacentBuildings("missing", -1).isEmpty());
    }

    @Test
    public void testIsAreaFreeReflectsOccupiedCells() {
        assertTrue(grid.isAreaFree(0, 0, buildingDescription.getFootprint()));
        grid.constructBuildingAt(0, 0, buildingDescription);
        assertFalse(grid.isAreaFree(0, 0, buildingDescription.getFootprint()));
    }

    @Test
    public void testConstructBuildingWithNullDescription() {
        assertThrows(IllegalArgumentException.class, () -> grid.constructBuildingAt(0, 0, null));
    }

    @Test
    public void testConstructBuildingOutOfBounds() {
        assertThrows(IllegalArgumentException.class,
                () -> grid.constructBuildingAt(9, 9, buildingDescription));
    }

    @Test
    public void testConstructBuildingOnOccupiedCell() {
        grid.constructBuildingAt(0, 0, buildingDescription);
        assertThrows(IllegalArgumentException.class,
                () -> grid.constructBuildingAt(0, 0, buildingDescription));
    }

    @Test
    public void testConstructBuildingOnPartiallyOccupiedSpace() {
        grid.constructBuildingAt(0, 0, buildingDescription);
        
        Dimension footprint = new Dimension(2, 2);
        BuildingDescription desc = new BuildingDescription("Another Building", 300, 75, footprint);
        assertThrows(IllegalArgumentException.class, () -> grid.constructBuildingAt(1, 1, desc));
    }

    @Test
    public void testRemoveBuildingAt() {
        grid.constructBuildingAt(3, 3, buildingDescription);

        // Verify building is present
        assertTrue(grid.getCell(3, 3).isOccupied());

        // Remove building
        BuildingInstance removed = grid.removeBuildingAt(3, 3);

        assertNotNull(removed);
        assertEquals(3, removed.getPosition().getX());
        assertEquals(3, removed.getPosition().getY());

        // Verify cells are now empty
        assertFalse(grid.getCell(3, 3).isOccupied());
        assertFalse(grid.getCell(4, 3).isOccupied());
        assertFalse(grid.getCell(3, 4).isOccupied());
        assertFalse(grid.getCell(4, 4).isOccupied());
    }

    @Test
    public void testRemoveBuildingFromInteriorCellClearsWholeFootprint() {
        BuildingInstance building = grid.constructBuildingAt(3, 3, buildingDescription);

        BuildingInstance removed = grid.removeBuildingAt(4, 4);

        assertEquals(building.getId(), removed.getId());
        assertFalse(grid.getCell(3, 3).isOccupied());
        assertFalse(grid.getCell(4, 3).isOccupied());
        assertFalse(grid.getCell(3, 4).isOccupied());
        assertFalse(grid.getCell(4, 4).isOccupied());
        assertTrue(grid.getAllBuildings().isEmpty());
    }

    @Test
    public void testRemoveBuildingAtEmptyCell() {
        BuildingInstance removed = grid.removeBuildingAt(5, 5);
        assertNull(removed);
    }

    @Test
    public void testRemoveBuildingAtRemovesItFromLookupAndCellState() {
        BuildingInstance building = grid.constructBuildingAt(3, 3, buildingDescription);

        BuildingInstance removed = grid.removeBuildingAt(3, 3);

        assertNotNull(removed);
        assertFalse(grid.getBuildingById(building.getId()).isPresent());
        assertTrue(grid.getAllBuildings().isEmpty());
        assertNull(grid.getCell(3, 3).getBuilding());
        assertFalse(grid.getCell(3, 3).isOccupied());
    }

    @Test
    public void testRemoveBuildingAtOutOfBounds() {
        BuildingInstance removed = grid.removeBuildingAt(15, 15);
        assertNull(removed);
    }

    @Test
    public void testConstructAndRemoveMultipleBuildings() {
        grid.constructBuildingAt(0, 0, buildingDescription);
        
        Dimension footprint2 = new Dimension(1, 1);
        BuildingDescription desc2 = new BuildingDescription("Small Building", 200, 50, footprint2);
        grid.constructBuildingAt(5, 5, desc2);

        assertTrue(grid.getCell(0, 0).isOccupied());
        assertTrue(grid.getCell(5, 5).isOccupied());

        grid.removeBuildingAt(0, 0);
        assertFalse(grid.getCell(0, 0).isOccupied());
        assertTrue(grid.getCell(5, 5).isOccupied());

        grid.removeBuildingAt(5, 5);
        assertFalse(grid.getCell(5, 5).isOccupied());
    }

    @Test
    public void testMapWithSmallDimensions() {
        Dimension small = new Dimension(2, 2);
        Grid smallMap = new Grid(small, factory);

        Dimension footprint = new Dimension(1, 1);
        BuildingDescription desc = new BuildingDescription("Small", 100, 50, footprint);

        smallMap.constructBuildingAt(0, 0, desc);
        smallMap.constructBuildingAt(1, 1, desc);

        assertEquals(2, smallMap.getDimensions().getWidth());
        assertEquals(2, smallMap.getDimensions().getHeight());

        assertTrue(smallMap.getCell(0, 0).isOccupied());
        assertTrue(smallMap.getCell(1, 1).isOccupied());
    }

    @Test
    public void testBuildingPlacedAtCorners() {
        Dimension footprint = new Dimension(1, 1);
        BuildingDescription desc = new BuildingDescription("Corner", 100, 50, footprint);

        grid.constructBuildingAt(0, 0, desc);
        assertTrue(grid.getCell(0, 0).isOccupied());

        grid.constructBuildingAt(9, 9, desc);
        assertTrue(grid.getCell(9, 9).isOccupied());
    }
}
