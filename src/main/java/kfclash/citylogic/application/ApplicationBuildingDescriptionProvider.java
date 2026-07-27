package kfclash.citylogic.application;

import java.math.BigDecimal;

import kfclash.citylogic.domain.buildings.BuildingDescription;
import kfclash.citylogic.domain.core.ResourceDelta;
import kfclash.citylogic.domain.map.Dimension;

/**
 
Registers a set of standard BuildingDescription instances into the
BuildingCatalog.*/
public final class ApplicationBuildingDescriptionProvider {
    static {
        initDefaultCatalog();
    }

    public static void initDefaultCatalog() {
        BuildingCatalog catalog = BuildingCatalog.getInstance();

        // House: Impatta esclusivamente la popolazione (incremento di +4)
        BuildingDescription house = new BuildingDescription(
                "House", 
                100, 
                1, 
                new Dimension(1, 1),
                new ResourceDelta(BigDecimal.ZERO, 0.0, 4, 0.0) 
        );
        catalog.register(house);

        BuildingDescription factory = new BuildingDescription(
                "Factory", 
                1000, 
                5, 
                new Dimension(2, 2),
                new ResourceDelta(new BigDecimal("150.00"), 10.0, 0, 0.0) 
        );
        catalog.register(factory);

        // Park: Impatta esclusivamente la felicità della città
        BuildingDescription park = new BuildingDescription(
                "Park", 
                150, 
                0, 
                new Dimension(1, 1),
                new ResourceDelta(BigDecimal.ZERO, 0.0, 0, 2.0) 
        );
        catalog.register(park);
    }

    private ApplicationBuildingDescriptionProvider() {
    }
}