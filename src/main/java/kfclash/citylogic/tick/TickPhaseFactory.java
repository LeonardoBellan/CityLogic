package it.unipd.citylogic.core.tick;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Factory (pattern GoF Factory, requisito d'esame): costruisce la pipeline
 * di {@link ITickPhase} a partire dalla configurazione.
 * <p>
 * Grazie a questa classe il {@code SimulationEngine} non conosce le classi
 * concrete delle fasi che esegue (come richiesto dalle specifiche per il
 * Factory: "il motore di simulazione non deve conoscere le classi concrete
 * che crea"): riceve una {@code List<ITickPhase>} già pronta e la itera.
 * Aggiungere una fase nuova = una classe nuova + un case qui, zero
 * modifiche all'engine.
 */
public class TickPhaseFactory {

    /** Nome della fase di produzione (somma dei contributi degli edifici). */
    public static final String PHASE_PRODUCTION = "PRODUCTION";

    /** Nome della fase di valutazione delle politiche attive. */
    public static final String PHASE_POLICY = "POLICY";

    /**
     * Crea le fasi della pipeline nell'ordine dichiarato dalla
     * configurazione.
     *
     * @param config la configurazione con i nomi delle fasi (non null)
     * @return lista delle fasi istanziate, nell'ordine di esecuzione
     * @throws IllegalArgumentException se un nome di fase non è riconosciuto
     */
    public List<ITickPhase> createPhases(SimulationConfig config) {
        Objects.requireNonNull(config, "config non può essere null");

        List<ITickPhase> phases = new ArrayList<>();
        for (String phaseName : config.enabledPhases()) {
            phases.add(createPhase(phaseName));
        }
        return phases;
    }

    /**
     * Istanzia la singola fase a partire dal nome.
     *
     * @param phaseName il nome della fase (una delle costanti {@code PHASE_*})
     * @return la fase concreta
     * @throws IllegalArgumentException se il nome non è riconosciuto
     */
    private ITickPhase createPhase(String phaseName) {
        return switch (phaseName) {
            case PHASE_PRODUCTION -> new ProductionPhase();
            case PHASE_POLICY -> new PolicyEvaluationPhase();
            default -> throw new IllegalArgumentException(
                    "Fase sconosciuta: '" + phaseName + "'. Fasi disponibili: "
                            + PHASE_PRODUCTION + ", " + PHASE_POLICY);
        };
    }
}