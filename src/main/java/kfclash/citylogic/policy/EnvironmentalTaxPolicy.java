package kfclash.citylogic.policies;

import java.math.BigDecimal;

import kfclash.citylogic.domain.core.ResourceDelta;
import kfclash.citylogic.ports.IBuildingState;
import kfclash.citylogic.ports.IGridReadPort;

/**
 * Ordinanza "Tassa Ambientale": per ogni edificio inquinante alimentato
 * incassa un gettito fisso e paga in felicità. Non altera le emissioni.
 */
public final class EnvironmentalTaxPolicy extends AbstractPolicyStrategy {

    public static final String POLICY_NAME = "Tassa Ambientale";
    public static final BigDecimal DEFAULT_TAX_PER_BUILDING = new BigDecimal("25.00");
    public static final double DEFAULT_HAPPINESS_PENALTY = 0.15;

    private final BigDecimal taxPerBuilding;
    private final double happinessPenalty;

    public EnvironmentalTaxPolicy() {
        this(DEFAULT_TAX_PER_BUILDING, DEFAULT_HAPPINESS_PENALTY);
    }

    /** I parametri sono iniettabili per il bilanciamento e per i test. */
    public EnvironmentalTaxPolicy(BigDecimal taxPerBuilding, double happinessPenalty) {
        super(POLICY_NAME);
        if (taxPerBuilding == null) {
            throw new IllegalArgumentException("taxPerBuilding cannot be null");
        }
        if (taxPerBuilding.signum() < 0 || happinessPenalty < 0) {
            throw new IllegalArgumentException("Policy parameters cannot be negative");
        }
        this.taxPerBuilding = taxPerBuilding;
        this.happinessPenalty = happinessPenalty;
    }

    @Override
    protected boolean appliesTo(IBuildingState building) {
        return BuildingTypes.isAnyOf(building, BuildingTypes.POLLUTING);
    }

    @Override
    protected ResourceDelta doCalculate(IBuildingState building, IGridReadPort grid) {
        return new ResourceDelta(taxPerBuilding, 0.0, 0, -happinessPenalty);
    }

    public BigDecimal getTaxPerBuilding() {
        return taxPerBuilding;
    }

    public double getHappinessPenalty() {
        return happinessPenalty;
    }
}
