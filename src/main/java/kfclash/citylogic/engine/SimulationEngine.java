package kfclash.citylogic.engine;

import java.util.List;
import java.util.Objects;

import kfclash.citylogic.domain.core.CityAggregate;
import kfclash.citylogic.domain.core.CitySnapshot;
import kfclash.citylogic.domain.core.ResourceDelta;
import kfclash.citylogic.domain.core.SimulationException;
import kfclash.citylogic.ports.ICityEventPublisher;
import kfclash.citylogic.ports.IGridReadPort;
import kfclash.citylogic.ports.IPolicyStrategy;
import kfclash.citylogic.ports.ITickPhase;
import kfclash.citylogic.tick.PolicyEvaluationPhase;
import kfclash.citylogic.tick.SimulationConfig;
import kfclash.citylogic.tick.TickPhaseFactory;

/**
 * Facade del Core (pattern GoF Facade): l'unico punto d'ingresso con cui il
 * resto dell'applicazione (UI, persistenza, timer) interagisce con la
 * simulazione.
 * <p>
 * <b>Il tick è transazionale</b> (vedi internal sequence diagram):
 * <ol>
 * <li>esporta lo snapshot di inizio tick (backup + vista read-only,
 * identica per tutte le fasi);</li>
 * <li>esegue la pipeline di {@link ITickPhase} accumulando i delta via
 * {@code merge};</li>
 * <li>applica il delta totale al {@link CityAggregate} in un'unica
 * operazione: se gli invarianti sono violati (es. bancarotta), lo
 * stato viene ripristinato dal backup e il tick fallisce con
 * {@link SimulationException} — commit o rollback, mai stati a
 * metà;</li>
 * <li>solo a commit avvenuto, pubblica il nuovo snapshot agli observer
 * (pattern Observer, UI disaccoppiata dal Core).</li>
 * </ol>
 * Le fasi sono create dalla {@link TickPhaseFactory} (pattern Factory):
 * l'engine non conosce le loro classi concrete, con la sola eccezione
 * documentata della {@link PolicyEvaluationPhase}, verso cui fa da facade
 * per l'attivazione delle ordinanze.
 * <p>
 * <b>Thread-safety</b>: tutti i metodi pubblici sono {@code synchronized}
 * sull'istanza — un solo thread alla volta può mutare o leggere lo stato
 * della simulazione, quindi tick concorrenti non possono corrompere lo
 * stato né interferire con il meccanismo di rollback. <b>Attenzione</b>:
 * questa garanzia copre lo stato della città, non la griglia — durante un
 * tick le fasi leggono {@code IGridReadPort}, e una mutazione concorrente
 * della mappa (es. demolizione da un altro thread a tick in corso) resta
 * responsabilità del chiamante. Raccomandazione di sistema: invocare
 * engine e mutazioni della mappa dallo stesso thread (es. l'application
 * thread di JavaFX); il lock interno resta come difesa in profondità.
 */
public class SimulationEngine {

    private final CityAggregate cityState;
    private final List<ITickPhase> phases;
    private final IGridReadPort gridReader;
    private final ICityEventPublisher eventPublisher;

    /**
     * Costruisce l'engine con tutte le dipendenze iniettate (Dependency
     * Injection manuale: nessuna dipendenza è creata internamente, tutto è
     * sostituibile nei test con stub).
     *
     * @param cityState      lo stato aggregato della città (non null)
     * @param gridReader     accesso read-only alla griglia (non null)
     * @param eventPublisher canale di notifica verso gli observer (non null)
     * @param factory        factory delle fasi (non null)
     * @param config         configurazione della pipeline (non null)
     * @throws IllegalArgumentException se la config contiene fasi sconosciute
     */
    public SimulationEngine(CityAggregate cityState,
            IGridReadPort gridReader,
            ICityEventPublisher eventPublisher,
            TickPhaseFactory factory,
            SimulationConfig config) {
        this.cityState = Objects.requireNonNull(cityState, "cityState non può essere null");
        this.gridReader = Objects.requireNonNull(gridReader, "gridReader non può essere null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher non può essere null");
        Objects.requireNonNull(factory, "factory non può essere null");
        Objects.requireNonNull(config, "config non può essere null");
        this.phases = factory.createPhases(config);
    }

    /**
     * Avanza la simulazione di un tick, in modo transazionale.
     *
     * @throws SimulationException se il tick viola gli invarianti di
     *                             dominio; in tal caso lo stato è già stato
     *                             ripristinato a inizio tick (rollback) e
     *                             nessun observer è stato notificato
     */
    public synchronized void advanceTick() {
        // 1. Backup per la transazionalità + vista read-only per le fasi
        CitySnapshot startSnapshot = cityState.exportSnapshot();

        // 2. Pipeline: tutte le fasi leggono lo stesso stato di inizio tick
        ResourceDelta totalDelta = ResourceDelta.zero();
        for (ITickPhase phase : phases) {
            totalDelta = totalDelta.merge(phase.execute(startSnapshot, gridReader));
        }

        // 3. Commit atomico, con rollback in caso di violazione invarianti
        try {
            cityState.applyDelta(totalDelta);
        } catch (IllegalStateException invariantViolation) {
            cityState.restoreFromSnapshot(startSnapshot);
            throw new SimulationException(
                    "Tick fallito, stato ripristinato: " + invariantViolation.getMessage(),
                    invariantViolation);
        }

        // 4. Notifica gli observer solo a commit avvenuto
        eventPublisher.publish(cityState.exportSnapshot());
    }

    /**
     * Fotografia immutabile dello stato corrente. Punto di aggancio per il
     * salvataggio su file (modulo persistenza) e per interrogazioni della UI.
     *
     * @return lo snapshot corrente delle metriche
     */
    public synchronized CitySnapshot getCurrentSnapshot() {
        return cityState.exportSnapshot();
    }

    /**
     * Ripristina lo stato delle metriche da uno snapshot (caricamento di
     * una partita salvata). Nota: la griglia viene ricaricata separatamente
     * dal modulo mappa/persistenza.
     *
     * @param snapshot lo stato da caricare (non null)
     */
    public synchronized void loadState(CitySnapshot snapshot) {
        cityState.restoreFromSnapshot(
                Objects.requireNonNull(snapshot, "snapshot non può essere null"));
    }

    /**
     * Attiva un'ordinanza cittadina (delegato alla
     * {@link PolicyEvaluationPhase} della pipeline).
     *
     * @param policy la politica da attivare (non null)
     * @throws IllegalStateException se la pipeline non include la fase di
     *                               valutazione politiche
     */
    public synchronized void activatePolicy(IPolicyStrategy policy) {
        findPolicyPhase().activatePolicy(policy);
    }

    /**
     * Disattiva l'ordinanza con il nome indicato (no-op se non attiva).
     *
     * @param policyName il nome della politica (non null)
     * @throws IllegalStateException se la pipeline non include la fase di
     *                               valutazione politiche
     */
    public synchronized void deactivatePolicy(String policyName) {
        findPolicyPhase().deactivatePolicy(policyName);
    }

    /**
     * I nomi delle politiche attualmente attive (vista read-only, per la
     * Dashboard).
     *
     * @return lista non modificabile dei nomi
     * @throws IllegalStateException se la pipeline non include la fase di
     *                               valutazione politiche
     */
    public synchronized List<String> getActivePolicyNames() {
        return findPolicyPhase().getActivePolicyNames();
    }

    /** Cerca la fase di valutazione politiche nella pipeline. */
    private PolicyEvaluationPhase findPolicyPhase() {
        return phases.stream()
                .filter(PolicyEvaluationPhase.class::isInstance)
                .map(PolicyEvaluationPhase.class::cast)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "La pipeline non include la fase " + TickPhaseFactory.PHASE_POLICY
                                + ": impossibile gestire le politiche"));
    }
}