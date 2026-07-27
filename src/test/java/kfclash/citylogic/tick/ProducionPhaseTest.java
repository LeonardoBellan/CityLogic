package kfclash.citylogic.tick;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kfclash.citylogic.domain.core.CitySnapshot;
import kfclash.citylogic.domain.core.ResourceDelta;
import kfclash.citylogic.testutil.FakeGridReadPort;

/**
 * Unit test per {@link ProductionPhase}, eseguiti contro lo stub
 * {@link FakeGridReadPort}: il Core è testabile senza il modulo mappa.
 */
class ProductionPhaseTest {

    private ProductionPhase phase;
    private FakeGridReadPort grid;
    private CitySnapshot snapshot;

    @BeforeEach
    void setUp() {
        phase = new ProductionPhase();
        grid = new FakeGridReadPort();
        snapshot = new CitySnapshot(new BigDecimal("1000"), 0.0, 50, 50.0, 0);
    }

    @Test
    @DisplayName("griglia vuota produce delta zero")
    void emptyGridProducesZero() {
        assertEquals(ResourceDelta.zero(), phase.execute(snapshot, grid));
    }

    @Test
    @DisplayName("somma la produzione di tutti gli edifici alimentati")
    void sumsProductionOfPoweredBuildings() {
        // fabbrica: +200 budget, +5 inquinamento
        grid.addBuilding("f1", "INDUSTRIAL", true,
                new ResourceDelta(new BigDecimal("200"), 5.0, 0, -1.0), 0, 0);
        // parco: -50 budget (manutenzione), +3 felicità
        grid.addBuilding("p1", "PARK", true,
                new ResourceDelta(new BigDecimal("-50"), -1.0, 0, 3.0), 5, 5);

        ResourceDelta result = phase.execute(snapshot, grid);

        assertEquals(0, new BigDecimal("150").compareTo(result.budgetDelta()));
        assertEquals(4.0, result.pollutionDelta(), 1e-9);
        assertEquals(2.0, result.happinessDelta(), 1e-9);
    }

    @Test
    @DisplayName("gli edifici non alimentati non producono (regola di business)")
    void unpoweredBuildingsAreSkipped() {
        grid.addBuilding("f1", "INDUSTRIAL", true,
                new ResourceDelta(new BigDecimal("200"), 5.0, 0, 0.0), 0, 0);
        // stessa fabbrica ma spenta: deve essere ignorata del tutto
        grid.addBuilding("f2", "INDUSTRIAL", false,
                new ResourceDelta(new BigDecimal("200"), 5.0, 0, 0.0), 1, 0);

        ResourceDelta result = phase.execute(snapshot, grid);

        assertEquals(0, new BigDecimal("200").compareTo(result.budgetDelta()));
        assertEquals(5.0, result.pollutionDelta(), 1e-9);
    }

    @Test
    @DisplayName("la fase non muta la griglia (purezza)")
    void phaseDoesNotMutateGrid() {
        grid.addBuilding("f1", "INDUSTRIAL", true,
                new ResourceDelta(new BigDecimal("200"), 5.0, 0, 0.0), 0, 0);

        phase.execute(snapshot, grid);
        phase.execute(snapshot, grid);

        // due esecuzioni identiche producono lo stesso risultato:
        // nessuno stato nascosto né nella fase né nella griglia
        assertEquals(phase.execute(snapshot, grid), phase.execute(snapshot, grid));
        assertEquals(1, grid.getAllBuildings().size());
    }
}