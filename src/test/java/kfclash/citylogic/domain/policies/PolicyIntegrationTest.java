package kfclash.citylogic.domain.policies;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kfclash.citylogic.domain.core.CityAggregate;
import kfclash.citylogic.domain.core.CitySnapshot;
import kfclash.citylogic.domain.core.ResourceDelta;
import kfclash.citylogic.testutil.FakeGridReadPort;
import kfclash.citylogic.simulation.tick.PolicyEvaluationPhase;

/**
 * Integrazione fra le ordinanze concrete, la {@link PolicyEvaluationPhase} e
 * l'applicazione del delta al {@link CityAggregate}.
 */
class PolicyIntegrationTest {

    private static final double EPS = 1e-9;

    private PolicyEvaluationPhase phase;
    private FakeGridReadPort grid;
    private CitySnapshot snapshot;

    @BeforeEach
    void setUp() {
        phase = new PolicyEvaluationPhase();
        grid = new FakeGridReadPort();
        snapshot = new CitySnapshot(new BigDecimal("1000.00"), 0.0, 100, 50.0, 0);
    }

    private void add(String id, String type, boolean powered, int x, int y) {
        grid.addBuilding(id, type, powered, ResourceDelta.zero(), x, y);
    }

    @Test
    @DisplayName("gli edifici non alimentati non vengono tassati")
    void unpoweredBuildingsAreSkipped() {
        add("f1", "INDUSTRIAL", true, 1, 1);
        add("f2", "INDUSTRIAL", false, 8, 8);
        phase.activatePolicy(new EnvironmentalTaxPolicy());

        assertEquals(0, new BigDecimal("25.00").compareTo(phase.execute(snapshot, grid).budgetDelta()));
    }

    @Test
    @DisplayName("due ordinanze attive: i modificatori si sommano su tutta la griglia")
    void multiplePoliciesAccumulate() {
        add("f1", "INDUSTRIAL", true, 4, 4);
        add("f2", "INDUSTRIAL", true, 6, 6);
        add("p1", "PARK", true, 5, 5);
        phase.activatePolicy(new EnvironmentalTaxPolicy());
        phase.activatePolicy(new GreenSubsidiesPolicy());

        ResourceDelta total = phase.execute(snapshot, grid);

        // tassa 2 x 25.00, sussidio -15.00
        assertEquals(0, new BigDecimal("35.00").compareTo(total.budgetDelta()));
        // parco con 2 fabbriche adiacenti: -(1.0 + 2 x 1.5)
        assertEquals(-4.0, total.pollutionDelta(), EPS);
        assertEquals(-0.05, total.happinessDelta(), EPS); // 2 x -0.15 + 0.25
        assertEquals(List.of("Tassa Ambientale", "Sussidio Verde"), phase.getActivePolicyNames());
    }

    @Test
    @DisplayName("l'ordine di attivazione non cambia il delta (merge commutativa)")
    void activationOrderDoesNotAffectTheResult() {
        add("f1", "INDUSTRIAL", true, 4, 4);
        add("f2", "INDUSTRIAL", true, 6, 6);
        add("p1", "PARK", true, 5, 5);

        PolicyEvaluationPhase taxFirst = new PolicyEvaluationPhase();
        taxFirst.activatePolicy(new EnvironmentalTaxPolicy());
        taxFirst.activatePolicy(new GreenSubsidiesPolicy());

        PolicyEvaluationPhase greenFirst = new PolicyEvaluationPhase();
        greenFirst.activatePolicy(new GreenSubsidiesPolicy());
        greenFirst.activatePolicy(new EnvironmentalTaxPolicy());

        assertEquals(taxFirst.execute(snapshot, grid), greenFirst.execute(snapshot, grid));
    }

    @Test
    @DisplayName("ordinanze opposte possono coesistere e i loro effetti si sommano")
    void opposingEconomicPoliciesCoexist() {
        add("f1", "INDUSTRIAL", true, 1, 1);
        phase.activatePolicy(new EnvironmentalTaxPolicy());
        phase.activatePolicy(new IndustrialExpansionPolicy());

        ResourceDelta total = phase.execute(snapshot, grid);

        assertEquals(0, new BigDecimal("85.00").compareTo(total.budgetDelta()));
        assertEquals(2.0, total.pollutionDelta(), EPS);
        assertEquals(-0.25, total.happinessDelta(), EPS);
    }

    @Test
    @DisplayName("cambio di strategia a runtime: da tassa ad espansione")
    void strategySwapAtRuntime() {
        add("f1", "INDUSTRIAL", true, 1, 1);

        phase.activatePolicy(new EnvironmentalTaxPolicy());
        ResourceDelta withTax = phase.execute(snapshot, grid);

        phase.deactivatePolicy(EnvironmentalTaxPolicy.POLICY_NAME);
        phase.activatePolicy(new IndustrialExpansionPolicy());
        ResourceDelta withExpansion = phase.execute(snapshot, grid);

        assertEquals(0, new BigDecimal("25.00").compareTo(withTax.budgetDelta()));
        assertEquals(0.0, withTax.pollutionDelta(), EPS);
        assertEquals(0, new BigDecimal("60.00").compareTo(withExpansion.budgetDelta()));
        assertEquals(2.0, withExpansion.pollutionDelta(), EPS);
        assertEquals(List.of("Espansione Industriale"), phase.getActivePolicyNames());
    }

    @Test
    @DisplayName("disattivare un'ordinanza ne annulla l'effetto dal tick successivo")
    void deactivationRemovesTheEffect() {
        add("f1", "INDUSTRIAL", true, 1, 1);
        phase.activatePolicy(new EnvironmentalTaxPolicy());
        assertFalse(phase.execute(snapshot, grid).isEmpty());

        phase.deactivatePolicy(EnvironmentalTaxPolicy.POLICY_NAME);

        assertTrue(phase.execute(snapshot, grid).isEmpty());
    }

    @Test
    @DisplayName("il delta accumulato aggiorna le metriche del CityAggregate")
    void accumulatedDeltaIsAppliedToTheAggregate() {
        add("f1", "INDUSTRIAL", true, 4, 4);
        add("h1", "RESIDENTIAL", true, 4, 5);
        phase.activatePolicy(new IndustrialExpansionPolicy());
        CityAggregate city = new CityAggregate(new BigDecimal("1000.00"), 100, 50.0);

        city.applyDelta(phase.execute(snapshot, grid));
        CitySnapshot after = city.exportSnapshot();

        assertEquals(0, new BigDecimal("1060.00").compareTo(after.budget()));
        assertEquals(2.0, after.pollution(), EPS);
        assertEquals(102, after.population()); // il residenziale confinante attira 2 abitanti
        assertEquals(1, after.tickCount());
    }

    @Test
    @DisplayName("edge case: un sussidio insostenibile viola l'invariante di bancarotta")
    void unsustainableSubsidyBreaksTheBudgetInvariant() {
        add("p1", "PARK", true, 1, 1);
        add("p2", "PARK", true, 4, 4);
        add("p3", "PARK", true, 8, 8);
        phase.activatePolicy(new GreenSubsidiesPolicy(new BigDecimal("5000.00"), 1.0, 1.5, 0.25));
        CityAggregate city = new CityAggregate(BigDecimal.ZERO, 100, 50.0);
        ResourceDelta total = phase.execute(snapshot, grid);

        // -15000 supera MIN_BUDGET: il rollback dallo snapshot spetta all'engine
        assertThrows(IllegalStateException.class, () -> city.applyDelta(total));
    }
}
