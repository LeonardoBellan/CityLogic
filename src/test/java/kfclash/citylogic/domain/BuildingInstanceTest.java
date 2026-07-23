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
        assertEquals(5, building.getPosition().getX());
        assertEquals(10, building.getPosition().getY());
        assertTrue(building.isPowered());
        assertEquals(200, building.getCurrentMaintenanceCost());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildingInstanceWithNullDescription() {
        new BuildingInstance(null, 0, 0);
    }

    @Test
    public void testBuildingInstanceInitiallyPowered() {
        assertTrue(building.isPowered());
    }

    @Test
    public void testSetBuildingNotPowered() {
        building.setPowered(false);
        assertFalse(building.isPowered());
    }

    @Test
    public void testSetBuildingPowered() {
        building.setPowered(false);
        assertFalse(building.isPowered());

        building.setPowered(true);
        assertTrue(building.isPowered());
    }

    @Test
    public void testBuildingStateInterfaces() {
        assertNotNull(building.getId());
        assertFalse(building.getId().isBlank());
        assertEquals("Office", building.getType());
        assertTrue(building.isPowered());
    }

    @Test
    public void testGetCurrentProductionReturnsBaseProduction() {
        Dimension footprint = new Dimension(1, 1);
        BuildingDescription desc = new BuildingDescription("Plant", 500, 100, footprint, Collections.singletonList(new Resource("energy", 10)));
        BuildingInstance instance = new BuildingInstance(desc, 1, 1);

        List<Resource> production = instance.getCurrentProduction();
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

        assertEquals(15, building.getPosition().getX());
        assertEquals(20, building.getPosition().getY());
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

        assertEquals(0, instance.getPosition().getX());
        assertEquals(0, instance.getPosition().getY());
    }

    @Test
    public void testBuildingWithNegativeCoordinates() {
        BuildingDescription desc = new BuildingDescription("Negative Coords", 100, 50, new Dimension(1, 1));
        BuildingInstance instance = new BuildingInstance(desc, -5, -10);

        assertEquals(-5, instance.getPosition().getX());
        assertEquals(-10, instance.getPosition().getY());
    }

    @Test
    public void testMultiplePoweredStatusToggles() {
        assertTrue(building.isPowered());

        building.setPowered(false);
        assertFalse(building.isPowered());

        building.setPowered(true);
        assertTrue(building.isPowered());

        building.setPowered(false);
        assertFalse(building.isPowered());
    }
}
