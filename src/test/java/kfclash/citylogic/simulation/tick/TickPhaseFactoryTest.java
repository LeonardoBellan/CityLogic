package kfclash.citylogic.simulation.tick;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kfclash.citylogic.ports.ITickPhase;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test per {@link TickPhaseFactory} e {@link SimulationConfig}.
 */
class TickPhaseFactoryTest {

    private final TickPhaseFactory factory = new TickPhaseFactory();

    @Test
    @DisplayName("la config di default crea produzione e politiche, in quest'ordine")
    void defaultConfigCreatesProductionThenPolicy() {
        List<ITickPhase> phases = factory.createPhases(SimulationConfig.defaultConfig());

        assertEquals(2, phases.size());
        assertInstanceOf(ProductionPhase.class, phases.get(0));
        assertInstanceOf(PolicyEvaluationPhase.class, phases.get(1));
    }

    @Test
    @DisplayName("una config parziale crea solo le fasi richieste")
    void partialConfigCreatesOnlyRequestedPhases() {
        SimulationConfig onlyProduction = new SimulationConfig(List.of(TickPhaseFactory.PHASE_PRODUCTION));

        List<ITickPhase> phases = factory.createPhases(onlyProduction);

        assertEquals(1, phases.size());
        assertInstanceOf(ProductionPhase.class, phases.get(0));
    }

    @Test
    @DisplayName("un nome di fase sconosciuto viene rifiutato con messaggio chiaro")
    void unknownPhaseNameIsRejected() {
        SimulationConfig bad = new SimulationConfig(List.of("DISASTER"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> factory.createPhases(bad));
        assertTrue(ex.getMessage().contains("DISASTER"));
    }

    @Test
    @DisplayName("ogni chiamata crea istanze nuove (nessuno stato condiviso tra engine)")
    void eachCallCreatesFreshInstances() {
        List<ITickPhase> first = factory.createPhases(SimulationConfig.defaultConfig());
        List<ITickPhase> second = factory.createPhases(SimulationConfig.defaultConfig());

        assertNotSame(first.get(0), second.get(0));
        assertNotSame(first.get(1), second.get(1));
    }

    @Test
    @DisplayName("SimulationConfig è un value object: la lista interna è immutabile")
    void configListIsImmutable() {
        SimulationConfig config = SimulationConfig.defaultConfig();

        assertThrows(UnsupportedOperationException.class,
                () -> config.enabledPhases().add("HACK"));
    }
}