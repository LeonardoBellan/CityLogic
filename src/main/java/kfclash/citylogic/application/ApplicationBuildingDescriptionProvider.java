package kfclash.citylogic.application;

import java.math.BigDecimal;

import kfclash.citylogic.domain.buildings.BuildingDescription;
import kfclash.citylogic.domain.core.ResourceDelta;
import kfclash.citylogic.domain.map.Dimension;

/**
 
Registers a set of standard BuildingDescription instances into the
BuildingCatalog.*/
public final class ApplicationBuildingDescriptionProvider {
    public static void initDefaultCatalog(BuildingCatalog catalog) {
        if (catalog == null) {
            throw new IllegalArgumentException("catalog cannot be null");
        }

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

            // Commercial Hub: generates municipal revenue.
            BuildingDescription commercial = new BuildingDescription(
                "Commercial", 
                500, 
                3, 
                new Dimension(1, 1),
                new ResourceDelta(new BigDecimal("75.00"), 0.0, 0, 0.5)
            );
            catalog.register(commercial);

            // Power Plant: provides the infrastructure source for future power rules.
            BuildingDescription powerPlant = new BuildingDescription(
                "Power Plant", 
                2000, 
                10, 
                new Dimension(2, 2),
                new ResourceDelta(new BigDecimal("-50.00"), 8.0, 0, -0.5)
            );
            catalog.register(powerPlant);

            // Road: infrastructure occupying one grid cell without a tick effect.
            BuildingDescription road = new BuildingDescription(
                "Road", 
                50, 
                1, 
                new Dimension(1, 1),
                ResourceDelta.zero()
            );
            catalog.register(road);
    }

    private ApplicationBuildingDescriptionProvider() {
    }
}