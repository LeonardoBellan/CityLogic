package kfclash.citylogic.domain.core;

/**
 * Eccezione di business sollevata dal {@code SimulationEngine} quando un
 * tick fallisce per violazione degli invarianti di dominio (es. bancarotta:
 * budget sotto la soglia minima consentita).
 * <p>
 * Quando viene lanciata, lo stato della città è già stato ripristinato allo
 * snapshot di inizio tick (rollback): il chiamante può quindi gestirla
 * mostrando un messaggio all'utente, con la garanzia che la simulazione sia
 * rimasta in uno stato consistente.
 */
public class SimulationException extends RuntimeException {

    /**
     * @param message descrizione del motivo del fallimento del tick
     */
    public SimulationException(String message) {
        super(message);
    }

    /**
     * @param message descrizione del motivo del fallimento del tick
     * @param cause   l'eccezione di dominio originale (tipicamente la
     *                {@link IllegalStateException} degli invarianti)
     */
    public SimulationException(String message, Throwable cause) {
        super(message, cause);
    }
}