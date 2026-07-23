package kfclash.citylogic.application;

import kfclash.citylogic.domain.buildings.BuildingDescription;
import kfclash.citylogic.domain.map.Dimension;
import kfclash.citylogic.domain.map.Resource;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class BuildingCatalogTest {
    @Test
    public void internReturnsSameInstanceForSameTypeId() {
        BuildingCatalog catalog = BuildingCatalog.getInstance();
        BuildingDescription a = new BuildingDescription("TestBuilding", 10, 1, new Dimension(1,1), List.of(new Resource("r",1)));
        BuildingDescription b = new BuildingDescription("TestBuilding", 10, 1, new Dimension(1,1), List.of(new Resource("r",1)));

        BuildingDescription sa = catalog.intern(a);
        BuildingDescription sb = catalog.intern(b);

        Assert.assertTrue("Interned instances should be identical (flyweight)", sa == sb);
    }

    @Test
    public void getByTypeIdFindsRegisteredDescription() {
        BuildingCatalog catalog = BuildingCatalog.getInstance();
        BuildingDescription c = new BuildingDescription("UniqueBuilding", 20, 2, new Dimension(1,1));
        catalog.register(c);

        BuildingDescription found = catalog.getByTypeId(c.getTypeId()).orElse(null);
        Assert.assertNotNull(found);
        Assert.assertEquals(c.getTypeId(), found.getTypeId());
    }
}
