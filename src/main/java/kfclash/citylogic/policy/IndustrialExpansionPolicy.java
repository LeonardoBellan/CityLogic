package kfclash.citylogic.policies;

import java.math.BigDecimal;
import java.util.Set;

import kfclash.citylogic.domain.core.ResourceDelta;
import kfclash.citylogic.ports.IBuildingState;
import kfclash.citylogic.ports.IGridReadPort;

/**
 * Ordinanza "Espansione Industriale": incentiva le fabbriche (più entrate,
 * più inquinamento, meno felicità) e fa crescere la popolazione nei
 * residenziali confinanti con un'industria, dove il lavoro è raggiungibile.
 * <p>
 * I due effetti stanno in un'unica ordinanza perché sono le due facce dello
 * stesso trade-off: separarli permetterebbe di prendere i benefici senza i costi.
 */
public final class IndustrialExpansionPolicy extends AbstractPolicyStrategy {

    public static final String POLICY_NAME = "Espansione Industriale";
    public static final BigDecimal DEFAULT_REVENUE_BONUS = new BigDecimal("60.00");
    public static final double DEFAULT_POLLUTION_SURCHARGE = 2.0;
    public static final double DEFAULT_HAPPINESS_PENALTY = 0.10;
    public static final int DEFAULT_POPULATION_GROWTH = 2;

    private static final Set<String> JOB_SOURCES = Set.of(BuildingTypes.INDUSTRIAL);

    private final BigDecimal revenueBonus;
    private final double pollutionSurcharge;
    private final double happinessPenalty;
    private final int populationGrowth;

    public IndustrialExpansionPolicy() {
        this(DEFAULT_REVENUE_BONUS, DEFAULT_POLLUTION_SURCHARGE,
                DEFAULT_HAPPINESS_PENALTY, DEFAULT_POPULATION_GROWTH);
    }

    public IndustrialExpansionPolicy(BigDecimal revenueBonus, double pollutionSurcharge,
            double happinessPenalty, int populationGrowth) {
        super(POLICY_NAME);
        if (revenueBonus == null) {
            throw new IllegalArgumentException("revenueBonus cannot be null");
        }
        if (revenueBonus.signum() < 0 || pollutionSurcharge < 0
                || happinessPenalty < 0 || populationGrowth < 0) {
            throw new IllegalArgumentException("Policy parameters cannot be negative");
        }
        this.revenueBonus = revenueBonus;
        this.pollutionSurcharge = pollutionSurcharge;
        this.happinessPenalty = happinessPenalty;
        this.populationGrowth = populationGrowth;
    }

    @Override
    protected boolean appliesTo(IBuildingState building) {
        return BuildingTypes.is(building, BuildingTypes.INDUSTRIAL)
                || BuildingTypes.is(building, BuildingTypes.RESIDENTIAL);
    }

    @Override
    protected ResourceDelta doCalculate(IBuildingState building, IGridReadPort grid) {
        if (BuildingTypes.is(building, BuildingTypes.INDUSTRIAL)) {
            return new ResourceDelta(revenueBonus, pollutionSurcharge, 0, -happinessPenalty);
        }

        // Residenziale: cresce solo se ha una fabbrica adiacente
        if (!GridNeighborhood.hasAdjacentOfType(building, grid, JOB_SOURCES)) {
            return ResourceDelta.zero();
        }
        return new ResourceDelta(BigDecimal.ZERO, 0.0, populationGrowth, 0.0);
    }

    public BigDecimal getRevenueBonus() {
        return revenueBonus;
    }

    public double getPollutionSurcharge() {
        return pollutionSurcharge;
    }

    public double getHappinessPenalty() {
        return happinessPenalty;
    }

    public int getPopulationGrowth() {
        return populationGrowth;
    }
}
