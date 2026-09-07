package kfclash.citylogic.persistence;

import java.math.BigDecimal;

/** Persistence DTO for city metrics, separate from the domain snapshot type. */
public record CitySnapshotSaveData(
        BigDecimal budget,
        double pollution,
        int population,
        double happiness,
        int tickCount) {

}
