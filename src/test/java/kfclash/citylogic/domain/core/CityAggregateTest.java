package kfclash.citylogic.domain.core;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kfclash.citylogic.domain.core.CityAggregate;
import kfclash.citylogic.domain.core.CitySnapshot;
import kfclash.citylogic.domain.core.ResourceDelta;

/**
 * Unit test per {@link CityAggregate}.
 * <p>
 * Copre: applicazione dei delta, clamping di felicità e inquinamento,
 * violazione degli invarianti (bancarotta, popolazione negativa, edge case
 * "budget negativo" suggerito dalla consegna), roundtrip export/restore
 * dello snapshot e lo scenario completo di rollback.
 */
class CityAggregateTest {

    private CityAggregate city;

    @BeforeEach
    void setUp() {
        // Stato di partenza standard: 1000 di budget, 50 abitanti, felicità 50
        city = new CityAggregate(new BigDecimal("1000"), 50, 50.0);
    }

    @Test
    @DisplayName("applyDelta aggiorna tutte le metriche e incrementa il tick")
    void applyDeltaUpdatesMetricsAndIncrementsTick() {
        ResourceDelta delta = new ResourceDelta(new BigDecimal("250.50"), 3.0, 10, 5.0);

        city.applyDelta(delta);
        CitySnapshot s = city.exportSnapshot();

        assertEquals(0, new BigDecimal("1250.50").compareTo(s.budget()));
        assertEquals(3.0, s.pollution(), 1e-9);
        assertEquals(60, s.population());
        assertEquals(55.0, s.happiness(), 1e-9);
        assertEquals(1, s.tickCount());
    }

    @Test
    @DisplayName("la felicità viene clampata a 100 verso l'alto")
    void happinessIsClampedAtMax() {
        city.applyDelta(new ResourceDelta(BigDecimal.ZERO, 0.0, 0, 75.0));

        assertEquals(100.0, city.exportSnapshot().happiness(), 1e-9);
    }

    @Test
    @DisplayName("la felicità viene clampata a 0 verso il basso")
    void happinessIsClampedAtMin() {
        city.applyDelta(new ResourceDelta(BigDecimal.ZERO, 0.0, 0, -75.0));

        assertEquals(0.0, city.exportSnapshot().happiness(), 1e-9);
    }

    @Test
    @DisplayName("l'inquinamento non scende mai sotto zero")
    void pollutionIsFlooredAtZero() {
        // partiamo da inquinamento 0 e applichiamo un delta negativo (es. parchi)
        city.applyDelta(new ResourceDelta(BigDecimal.ZERO, -5.0, 0, 0.0));

        assertEquals(0.0, city.exportSnapshot().pollution(), 1e-9);
    }

    @Test
    @DisplayName("il budget può andare in negativo sopra la soglia di bancarotta")
    void budgetMayGoNegativeAboveBankruptcyThreshold() {
        // edge case della consegna: "cosa succede se il budget diventa negativo?"
        city.applyDelta(new ResourceDelta(new BigDecimal("-5000"), 0.0, 0, 0.0));

        assertEquals(0, new BigDecimal("-4000").compareTo(city.exportSnapshot().budget()));
    }

    @Test
    @DisplayName("budget esattamente alla soglia minima è accettato (boundary)")
    void budgetExactlyAtThresholdIsAccepted() {
        // 1000 - 11000 = -10000 = MIN_BUDGET: limite incluso
        assertDoesNotThrow(() -> city.applyDelta(new ResourceDelta(new BigDecimal("-11000"), 0.0, 0, 0.0)));
    }

    @Test
    @DisplayName("budget sotto la soglia di bancarotta viola l'invariante")
    void budgetBelowThresholdViolatesInvariant() {
        assertThrows(IllegalStateException.class,
                () -> city.applyDelta(new ResourceDelta(new BigDecimal("-11000.01"), 0.0, 0, 0.0)));
    }

    @Test
    @DisplayName("popolazione negativa viola l'invariante")
    void negativePopulationViolatesInvariant() {
        assertThrows(IllegalStateException.class,
                () -> city.applyDelta(new ResourceDelta(BigDecimal.ZERO, 0.0, -51, 0.0)));
    }

    @Test
    @DisplayName("stato iniziale invalido viene rifiutato dal costruttore")
    void invalidInitialStateIsRejected() {
        assertThrows(IllegalStateException.class, () -> new CityAggregate(new BigDecimal("-20000"), 0, 50.0));
        assertThrows(IllegalStateException.class, () -> new CityAggregate(BigDecimal.ZERO, -1, 50.0));
    }

    @Test
    @DisplayName("roundtrip export/restore riproduce esattamente lo stato")
    void snapshotRoundtripRestoresExactState() {
        city.applyDelta(new ResourceDelta(new BigDecimal("123.45"), 2.0, 7, -3.0));
        CitySnapshot saved = city.exportSnapshot();

        // mutiamo ulteriormente lo stato...
        city.applyDelta(new ResourceDelta(new BigDecimal("-999"), 10.0, 100, 20.0));
        // ...e ripristiniamo
        city.restoreFromSnapshot(saved);

        assertEquals(saved, city.exportSnapshot());
    }

    @Test
    @DisplayName("scenario di rollback: dopo un tick fallito lo stato è ripristinabile")
    void rollbackScenarioAfterFailedTick() {
        CitySnapshot backup = city.exportSnapshot();

        // tick che manda la città in bancarotta -> invariante violato
        assertThrows(IllegalStateException.class,
                () -> city.applyDelta(new ResourceDelta(new BigDecimal("-50000"), 0.0, 0, 0.0)));

        // lo stato è "sporco": il rollback (come farà l'engine) lo ripristina
        city.restoreFromSnapshot(backup);

        assertEquals(backup, city.exportSnapshot());
    }

    @Test
    @DisplayName("applyDelta(null) e restoreFromSnapshot(null) vengono rifiutati")
    void nullArgumentsAreRejected() {
        assertThrows(NullPointerException.class, () -> city.applyDelta(null));
        assertThrows(NullPointerException.class, () -> city.restoreFromSnapshot(null));
    }
}