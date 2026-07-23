package kfclash.citylogic.application;

import kfclash.citylogic.domain.BuildingDescription;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple flyweight catalog for shared BuildingDescription instances.
 */
public final class BuildingCatalog {
    private static final BuildingCatalog INSTANCE = new BuildingCatalog();
    private final Map<String, BuildingDescription> byTypeId = new ConcurrentHashMap<>();

    private BuildingCatalog() {
    }

    public static BuildingCatalog getInstance() {
        return INSTANCE;
    }

    /**
     * Returns a shared BuildingDescription instance for the same typeId. If a description
     * with the same typeId is already present, it is returned; otherwise the provided
     * description is stored and returned.
     */
    public BuildingDescription intern(BuildingDescription description) {
        if (description == null) {
            throw new IllegalArgumentException("description cannot be null");
        }
        return byTypeId.computeIfAbsent(description.getTypeId(), k -> description);
    }

    public Optional<BuildingDescription> getByTypeId(String typeId) {
        if (typeId == null) return Optional.empty();
        return Optional.ofNullable(byTypeId.get(typeId));
    }

    public Collection<BuildingDescription> listAll() {
        return Collections.unmodifiableCollection(byTypeId.values());
    }

    public void register(BuildingDescription description) {
        intern(description);
    }
}
