package it.unipd.citylogic.core.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * DTO immutabile che fotografa le metriche globali della città in un dato
 * istante della simulazione.
 * <p>
 * Ha tre usi distinti nel sistema:
 * <ul>
 * <li><b>Backup transazionale</b>: il {@code SimulationEngine} lo esporta
 * a inizio tick per poter fare rollback in caso di violazione degli
 * invarianti;</li>
 * <li><b>Vista read-only per le fasi</b>: le {@code ITickPhase} leggono lo
 * stato di inizio tick senza poter mutare l'aggregate;</li>
 * <li><b>Notifica verso l'esterno</b>: viene pubblicato agli
 * {@code ICityObserver} (dashboard, logger) e serializzato dal modulo
 * di persistenza per il save/load.</li>
 * </ul>
 *
 * @param budget     budget corrente della città
 * @param pollution  livello di inquinamento totale
 * @param population popolazione totale
 * @param happiness  felicità collettiva (scala 0-100)
 * @param tickCount  numero di tick simulati finora
 */
public record CitySnapshot(
        BigDecimal budget,
        double pollution,
        int population,
        double happiness,
        int tickCount) {

    /** Costruttore compatto: il budget non può mai essere null. */
    public CitySnapshot {
        Objects.requireNonNull(budget, "budget non può essere null");
    }
}