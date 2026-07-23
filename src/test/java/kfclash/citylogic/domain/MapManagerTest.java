package kfclash.citylogic.domain;

import kfclash.citylogic.domain.buildings.BuildingDescription;
import kfclash.citylogic.domain.buildings.BuildingFactory;
import kfclash.citylogic.domain.buildings.BuildingInstance;
import kfclash.citylogic.domain.map.Cell;
import kfclash.citylogic.domain.map.Dimension;
import kfclash.citylogic.domain.map.MapManager;
import kfclash.citylogic.ports.IBuildingState;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class MapManagerTest {

    private MapManager mapManager;
    private BuildingFactory factory;
    private Dimension mapDimension;
    private BuildingDescription buildingDescription;

    @Before
    public void setUp() {
        factory = new BuildingFactory();
        mapDimension = new Dimension(10, 10);
        mapManager = new MapManager(mapDimension, factory);
        Dimension footprint = new Dimension(2, 2);
        buildingDescription = new BuildingDescription("Test Building", 500, 100, footprint);
    }

    @Test
    public void testMapManagerCreation() {
        assertEquals(10, mapManager.getDimensions().getWidth());
        assertEquals(10, mapManager.getDimensions().getHeight());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMapManagerWithNullDimensions() {
        new MapManager(null, factory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMapManagerWithNullFactory() {
        new MapManager(mapDimension, null);
    }

    @Test
    public void testGetCellWithinBounds() {
        Cell cell = mapManager.getCell(5, 5);
        assertNotNull(cell);
        assertEquals(5, cell.getX());
        assertEquals(5, cell.getY());
    }

    @Test
    public void testGetCellOutOfBounds() {
        Cell cell = mapManager.getCell(15, 15);
        assertNull(cell);
    }

    @Test
    public void testGetCellNegativeCoordinates() {
        Cell cell = mapManager.getCell(-1, -1);
        assertNull(cell);
    }

    @Test
    public void testValidateSpatialPlacementValid() {
        Dimension footprint = new Dimension(2, 2);
        assertTrue(mapManager.validateSpatialPlacement(0, 0, footprint));
        assertTrue(mapManager.validateSpatialPlacement(5, 5, footprint));
        assertTrue(mapManager.validateSpatialPlacement(8, 8, footprint));
    }

    @Test
    public void testValidateSpatialPlacementOutOfBounds() {
        Dimension footprint = new Dimension(2, 2);
        assertFalse(mapManager.validateSpatialPlacement(9, 9, footprint));
        assertFalse(mapManager.validateSpatialPlacement(10, 10, footprint));
        assertFalse(mapManager.validateSpatialPlacement(100, 100, footprint));
    }

    @Test
    public void testValidateSpatialPlacementNegativeCoordinates() {
        Dimension footprint = new Dimension(2, 2);
        assertFalse(mapManager.validateSpatialPlacement(-1, 0, footprint));
        assertFalse(mapManager.validateSpatialPlacement(0, -1, footprint));
    }

    @Test
    public void testValidateSpatialPlacementWithNullFootprint() {
        assertFalse(mapManager.validateSpatialPlacement(0, 0, null));
    }

    @Test
    public void testConstructBuildingAt() {
        BuildingInstance building = mapManager.constructBuildingAt(0, 0, buildingDescription);

        assertNotNull(building);
        assertEquals(0, building.getX());
        assertEquals(0, building.getY());
        assertEquals(buildingDescription, building.getDescription());

        // Verify cells are occupied
        assertTrue(mapManager.getCell(0, 0).isOccupied());
        assertTrue(mapManager.getCell(1, 0).isOccupied());
        assertTrue(mapManager.getCell(0, 1).isOccupied());
        assertTrue(mapManager.getCell(1, 1).isOccupied());
    }

    @Test
    public void testGetTerrainAtReturnsLand() {
        assertEquals("land", mapManager.getTerrainAt(0, 0));
    }

    @Test
    public void testGetBuildingByIdReturnsExpectedBuilding() {
        BuildingInstance building = mapManager.constructBuildingAt(0, 0, buildingDescription);
        assertTrue(mapManager.getBuildingById(building.getId()).isPresent());
        assertEquals(building.getId(), mapManager.getBuildingById(building.getId()).get().getId());
    }

    @Test
    public void testGetAllBuildingsDoesNotDuplicateBuildings() {
        BuildingInstance building = mapManager.constructBuildingAt(0, 0, buildingDescription);

        List<IBuildingState> allBuildings = mapManager.getAllBuildings();
        assertEquals(1, allBuildings.size());
        assertEquals(building.getId(), allBuildings.get(0).getId());
    }

    @Test
    public void testGetAdjacentBuildingsWithinRadius() {
        BuildingInstance origin = mapManager.constructBuildingAt(0, 0, buildingDescription);
        Dimension footprint = new Dimension(1, 1);
        BuildingDescription neighborDesc = new BuildingDescription("Neighbor", 100, 20, footprint);
        BuildingInstance neighbor = mapManager.constructBuildingAt(2, 0, neighborDesc);

        List<IBuildingState> adjacent = mapManager.getAdjacentBuildings(origin.getId(), 2);
        assertEquals(1, adjacent.size());
        assertEquals(neighbor.getId(), adjacent.get(0).getId());
    }

    @Test
    public void testIsAreaFreeReflectsOccupiedCells() {
        assertTrue(mapManager.isAreaFree(0, 0, buildingDescription.getFootprint()));
        mapManager.constructBuildingAt(0, 0, buildingDescription);
        assertFalse(mapManager.isAreaFree(0, 0, buildingDescription.getFootprint()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructBuildingWithNullDescription() {
        mapManager.constructBuildingAt(0, 0, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructBuildingOutOfBounds() {
        mapManager.constructBuildingAt(9, 9, buildingDescription);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructBuildingOnOccupiedCell() {
        mapManager.constructBuildingAt(0, 0, buildingDescription);
        mapManager.constructBuildingAt(0, 0, buildingDescription);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructBuildingOnPartiallyOccupiedSpace() {
        mapManager.constructBuildingAt(0, 0, buildingDescription);
        
        Dimension footprint = new Dimension(2, 2);
        BuildingDescription desc = new BuildingDescription("Another Building", 300, 75, footprint);
        mapManager.constructBuildingAt(1, 1, desc);
    }

    @Test
    public void testRemoveBuildingAt() {
        mapManager.constructBuildingAt(3, 3, buildingDescription);

        // Verify building is present
        assertTrue(mapManager.getCell(3, 3).isOccupied());

        // Remove building
        BuildingInstance removed = mapManager.removeBuildingAt(3, 3);

        assertNotNull(removed);
        assertEquals(3, removed.getX());
        assertEquals(3, removed.getY());

        // Verify cells are now empty
        assertFalse(mapManager.getCell(3, 3).isOccupied());
        assertFalse(mapManager.getCell(4, 3).isOccupied());
        assertFalse(mapManager.getCell(3, 4).isOccupied());
        assertFalse(mapManager.getCell(4, 4).isOccupied());
    }

    @Test
    public void testRemoveBuildingAtEmptyCell() {
        BuildingInstance removed = mapManager.removeBuildingAt(5, 5);
        assertNull(removed);
    }

    @Test
    public void testRemoveBuildingAtOutOfBounds() {
        BuildingInstance removed = mapManager.removeBuildingAt(15, 15);
        assertNull(removed);
    }

    @Test
    public void testConstructAndRemoveMultipleBuildings() {
        mapManager.constructBuildingAt(0, 0, buildingDescription);
        
        Dimension footprint2 = new Dimension(1, 1);
        BuildingDescription desc2 = new BuildingDescription("Small Building", 200, 50, footprint2);
        mapManager.constructBuildingAt(5, 5, desc2);

        assertTrue(mapManager.getCell(0, 0).isOccupied());
        assertTrue(mapManager.getCell(5, 5).isOccupied());

        mapManager.removeBuildingAt(0, 0);
        assertFalse(mapManager.getCell(0, 0).isOccupied());
        assertTrue(mapManager.getCell(5, 5).isOccupied());

        mapManager.removeBuildingAt(5, 5);
        assertFalse(mapManager.getCell(5, 5).isOccupied());
    }

    @Test
    public void testMapWithSmallDimensions() {
        Dimension small = new Dimension(2, 2);
        MapManager smallMap = new MapManager(small, factory);

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

        mapManager.constructBuildingAt(0, 0, desc);
        assertTrue(mapManager.getCell(0, 0).isOccupied());

        mapManager.constructBuildingAt(9, 9, desc);
        assertTrue(mapManager.getCell(9, 9).isOccupied());
    }
}
