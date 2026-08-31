package kfclash.citylogic.policies;

import java.math.BigDecimal;

import kfclash.citylogic.domain.core.ResourceDelta;
import kfclash.citylogic.ports.IBuildingState;
import kfclash.citylogic.ports.IGridReadPort;

/**
 * Ordinanza "Sussidio Verde": il comune finanzia i parchi alimentati.
 * L'abbattimento di inquinamento cresce con il numero di edifici inquinanti
 * adiacenti, quindi un parco fra le fabbriche vale più di uno isolato.
 */
public final class GreenSubsidiesPolicy extends AbstractPolicyStrategy {

    public static final String POLICY_NAME = "Sussidio Verde";
    public static final BigDecimal DEFAULT_SUBSIDY_PER_PARK = new BigDecimal("15.00");
    public static final double DEFAULT_BASE_ABATEMENT = 1.0;
    public static final double DEFAULT_ABATEMENT_PER_NEIGHBOUR = 1.5;
    public static final double DEFAULT_HAPPINESS_BONUS = 0.25;

    private final BigDecimal subsidyPerPark;
    private final double baseAbatement;
    private final double abatementPerNeighbour;
    private final double happinessBonus;

    public GreenSubsidiesPolicy() {
        this(DEFAULT_SUBSIDY_PER_PARK, DEFAULT_BASE_ABATEMENT,
                DEFAULT_ABATEMENT_PER_NEIGHBOUR, DEFAULT_HAPPINESS_BONUS);
    }

    public GreenSubsidiesPolicy(BigDecimal subsidyPerPark, double baseAbatement,
            double abatementPerNeighbour, double happinessBonus) {
        super(POLICY_NAME);
        if (subsidyPerPark == null) {
            throw new IllegalArgumentException("subsidyPerPark cannot be null");
        }
        if (subsidyPerPark.signum() < 0 || baseAbatement < 0
                || abatementPerNeighbour < 0 || happinessBonus < 0) {
            throw new IllegalArgumentException("Policy parameters cannot be negative");
        }
        this.subsidyPerPark = subsidyPerPark;
        this.baseAbatement = baseAbatement;
        this.abatementPerNeighbour = abatementPerNeighbour;
        this.happinessBonus = happinessBonus;
    }

    @Override
    protected boolean appliesTo(IBuildingState building) {
        return BuildingTypes.isAnyOf(building, BuildingTypes.GREEN);
    }

    @Override
    protected ResourceDelta doCalculate(IBuildingState building, IGridReadPort grid) {
        int pollutingNeighbours = GridNeighborhood.countAdjacentOfType(
                building, grid, BuildingTypes.POLLUTING);
        double abatement = baseAbatement + abatementPerNeighbour * pollutingNeighbours;

        // Delta negativo: il floor a zero dell'inquinamento è del CityAggregate
        return new ResourceDelta(subsidyPerPark.negate(), -abatement, 0, happinessBonus);
    }

    public BigDecimal getSubsidyPerPark() {
        return subsidyPerPark;
    }

    public double getBaseAbatement() {
        return baseAbatement;
    }

    public double getAbatementPerNeighbour() {
        return abatementPerNeighbour;
    }

    public double getHappinessBonus() {
        return happinessBonus;
    }
}
