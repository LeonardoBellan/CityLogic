package kfclash.citylogic.policies;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import kfclash.citylogic.domain.core.ResourceDelta;
import kfclash.citylogic.ports.IBuildingState;
import kfclash.citylogic.testutil.FakeGridReadPort;

/** Unit test per {@link EnvironmentalTaxPolicy}. */
class EnvironmentalTaxPolicyTest {

    private static final double EPS = 1e-9;

    private EnvironmentalTaxPolicy policy;
    private FakeGridReadPort grid;

    @BeforeEach
    void setUp() {
        policy = new EnvironmentalTaxPolicy();
        grid = new FakeGridReadPort();
    }

    private IBuildingState building(String type) {
        grid.addBuilding("b1", type, true, ResourceDelta.zero(), 5, 5);
        return grid.getBuildingById("b1").orElseThrow();
    }

    @Test
    @DisplayName("espone il nome pubblico dell'ordinanza")
    void exposesPolicyName() {
        assertEquals("Tassa Ambientale", policy.getName());
    }

    @Test
    @DisplayName("su un'industria produce gettito positivo e felicità negativa")
    void taxesIndustrialBuilding() {
        ResourceDelta delta = policy.calculateModifier(building("INDUSTRIAL"), grid);

        assertEquals(0, new BigDecimal("25.00").compareTo(delta.budgetDelta()));
        assertEquals(-0.15, delta.happinessDelta(), EPS);
        assertEquals(0.0, delta.pollutionDelta(), EPS); // la tassa non cambia le emissioni
        assertEquals(0, delta.populationDelta());
    }

    @Test
    @DisplayName("tassa anche le centrali elettriche")
    void taxesPowerPlant() {
        ResourceDelta delta = policy.calculateModifier(building("POWER_PLANT"), grid);

        assertEquals(0, new BigDecimal("25.00").compareTo(delta.budgetDelta()));
    }

    @ParameterizedTest(name = "tipo non pertinente: {0}")
    @ValueSource(strings = { "RESIDENTIAL", "House", "COMMERCIAL", "PARK", "ROAD", "Sfera di Cristallo" })
    @DisplayName("restituisce zero() per ogni tipo non inquinante")
    void returnsZeroForNonPollutingTypes(String type) {
        ResourceDelta delta = policy.calculateModifier(building(type), grid);

        assertTrue(delta.isEmpty());
        assertSame(ResourceDelta.zero(), delta); // riusa l'istanza condivisa
    }

    @Test
    @DisplayName("riconosce il nome del catalogo applicativo (\"Factory\")")
    void recognisesCatalogName() {
        ResourceDelta delta = policy.calculateModifier(building("Factory"), grid);

        assertEquals(0, new BigDecimal("25.00").compareTo(delta.budgetDelta()));
    }

    @Test
    @DisplayName("è pura: due invocazioni consecutive danno lo stesso risultato")
    void isPure() {
        IBuildingState factory = building("INDUSTRIAL");

        assertEquals(policy.calculateModifier(factory, grid), policy.calculateModifier(factory, grid));
    }

    @Test
    @DisplayName("accetta parametri di bilanciamento personalizzati")
    void honoursCustomParameters() {
        EnvironmentalTaxPolicy severe = new EnvironmentalTaxPolicy(new BigDecimal("100.00"), 1.0);

        ResourceDelta delta = severe.calculateModifier(building("INDUSTRIAL"), grid);

        assertEquals(0, new BigDecimal("100.00").compareTo(delta.budgetDelta()));
        assertEquals(-1.0, delta.happinessDelta(), EPS);
    }

    @Test
    @DisplayName("rifiuta parametri nulli o negativi")
    void rejectsInvalidParameters() {
        assertThrows(IllegalArgumentException.class, () -> new EnvironmentalTaxPolicy(null, 0.1));
        assertThrows(IllegalArgumentException.class,
                () -> new EnvironmentalTaxPolicy(new BigDecimal("-1"), 0.1));
        assertThrows(IllegalArgumentException.class,
                () -> new EnvironmentalTaxPolicy(BigDecimal.TEN, -0.1));
    }
}
