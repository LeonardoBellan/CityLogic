package kfclash.citylogic.domain.policies;

import java.util.List;
import java.util.Set;

import kfclash.citylogic.ports.IBuildingState;
import kfclash.citylogic.ports.IGridReadPort;

/**
 * Query di vicinato usate dalle ordinanze spaziali.
 * <p>
 * È l'unico punto del modulo che dipende dalla forma dell'API di
 * {@link IGridReadPort}: se il port cambia, le policy restano invariate.
 */
public final class GridNeighborhood {

    /** Raggio di adiacenza: 1 = le 8 celle confinanti (distanza di Chebyshev). */
    public static final int ADJACENCY_RADIUS = 1;

    private GridNeighborhood() {
    }

    public static int countAdjacentOfType(IBuildingState center, IGridReadPort grid, Set<String> categories) {
        List<IBuildingState> neighbours = grid.getAdjacentBuildings(center.getId(), ADJACENCY_RADIUS);
        int count = 0;
        for (IBuildingState neighbour : neighbours) {
            if (BuildingTypes.isAnyOf(neighbour, categories)) {
                count++;
            }
        }
        return count;
    }

    public static boolean hasAdjacentOfType(IBuildingState center, IGridReadPort grid, Set<String> categories) {
        return countAdjacentOfType(center, grid, categories) > 0;
    }
}
