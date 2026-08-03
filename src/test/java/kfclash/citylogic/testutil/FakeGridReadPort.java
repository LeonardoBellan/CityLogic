package kfclash.citylogic.testutil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import kfclash.citylogic.domain.core.ResourceDelta;
import kfclash.citylogic.domain.map.Dimension;
import kfclash.citylogic.domain.map.Point;
import kfclash.citylogic.ports.IBuildingState;
import kfclash.citylogic.ports.IGridReadPort;

/**
 * Stub in memoria di {@link IGridReadPort} per i test del Core.
 * <p>
 * Permette di testare fasi, policy ed engine <b>senza il modulo mappa</b>:
 * si popolano edifici finti con {@link #addBuilding} e lo stub risponde
 * alle query del port. Implementa anche l'adiacenza (distanza di Chebyshev)
 * per testare le politiche spaziali.
 * <p>
 * Vive in {@code src/test/java}: non fa parte del prodotto consegnato.
 */
public class FakeGridReadPort implements IGridReadPort {

    /**
     * Edificio finto: implementa {@link IBuildingState} come record, con in
     * più le coordinate di cella per il calcolo dell'adiacenza.
     */
    public record FakeBuilding(
            String id,
            String type,
            boolean powered,
            ResourceDelta baseProduction,
            int x,
            int y) implements IBuildingState {

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public boolean isPowered() {
            return powered;
        }

        @Override
        public ResourceDelta getBaseProduction() {
            return baseProduction;
        }

        @Override
        public ResourceDelta getCurrentProduction() {
            return baseProduction;
        }

        @Override
        public Point getPosition() {
            return new Point(x, y);
        }

        @Override
        public kfclash.citylogic.domain.buildings.BuildingDescription getDescription() {
            return null;
        }
    }

    private final List<FakeBuilding> buildings = new ArrayList<>();

    /**
     * Aggiunge un edificio finto alla griglia.
     *
     * @return {@code this} per concatenare le chiamate (fluent)
     */
    public FakeGridReadPort addBuilding(String id, String type, boolean powered,
            ResourceDelta baseProduction, int x, int y) {
        buildings.add(new FakeBuilding(id, type, powered, baseProduction, x, y));
        return this;
    }

    @Override
    public Optional<IBuildingState> getBuildingById(String id) {
        return buildings.stream()
                .filter(b -> b.id().equals(id))
                .map(b -> (IBuildingState) b)
                .findFirst();
    }

    @Override
    public List<IBuildingState> getAllBuildings() {
        return new ArrayList<>(buildings);
    }

    @Override
    public String getTerrainAt(int x, int y) {
        return "land";
    }

    @Override
    public boolean isAreaFree(int x, int y, Dimension footprint) {
        return true;
    }

    @Override
    public List<IBuildingState> getAdjacentBuildings(String id, int radius) {
        Optional<FakeBuilding> center = buildings.stream()
                .filter(b -> b.id().equals(id))
                .findFirst();
        if (center.isEmpty()) {
            return List.of();
        }
        int cx = center.get().x();
        int cy = center.get().y();

        // Distanza di Chebyshev: max(|dx|, |dy|) <= radius, escluso il centro
        return buildings.stream()
                .filter(b -> !b.id().equals(id))
                .filter(b -> Math.max(Math.abs(b.x() - cx), Math.abs(b.y() - cy)) <= radius)
                .map(b -> (IBuildingState) b)
                .toList();
    }
}