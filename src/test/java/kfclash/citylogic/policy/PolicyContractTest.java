package kfclash.citylogic.policies;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import kfclash.citylogic.domain.core.ResourceDelta;
import kfclash.citylogic.ports.IBuildingState;
import kfclash.citylogic.ports.IGridReadPort;
import kfclash.citylogic.ports.IPolicyStrategy;
import kfclash.citylogic.testutil.FakeGridReadPort;

/**
 * Verifica le regole di {@link IPolicyStrategy} su tutte le ordinanze in un
 * colpo solo: una nuova policy eredita l'intera batteria aggiungendola a
 * {@link #allPolicies()}.
 */
class PolicyContractTest {

    private static final List<String> ALL_TYPES = List.of(
            "RESIDENTIAL", "INDUSTRIAL", "COMMERCIAL", "PARK", "POWER_PLANT", "ROAD", "TIPO_IGNOTO");

    static Stream<IPolicyStrategy> allPolicies() {
        return Stream.of(
                new EnvironmentalTaxPolicy(),
                new GreenSubsidiesPolicy(),
                new IndustrialExpansionPolicy());
    }

    /** Griglia con un edificio per ciascun tipo, tutti reciprocamente adiacenti. */
    private static FakeGridReadPort gridWithAllTypes() {
        FakeGridReadPort grid = new FakeGridReadPort();
        int x = 0;
        for (String type : ALL_TYPES) {
            grid.addBuilding(type, type, true, ResourceDelta.zero(), x, 0);
            x++;
        }
        return grid;
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("allPolicies")
    @DisplayName("ha un nome non vuoto")
    void hasNonBlankName(IPolicyStrategy policy) {
        assertNotNull(policy.getName());
        assertFalse(policy.getName().isBlank());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("allPolicies")
    @DisplayName("non restituisce mai null, qualunque sia il tipo di edificio")
    void neverReturnsNull(IPolicyStrategy policy) {
        IGridReadPort grid = gridWithAllTypes();

        for (String type : ALL_TYPES) {
            IBuildingState building = grid.getBuildingById(type).orElseThrow();
            assertNotNull(policy.calculateModifier(building, grid), "delta null per il tipo " + type);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("allPolicies")
    @DisplayName("su un tipo sconosciuto restituisce il delta neutro")
    void returnsZeroForUnknownType(IPolicyStrategy policy) {
        IGridReadPort grid = gridWithAllTypes();
        IBuildingState unknown = grid.getBuildingById("TIPO_IGNOTO").orElseThrow();

        assertTrue(policy.calculateModifier(unknown, grid).isEmpty());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("allPolicies")
    @DisplayName("fallisce fail-fast su argomenti null")
    void rejectsNullArguments(IPolicyStrategy policy) {
        IGridReadPort grid = gridWithAllTypes();
        IBuildingState building = grid.getBuildingById("INDUSTRIAL").orElseThrow();

        assertThrows(NullPointerException.class, () -> policy.calculateModifier(null, grid));
        assertThrows(NullPointerException.class, () -> policy.calculateModifier(building, null));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("allPolicies")
    @DisplayName("è deterministica su invocazioni ripetute")
    void isPure(IPolicyStrategy policy) {
        IGridReadPort grid = gridWithAllTypes();

        for (String type : ALL_TYPES) {
            IBuildingState building = grid.getBuildingById(type).orElseThrow();
            assertEquals(policy.calculateModifier(building, grid),
                    policy.calculateModifier(building, grid));
        }
    }
}
