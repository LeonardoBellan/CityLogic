package kfclash.citylogic.domain;

import kfclash.citylogic.application.BuildingDescription;
import kfclash.citylogic.ports.IBuildingState;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class BuildingInstance implements IBuildingState {
    private final String id;
    private final BuildingDescription description;
    private final int x;
    private final int y;
    private final Point position;
    private boolean operationalStatus;
    private final int currentMaintenanceCost;

    public BuildingInstance(BuildingDescription description, int x, int y) {
        if (description == null) {
            throw new IllegalArgumentException("BuildingDescription cannot be null");
        }
        this.id = UUID.randomUUID().toString();
        this.description = description;
        this.x = x;
        this.y = y;
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
        return x;
    }

    public int getY() {
        return y;
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

    public void setOperational(boolean operationalStatus) {
        this.operationalStatus = operationalStatus;
    }

    public int getCurrentMaintenanceCost() {
        return currentMaintenanceCost;
    }
}
