package kfclash.citylogic.application;

import kfclash.citylogic.domain.Dimension;
import kfclash.citylogic.domain.Resource;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class BuildingDescription {
    private final String id;
    private final String name;
    private final int constructionCost;
    private final int baseMaintenanceCost;
    private final Dimension footprint;
    private final List<Resource> baseProduction;

    public BuildingDescription(String name, int constructionCost, int baseMaintenanceCost, Dimension footprint) {
        this(name, constructionCost, baseMaintenanceCost, footprint, Collections.emptyList());
    }

    public BuildingDescription(String name, int constructionCost, int baseMaintenanceCost, Dimension footprint, List<Resource> baseProduction) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("BuildingDescription name cannot be null or blank");
        }
        if (constructionCost < 0 || baseMaintenanceCost < 0) {
            throw new IllegalArgumentException("Costs cannot be negative");
        }
        if (footprint == null) {
            throw new IllegalArgumentException("Footprint cannot be null");
        }
        if (baseProduction == null) {
            throw new IllegalArgumentException("Base production cannot be null");
        }
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.constructionCost = constructionCost;
        this.baseMaintenanceCost = baseMaintenanceCost;
        this.footprint = footprint;
        this.baseProduction = Collections.unmodifiableList(baseProduction);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getConstructionCost() {
        return constructionCost;
    }

    public int getBaseMaintenanceCost() {
        return baseMaintenanceCost;
    }

    public Dimension getFootprint() {
        return footprint;
    }

    public List<Resource> getBaseProduction() {
        return baseProduction;
    }
}
