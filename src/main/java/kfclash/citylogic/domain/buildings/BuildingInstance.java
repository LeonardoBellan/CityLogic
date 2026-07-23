package kfclash.citylogic.domain.buildings;

import kfclash.citylogic.domain.map.Point;
import kfclash.citylogic.domain.map.Resource;
import kfclash.citylogic.ports.IBuildingState;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class BuildingInstance implements IBuildingState {
    private final String id;
    private final BuildingDescription description;
    private final Point position;
    private boolean isPowered; 
    private int currentMaintenanceCost;

    public BuildingInstance(BuildingDescription description, int x, int y) {
        if (description == null) {
            throw new IllegalArgumentException("BuildingDescription cannot be null");
        }
        this.id = UUID.randomUUID().toString();
        this.description = description;
        this.position = new Point(x, y);
        this.isPowered = true;
        this.currentMaintenanceCost = description.getBaseMaintenanceCost();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getType() {
        return description.getName();
    }

    @Override
    public Point getPosition() {
        return position;
    }

    @Override
    public BuildingDescription getDescription() {
        return description;
    }

    @Override
    public boolean isPowered() {
        return isPowered;
    }
    
    @Override
    public List<Resource> getCurrentProduction() {
        if (!isPowered) {
            // Un edificio spento o non operativo non produce nulla!
            return Collections.emptyList(); 
        }
        return Collections.unmodifiableList(description.getBaseProduction());
    }

    public void setPowered(boolean powered) {
        this.isPowered = powered;
    }

    public int getCurrentMaintenanceCost() {
        return currentMaintenanceCost;
    }
}