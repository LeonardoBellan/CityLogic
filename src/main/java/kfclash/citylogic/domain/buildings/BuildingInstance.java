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
    private boolean operationalStatus;
    private final int currentMaintenanceCost;

    public BuildingInstance(BuildingDescription description, int x, int y) {
        if (description == null) {
            throw new IllegalArgumentException("BuildingDescription cannot be null");
        }
        this.id = UUID.randomUUID().toString();
        this.description = description;
        this.position = new Point(x, y);
        this.operationalStatus = true;
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
    public boolean isPowered() {
        return operationalStatus;
    }

    public List<Resource> calculateCurrentProduction() {
        return Collections.unmodifiableList(description.getBaseProduction());
    }

    public int getX() {
        return position.getX();
    }

    public int getY() {
        return position.getY();
    }

    public Point getPosition() {
        return position;
    }

    public BuildingDescription getDescription() {
        return description;
    }

    public boolean isOperational() {
        return operationalStatus;
    }

    public void setPowered(boolean operationalStatus) {
        this.operationalStatus = operationalStatus;
    }

    public int getCurrentMaintenanceCost() {
        return currentMaintenanceCost;
    }
}
