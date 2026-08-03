package kfclash.citylogic.domain.buildings;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import kfclash.citylogic.domain.core.ResourceDelta;
import kfclash.citylogic.domain.map.Dimension;
import kfclash.citylogic.domain.map.Resource;

public final class BuildingDescription {
    private final String typeId;
    private final String name;
    private final int constructionCost;
    private final int baseMaintenanceCost;
    private final Dimension footprint;
    private final ResourceDelta baseProduction;
    private final List<Resource> legacyResources;

    public BuildingDescription(String name, int constructionCost, int baseMaintenanceCost, Dimension footprint) {
        this(name, constructionCost, baseMaintenanceCost, footprint, ResourceDelta.zero(), List.of());
    }

    public BuildingDescription(String name, int constructionCost, int baseMaintenanceCost, Dimension footprint, ResourceDelta baseProduction) {
        this(name, constructionCost, baseMaintenanceCost, footprint, Objects.requireNonNull(baseProduction, "baseProduction cannot be null"), List.of());
    }

    /** Compatibility constructor used by older tests that still pass a legacy resource list. */
    public BuildingDescription(String name, int constructionCost, int baseMaintenanceCost, Dimension footprint, List<Resource> production) {
        this(name, constructionCost, baseMaintenanceCost, footprint,
                ResourceDelta.zero(), List.copyOf(production == null ? List.of() : production));
    }

    public BuildingDescription(String name, int constructionCost, int baseMaintenanceCost, Dimension footprint, ResourceDelta baseProduction, List<Resource> legacyResources) {
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
        this.legacyResources = legacyResources == null ? List.of() : List.copyOf(legacyResources);
        this.typeId = normalizeTypeId(name);
        this.name = name;
        this.constructionCost = constructionCost;
        this.baseMaintenanceCost = baseMaintenanceCost;
        this.footprint = footprint;
        this.baseProduction = baseProduction;
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

    public ResourceDelta getBaseProduction() {
        return baseProduction;
    }

    public List<Resource> getLegacyResources() {
        return Collections.unmodifiableList(legacyResources);
    }

    public List<Resource> getProduction() {
        return getLegacyResources();
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
