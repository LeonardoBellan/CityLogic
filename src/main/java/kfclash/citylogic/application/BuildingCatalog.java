package kfclash.citylogic.application;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import kfclash.citylogic.domain.buildings.BuildingDescription;

/**
 * Application-level registry for user-selectable building descriptions.
 */
public final class BuildingCatalog {
    private final Map<String, BuildingDescription> byTypeId = new ConcurrentHashMap<>();

    public BuildingCatalog() {
    }

    public BuildingDescription intern(BuildingDescription description) {
        if (description == null) {
            throw new IllegalArgumentException("description cannot be null");
        }
        return byTypeId.computeIfAbsent(description.getTypeId(), key -> description);
    }

    public Optional<BuildingDescription> getByTypeId(String typeId) {
        if (typeId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(byTypeId.get(typeId));
    }

    public Collection<BuildingDescription> listAll() {
        return Collections.unmodifiableCollection(byTypeId.values());
    }

    public void register(BuildingDescription description) {
        intern(description);
    }
}
