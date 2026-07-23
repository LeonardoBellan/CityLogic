package kfclash.citylogic.domain.buildings;

import java.util.Collections;
import java.util.List;

import kfclash.citylogic.domain.map.Dimension;
import kfclash.citylogic.domain.map.Resource;

public final class BuildingDescription {
    private final String typeId;
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
        this.typeId = normalizeTypeId(name);
        this.name = name;
        this.constructionCost = constructionCost;
        this.baseMaintenanceCost = baseMaintenanceCost;
        this.footprint = footprint;
        this.baseProduction = Collections.unmodifiableList(baseProduction);
    }

    public String getTypeId() {
        return typeId;
    }

    private static String normalizeTypeId(String name) {
        return name.trim().toLowerCase().replaceAll("\\s+", "_");
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BuildingDescription)) {
            return false;
        }
        BuildingDescription other = (BuildingDescription) obj;
        return typeId.equals(other.typeId);
    }

    @Override
    public int hashCode() {
        return typeId.hashCode();
    }

    @Override
    public String toString() {
        return "BuildingDescription{" + "typeId='" + typeId + '\'' + ", name='" + name + '\'' + '}';
    }
}
