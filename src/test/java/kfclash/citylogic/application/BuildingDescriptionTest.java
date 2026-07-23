package kfclash.citylogic.application;

import kfclash.citylogic.domain.buildings.BuildingDescription;
import kfclash.citylogic.domain.map.Dimension;
import kfclash.citylogic.domain.map.Resource;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class BuildingDescriptionTest {

    @Test
    public void testBuildingDescriptionCreation() {
        Dimension footprint = new Dimension(3, 3);
        BuildingDescription desc = new BuildingDescription("Warehouse", 500, 100, footprint);

        assertEquals("Warehouse", desc.getName());
        assertEquals(500, desc.getConstructionCost());
        assertEquals(100, desc.getBaseMaintenanceCost());
        assertEquals(footprint, desc.getFootprint());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildingDescriptionWithNullName() {
        Dimension footprint = new Dimension(2, 2);
        new BuildingDescription(null, 100, 50, footprint);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildingDescriptionWithBlankName() {
        Dimension footprint = new Dimension(2, 2);
        new BuildingDescription("   ", 100, 50, footprint);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildingDescriptionWithNegativeConstructionCost() {
        Dimension footprint = new Dimension(2, 2);
        new BuildingDescription("Building", -100, 50, footprint);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildingDescriptionWithNegativeMaintenanceCost() {
        Dimension footprint = new Dimension(2, 2);
        new BuildingDescription("Building", 100, -50, footprint);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildingDescriptionWithNullFootprint() {
        new BuildingDescription("Building", 100, 50, null);
    }

    @Test
    public void testBuildingDescriptionWithZeroCosts() {
        Dimension footprint = new Dimension(1, 1);
        BuildingDescription desc = new BuildingDescription("FreeBuilding", 0, 0, footprint);

        assertEquals("FreeBuilding", desc.getName());
        assertEquals(0, desc.getConstructionCost());
        assertEquals(0, desc.getBaseMaintenanceCost());
    }

    @Test
    public void testBuildingDescriptionWithLargeCosts() {
        Dimension footprint = new Dimension(5, 5);
        BuildingDescription desc = new BuildingDescription("Expensive", 1000000, 50000, footprint);

        assertEquals(1000000, desc.getConstructionCost());
        assertEquals(50000, desc.getBaseMaintenanceCost());
    }

    @Test
    public void testBuildingDescriptionGeneratesIdAndEmptyProductionByDefault() {
        Dimension footprint = new Dimension(2, 2);
        BuildingDescription desc = new BuildingDescription("Storage", 100, 20, footprint);

        assertNotNull(desc.getTypeId());
        assertFalse(desc.getTypeId().isBlank());
        assertNotNull(desc.getBaseProduction());
        assertTrue(desc.getBaseProduction().isEmpty());
    }

    @Test
    public void testBuildingDescriptionBaseProductionImmutability() {
        Dimension footprint = new Dimension(2, 2);
        List<Resource> production = Collections.singletonList(new Resource("energy", 10));
        BuildingDescription desc = new BuildingDescription("PowerPlant", 500, 100, footprint, production);

        try {
            desc.getBaseProduction().add(new Resource("water", 5));
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildingDescriptionWithNullBaseProduction() {
        Dimension footprint = new Dimension(2, 2);
        new BuildingDescription("Invalid", 100, 20, footprint, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildingDescriptionWithEmptyName() {
        Dimension footprint = new Dimension(2, 2);
        new BuildingDescription("", 100, 50, footprint);
    }
}
