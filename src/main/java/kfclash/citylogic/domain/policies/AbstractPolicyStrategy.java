package kfclash.citylogic.domain.policies;

import java.util.Objects;

import kfclash.citylogic.domain.core.ResourceDelta;
import kfclash.citylogic.ports.IBuildingState;
import kfclash.citylogic.ports.IGridReadPort;
import kfclash.citylogic.ports.IPolicyStrategy;

/**
 * Base delle ordinanze concrete (Template Method sopra il contratto Strategy).
 * <p>
 * {@code calculateModifier} è final e garantisce una volta per tutte le regole
 * di {@link IPolicyStrategy}: fail-fast sui null, {@code zero()} se l'edificio
 * non è pertinente, mai {@code null} verso la PolicyEvaluationPhase.
 * Le sottoclassi implementano solo il filtro e la formula.
 */
public abstract class AbstractPolicyStrategy implements IPolicyStrategy {

    private final String name;

    protected AbstractPolicyStrategy(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Policy name cannot be null or blank");
        }
        this.name = name;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final ResourceDelta calculateModifier(IBuildingState building, IGridReadPort grid) {
        Objects.requireNonNull(building, "building non può essere null");
        Objects.requireNonNull(grid, "grid non può essere null");

        if (!appliesTo(building)) {
            return ResourceDelta.zero();
        }
        ResourceDelta delta = doCalculate(building, grid);
        // Rete di sicurezza: un hook difettoso non deve far esplodere merge()
        return delta == null ? ResourceDelta.zero() : delta;
    }

    /** L'ordinanza ha effetto su questo edificio? Solo controlli sul tipo. */
    protected abstract boolean appliesTo(IBuildingState building);

    /** Formula dell'ordinanza: deve essere pura (legge, non muta). */
    protected abstract ResourceDelta doCalculate(IBuildingState building, IGridReadPort grid);

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + name + "]";
    }
}
