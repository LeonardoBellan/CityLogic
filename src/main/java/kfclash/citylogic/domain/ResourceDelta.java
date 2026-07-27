package it.unipd.citylogic.core.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object immutabile che rappresenta una variazione (delta) delle
 * metriche cittadine prodotta da una fase del tick o da una policy.
 * <p>
 * Essendo un {@code record}, è immutabile per costruzione: le fasi della
 * pipeline non possono mutare lo stato della città direttamente, ma solo
 * restituire delta che il {@code SimulationEngine} accumula tramite
 * {@link #merge(ResourceDelta)} e applica atomicamente al
 * {@code CityAggregate}.
 *
 * @param budgetDelta     variazione di budget (può essere negativa)
 * @param pollutionDelta  variazione di inquinamento
 * @param populationDelta variazione di popolazione
 * @param happinessDelta  variazione di felicità collettiva
 */
public record ResourceDelta(
        BigDecimal budgetDelta,
        double pollutionDelta,
        int populationDelta,
        double happinessDelta) {

    /** Delta neutro: elemento identità rispetto a {@link #merge}. */
    private static final ResourceDelta ZERO = new ResourceDelta(BigDecimal.ZERO, 0.0, 0, 0.0);

    /**
     * Costruttore compatto: garantisce l'invariante "budgetDelta mai null",
     * così tutte le operazioni aritmetiche a valle sono null-safe.
     */
    public ResourceDelta {
        Objects.requireNonNull(budgetDelta, "budgetDelta non può essere null");
    }

    /**
     * Restituisce il delta neutro (tutte le componenti a zero).
     * Usato dal {@code SimulationEngine} come accumulatore iniziale.
     *
     * @return il delta zero
     */
    public static ResourceDelta zero() {
        return ZERO;
    }

    /**
     * Combina questo delta con un altro sommando componente per componente.
     * <p>
     * L'operazione è associativa e commutativa, con {@link #zero()} come
     * elemento neutro: l'ordine in cui le fasi della pipeline vengono
     * accumulate non influenza il delta totale.
     *
     * @param other il delta da sommare (non null)
     * @return un nuovo {@code ResourceDelta}, somma dei due
     */
    public ResourceDelta merge(ResourceDelta other) {
        Objects.requireNonNull(other, "other non può essere null");
        return new ResourceDelta(
                this.budgetDelta.add(other.budgetDelta),
                this.pollutionDelta + other.pollutionDelta,
                this.populationDelta + other.populationDelta,
                this.happinessDelta + other.happinessDelta);
    }
}