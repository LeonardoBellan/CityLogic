package kfclash.citylogic.simulation.tick;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import kfclash.citylogic.domain.core.CitySnapshot;
import kfclash.citylogic.domain.core.ResourceDelta;
import kfclash.citylogic.ports.IBuildingState;
import kfclash.citylogic.ports.IGridReadPort;
import kfclash.citylogic.ports.IPolicyStrategy;
import kfclash.citylogic.ports.ITickPhase;

/**
 * Fase di valutazione delle politiche: applica tutte le ordinanze attive
 * (oggetti {@link IPolicyStrategy}, pattern Strategy) a ogni edificio
 * alimentato della griglia e accumula i modificatori risultanti.
 * <p>
 * È il punto di aggancio del modulo policy: attivare una nuova ordinanza
 * significa chiamare {@link #activatePolicy(IPolicyStrategy)} con un nuovo
 * oggetto strategia — nessuna modifica al Core, nessun {@code if/else} sul
 * tipo di politica.
 * <p>
 * <b>Scelta di design</b>: le politiche vengono valutate solo sugli edifici
 * alimentati, coerentemente con la {@link ProductionPhase} — un edificio
 * spento è inattivo a tutti gli effetti (non produce, quindi non viene
 * nemmeno tassato o sussidiato).
 */
public class PolicyEvaluationPhase implements ITickPhase {

    private final List<IPolicyStrategy> activePolicies = new ArrayList<>();

    /**
     * Attiva un'ordinanza. Se una politica con lo stesso nome è già attiva,
     * la chiamata è ignorata (no doppie applicazioni dello stesso effetto).
     *
     * @param policy la politica da attivare (non null)
     */
    public void activatePolicy(IPolicyStrategy policy) {
        Objects.requireNonNull(policy, "policy non può essere null");
        boolean alreadyActive = activePolicies.stream()
                .anyMatch(p -> p.getName().equals(policy.getName()));
        if (!alreadyActive) {
            activePolicies.add(policy);
        }
    }

    /**
     * Disattiva l'ordinanza con il nome indicato. Disattivare una politica
     * non attiva non è un errore (no-op).
     *
     * @param policyName il nome della politica da disattivare (non null)
     */
    public void deactivatePolicy(String policyName) {
        Objects.requireNonNull(policyName, "policyName non può essere null");
        activePolicies.removeIf(p -> p.getName().equals(policyName));
    }

    /**
     * I nomi delle politiche attualmente attive, in vista non modificabile.
     * Utile alla Dashboard per mostrare le ordinanze in vigore.
     *
     * @return lista read-only dei nomi delle politiche attive
     */
    public List<String> getActivePolicyNames() {
        return Collections.unmodifiableList(
                activePolicies.stream().map(IPolicyStrategy::getName).toList());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Per ogni edificio alimentato, somma i modificatori di tutte le
     * politiche attive. Con zero politiche attive restituisce
     * {@link ResourceDelta#zero()}.
     */
    @Override
    public ResourceDelta execute(CitySnapshot snapshot, IGridReadPort grid) {
        ResourceDelta total = ResourceDelta.zero();

        for (IBuildingState building : grid.getAllBuildings()) {
            if (!building.isPowered()) {
                continue;
            }
            for (IPolicyStrategy policy : activePolicies) {
                total = total.merge(policy.calculateModifier(building, grid));
            }
        }
        return total;
    }
}