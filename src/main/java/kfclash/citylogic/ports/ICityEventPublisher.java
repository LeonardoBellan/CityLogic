package it.unipd.citylogic.core.ports;

import it.unipd.citylogic.core.domain.CitySnapshot;

/**
 * Port (Output Port) e Subject del pattern Observer: il canale con cui il
 * Core pubblica verso l'esterno gli aggiornamenti di stato, senza conoscere
 * i destinatari.
 * <p>
 * Il {@code SimulationEngine} chiama solo {@link #publish(CitySnapshot)};
 * la registrazione degli observer ({@link #subscribe} /
 * {@link #unsubscribe}) avviene in fase di setup dell'applicazione (es. il
 * main che collega la Dashboard).
 * <p>
 * <b>Contratto verso il modulo UI/persistenza</b>: l'implementazione
 * concreta (es. un semplice publisher sincrono con una lista di observer)
 * può vivere fuori dal Core o esservi fornita come classe di utilità.
 */
public interface ICityEventPublisher {

    /**
     * Notifica lo snapshot a tutti gli observer registrati.
     *
     * @param snapshot lo stato della città da pubblicare (non null)
     */
    void publish(CitySnapshot snapshot);

    /**
     * Registra un observer. Registrazioni duplicate dello stesso observer
     * non devono produrre notifiche doppie.
     *
     * @param observer l'observer da registrare (non null)
     */
    void subscribe(ICityObserver observer);

    /**
     * Rimuove un observer precedentemente registrato. Rimuovere un observer
     * non registrato non è un errore (no-op).
     *
     * @param observer l'observer da rimuovere (non null)
     */
    void unsubscribe(ICityObserver observer);
}