package kfclash.citylogic.persistence;

import java.util.List;

/** Serializable representation of a complete city save. */
public record CitySaveData(
    CitySnapshotSaveData snapshot,
        int gridWidth,
        int gridHeight,
        List<BuildingSaveData> buildings) {

    public CitySaveData {
        if (snapshot == null) {
            throw new IllegalArgumentException("snapshot cannot be null");
        }
        if (gridWidth <= 0 || gridHeight <= 0) {
            throw new IllegalArgumentException("Grid dimensions must be positive");
        }
        buildings = buildings == null ? List.of() : List.copyOf(buildings);
    }
}
