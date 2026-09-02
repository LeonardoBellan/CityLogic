# CityLogic documentation

## Current references

- [Design document](design-document.md): canonical overview of the implemented architecture.
- [Map and building domain](Design%20Class%20Diagrams/mapDomain.md): grid, cells, building metadata, and placed instances.
- [Simulation engine](Design%20Class%20Diagrams/simulationDomain.md): tick phases, snapshots, deltas, and rollback.

## Separate workstreams

- [Presentation and policy](Design%20Class%20Diagrams/presentationDomain.md) covers areas maintained by other team members.
- [Shared contracts](Design%20Class%20Diagrams/sharedContracts.md) is a cross-cutting contract reference and should be updated when public ports change.

## History

- [`prompt-log/`](prompt-log/) contains historical prompts and design notes. It is not a source of current architecture.
- The former `temp diagrams/` folder was removed because its drafts duplicated and contradicted the maintained references.
