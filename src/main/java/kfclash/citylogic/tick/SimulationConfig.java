package kfclash.citylogic.tick;

import java.util.List;
import java.util.Objects;

/**
 * Value Object di configurazione: dichiara quali fasi compongono la
 * pipeline del tick e in che ordine.
 * <p>
 * I nomi ammessi sono le costanti {@code PHASE_*} di
 * {@link TickPhaseFactory}. Tenere la pipeline configurabile (invece di
 * cablarla nell'engine) permette di aggiungere fasi future — es. una
 * {@code DisasterPhase} per gli eventi casuali opzionali — senza toccare
 * il {@code SimulationEngine}.
 *
 * @param enabledPhases nomi delle fasi da creare, nell'ordine di esecuzione
 */
public record SimulationConfig(List<String> enabledPhases) {

    /**
     * Costruttore compatto: rifiuta null e congela la lista in una copia
     * immutabile (un record con dentro una lista mutabile non sarebbe
     * davvero un value object).
     */
    public SimulationConfig {
        Objects.requireNonNull(enabledPhases, "enabledPhases non può essere null");
        enabledPhases = List.copyOf(enabledPhases);
    }

    /**
     * Configurazione standard del gioco: produzione seguita dalla
     * valutazione delle politiche.
     *
     * @return la configurazione di default
     */
    public static SimulationConfig defaultConfig() {
        return new SimulationConfig(List.of(
                TickPhaseFactory.PHASE_PRODUCTION,
                TickPhaseFactory.PHASE_POLICY));
    }
}