## Domain model

```mermaid
%%{init: {
  'theme': 'base',
  'themeVariables': {
    'primaryColor': '#f9f9f9',
    'strokeColor': '#333333',
    'lineColor': '#555555',
    'fontSize': '13px',
  },
  'config': { 'layout': 'orthogonal' }
}}%%
classDiagram
    direction TB

    class Player

    class Resource {
        type
        amount
    }

    %% Map
    class Cell {
        position
        isOccupied
        pollutionLevel
        activeBonus
    }

    class Grid {
        width
        height
    }

    %% Buildings
    class BuildingInstance {
        position
        operationalStatus
        currentMaintenanceCost
    }

    class BuildingDescription {
        name
        constructionCost
        baseMaintenanceCost
        footprint
        effectRadius
    }

    class EnvironmentalEffect {
        type
        intensity
        radius
    }


    %% %%
    Player "1" -- "1" Grid : manages
    Player "1" -- "*" Resource : owns/accumulates
    Player "1" -- "*" BuildingInstance : builds/demolishes
    Grid "1" *-- "400" Cell : consists of

    Cell "0..1" --> "1" BuildingInstance : hosts
    BuildingInstance "*" -- "1" BuildingDescription : based on (Immutable Metadata)
    BuildingInstance ..> Resource : produces
    BuildingInstance o-- EnvironmentalEffect : generates
    EnvironmentalEffect "*" -- "*" Cell : affects
```

## System Sequence Diagram

```mermaid
sequenceDiagram
    autonumber
    actor Player
    participant System as :System

    Note over Player, System: Construction Process
    Player->>System: selectBuilding(buildingType)
    System-->>Player: showPreview(buildingDetails)

    loop During Drag/Hover
        Player->>System: updatePosition(x,y)
        System-->>Player: showPlacementValidity(isValid)
    end

    Player->>System: confirmPlacement(x,y)
    opt Placement is valid
        System-->>Player: updateMap(), deductBudget(), applyAreaEffects()
    end

    Note over Player, System: Resource Management & Lifecycle
    Note right of System: System internally processes simulation tick

    System-->>Player: updateResources(power, water, taxes)

    alt Insufficient resources for building
        System-->>Player: signalInactiveBuilding(position)
    else Resources restored
        System-->>Player: signalActiveBuilding(position)
    end

    Note over Player, System: Demolition Process
    Player->>System: toggleDemolitionTool(active)
    System-->>Player: updateCursorIcon(Demolition)

    Player->>System: clickCell(x,y)
    alt Cell occupied by building
        System-->>Player: removeBuilding(), ceaseEffects()
    else Cell empty
        System-->>Player: feedbackError("No building to demolish")
    end

    Player->>System: deactivateDemolitionTool()
    System-->>Player: restoreStandardCursor()
```

# Design class diagram

```mermaid
classDiagram
    direction TB

    %% ==========================================
    %% 1. ENUMERATIONS AND BASE DATA STRUCTURES
    %% ==========================================
    class ToolType {
        <<enumeration>>
        NONE
        PLACEMENT
        DEMOLITION
    }

    class Dimension {
        <<struct>>
        +width: int
        +height: int
    }

    class Resource {
        <<value object>>
        -type: String
        -amount: int
        +getType() String
        +getAmount() int
    }

    class ProductionDisplayDetails {
        <<DTO - Data Transfer Object>>
        %% Data sent to the UI to show the preview
        +isValidPlacement: boolean
        +baseValue: int
        +modifierBonus: int
        +finalValue: int
        +bonusLabel: String
    }

    %% ==========================================
    %% 2. RULES AND ADJACENCY SYSTEM (STRATEGY)
    %% ==========================================
    class IPlacementRule {
        <<interface>>
        %% Generic spatial/logical placement rule
        +isValid(x: int, y: int, map: MapManager) boolean
    }

    class RoadAdjacencyRule {
        %% Example: "Valid only if there is an adjacent road"
        +isValid(x: int, y: int, map: MapManager) boolean
    }

    %% ==========================================
    %% 3. POLICY SYSTEM (OBSERVER) - External Module
    %% ==========================================
    class PolicyChangeEvent {
        -policy: Policy
        -isActivated: boolean
        +getPolicy() Policy
        +isActivated() boolean
    }

    class IPolicyObserver {
        <<interface>>
        +onPolicyChanged(event: PolicyChangeEvent) void
    }

    class Policy {
        <<interface>>
        +getName() String
        +appliesTo(description: BuildingDescription) boolean
        +modifyProduction(building: BuildingInstance, baseResource: Resource) Resource
    }

    class GreenSubsidyPolicy {
        +getName() String
        +appliesTo(description: BuildingDescription) boolean
        +modifyProduction(building: BuildingInstance, baseResource: Resource) Resource
    }

    %% ==========================================
    %% 4. CENTRAL ORCHESTRATION (FACADE & CORE)
    %% ==========================================
    class IGameCoreFacade {
        <<interface>>
        %% Masks the Core for the Controller: exposes only user input logic
        +constructBuilding(x: int, y: int, selectedDesc: BuildingDescription) boolean
        +demolishBuilding(x: int, y: int) boolean
        +getBuildingDescriptionById(description_id: String) BuildingDescription
        +getBuildingPreviewDetails(x: int, y: int, desc: BuildingDescription) ProductionDisplayDetails
    }

    class GameCore {
        %% Orchestrator: Coordinates Map, Economy and Policies
        -mapManager: MapManager
        -activePolicies: List~Policy~
        -policyObservers: List~IPolicyObserver~
        -buildingCatalog: Map~String, BuildingDescription~

        +constructBuilding(x: int, y: int, selectedDesc: BuildingDescription) boolean
        +demolishBuilding(x: int, y: int) boolean
        +getBuildingDescriptionById(description_id: String) BuildingDescription
        +getBuildingPreviewDetails(x: int, y: int, desc: BuildingDescription) ProductionDisplayDetails

        %% Internal logic methods and external modules
        ~registerPolicyObserver(observer: IPolicyObserver) void
        ~unregisterPolicyObserver(observer: IPolicyObserver) void
        ~activatePolicy(policy: Policy) void
        -notifyPolicyObservers(event: PolicyChangeEvent) void
        -hasEnoughResources(desc: BuildingDescription) boolean
        -deductConstructionCosts(desc: BuildingDescription) void
    }

    %% ==========================================
    %% 5. USER INTERFACE (CONTROLLER)
    %% ==========================================
    class MapController {
        %% Middleman: converts UI input into GameCore calls
        -gameCore: IGameCoreFacade
        -currentTool: ToolType
        -selectedBuildingDesc: BuildingDescription
        +clickCell(x: int, y: int) void
        +hoverCell(x: int, y: int) void
        +selectBuildingForPlacement(description_id: String) void
        +toggleDemolitionTool(active: boolean) void
    }

    %% ==========================================
    %% 6. MAP MANAGEMENT (GEOMETRY AND CELLS)
    %% ==========================================
    class MapManager {
        %% Spatial Manager: Controls placement and coordinates, ignores economy
        -dimensions: Dimension
        -map: Cell[][]
        -factory: BuildingFactory
        +getCell(x: int, y: int) Cell
        +validateSpatialPlacement(x: int, y: int, footprint: Dimension) boolean
        +constructBuildingAt(x: int, y: int, desc: BuildingDescription) BuildingInstance
        +removeBuildingAt(x: int, y: int) BuildingInstance
    }

    class BuildingFactory {
        <<simple factory>>
        +createBuilding(description: BuildingDescription, x: int, y: int) BuildingInstance
    }

    class Cell {
        %% Base cell of the two-dimensional grid
        -position: Point
        -pollutionLevel: int
        -currentBuilding: BuildingInstance
        +setBuilding(building: BuildingInstance) void
        +clear() void
        +getBuilding() BuildingInstance
        +isOccupied() boolean
    }

    %% ==========================================
    %% 7. GAME ENTITIES (BUILDINGS)
    %% ==========================================
    class BuildingInstance {
        %% Physical Instance: Building placed on the map
        -position: Point
        -operationalStatus: boolean
        -description: BuildingDescription
        -activeAppliedPolicies: List~Policy~
        +onPolicyChanged(event: PolicyChangeEvent) void
        +calculateCurrentProduction() List~Resource~
        +getPosition() Point
        +getDescription() BuildingDescription
    }

    class BuildingDescription {
        <<flyweight / metadata>>
        %% Immutable Data: Costs, footprint and rules shared among similar instances
        -name: String
        -constructionCost: int
        -footprint: Dimension
        -baseProduction: List~Resource~
        -placementRules: List~IPlacementRule~
        +getFootprint() Dimension
        +getConstructionCost() int
        +getBaseProduction() List~Resource~
        +getPlacementRules() List~IPlacementRule~
    }

    %% ==========================================
    %% ARCHITECTURAL RELATIONS AND DEPENDENCIES
    %% ==========================================

    %% UI Input and DTO
    MapController --> ToolType : selects
    MapController --> IGameCoreFacade : sends commands to
    IGameCoreFacade <|.. GameCore : implements
    GameCore ..> ProductionDisplayDetails : generates
    MapController ..> ProductionDisplayDetails : reads

    %% Core, Catalog and Dimensions
    GameCore "1" o-- "*" BuildingDescription : contains catalog
    BuildingDescription *-- Dimension : uses for footprint
    MapManager *-- Dimension : uses for map size

    %% Adjacency and Validation Rules
    BuildingDescription *-- IPlacementRule : defines rules per type
    IPlacementRule <|.. RoadAdjacencyRule : implements
    IPlacementRule ..> MapManager : analyzes adjacencies on
    GameCore ..> IPlacementRule : executes validation

    %% Map Orchestration and Factory
    GameCore --> MapManager : orchestrates
    MapManager *-- BuildingFactory : owns
    MapManager *-- Cell : composed of
    BuildingFactory ..> BuildingInstance : instantiates
    Cell "0..1" --> "1" BuildingInstance : hosts

    %% Observer Pattern (Policy)
    IPolicyObserver <|.. BuildingInstance : implements
    GameCore "1" o-- "*" IPolicyObserver : notifies changes
    BuildingInstance ..> PolicyChangeEvent : reacts to
    GameCore ..> PolicyChangeEvent : creates event

    %% Strategy Pattern and Building Meta-Data
    BuildingInstance "*" --> "1" BuildingDescription : reads base data from
    BuildingInstance "0..*" o-- "0..*" Policy : actively applies
    Policy <|.. GreenSubsidyPolicy : implements
    BuildingInstance ..> Resource : generates
    Policy ..> Resource : modifies
```
