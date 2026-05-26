package kfclash.citylogic.model;

public class BuildingDescription {
    private final String name;
    private final int constructionCost;
    private final int baseMaintenanceCost;
    private final Dimension footprint;

    public BuildingDescription(String name, int constructionCost, int baseMaintenanceCost, Dimension footprint) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("BuildingDescription name cannot be null or blank");
        }
        if (constructionCost < 0 || baseMaintenanceCost < 0) {
            throw new IllegalArgumentException("Costs cannot be negative");
        }
        if (footprint == null) {
            throw new IllegalArgumentException("Footprint cannot be null");
        }
        this.name = name;
        this.constructionCost = constructionCost;
        this.baseMaintenanceCost = baseMaintenanceCost;
        this.footprint = footprint;
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
}
