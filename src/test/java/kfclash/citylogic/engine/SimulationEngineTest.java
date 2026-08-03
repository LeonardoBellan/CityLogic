package kfclash.citylogic.engine;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kfclash.citylogic.domain.core.CityAggregate;
import kfclash.citylogic.domain.core.CitySnapshot;
import kfclash.citylogic.domain.core.ResourceDelta;
import kfclash.citylogic.domain.core.SimulationException;
import kfclash.citylogic.ports.IBuildingState;
import kfclash.citylogic.ports.ICityEventPublisher;
import kfclash.citylogic.ports.ICityObserver;
import kfclash.citylogic.ports.IGridReadPort;
import kfclash.citylogic.ports.IPolicyStrategy;
import kfclash.citylogic.testutil.FakeGridReadPort;
import kfclash.citylogic.tick.SimulationConfig;
import kfclash.citylogic.tick.TickPhaseFactory;

/**
 * Test di integrazione del {@link SimulationEngine}: verifica il flusso
 * completo del tick documentato nell'internal sequence diagram (pipeline,
 * commit atomico, rollback, notifica observer), usando stub al posto dei
 * moduli esterni (mappa e UI).
 */
class SimulationEngineTest {

    /**
     * Publisher di test: registra gli snapshot pubblicati, così i test
     * possono verificare quando e con che payload gli observer vengono
     * notificati.
     */
    private static class RecordingPublisher implements ICityEventPublisher {
        final List<CitySnapshot> published = new ArrayList<>();

        @Override
        public void publish(CitySnapshot snapshot) {
            published.add(snapshot);
        }

        @Override
        public void subscribe(ICityObserver observer) {
            // non necessario per questi test
        }

        @Override
        public void unsubscribe(ICityObserver observer) {
            // non necessario per questi test
        }
    }

    private CityAggregate city;
    private FakeGridReadPort grid;
    private RecordingPublisher publisher;
    private SimulationEngine engine;

    @BeforeEach
    void setUp() {
        city = new CityAggregate(new BigDecimal("1000"), 50, 50.0);
        grid = new FakeGridReadPort();
        publisher = new RecordingPublisher();
        engine = new SimulationEngine(city, grid, publisher,
                new TickPhaseFactory(), SimulationConfig.defaultConfig());
    }

    @Test
    @DisplayName("tick riuscito: applica i delta della pipeline e notifica gli observer")
    void successfulTickAppliesDeltasAndPublishes() {
        // fabbrica alimentata: +200 budget, +5 inquinamento
        grid.addBuilding("f1", "INDUSTRIAL", true,
                new ResourceDelta(new BigDecimal("200"), 5.0, 0, 0.0), 0, 0);

        engine.advanceTick();
        CitySnapshot s = engine.getCurrentSnapshot();

        assertEquals(0, new BigDecimal("1200").compareTo(s.budget()));
        assertEquals(5.0, s.pollution(), 1e-9);
        assertEquals(1, s.tickCount());
        // un solo publish, con lo stato post-commit
        assertEquals(List.of(s), publisher.published);
    }

    @Test
    @DisplayName("tick fallito (bancarotta): rollback completo e nessuna notifica")
    void failedTickRollsBackAndDoesNotPublish() {
        // costo mostruoso: manda il budget sotto la soglia di bancarotta
        grid.addBuilding("money-pit", "INDUSTRIAL", true,
                new ResourceDelta(new BigDecimal("-50000"), 0.0, 0, 0.0), 0, 0);
        CitySnapshot before = engine.getCurrentSnapshot();

        assertThrows(SimulationException.class, () -> engine.advanceTick());

        // lo stato è identico a prima del tick (tickCount incluso)...
        assertEquals(before, engine.getCurrentSnapshot());
        // ...e gli observer non hanno mai visto lo stato invalido
        assertTrue(publisher.published.isEmpty());
    }

    @Test
    @DisplayName("dopo un tick fallito la simulazione può proseguire")
    void simulationContinuesAfterFailedTick() {
        grid.addBuilding("money-pit", "INDUSTRIAL", true,
                new ResourceDelta(new BigDecimal("-50000"), 0.0, 0, 0.0), 0, 0);
        assertThrows(SimulationException.class, () -> engine.advanceTick());

        // l'utente "demolisce" l'edificio rovinoso: nuova griglia, stesso engine?
        // No: la griglia è esterna; qui simuliamo il caso minimo di un tick
        // successivo valido ricreando l'engine sulla stessa città con griglia sana.
        FakeGridReadPort healthyGrid = new FakeGridReadPort()
                .addBuilding("p1", "PARK", true,
                        new ResourceDelta(new BigDecimal("-10"), 0.0, 0, 2.0), 0, 0);
        SimulationEngine engine2 = new SimulationEngine(city, healthyGrid, publisher,
                new TickPhaseFactory(), SimulationConfig.defaultConfig());

        assertDoesNotThrow(engine2::advanceTick);
        assertEquals(1, engine2.getCurrentSnapshot().tickCount());
    }

    @Test
    @DisplayName("le politiche attivate via engine influenzano il tick (Strategy end-to-end)")
    void activatedPoliciesAffectTheTick() {
        grid.addBuilding("f1", "INDUSTRIAL", true,
                new ResourceDelta(new BigDecimal("200"), 5.0, 0, 0.0), 0, 0);

        // tassa ambientale: +100 budget per ogni fabbrica
        engine.activatePolicy(new IPolicyStrategy() {
            @Override
            public String getName() {
                return "Tassa Ambientale";
            }

            @Override
            public ResourceDelta calculateModifier(IBuildingState b, IGridReadPort g) {
                return b.getType().equals("INDUSTRIAL")
                        ? new ResourceDelta(new BigDecimal("100"), 0.0, 0, 0.0)
                        : ResourceDelta.zero();
            }
        });

        engine.advanceTick();

        // 1000 + 200 (produzione) + 100 (tassa) = 1300
        assertEquals(0, new BigDecimal("1300")
                .compareTo(engine.getCurrentSnapshot().budget()));
        assertEquals(List.of("Tassa Ambientale"), engine.getActivePolicyNames());
    }

    @Test
    @DisplayName("disattivare una politica ne rimuove l'effetto dal tick successivo")
    void deactivatedPolicyNoLongerAffectsTicks() {
        grid.addBuilding("f1", "INDUSTRIAL", true,
                new ResourceDelta(new BigDecimal("100"), 0.0, 0, 0.0), 0, 0);
        engine.activatePolicy(new IPolicyStrategy() {
            @Override
            public String getName() {
                return "Tassa";
            }

            @Override
            public ResourceDelta calculateModifier(IBuildingState b, IGridReadPort g) {
                return new ResourceDelta(new BigDecimal("50"), 0.0, 0, 0.0);
            }
        });

        engine.advanceTick(); // 1000 + 100 + 50 = 1150
        engine.deactivatePolicy("Tassa");
        engine.advanceTick(); // 1150 + 100 = 1250

        assertEquals(0, new BigDecimal("1250")
                .compareTo(engine.getCurrentSnapshot().budget()));
    }

    @Test
    @DisplayName("attivare la stessa politica più volte non duplica gli effetti")
    void duplicatePolicyActivationIsIgnored() {
        grid.addBuilding("f1", "INDUSTRIAL", true,
                new ResourceDelta(new BigDecimal("100"), 0.0, 0, 0.0), 0, 0);
        IPolicyStrategy policy = new IPolicyStrategy() {
            @Override
            public String getName() {
                return "Tassa";
            }

            @Override
            public ResourceDelta calculateModifier(IBuildingState b, IGridReadPort g) {
                return new ResourceDelta(new BigDecimal("50"), 0.0, 0, 0.0);
            }
        };

        engine.activatePolicy(policy);
        engine.activatePolicy(policy);
        engine.advanceTick();

        assertEquals(List.of("Tassa"), engine.getActivePolicyNames());
        assertEquals(0, new BigDecimal("1150").compareTo(engine.getCurrentSnapshot().budget()));
    }

    @Test
    @DisplayName("se la pipeline non include la fase policy, attivare una politica fallisce in modo chiaro")
    void activatingPolicyWithoutPolicyPhaseThrowsHelpfulException() {
        SimulationEngine engineWithoutPolicyPhase = new SimulationEngine(city, grid, publisher,
                new TickPhaseFactory(), new SimulationConfig(List.of(TickPhaseFactory.PHASE_PRODUCTION)));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> engineWithoutPolicyPhase.activatePolicy(new IPolicyStrategy() {
                    @Override
                    public String getName() {
                        return "Tassa";
                    }

                    @Override
                    public ResourceDelta calculateModifier(IBuildingState b, IGridReadPort g) {
                        return ResourceDelta.zero();
                    }
                }));

        assertTrue(exception.getMessage().contains("POLICY"));
    }

    @Test
    @DisplayName("loadState ripristina una partita salvata")
    void loadStateRestoresSavedGame() {
        CitySnapshot saved = new CitySnapshot(
                new BigDecimal("9999"), 12.5, 300, 80.0, 42);

        engine.loadState(saved);

        assertEquals(saved, engine.getCurrentSnapshot());
    }

    @Test
    @DisplayName("più tick consecutivi accumulano correttamente (smoke test)")
    void multipleTicksAccumulate() {
        grid.addBuilding("f1", "INDUSTRIAL", true,
                new ResourceDelta(new BigDecimal("10"), 1.0, 1, 0.0), 0, 0);

        for (int i = 0; i < 5; i++) {
            engine.advanceTick();
        }
        CitySnapshot s = engine.getCurrentSnapshot();

        assertEquals(0, new BigDecimal("1050").compareTo(s.budget()));
        assertEquals(5.0, s.pollution(), 1e-9);
        assertEquals(55, s.population());
        assertEquals(5, s.tickCount());
        assertEquals(5, publisher.published.size());
    }

    @Test
    @DisplayName("dipendenze null vengono rifiutate alla costruzione")
    void nullDependenciesAreRejected() {
        TickPhaseFactory f = new TickPhaseFactory();
        SimulationConfig c = SimulationConfig.defaultConfig();

        assertThrows(NullPointerException.class,
                () -> new SimulationEngine(null, grid, publisher, f, c));
        assertThrows(NullPointerException.class,
                () -> new SimulationEngine(city, null, publisher, f, c));
        assertThrows(NullPointerException.class,
                () -> new SimulationEngine(city, grid, null, f, c));
        assertThrows(NullPointerException.class,
                () -> new SimulationEngine(city, grid, publisher, null, c));
        assertThrows(NullPointerException.class,
                () -> new SimulationEngine(city, grid, publisher, f, null));
    }
}