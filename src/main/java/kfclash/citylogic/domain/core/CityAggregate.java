package kfclash.citylogic.domain.core;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Aggregate Root dello stato globale della città (pattern DDD).
 * <p>
 * È l'<b>unico</b> punto del sistema in cui le metriche cittadine possono
 * essere mutate, e la mutazione passa esclusivamente da
 * {@link #applyDelta(ResourceDelta)}: le fasi del tick e le policy producono
 * solo {@link ResourceDelta} immutabili, che il {@code SimulationEngine}
 * accumula e applica qui in un'unica operazione atomica.
 * <p>
 * <b>Invarianti di dominio</b> (verificati dopo ogni applyDelta):
 * <ul>
 * <li>budget &ge; {@link #MIN_BUDGET} (sotto la soglia: bancarotta,
 * il tick viene rifiutato);</li>
 * <li>popolazione &ge; 0.</li>
 * </ul>
 * <b>Metriche clampate</b> (non sono errori, vengono corrette in silenzio):
 * <ul>
 * <li>felicità riportata nell'intervallo [{@link #MIN_HAPPINESS},
 * {@link #MAX_HAPPINESS}];</li>
 * <li>inquinamento mai negativo (floor a 0).</li>
 * </ul>
 * In caso di violazione degli invarianti viene lanciata una
 * {@link IllegalStateException}: il chiamante (engine) è responsabile del
 * rollback tramite {@link #restoreFromSnapshot(CitySnapshot)}.
 */
public class CityAggregate {

    /** Soglia minima di budget: sotto questo valore la città è in bancarotta. */
    public static final BigDecimal MIN_BUDGET = new BigDecimal("-10000");

    /** Estremo inferiore della scala di felicità. */
    public static final double MIN_HAPPINESS = 0.0;

    /** Estremo superiore della scala di felicità. */
    public static final double MAX_HAPPINESS = 100.0;

    private BigDecimal budget;
    private double pollution;
    private int population;
    private double happiness;
    private int tickCount;

    /**
     * Crea lo stato iniziale della città.
     *
     * @param initialBudget     budget di partenza (non null)
     * @param initialPopulation popolazione di partenza (&ge; 0)
     * @param initialHappiness  felicità di partenza (verrà clampata in [0,100])
     * @throws IllegalStateException se i valori iniziali violano gli invarianti
     */
    public CityAggregate(BigDecimal initialBudget, int initialPopulation, double initialHappiness) {
        this.budget = Objects.requireNonNull(initialBudget, "initialBudget non può essere null");
        this.pollution = 0.0;
        this.population = initialPopulation;
        this.happiness = clamp(initialHappiness);
        this.tickCount = 0;
        validateInvariants();
    }

    /**
     * Applica una variazione delle metriche e avanza il contatore dei tick.
     * <p>
     * L'operazione è pensata per essere chiamata <b>una volta per tick</b> dal
     * {@code SimulationEngine} con il delta totale (già accumulato via
     * {@code merge}). Felicità e inquinamento vengono clampati; budget e
     * popolazione vengono invece validati: in caso di violazione lo stato
     * resta mutato e spetta al chiamante ripristinarlo dallo snapshot di
     * backup (design transazionale documentato nel sequence diagram).
     *
     * @param delta la variazione da applicare (non null)
     * @throws IllegalStateException se il nuovo stato viola gli invarianti
     */
    public void applyDelta(ResourceDelta delta) {
        Objects.requireNonNull(delta, "delta non può essere null");

        this.budget = this.budget.add(delta.budgetDelta());
        this.pollution = Math.max(0.0, this.pollution + delta.pollutionDelta());
        this.population = this.population + delta.populationDelta();
        this.happiness = clamp(this.happiness + delta.happinessDelta());
        this.tickCount++;

        validateInvariants();
    }

    /**
     * Esporta una fotografia immutabile dello stato corrente.
     * Usata come backup transazionale, vista read-only per le fasi,
     * payload per gli observer e base per il salvataggio su file.
     *
     * @return lo snapshot corrente
     */
    public CitySnapshot exportSnapshot() {
        return new CitySnapshot(budget, pollution, population, happiness, tickCount);
    }

    /**
     * Ripristina integralmente lo stato da uno snapshot.
     * Usata dall'engine per il rollback di un tick fallito e dal modulo di
     * persistenza per il caricamento di una partita salvata.
     *
     * @param snapshot lo stato da ripristinare (non null)
     */
    public void restoreFromSnapshot(CitySnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot non può essere null");
        this.budget = snapshot.budget();
        this.pollution = snapshot.pollution();
        this.population = snapshot.population();
        this.happiness = snapshot.happiness();
        this.tickCount = snapshot.tickCount();
    }

    /**
     * Verifica gli invarianti di dominio sullo stato corrente.
     *
     * @throws IllegalStateException con messaggio esplicativo se violati
     */
    private void validateInvariants() {
        if (budget.compareTo(MIN_BUDGET) < 0) {
            throw new IllegalStateException(
                    "Invariante violato: budget " + budget
                            + " sotto la soglia di bancarotta " + MIN_BUDGET);
        }
        if (population < 0) {
            throw new IllegalStateException(
                    "Invariante violato: popolazione negativa (" + population + ")");
        }
    }

    /** Riporta la felicità nell'intervallo ammesso [0, 100]. */
    private static double clamp(double value) {
        return Math.min(MAX_HAPPINESS, Math.max(MIN_HAPPINESS, value));
    }
}