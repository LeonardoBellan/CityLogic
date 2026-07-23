package kfclash.citylogic.domain;

import kfclash.citylogic.domain.buildings.BuildingDescription;
import kfclash.citylogic.domain.buildings.BuildingInstance;
import kfclash.citylogic.domain.map.Dimension;
import kfclash.citylogic.domain.map.Point;
import kfclash.citylogic.domain.map.Resource;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class BuildingInstanceTest {

    private BuildingDescription description;
    private BuildingInstance building;

    @Before
    public void setUp() {
        Dimension footprint = new Dimension(2, 3);
        description = new BuildingDescription("Office", 1000, 200, footprint);
        building = new BuildingInstance(description, 5, 10);
    }

    @Test
    public void testBuildingInstanceCreation() {
        assertEquals(description, building.getDescription());
        assertEquals(5, building.getX());
        assertEquals(10, building.getY());
        assertTrue(building.isOperational());
        assertEquals(200, building.getCurrentMaintenanceCost());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildingInstanceWithNullDescription() {
        new BuildingInstance(null, 0, 0);
    }

    @Test
    public void testBuildingInstanceInitiallyOperational() {
        assertTrue(building.isOperational());
    }

    @Test
    public void testSetBuildingNotOperational() {
        building.setPowered(false);
        assertFalse(building.isOperational());
    }

    @Test
    public void testSetBuildingOperational() {
        building.setPowered(false);
        assertFalse(building.isOperational());

        building.setPowered(true);
        assertTrue(building.isOperational());
    }

    @Test
    public void testBuildingStateInterfaces() {
        assertNotNull(building.getId());
        assertFalse(building.getId().isBlank());
        assertEquals("Office", building.getType());
        assertTrue(building.isPowered());
    }

    @Test
    public void testCalculateCurrentProductionReturnsBaseProduction() {
        Dimension footprint = new Dimension(1, 1);
        BuildingDescription desc = new BuildingDescription("Plant", 500, 100, footprint, Collections.singletonList(new Resource("energy", 10)));
        BuildingInstance instance = new BuildingInstance(desc, 1, 1);

        List<Resource> production = instance.calculateCurrentProduction();
        assertNotNull(production);
        assertEquals(1, production.size());
        assertEquals(new Resource("energy", 10), production.get(0));
    }

    @Test
    public void testGetPositionFromBuildingInstance() {
        Point position = building.getPosition();
        assertEquals(5, position.getX());
        assertEquals(10, position.getY());
    }

    @Test
    public void testBuildingCoordinates() {
        BuildingDescription desc = new BuildingDescription("Shop", 500, 100, new Dimension(1, 1));
        BuildingInstance building = new BuildingInstance(desc, 15, 20);

        assertEquals(15, building.getX());
        assertEquals(20, building.getY());
    }

    @Test
    public void testBuildingMaintenanceCostMatchesDescription() {
        Dimension footprint = new Dimension(1, 1);
        BuildingDescription desc = new BuildingDescription("Maintenance Test", 300, 75, footprint);
        BuildingInstance instance = new BuildingInstance(desc, 0, 0);

        assertEquals(75, instance.getCurrentMaintenanceCost());
    }

    @Test
    public void testBuildingWithZeroCoordinates() {
        BuildingDescription desc = new BuildingDescription("Origin Building", 100, 50, new Dimension(1, 1));
        BuildingInstance instance = new BuildingInstance(desc, 0, 0);

        assertEquals(0, instance.getX());
        assertEquals(0, instance.getY());
    }

    @Test
    public void testBuildingWithNegativeCoordinates() {
        BuildingDescription desc = new BuildingDescription("Negative Coords", 100, 50, new Dimension(1, 1));
        BuildingInstance instance = new BuildingInstance(desc, -5, -10);

        assertEquals(-5, instance.getX());
        assertEquals(-10, instance.getY());
    }

    @Test
    public void testMultipleOperationalStatusToggles() {
        assertTrue(building.isOperational());

        building.setPowered(false);
        assertFalse(building.isOperational());

        building.setPowered(true);
        assertTrue(building.isOperational());

        building.setPowered(false);
        assertFalse(building.isOperational());
    }
}
