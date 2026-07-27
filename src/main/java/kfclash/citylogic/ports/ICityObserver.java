package kfclash.citylogic.ports;

import kfclash.citylogic.domain.core.CitySnapshot;

/**
 * Observer (pattern GoF Observer): chi implementa questa interfaccia viene
 * notificato a ogni tick completato con successo, ricevendo lo snapshot
 * aggiornato delle metriche cittadine.
 * <p>
 * Implementatori tipici: la Dashboard grafica (Swing/JavaFX), un logger di
 * stato, il modulo di autosalvataggio. Il Core non sa né quanti né quali
 * observer esistono — conosce solo questa interfaccia, garantendo la
 * separazione UI/logica richiesta dalle specifiche.
 */
public interface ICityObserver {

    /**
     * Invocato dopo ogni tick andato a buon fine (mai per tick falliti e
     * rollbackati).
     * <p>
     * Lo snapshot è immutabile: l'observer può conservarlo o leggerlo senza
     * rischi di mutazione concorrente. L'implementazione dovrebbe essere
     * rapida e non lanciare eccezioni (un observer lento o difettoso non
     * deve bloccare la simulazione).
     *
     * @param snapshot lo stato della città a fine tick
     */
    void onMetricsChanged(CitySnapshot snapshot);
}