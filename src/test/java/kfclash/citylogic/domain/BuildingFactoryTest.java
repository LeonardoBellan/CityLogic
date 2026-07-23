package kfclash.citylogic.domain;

import org.junit.Before;
import org.junit.Test;

import kfclash.citylogic.domain.buildings.BuildingDescription;
import kfclash.citylogic.domain.buildings.BuildingFactory;
import kfclash.citylogic.domain.buildings.BuildingInstance;
import kfclash.citylogic.domain.map.Dimension;

import static org.junit.Assert.*;

public class BuildingFactoryTest {

    private BuildingFactory factory;
    private BuildingDescription description;

    @Before
    public void setUp() {
        factory = new BuildingFactory();
        Dimension footprint = new Dimension(2, 2);
        description = new BuildingDescription("Factory Test Building", 500, 100, footprint);
    }

    @Test
    public void testCreateBuilding() {
        BuildingInstance building = factory.createBuilding(description, 5, 10);

        assertNotNull(building);
        assertEquals(description, building.getDescription());
        assertEquals(5, building.getPosition().getX());
        assertEquals(10, building.getPosition().getY());
    }

    @Test
    public void testCreateBuildingWithDifferentCoordinates() {
        BuildingInstance building1 = factory.createBuilding(description, 0, 0);
        BuildingInstance building2 = factory.createBuilding(description, 100, 100);

        assertEquals(0, building1.getPosition().getX());
        assertEquals(0, building1.getPosition().getY());
        assertEquals(100, building2.getPosition().getX());
        assertEquals(100, building2.getPosition().getY());
    }

    @Test
    public void testCreateMultipleBuildings() {
        BuildingInstance building1 = factory.createBuilding(description, 1, 2);
        BuildingInstance building2 = factory.createBuilding(description, 3, 4);
        BuildingInstance building3 = factory.createBuilding(description, 5, 6);

        assertNotSame(building1, building2);
        assertNotSame(building2, building3);
        assertNotSame(building1, building3);

        assertEquals(description, building1.getDescription());
        assertEquals(description, building2.getDescription());
        assertEquals(description, building3.getDescription());
    }

    @Test
    public void testCreateBuildingIsPoweredByDefault() {
        BuildingInstance building = factory.createBuilding(description, 0, 0);
        assertTrue(building.isPowered());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBuildingWithNullDescription() {
        factory.createBuilding(null, 0, 0);
    }

    @Test
    public void testCreateBuildingWithDifferentDescriptions() {
        Dimension footprint1 = new Dimension(1, 1);
        Dimension footprint2 = new Dimension(3, 3);
        BuildingDescription desc1 = new BuildingDescription("Small", 100, 50, footprint1);
        BuildingDescription desc2 = new BuildingDescription("Large", 500, 200, footprint2);

        BuildingInstance building1 = factory.createBuilding(desc1, 0, 0);
        BuildingInstance building2 = factory.createBuilding(desc2, 0, 0);

        assertEquals(desc1, building1.getDescription());
        assertEquals(desc2, building2.getDescription());
        assertEquals(50, building1.getCurrentMaintenanceCost());
        assertEquals(200, building2.getCurrentMaintenanceCost());
    }
}
