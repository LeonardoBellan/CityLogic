package kfclash.citylogic.domain;

import kfclash.citylogic.application.BuildingDescription;
import kfclash.citylogic.domain.Point;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CellTest {

    private Cell cell;
    private BuildingDescription buildingDescription;
    private BuildingInstance building;

    @Before
    public void setUp() {
        cell = new Cell(5, 10);
        Dimension footprint = new Dimension(2, 2);
        buildingDescription = new BuildingDescription("Test Building", 100, 50, footprint);
        building = new BuildingInstance(buildingDescription, 5, 10);
    }

    @Test
    public void testCellCoordinates() {
        Cell testCell = new Cell(3, 7);
        assertEquals(3, testCell.getX());
        assertEquals(7, testCell.getY());
    }

    @Test
    public void testCellWithZeroCoordinates() {
        Cell testCell = new Cell(0, 0);
        assertEquals(0, testCell.getX());
        assertEquals(0, testCell.getY());
    }

    @Test
    public void testCellInitiallyNotOccupied() {
        assertFalse(cell.isOccupied());
        assertNull(cell.getBuilding());
    }

    @Test
    public void testSetBuilding() {
        cell.setBuilding(building);
        assertTrue(cell.isOccupied());
        assertEquals(building, cell.getBuilding());
    }

    @Test
    public void testClearBuilding() {
        cell.setBuilding(building);
        assertTrue(cell.isOccupied());
        
        cell.clear();
        assertFalse(cell.isOccupied());
        assertNull(cell.getBuilding());
    }

    @Test
    public void testCellPositionAndPollutionLevel() {
        assertEquals(5, cell.getPosition().getX());
        assertEquals(10, cell.getPosition().getY());
        assertEquals(0, cell.getPollutionLevel());

        cell.setPollutionLevel(7);
        assertEquals(7, cell.getPollutionLevel());
    }

    @Test
    public void testSetBuildingToNull() {
        cell.setBuilding(building);
        assertTrue(cell.isOccupied());
        
        cell.setBuilding(null);
        assertFalse(cell.isOccupied());
        assertNull(cell.getBuilding());
    }

    @Test
    public void testMultipleSetBuildingOperations() {
        BuildingDescription desc2 = new BuildingDescription("Second Building", 200, 100, new Dimension(1, 1));
        BuildingInstance building2 = new BuildingInstance(desc2, 5, 10);

        cell.setBuilding(building);
        assertEquals(building, cell.getBuilding());

        cell.setBuilding(building2);
        assertEquals(building2, cell.getBuilding());

        cell.clear();
        assertNull(cell.getBuilding());
    }
}
