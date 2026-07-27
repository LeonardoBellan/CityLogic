package it.unipd.citylogic.core.tick;

import it.unipd.citylogic.core.domain.CitySnapshot;
import it.unipd.citylogic.core.domain.ResourceDelta;
import it.unipd.citylogic.core.policy.IPolicyStrategy;
import it.unipd.citylogic.core.ports.IBuildingState;
import it.unipd.citylogic.core.ports.IGridReadPort;
import it.unipd.citylogic.core.testutil.FakeGridReadPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test per {@link PolicyEvaluationPhase}.
 * <p>
 * Usa una politica di test ({@link FlatTaxOnType}) che rispetta il
 * contratto di {@link IPolicyStrategy}: si applica solo a un tipo di
 * edificio e restituisce {@code zero()} per gli altri.
 */
class PolicyEvaluationPhaseTest {

    /**
     * Politica di test: tassa fissa sugli edifici di un certo tipo.
     * (+amount budget, -1 felicità per ogni edificio colpito)
     */
    private static class FlatTaxOnType implements IPolicyStrategy {
        private final String name;
        private final String targetType;
        private final BigDecimal amount;

        FlatTaxOnType(String name, String targetType, BigDecimal amount) {
            this.name = name;
            this.targetType = targetType;
            this.amount = amount;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ResourceDelta calculateModifier(IBuildingState building, IGridReadPort grid) {
            if (!building.getType().equals(targetType)) {
                return ResourceDelta.zero(); // contratto: mai null
            }
            return new ResourceDelta(amount, 0.0, 0, -1.0);
        }
    }

    private PolicyEvaluationPhase phase;
    private FakeGridReadPort grid;
    private CitySnapshot snapshot;

    @BeforeEach
    void setUp() {
        phase = new PolicyEvaluationPhase();
        grid = new FakeGridReadPort();
        snapshot = new CitySnapshot(new BigDecimal("1000"), 0.0, 50, 50.0, 0);

        // due fabbriche alimentate, un parco alimentato, una fabbrica spenta
        ResourceDelta any = ResourceDelta.zero();
        grid.addBuilding("f1", "INDUSTRIAL", true, any, 0, 0)
                .addBuilding("f2", "INDUSTRIAL", true, any, 3, 3)
                .addBuilding("p1", "PARK", true, any, 6, 6)
                .addBuilding("f3", "INDUSTRIAL", false, any, 9, 9);
    }

    @Test
    @DisplayName("senza politiche attive il delta è zero")
    void noPoliciesProducesZero() {
        assertEquals(ResourceDelta.zero(), phase.execute(snapshot, grid));
    }

    @Test
    @DisplayName("la politica colpisce solo gli edifici del tipo bersaglio")
    void policyHitsOnlyTargetType() {
        phase.activatePolicy(new FlatTaxOnType("Tassa Ambientale", "INDUSTRIAL", new BigDecimal("100")));

        ResourceDelta result = phase.execute(snapshot, grid);

        // colpite f1 e f2 (alimentate); p1 è PARK, f3 è spenta
        assertEquals(0, new BigDecimal("200").compareTo(result.budgetDelta()));
        assertEquals(-2.0, result.happinessDelta(), 1e-9);
    }

    @Test
    @DisplayName("gli edifici non alimentati non vengono valutati dalle politiche")
    void unpoweredBuildingsAreNotEvaluated() {
        phase.activatePolicy(new FlatTaxOnType("Tassa Ambientale", "INDUSTRIAL", new BigDecimal("100")));

        ResourceDelta result = phase.execute(snapshot, grid);

        // se f3 (spenta) fosse valutata, il budget sarebbe 300
        assertEquals(0, new BigDecimal("200").compareTo(result.budgetDelta()));
    }

    @Test
    @DisplayName("più politiche attive si sommano")
    void multiplePoliciesAreMerged() {
        phase.activatePolicy(new FlatTaxOnType("Tassa Ambientale", "INDUSTRIAL", new BigDecimal("100")));
        phase.activatePolicy(new FlatTaxOnType("Tassa sul Verde", "PARK", new BigDecimal("10")));

        ResourceDelta result = phase.execute(snapshot, grid);

        // 2 fabbriche * 100 + 1 parco * 10 = 210
        assertEquals(0, new BigDecimal("210").compareTo(result.budgetDelta()));
    }

    @Test
    @DisplayName("attivare due volte la stessa politica non raddoppia l'effetto")
    void duplicateActivationIsIgnored() {
        IPolicyStrategy tax = new FlatTaxOnType("Tassa Ambientale", "INDUSTRIAL", new BigDecimal("100"));
        phase.activatePolicy(tax);
        phase.activatePolicy(new FlatTaxOnType("Tassa Ambientale", "INDUSTRIAL", new BigDecimal("100")));

        ResourceDelta result = phase.execute(snapshot, grid);

        assertEquals(0, new BigDecimal("200").compareTo(result.budgetDelta()));
        assertEquals(1, phase.getActivePolicyNames().size());
    }

    @Test
    @DisplayName("disattivare una politica ne rimuove l'effetto")
    void deactivationRemovesEffect() {
        phase.activatePolicy(new FlatTaxOnType("Tassa Ambientale", "INDUSTRIAL", new BigDecimal("100")));
        phase.deactivatePolicy("Tassa Ambientale");

        assertEquals(ResourceDelta.zero(), phase.execute(snapshot, grid));
        assertTrue(phase.getActivePolicyNames().isEmpty());
    }

    @Test
    @DisplayName("disattivare una politica inesistente è un no-op")
    void deactivatingUnknownPolicyIsNoOp() {
        assertDoesNotThrow(() -> phase.deactivatePolicy("Non Esiste"));
    }

    @Test
    @DisplayName("getActivePolicyNames espone i nomi in vista read-only")
    void activePolicyNamesAreExposedReadOnly() {
        phase.activatePolicy(new FlatTaxOnType("Tassa Ambientale", "INDUSTRIAL", BigDecimal.TEN));

        var names = phase.getActivePolicyNames();

        assertEquals(1, names.size());
        assertEquals("Tassa Ambientale", names.get(0));
        assertThrows(UnsupportedOperationException.class, () -> names.add("Hack"));
    }
}