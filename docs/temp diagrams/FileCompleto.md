# Citylogic — Core Logic Design (v2, pronto per lo sviluppo)

## Domain Model

```mermaid
classDiagram
    direction TB

    %% Nucleo Urbano ed Entità
    class EntitaMappa {
        <<abstract>>
        identificativo
        integritaStrutturale
    }
    class Edificio {
        <<abstract>>
        consumoEnergetico
        impattoEconomicoLocale
    }
    class EdificioResidenziale {
        densitaAbitativa
    }
    class EdificioCommerciale {
        postiLavoroGenerati
    }
    class EdificioIndustriale {
        livelloInquinamentoProdotto
    }
    class Infrastruttura {
        <<abstract>>
    }
    class CentraleElettrica {
        raggioCopertura
        energiaGenerata
    }
    class Parco {
        bonusFelicita
    }
    class Strada {
        capacitaTraffico
    }

    %% Stato e Dinamiche Globali
    class Citta {
        nome
    }
    class StatoMetrico {
        budgetGlobale
        inquinamentoTotale
        popolazioneTotale
        felicitaCollettiva
        conteggioTick
    }
    class GrigliaLogica {
        righe
        colonne
    }
    class Cella {
        coordinataX
        coordinataY
        eAlimentata
    }

    %% Sistemi Regolatori ed Eventi
    class OrdinanzaCittadina {
        codiceIdentificativo
        nomePolitica
        gradoPriorita
    }
    class GruppoCittadini {
        tipologiaSociale
        livelloSoddisfazioneSpecifico
        requisitiOccupazionali
    }
    class EventoCasuale {
        naturaEvento
        tassoProbabilita
        magnitudoDanno
    }

    %% Relazioni e Molteplicità
    Citta "1" *-- "1" StatoMetrico : è caratterizzata da
    Citta "1" *-- "1" GrigliaLogica : possiede
    GrigliaLogica "1" *-- "400" Cella : si articola in
    Cella "1" o-- "0..1" EntitaMappa : ospita
    EntitaMappa <|-- Edificio
    EntitaMappa <|-- Infrastruttura
    Edificio <|-- EdificioResidenziale
    Edificio <|-- EdificioCommerciale
    Edificio <|-- EdificioIndustriale
    Infrastruttura <|-- CentraleElettrica
    Infrastruttura <|-- Parco
    Infrastruttura <|-- Strada
    Citta "1" *-- "0..*" OrdinanzaCittadina : applica
    Citta "1" *-- "1..*" GruppoCittadini : ospita
    Citta "1" --> "0..*" EventoCasuale : è soggetta a
    GruppoCittadini "1..*" -- "1..*" Edificio : occupa
    OrdinanzaCittadina "1" -- "0..*" EntitaMappa : influenza
```

**Tracciabilità domain model → design model** (utile per la discussione): `Citta` + `StatoMetrico` → `CityAggregate`; lo stato esportabile di `StatoMetrico` → `CitySnapshot`; `OrdinanzaCittadina` → `IPolicyStrategy`; le entità della griglia (`GrigliaLogica`, `Cella`, gerarchia `EntitaMappa`) restano nel dominio ma nel design del Core compaiono solo dietro i port `IGridReadPort`/`IBuildingState`, perché la loro implementazione è responsabilità del modulo mappa. `GruppoCittadini` ed `EventoCasuale` sono funzionalità opzionali della consegna: nel domain model documentano la visione completa, nel design class diagram entreranno solo se le implementate (un `EventoCasuale` diventerebbe naturalmente una `ITickPhase` aggiuntiva o un evento pubblicato via Observer).

## Design Class Diagram

```mermaid
classDiagram
    direction TB

    %% ==========================================
    %% Core Domain
    %% ==========================================
    class ResourceDelta {
        <<Record / ValueObject>>
        +budgetDelta: BigDecimal
        +pollutionDelta: double
        +populationDelta: int
        +happinessDelta: double
        +merge(other: ResourceDelta) ResourceDelta
        +zero()$ ResourceDelta
    }

    class CitySnapshot {
        <<Record / DTO>>
        +budget: BigDecimal
        +pollution: double
        +population: int
        +happiness: double
        +tickCount: int
    }

    class CityAggregate {
        <<Aggregate Root>>
        -budget: BigDecimal
        -pollution: double
        -population: int
        -happiness: double
        -tickCount: int
        +applyDelta(delta: ResourceDelta) void
        +exportSnapshot() CitySnapshot
        +restoreFromSnapshot(snapshot: CitySnapshot) void
        -validateInvariants() void
    }

    %% ==========================================
    %% Ports (Boundary Interfaces)
    %% ==========================================
    class IBuildingState {
        <<interface / InputPort>>
        +getId() String
        +getType() String
        +isPowered() boolean
        +getBaseProduction() ResourceDelta
    }

    class IGridReadPort {
        <<interface / InputPort>>
        +getBuildingById(id: String) Optional~IBuildingState~
        +getAllBuildings() List~IBuildingState~
        +getAdjacentBuildings(id: String, radius: int) List~IBuildingState~
    }

    class ICityObserver {
        <<interface / Observer>>
        +onMetricsChanged(snapshot: CitySnapshot) void
    }

    class ICityEventPublisher {
        <<interface / OutputPort>>
        +publish(snapshot: CitySnapshot) void
        +subscribe(observer: ICityObserver) void
        +unsubscribe(observer: ICityObserver) void
    }

    %% ==========================================
    %% Application Logic (Use Cases & Patterns)
    %% ==========================================
    class IPolicyStrategy {
        <<interface / Strategy>>
        +getName() String
        +calculateModifier(building: IBuildingState, grid: IGridReadPort) ResourceDelta
    }

    class ITickPhase {
        <<interface / Strategy>>
        +execute(snapshot: CitySnapshot, grid: IGridReadPort) ResourceDelta
    }

    class ProductionPhase {
        +execute(snapshot: CitySnapshot, grid: IGridReadPort) ResourceDelta
    }

    class PolicyEvaluationPhase {
        -activePolicies: List~IPolicyStrategy~
        +activatePolicy(policy: IPolicyStrategy) void
        +deactivatePolicy(policyName: String) void
        +execute(snapshot: CitySnapshot, grid: IGridReadPort) ResourceDelta
    }

    class TickPhaseFactory {
        <<Factory>>
        +createPhases(config: SimulationConfig) List~ITickPhase~
    }

    class SimulationConfig {
        <<Record / ValueObject>>
        +enabledPhases: List~String~
    }

    class SimulationEngine {
        <<Facade / Orchestrator>>
        -cityState: CityAggregate
        -phases: List~ITickPhase~
        -gridReader: IGridReadPort
        -eventPublisher: ICityEventPublisher
        +SimulationEngine(city: CityAggregate, grid: IGridReadPort, pub: ICityEventPublisher, factory: TickPhaseFactory, config: SimulationConfig)
        +advanceTick() void
        +getCurrentSnapshot() CitySnapshot
        +loadState(snapshot: CitySnapshot) void
    }

    %% ==========================================
    %% Relazioni Architetturali
    %% ==========================================
    CityAggregate ..> ResourceDelta : usa
    CityAggregate ..> CitySnapshot : produce / ripristina da

    ICityEventPublisher o-- ICityObserver : notifica

    IPolicyStrategy ..> IBuildingState : interroga
    IPolicyStrategy ..> IGridReadPort : legge contesto spaziale

    ITickPhase <|.. ProductionPhase : implementa
    ITickPhase <|.. PolicyEvaluationPhase : implementa
    ITickPhase ..> CitySnapshot : legge (read-only)
    PolicyEvaluationPhase o-- IPolicyStrategy : delega calcolo a
    ProductionPhase ..> IBuildingState : somma produzione base

    SimulationEngine o-- ITickPhase : orchestra pipeline
    TickPhaseFactory ..> ProductionPhase : istanzia
    TickPhaseFactory ..> PolicyEvaluationPhase : istanzia
    TickPhaseFactory ..> SimulationConfig : legge

    SimulationEngine --> CityAggregate : muta stato interno
    SimulationEngine --> IGridReadPort : interroga mappa
    SimulationEngine --> ICityEventPublisher : solleva eventi
    SimulationEngine ..> TickPhaseFactory : usa per configurare pipeline
```

## Internal Sequence Diagram — advanceTick()

```mermaid
sequenceDiagram
    participant Caller as Client (Timer / UI)
    participant Engine as engine: SimulationEngine
    participant City as cityState: CityAggregate
    participant Phase as phase_i: ITickPhase
    participant Grid as gridReader: IGridReadPort
    participant Strategy as strategy: IPolicyStrategy
    participant Pub as publisher: ICityEventPublisher

    Caller->>+Engine: advanceTick()

    %% 1. Snapshot iniziale: backup per rollback + vista read-only per le fasi
    Note over Engine,City: 1. Backup per Transazionalità (Atomicity)
    Engine->>+City: exportSnapshot()
    City-->>-Engine: startSnapshot (CitySnapshot)

    %% 2. Pipeline delle fasi (iniettate dalla Factory)
    Note over Engine,Phase: 2. Pipeline Esecutiva: iterazione delle fasi iniettate
    loop Per ogni phase in phases
        Engine->>+Phase: execute(startSnapshot, gridReader)

        opt Se la fase corrente è PolicyEvaluationPhase
            Phase->>+Grid: getAllBuildings()
            Grid-->>-Phase: List~IBuildingState~

            loop Per ogni activePolicy
                Phase->>+Strategy: calculateModifier(building, gridReader)
                Strategy-->>-Phase: partialDelta (ResourceDelta)
            end
        end

        Phase-->>-Engine: phaseDelta (ResourceDelta)
        Note right of Engine: totalDelta = totalDelta.merge(phaseDelta)
    end

    %% 3. Commit transazionale e validazione invarianti
    Note over Engine,City: 3. Commit e Verifica Invarianti
    Engine->>+City: applyDelta(totalDelta)
    City->>City: validateInvariants()

    alt Invarianti rispettati (commit)
        City-->>-Engine: void (stato accettato)

        Engine->>+City: exportSnapshot()
        City-->>-Engine: currentSnapshot (CitySnapshot)

        Engine->>+Pub: publish(currentSnapshot)
        Note right of Pub: Observer Pattern:<br/>notifica Dashboard / Logger
        Pub-->>-Engine: void

        Engine-->>-Caller: void (tick completato)

    else Violazione invarianti (rollback)
        City-->>Engine: throws IllegalStateException

        Note right of Engine: Rollback allo stato pre-tick
        Engine->>+City: restoreFromSnapshot(startSnapshot)
        City-->>-Engine: void (ripristino completato)

        Engine-->>Caller: throws SimulationException("Tick fallito")
    end
```

## Note di design (da riusare nel design doc)

**Pattern impiegati**
- **Strategy** (requisito d'esame): `IPolicyStrategy` per le ordinanze (parte di Ludo) e `ITickPhase` per le fasi della pipeline — cambiare politica significa scambiare un oggetto strategia, zero `if/else`.
- **Factory** (requisito d'esame): `TickPhaseFactory` costruisce la pipeline; il `SimulationEngine` non conosce le classi concrete delle fasi.
- **Observer** (requisito d'esame): `ICityEventPublisher`/`ICityObserver` disaccoppiano il Core dalla Dashboard (parte di Cesco) — la UI è strettamente separata dalla logica come da specifica.
- **Facade**: `SimulationEngine` espone al resto del sistema solo `advanceTick()`, `getCurrentSnapshot()`, `loadState()`.

**Scelte chiave e motivazioni (per la discussione orale)**
1. **Le fasi ricevono `CitySnapshot`, non `CityAggregate`**: tutte le fasi leggono lo stesso stato immutabile di inizio tick e restituiscono solo delta. Nessuna fase può mutare lo stato direttamente → la transazionalità è garantita per costruzione, l'ordine delle fasi non crea letture incoerenti, e ogni fase è testabile in isolamento con un semplice record.
2. **`ResourceDelta` come record immutabile** con `merge()`: l'engine accumula i delta e applica un solo `applyDelta()` atomico.
3. **Rollback**: se `validateInvariants()` fallisce (es. budget sotto la soglia minima consentita), lo stato viene ripristinato dallo snapshot di inizio tick e l'errore propagato come `SimulationException`.
4. **Port verso la mappa** (`IGridReadPort`, `IBuildingState`): il Core dipende solo da interfacce, mai dal `MapManager` concreto di Leo → da concordare con lui che le implementi (o un adapter).
5. **`getBaseProduction()` su `IBuildingState`**: senza, `ProductionPhase` non avrebbe dati su cui lavorare; le policy lo usano anche come base per i modificatori.
6. **`getCurrentSnapshot()`/`loadState()`** sull'engine: punto di aggancio per il save/load di Cesco per la parte metriche (la griglia la serializza lui separatamente).

## Ordine di sviluppo consigliato

1. `ResourceDelta`, `CitySnapshot` (record) + unit test su `merge()`
2. `CityAggregate` con `applyDelta`/`validateInvariants`/snapshot + test (incluso edge case budget negativo)
3. Interfacce: `ITickPhase`, `IPolicyStrategy`, `IGridReadPort`, `IBuildingState`, `ICityEventPublisher`, `ICityObserver`
4. `ProductionPhase` + `PolicyEvaluationPhase` testate con stub/mock di `IGridReadPort`
5. `TickPhaseFactory` + `SimulationConfig`
6. `SimulationEngine.advanceTick()` con test di integrazione su commit e rollback
