package kfclash.citylogic.ports;

import kfclash.citylogic.domain.core.CitySnapshot;
import kfclash.citylogic.domain.core.ResourceDelta;

/**
 * Strategy: una fase della pipeline di simulazione eseguita a ogni tick.
 * <p>
 * Il {@code SimulationEngine} esegue le fasi in sequenza (create dalla
 * {@code TickPhaseFactory}) e accumula i loro delta; la somma viene poi
 * applicata atomicamente al {@code CityAggregate}.
 * <p>
 * Regole del contratto (fondamentali per la transazionalità):
 * <ul>
 * <li>la fase riceve lo <b>snapshot di inizio tick</b>, identico per
 * tutte le fasi: nessuna fase vede gli effetti parziali delle altre
 * e l'ordine di esecuzione non altera il risultato;</li>
 * <li>la fase è <b>pura</b>: non muta né lo stato della città né la
 * griglia, restituisce solo un delta;</li>
 * <li>mai restituire {@code null}: una fase senza effetti restituisce
 * {@link ResourceDelta#zero()}.</li>
 * </ul>
 */
public interface ITickPhase {

    /**
     * Calcola il contributo di questa fase alle metriche del tick corrente.
     *
     * @param snapshot lo stato della città a inizio tick (read-only)
     * @param grid     accesso read-only alla griglia urbana
     * @return il delta prodotto dalla fase (mai {@code null})
     */
    ResourceDelta execute(CitySnapshot snapshot, IGridReadPort grid);
}