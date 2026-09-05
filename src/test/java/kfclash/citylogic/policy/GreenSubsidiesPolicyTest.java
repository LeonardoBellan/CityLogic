package kfclash.citylogic.policies;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kfclash.citylogic.domain.core.ResourceDelta;
import kfclash.citylogic.ports.IBuildingState;
import kfclash.citylogic.testutil.FakeGridReadPort;

/** Unit test per {@link GreenSubsidiesPolicy}, incluse le regole spaziali. */
class GreenSubsidiesPolicyTest {

    private static final double EPS = 1e-9;

    private GreenSubsidiesPolicy policy;
    private FakeGridReadPort grid;

    @BeforeEach
    void setUp() {
        policy = new GreenSubsidiesPolicy();
        grid = new FakeGridReadPort();
    }

    private IBuildingState add(String id, String type, int x, int y) {
        grid.addBuilding(id, type, true, ResourceDelta.zero(), x, y);
        return grid.getBuildingById(id).orElseThrow();
    }

    @Test
    @DisplayName("espone il nome pubblico dell'ordinanza")
    void exposesPolicyName() {
        assertEquals("Sussidio Verde", policy.getName());
    }

    @Test
    @DisplayName("parco isolato: costa budget e abbatte solo la quota base")
    void isolatedParkGetsBaseAbatementOnly() {
        IBuildingState park = add("p1", "PARK", 5, 5);

        ResourceDelta delta = policy.calculateModifier(park, grid);

        assertEquals(0, new BigDecimal("-15.00").compareTo(delta.budgetDelta()));
        assertEquals(-1.0, delta.pollutionDelta(), EPS);
        assertEquals(0.25, delta.happinessDelta(), EPS);
        assertEquals(0, delta.populationDelta());
    }

    @Test
    @DisplayName("parco con due inquinanti adiacenti: abbattimento 1.0 + 2 x 1.5 = 4.0")
    void abatementScalesWithPollutingNeighbours() {
        IBuildingState park = add("p1", "PARK", 5, 5);
        add("f1", "INDUSTRIAL", 4, 5);
        add("f2", "INDUSTRIAL", 6, 6);   // diagonale: conta
        add("h1", "RESIDENTIAL", 5, 4);  // non inquinante: non conta
        add("f3", "INDUSTRIAL", 5, 9);   // lontana: non conta

        ResourceDelta delta = policy.calculateModifier(park, grid);

        assertEquals(-4.0, delta.pollutionDelta(), EPS);
        assertEquals(0, new BigDecimal("-15.00").compareTo(delta.budgetDelta()));
    }

    @Test
    @DisplayName("anche le centrali contano come vicini inquinanti")
    void powerPlantsCountAsPollutingNeighbours() {
        IBuildingState park = add("p1", "PARK", 2, 2);
        add("pp1", "POWER_PLANT", 3, 2);

        assertEquals(-2.5, policy.calculateModifier(park, grid).pollutionDelta(), EPS);
    }

    @Test
    @DisplayName("restituisce zero() su un edificio non verde")
    void returnsZeroForNonGreenBuilding() {
        IBuildingState factory = add("f1", "INDUSTRIAL", 3, 3);

        assertTrue(policy.calculateModifier(factory, grid).isEmpty());
    }

    @Test
    @DisplayName("è pura: invocazioni ripetute danno lo stesso risultato")
    void isPure() {
        IBuildingState park = add("p1", "PARK", 5, 5);
        add("f1", "INDUSTRIAL", 4, 4);

        assertEquals(policy.calculateModifier(park, grid), policy.calculateModifier(park, grid));
    }

    @Test
    @DisplayName("rifiuta parametri nulli o negativi")
    void rejectsInvalidParameters() {
        assertThrows(IllegalArgumentException.class, () -> new GreenSubsidiesPolicy(null, 1.0, 1.0, 1.0));
        assertThrows(IllegalArgumentException.class,
                () -> new GreenSubsidiesPolicy(new BigDecimal("-5"), 1.0, 1.0, 1.0));
        assertThrows(IllegalArgumentException.class,
                () -> new GreenSubsidiesPolicy(BigDecimal.TEN, -1.0, 1.0, 1.0));
    }
}
