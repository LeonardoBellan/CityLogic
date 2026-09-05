package kfclash.citylogic.domain.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kfclash.citylogic.domain.core.ResourceDelta;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test per {@link ResourceDelta}.
 * <p>
 * Verifica le proprietà algebriche di {@code merge} (somma per componente,
 * elemento neutro, gestione dei negativi) e gli invarianti di costruzione
 * (null-safety sul budget).
 */
class ResourceDeltaTest {

    @Test
    @DisplayName("zero() restituisce un delta con tutte le componenti nulle")
    void zeroHasAllComponentsAtZero() {
        ResourceDelta zero = ResourceDelta.zero();

        assertEquals(0, BigDecimal.ZERO.compareTo(zero.budgetDelta()));
        assertEquals(0.0, zero.pollutionDelta());
        assertEquals(0, zero.populationDelta());
        assertEquals(0.0, zero.happinessDelta());
    }

    @Test
    @DisplayName("merge somma componente per componente")
    void mergeSumsComponentWise() {
        ResourceDelta a = new ResourceDelta(new BigDecimal("100.50"), 2.5, 10, 1.0);
        ResourceDelta b = new ResourceDelta(new BigDecimal("-30.25"), 0.5, 5, -0.5);

        ResourceDelta merged = a.merge(b);

        assertEquals(0, new BigDecimal("70.25").compareTo(merged.budgetDelta()));
        assertEquals(3.0, merged.pollutionDelta(), 1e-9);
        assertEquals(15, merged.populationDelta());
        assertEquals(0.5, merged.happinessDelta(), 1e-9);
    }

    @Test
    @DisplayName("zero() è elemento neutro del merge")
    void zeroIsIdentityForMerge() {
        ResourceDelta delta = new ResourceDelta(new BigDecimal("42"), 1.5, 3, -0.7);

        assertEquals(delta, delta.merge(ResourceDelta.zero()));
        assertEquals(delta, ResourceDelta.zero().merge(delta));
    }

    @Test
    @DisplayName("merge è commutativo")
    void mergeIsCommutative() {
        ResourceDelta a = new ResourceDelta(new BigDecimal("10"), 1.0, 2, 0.5);
        ResourceDelta b = new ResourceDelta(new BigDecimal("-4"), 2.0, -1, 0.25);

        assertEquals(a.merge(b), b.merge(a));
    }

    @Test
    @DisplayName("merge non muta gli operandi (immutabilità)")
    void mergeDoesNotMutateOperands() {
        ResourceDelta a = new ResourceDelta(new BigDecimal("10"), 1.0, 2, 0.5);
        ResourceDelta b = new ResourceDelta(new BigDecimal("5"), 0.0, 0, 0.0);

        a.merge(b);

        // Gli originali devono essere rimasti invariati
        assertEquals(0, new BigDecimal("10").compareTo(a.budgetDelta()));
        assertEquals(0, new BigDecimal("5").compareTo(b.budgetDelta()));
    }

    @Test
    @DisplayName("merge gestisce delta interamente negativi (es. crisi economica)")
    void mergeHandlesFullyNegativeDeltas() {
        ResourceDelta crisis = new ResourceDelta(new BigDecimal("-500"), 0.0, -20, -3.0);

        ResourceDelta merged = ResourceDelta.zero().merge(crisis);

        assertEquals(0, new BigDecimal("-500").compareTo(merged.budgetDelta()));
        assertEquals(-20, merged.populationDelta());
        assertEquals(-3.0, merged.happinessDelta(), 1e-9);
    }

    @Test
    @DisplayName("budgetDelta null viene rifiutato in costruzione")
    void nullBudgetIsRejected() {
        assertThrows(NullPointerException.class,
                () -> new ResourceDelta(null, 0.0, 0, 0.0));
    }

    @Test
    @DisplayName("merge(null) viene rifiutato")
    void mergeWithNullIsRejected() {
        ResourceDelta delta = ResourceDelta.zero();

        assertThrows(NullPointerException.class, () -> delta.merge(null));
    }
}