<!-- Status: current implementation reference. Keep aligned with src/main/java. -->

```mermaid
classDiagram
direction TB

    %% External interfaces used by the current implementation
    class IGridReadPort { <<interface>> }
    class IGridCommandPort { <<interface>> }
    class IBuildingState { <<interface>> }

    class Grid {
        <<Domain Service>>
        -dimensions: Dimension
        -map: Cell[][]
        -activeBuildings: Map<String, BuildingInstance>
        -factory: BuildingFactory
        +getTerrainAt(x: int, y: int) String
        +getBuildingById(id: String) Optional~IBuildingState~
        +getAllBuildings() List~IBuildingState~
        +getAdjacentBuildings(id: String, radius: int) List~IBuildingState~
        +isAreaFree(x: int, y: int, footprint: Dimension) boolean
        +constructBuildingAt(x: int, y: int, desc: BuildingDescription) BuildingInstance
        +removeBuildingAt(x: int, y: int) BuildingInstance
    }

    class BuildingInstance {
        <<Entity>>
        -id: String
        -description: BuildingDescription
        -position: Point
        -isPowered: boolean
        -currentMaintenanceCost: int
        +getId() String
        +getType() String
        +isPowered() boolean
        +getBaseProduction() ResourceDelta
        +getCurrentProduction() ResourceDelta
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
        <<Flyweight/Metadata>>
        -typeId: String
        -name: String
        -constructionCost: int
        -baseMaintenanceCost: int
        -footprint: Dimension
        -baseProduction: ResourceDelta
        +getTypeId() String
        +getName() String
        +getFootprint() Dimension
        +getBaseProduction() ResourceDelta
    }

    class BuildingFactory {
        <<Factory>>
        +createBuilding(description: BuildingDescription, x: int, y: int) BuildingInstance
    }

    class BuildingCatalog {
        <<Flyweight Registry>>
        +intern(description: BuildingDescription) BuildingDescription
        +getByTypeId(typeId: String) Optional~BuildingDescription~
    }

    %% Relations
    IGridReadPort <|.. Grid : implements
    IGridCommandPort <|.. Grid : implements
    IBuildingState <|.. BuildingInstance : implements

    Grid *-- Cell : contains
    Grid --> BuildingFactory : uses
    %% BuildingCatalog belongs to the application layer; Grid and the factory
    %% receive descriptions directly and do not depend on the catalog.
    Cell "0..1" --> "1" BuildingInstance : hosts
    BuildingFactory ..> BuildingInstance : instantiates
    BuildingInstance --> BuildingDescription : uses
```
