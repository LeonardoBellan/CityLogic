package it.unipd.citylogic.core.tick;

import it.unipd.citylogic.core.domain.CitySnapshot;
import it.unipd.citylogic.core.domain.ResourceDelta;
import it.unipd.citylogic.core.ports.IBuildingState;
import it.unipd.citylogic.core.ports.IGridReadPort;

/**
 * Fase di produzione: somma il contributo base di tutti gli edifici
 * <b>alimentati</b> presenti sulla griglia.
 * <p>
 * Implementa la regola di business della consegna "gli edifici non
 * alimentati non producono" (es. zone residenziali senza centrale
 * elettrica vicina): gli edifici con {@code isPowered() == false} vengono
 * semplicemente saltati, senza produrre né costare nulla.
 * <p>
 * La fase è stateless e pura: legge la griglia, restituisce un delta.
 */
public class ProductionPhase implements ITickPhase {

    /**
     * {@inheritDoc}
     * <p>
     * Itera tutti gli edifici della griglia e accumula via
     * {@link ResourceDelta#merge(ResourceDelta)} la produzione base dei
     * soli edifici alimentati.
     */
    @Override
    public ResourceDelta execute(CitySnapshot snapshot, IGridReadPort grid) {
        ResourceDelta total = ResourceDelta.zero();

        for (IBuildingState building : grid.getAllBuildings()) {
            if (building.isPowered()) {
                total = total.merge(building.getBaseProduction());
            }
        }
        return total;
    }
}