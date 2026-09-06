package kfclash.citylogic.persistence;

/** Serializable placement data for one building. */
public record BuildingSaveData(
        String typeId,
        int x,
        int y,
        boolean powered) {

    public BuildingSaveData {
        if (typeId == null || typeId.isBlank()) {
            throw new IllegalArgumentException("typeId cannot be null or blank");
        }
    }
}
