# CityLogic domain design

This document describes the implemented Building/map domain and simulation engine. The Java source and tests are authoritative when this document and an older diagram disagree.

## Scope and boundaries

| Area                      | Responsibility                                                                             | Main types                                                                   |
| ------------------------- | ------------------------------------------------------------------------------------------ | ---------------------------------------------------------------------------- |
| Building/map domain       | Store the grid, query spatial state, validate footprints, place and remove buildings       | `Grid`, `Cell`, `BuildingDescription`, `BuildingInstance`, `BuildingFactory` |
| Application orchestration | Resolve building types, validate commands, and delegate map/time operations                | `GameEngine`, `BuildingCatalog`, `PlacementValidator`                        |
| Simulation engine         | Execute phases, aggregate metric deltas, and commit or roll back city state                | `SimulationEngine`, `ITickPhase`, `CityAggregate`, `ResourceDelta`           |
| Policy                    | Provide strategies evaluated by a simulation phase; detailed policy rules are out of scope | `IPolicyStrategy`, `PolicyEvaluationPhase`                                   |
| UI/presentation           | Consume commands and published snapshots; out of scope here                                | Presentation module                                                          |

`Grid` implements both `IGridReadPort` and `IGridCommandPort`. The simulation engine receives only `IGridReadPort`, so phases cannot mutate the map. `GameEngine` is the application-facing facade used by the presentation layer.

## Building and map domain

```mermaid
classDiagram
    direction TB
    class IGridReadPort { <<interface>>
        +getTerrainAt(x: int, y: int) String
        +getBuildingById(id: String) Optional~IBuildingState~
        +getAllBuildings() List~IBuildingState~
        +getAdjacentBuildings(id: String, radius: int) List~IBuildingState~
        +isAreaFree(x: int, y: int, footprint: Dimension) boolean
    }
    class IGridCommandPort { <<interface>>
        +constructBuildingAt(x: int, y: int, desc: BuildingDescription) IBuildingState
        +removeBuildingAt(x: int, y: int) IBuildingState
    }
    class IBuildingState { <<interface>>
        +getId() String
        +getType() String
        +getPosition() Point
        +getDescription() BuildingDescription
        +isPowered() boolean
        +getBaseProduction() ResourceDelta
        +getCurrentProduction() ResourceDelta
    }
    class Grid {
        -dimensions: Dimension
        -map: Cell[][]
        -factory: BuildingFactory
        -activeBuildings: Map~String, BuildingInstance~
        +getDimensions() Dimension
        +getCell(x: int, y: int) Cell
        +getTerrainAt(x: int, y: int) String
        +getBuildingById(id: String) Optional~IBuildingState~
        +getAllBuildings() List~IBuildingState~
        +getAdjacentBuildings(id: String, radius: int) List~IBuildingState~
        +isAreaFree(x: int, y: int, footprint: Dimension) boolean
        +constructBuildingAt(x: int, y: int, desc: BuildingDescription) BuildingInstance
        +removeBuildingAt(x: int, y: int) BuildingInstance
    }
    class Cell {
        -position: Point
        -pollutionLevel: int
        -currentBuilding: BuildingInstance
        +setBuilding(building: BuildingInstance) void
        +clear() void
        +getBuilding() BuildingInstance
        +isOccupied() boolean
    }
    class BuildingDescription {
        <<immutable metadata>>
        -typeId: String
        -name: String
        -constructionCost: int
        -baseMaintenanceCost: int
        -footprint: Dimension
        -baseProduction: ResourceDelta
        +getTypeId() String
        +getName() String
        +getConstructionCost() int
        +getBaseMaintenanceCost() int
        +getFootprint() Dimension
        +getBaseProduction() ResourceDelta
    }
    class BuildingInstance {
        <<entity>>
        -id: String
        -description: BuildingDescription
        -position: Point
        -isPowered: boolean
        -currentMaintenanceCost: int
        +getId() String
        +getType() String
        +getPosition() Point
        +getDescription() BuildingDescription
        +isPowered() boolean
        +getBaseProduction() ResourceDelta
        +getCurrentProduction() ResourceDelta
        +setPowered(powered: boolean) void
    }
    class BuildingFactory {
        <<factory>>
        +createBuilding(description: BuildingDescription, x: int, y: int) BuildingInstance
    }
    IGridReadPort <|.. Grid
    IGridCommandPort <|.. Grid
    IBuildingState <|.. BuildingInstance
    Grid *-- Cell : owns
    Grid --> BuildingFactory : creates through
    Grid "1" o-- "0..*" BuildingInstance : indexes
    Cell "0..1" --> "1" BuildingInstance : hosts
    BuildingInstance --> BuildingDescription : references
    BuildingDescription --> Dimension : defines footprint
    BuildingFactory ..> BuildingInstance : instantiates
```

### Map invariants and behavior

- A `Grid` requires a non-null `Dimension` and `BuildingFactory` and creates one cell for every in-bounds coordinate.
- `isAreaFree` rejects null, out-of-bounds, and occupied footprints.
- `constructBuildingAt` rejects invalid input, creates one instance at the origin, writes that instance into every cell in its footprint, and indexes it by ID.
- `removeBuildingAt` returns null for an invalid or empty coordinate. Otherwise it clears the complete footprint and removes the instance from the active index.
- `getAdjacentBuildings` uses Chebyshev distance, `max(abs(dx), abs(dy)) <= radius`, and excludes the queried instance.
- `BuildingDescription` is immutable metadata. `BuildingCatalog` is an application-level type lookup and is not a dependency of `Grid` or `BuildingFactory`.

## Simulation engine

```mermaid
classDiagram
    direction TB
    class SimulationEngine {
        -cityState: CityAggregate
        -phases: List~ITickPhase~
        -gridReader: IGridReadPort
        -eventPublisher: ICityEventPublisher
        +advanceTick() void
        +getCurrentSnapshot() CitySnapshot
        +loadState(snapshot: CitySnapshot) void
        +activatePolicy(policy: IPolicyStrategy) void
        +deactivatePolicy(policyName: String) void
        +getActivePolicyNames() List~String~
    }
    class CityAggregate {
        -budget: BigDecimal
        -pollution: double
        -population: int
        -happiness: double
        -tickCount: int
        +applyDelta(delta: ResourceDelta) void
        +exportSnapshot() CitySnapshot
        +restoreFromSnapshot(snapshot: CitySnapshot) void
    }
    class CitySnapshot { <<immutable DTO>> }
    class ResourceDelta { <<immutable value object>>
        +merge(other: ResourceDelta) ResourceDelta
        +zero() ResourceDelta
    }
    class ITickPhase { <<interface>>
        +execute(snapshot: CitySnapshot, grid: IGridReadPort) ResourceDelta
    }
    class ProductionPhase
    class PolicyEvaluationPhase
    class TickPhaseFactory
    class SimulationConfig
    class IGridReadPort { <<interface>> }
    class ICityEventPublisher { <<interface>>
        +publish(snapshot: CitySnapshot) void
    }
    class IPolicyStrategy { <<interface>>
        +getName() String
        +calculateModifier(building: IBuildingState, grid: IGridReadPort) ResourceDelta
    }
    SimulationEngine --> CityAggregate : commits to
    SimulationEngine --> IGridReadPort : reads
    SimulationEngine --> ICityEventPublisher : publishes after commit
    SimulationEngine *-- ITickPhase : executes in order
    SimulationEngine --> TickPhaseFactory : constructs phases
    TickPhaseFactory --> SimulationConfig : reads
    ITickPhase <|.. ProductionPhase
    ITickPhase <|.. PolicyEvaluationPhase
    PolicyEvaluationPhase --> IPolicyStrategy : evaluates
```

### Tick transaction

```mermaid
sequenceDiagram
    participant Caller
    participant Engine as SimulationEngine
    participant City as CityAggregate
    participant Phase as ITickPhase list
    participant Grid as IGridReadPort
    participant Events as ICityEventPublisher
    Caller->>Engine: advanceTick()
    Engine->>City: exportSnapshot()
    City-->>Engine: startSnapshot
    loop each phase in configured order
        Engine->>Phase: execute(startSnapshot, Grid)
        Phase->>Grid: read buildings
        Grid-->>Phase: read-only state
        Phase-->>Engine: ResourceDelta
    end
    Engine->>City: applyDelta(totalDelta)
    alt invariant violation
        City-->>Engine: IllegalStateException
        Engine->>City: restoreFromSnapshot(startSnapshot)
        Engine-->>Caller: SimulationException
    else commit succeeds
        Engine->>City: exportSnapshot()
        City-->>Engine: committedSnapshot
        Engine->>Events: publish(committedSnapshot)
        Engine-->>Caller: success
    end
```

Each phase receives the same start-of-tick snapshot and read-only grid port. A phase must not mutate the city or grid and must return a non-null `ResourceDelta`; the engine merges all deltas and applies the total once. A failed invariant restores the start snapshot and publishes no event. Public engine methods are synchronized for city-state access, while callers must coordinate concurrent mutations of the concrete grid.

`ProductionPhase` sums base production from powered buildings. `PolicyEvaluationPhase` evaluates active `IPolicyStrategy` instances; its detailed rules belong to the policy workstream and are intentionally not defined here.

## Related documents

- [Map and building class diagram](Design%20Class%20Diagrams/mapDomain.md)
- [Simulation engine class diagram](Design%20Class%20Diagrams/simulationDomain.md)
- [Shared contracts](Design%20Class%20Diagrams/sharedContracts.md)
- [Presentation and policy diagram](Design%20Class%20Diagrams/presentationDomain.md), maintained separately
- [`prompt-log/`](prompt-log/), historical design-generation notes
