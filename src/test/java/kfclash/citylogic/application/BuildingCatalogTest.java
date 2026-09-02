package kfclash.citylogic.application;

import kfclash.citylogic.domain.buildings.BuildingDescription;
import kfclash.citylogic.domain.core.ResourceDelta;
import kfclash.citylogic.domain.map.Dimension;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BuildingCatalogTest {
    @Test
    public void internReturnsSameInstanceForSameTypeId() {
        BuildingCatalog catalog = new BuildingCatalog();
        BuildingDescription a = new BuildingDescription("TestBuilding", 10, 1,
                new Dimension(1, 1), ResourceDelta.zero());
        BuildingDescription b = new BuildingDescription("TestBuilding", 10, 1,
                new Dimension(1, 1), ResourceDelta.zero());

        BuildingDescription sa = catalog.intern(a);
        BuildingDescription sb = catalog.intern(b);

        assertTrue(sa == sb, "Interned instances should be identical (flyweight)");
    }

    @Test
    public void getByTypeIdFindsRegisteredDescription() {
        BuildingCatalog catalog = new BuildingCatalog();
        BuildingDescription c = new BuildingDescription("UniqueBuilding", 20, 2, new Dimension(1, 1));
        catalog.register(c);

        BuildingDescription found = catalog.getByTypeId(c.getTypeId()).orElse(null);
        assertNotNull(found);
        assertEquals(c.getTypeId(), found.getTypeId());
    }
}
