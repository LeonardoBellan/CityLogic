```mermaid

classDiagram
    direction TB

    %% ==========================================
    %% Core Domain (Infrastrutture Agnostiche)
    %% ==========================================
    class ResourceDelta {
        <<Record / ValueObject>>
        +BigDecimal budgetDelta
        +double pollutionDelta
        +int populationDelta
        +double happinessDelta
        +merge(other ResourceDelta) ResourceDelta
        +zero() ResourceDelta$
    }
    class CitySnapshot {
        <<DTO>>
        +BigDecimal budget
        +double pollution
        +int population
        +double happiness
        +int tickCount
    }
    class CityAggregate {
        <<Aggregate Root>>
        -BigDecimal budget
        -double pollution
        -int population
        -double happiness
        -int tickCount
        +applyDelta(delta ResourceDelta) void
        +exportSnapshot() CitySnapshot
        +restoreFromSnapshot(snapshot CitySnapshot) void
        -validateInvariants() void
    }

    %% ==========================================
    %% Ports (Boundary Interfaces)
    %% ==========================================
    class IBuildingState {
        <<interface>>
        +getId() String
        +getType() String
        +isPowered() boolean
    }
    class IGridReadPort {
        <<interface / InputPort>>
        +getBuildingById(id String) Optional~IBuildingState~
        +getAllBuildings() List~IBuildingState~
        +getAdjacentBuildings(id String, radius int) List~IBuildingState~
    }
    class ICityObserver {
        <<interface / Observer>>
        +onMetricsChanged(snapshot CitySnapshot) void
    }
    class ICityEventPublisher {
        <<interface / OutputPort>>
        +publish(snapshot CitySnapshot) void
        +subscribe(observer ICityObserver) void
    }

    %% ==========================================
    %% Application Logic (Use Cases & Patterns)
    %% ==========================================
    class IPolicyStrategy {
        <<interface / Strategy>>
        +calculateModifier(building IBuildingState, grid IGridReadPort) ResourceDelta
    }
    class ITickPhase {
        <<interface / Strategy>>
        +execute(city CityAggregate, grid IGridReadPort) ResourceDelta
    }
    class ProductionPhase {
        +execute(city CityAggregate, grid IGridReadPort) ResourceDelta
    }
    class PolicyEvaluationPhase {
        -List~IPolicyStrategy~ activePolicies
        +execute(city CityAggregate, grid IGridReadPort) ResourceDelta
    }
    class TickPhaseFactory {
        <<Factory>>
        +createPhases(config Map) List~ITickPhase~
    }
    class SimulationEngine {
        <<Facade / Orchestrator>>
        -CityAggregate cityState
        -List~ITickPhase~ phases
        -IGridReadPort gridReader
        -ICityEventPublisher eventPublisher
        +SimulationEngine(city CityAggregate, grid IGridReadPort, pub ICityEventPublisher, factory TickPhaseFactory)
        +advanceTick() void
    }

    %% ==========================================
    %% Relazioni Architetturali
    %% ==========================================
    CityAggregate ..> ResourceDelta : usa
    CityAggregate ..> CitySnapshot : produce
    
    ICityEventPublisher o-- ICityObserver : notifica
    
    IPolicyStrategy ..> IBuildingState : interroga
    IPolicyStrategy ..> IGridReadPort : legge contesto spaziale
    
    ITickPhase <|.. ProductionPhase : implementa
    ITickPhase <|.. PolicyEvaluationPhase : implementa
    PolicyEvaluationPhase o-- IPolicyStrategy : delega calcolo a
    
    SimulationEngine o-- ITickPhase : orchestra pipeline
    TickPhaseFactory ..> ProductionPhase : istanzia
    TickPhaseFactory ..> PolicyEvaluationPhase : istanzia
    
    SimulationEngine --> CityAggregate : muta stato interno
    SimulationEngine --> IGridReadPort : interroga mappa
    SimulationEngine --> ICityEventPublisher : solleva eventi
    SimulationEngine ..> TickPhaseFactory : usa per configurare pipeline

   ``` 
