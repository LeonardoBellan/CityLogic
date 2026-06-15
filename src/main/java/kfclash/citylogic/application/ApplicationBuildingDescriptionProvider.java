package kfclash.citylogic.application;

import kfclash.citylogic.domain.BuildingDescription;
import kfclash.citylogic.domain.Dimension;
import kfclash.citylogic.domain.Resource;

import java.util.List;

/**
 * Registers a set of standard BuildingDescription instances into the BuildingCatalog.
 * This class triggers registration via its static initializer; call explicitly from
 * application startup if you prefer explicit control.
 */
public final class ApplicationBuildingDescriptionProvider {
    static {
        initDefaultCatalog();
    }

    public static void initDefaultCatalog() {
        BuildingCatalog catalog = BuildingCatalog.getInstance();

        BuildingDescription house = new BuildingDescription("House", 100, 1, new Dimension(1, 1),
                List.of(new Resource("population", 4)));
        catalog.register(house);

        BuildingDescription factory = new BuildingDescription("Factory", 1000, 5, new Dimension(2, 2),
                List.of(new Resource("goods", 10)));
        catalog.register(factory);

        BuildingDescription park = new BuildingDescription("Park", 150, 0, new Dimension(1, 1),
                List.of(new Resource("happiness", 2)));
        catalog.register(park);
    }

    private ApplicationBuildingDescriptionProvider() {
    }
}
