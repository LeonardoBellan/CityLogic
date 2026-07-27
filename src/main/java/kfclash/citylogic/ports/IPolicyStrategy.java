package kfclash.citylogic.ports;

import kfclash.citylogic.domain.core.ResourceDelta;

/**
 * Strategy (pattern GoF Strategy, requisito d'esame): un'ordinanza
 * cittadina che modifica dinamicamente il calcolo delle metriche.
 * <p>
 * Attivare o disattivare una politica significa aggiungere o rimuovere un
 * oggetto strategia dalla {@code PolicyEvaluationPhase} — nessun
 * {@code if/else} sul tipo di politica nel Core. Esempi:
 * <ul>
 * <li><i>Tassa ambientale</i>: per ogni edificio "INDUSTRIAL", budget
 * positivo (gettito) e felicità negativa;</li>
 * <li><i>Sussidio verde</i>: per ogni "PARK", riduce l'inquinamento
 * delle celle vicine in cambio di budget.</li>
 * </ul>
 * <b>Contratto verso il modulo policy</b>: le ordinanze concrete
 * implementano questa interfaccia.
 */
public interface IPolicyStrategy {

    /**
     * Nome univoco e leggibile della politica (es. "Tassa Ambientale").
     * Usato per attivazione/disattivazione e per la Dashboard.
     *
     * @return il nome della politica
     */
    String getName();

    /**
     * Calcola il modificatore che questa politica applica a un singolo
     * edificio nel tick corrente.
     * <p>
     * Regole del contratto:
     * <ul>
     * <li>se la politica non riguarda l'edificio (tipo non pertinente),
     * restituire {@link ResourceDelta#zero()} — mai {@code null};</li>
     * <li>il metodo deve essere <b>puro</b>: legge l'edificio e il
     * contesto spaziale via {@code grid}, ma non muta nulla;</li>
     * <li>il delta restituito è <i>aggiuntivo</i> rispetto alla
     * produzione base dell'edificio (calcolata a parte dalla
     * {@code ProductionPhase}).</li>
     * </ul>
     *
     * @param building l'edificio su cui valutare la politica
     * @param grid     accesso read-only alla griglia per regole spaziali
     *                 (es. "solo se c'è un parco adiacente")
     * @return il delta modificatore (eventualmente {@code zero()}, mai null)
     */
    ResourceDelta calculateModifier(IBuildingState building, IGridReadPort grid);
}