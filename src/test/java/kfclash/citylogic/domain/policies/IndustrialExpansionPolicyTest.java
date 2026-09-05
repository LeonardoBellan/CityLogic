package kfclash.citylogic.domain.policies;

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

/** Unit test per {@link IndustrialExpansionPolicy} (branch industriale e residenziale). */
class IndustrialExpansionPolicyTest {

    private static final double EPS = 1e-9;

    private IndustrialExpansionPolicy policy;
    private FakeGridReadPort grid;

    @BeforeEach
    void setUp() {
        policy = new IndustrialExpansionPolicy();
        grid = new FakeGridReadPort();
    }

    private IBuildingState add(String id, String type, int x, int y) {
        grid.addBuilding(id, type, true, ResourceDelta.zero(), x, y);
        return grid.getBuildingById(id).orElseThrow();
    }

    @Test
    @DisplayName("espone il nome pubblico dell'ordinanza")
    void exposesPolicyName() {
        assertEquals("Espansione Industriale", policy.getName());
    }

    @Test
    @DisplayName("fabbrica: entrate elevate pagate in inquinamento e malcontento")
    void boostsRevenueAtEnvironmentalCost() {
        IBuildingState factory = add("f1", "INDUSTRIAL", 7, 7);

        ResourceDelta delta = policy.calculateModifier(factory, grid);

        assertEquals(0, new BigDecimal("60.00").compareTo(delta.budgetDelta()));
        assertEquals(2.0, delta.pollutionDelta(), EPS);
        assertEquals(-0.10, delta.happinessDelta(), EPS);
        assertEquals(0, delta.populationDelta()); // la crescita è dei residenziali
    }

    @Test
    @DisplayName("residenziale confinante con una fabbrica: attira nuovi abitanti")
    void residentialNearIndustryGrows() {
        IBuildingState house = add("h1", "RESIDENTIAL", 3, 3);
        add("f1", "INDUSTRIAL", 3, 4);

        ResourceDelta delta = policy.calculateModifier(house, grid);

        assertEquals(2, delta.populationDelta());
        assertEquals(0, BigDecimal.ZERO.compareTo(delta.budgetDelta()));
        assertEquals(0.0, delta.pollutionDelta(), EPS);
    }

    @Test
    @DisplayName("residenziale lontano da ogni fabbrica: nessuna crescita")
    void isolatedResidentialGetsNothing() {
        IBuildingState house = add("h1", "RESIDENTIAL", 3, 3);
        add("f1", "INDUSTRIAL", 10, 10);

        assertTrue(policy.calculateModifier(house, grid).isEmpty());
    }

    @Test
    @DisplayName("una centrale adiacente non è una fonte di lavoro")
    void powerPlantIsNotAJobSource() {
        IBuildingState house = add("h1", "RESIDENTIAL", 3, 3);
        add("pp1", "POWER_PLANT", 2, 3);

        assertTrue(policy.calculateModifier(house, grid).isEmpty());
    }

    @Test
    @DisplayName("restituisce zero() su tipi estranei all'ordinanza")
    void returnsZeroForUnrelatedTypes() {
        assertTrue(policy.calculateModifier(add("p1", "PARK", 1, 1), grid).isEmpty());
        assertTrue(policy.calculateModifier(add("c1", "COMMERCIAL", 2, 2), grid).isEmpty());
        assertTrue(policy.calculateModifier(add("r1", "ROAD", 3, 3), grid).isEmpty());
    }

    @Test
    @DisplayName("accetta parametri personalizzati e rifiuta quelli invalidi")
    void honoursCustomParametersAndValidatesThem() {
        IndustrialExpansionPolicy aggressive =
                new IndustrialExpansionPolicy(new BigDecimal("200.00"), 10.0, 1.0, 5);

        ResourceDelta delta = aggressive.calculateModifier(add("f1", "INDUSTRIAL", 1, 1), grid);

        assertEquals(0, new BigDecimal("200.00").compareTo(delta.budgetDelta()));
        assertEquals(10.0, delta.pollutionDelta(), EPS);

        assertThrows(IllegalArgumentException.class,
                () -> new IndustrialExpansionPolicy(null, 1.0, 1.0, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new IndustrialExpansionPolicy(BigDecimal.TEN, 1.0, 1.0, -1));
    }
}
