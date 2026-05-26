package kfclash.citylogic.model;

public class BuildingInstance {
    private final BuildingDescription description;
    private final int x;
    private final int y;
    private boolean operationalStatus;
    private final int currentMaintenanceCost;

    public BuildingInstance(BuildingDescription description, int x, int y) {
        if (description == null) {
            throw new IllegalArgumentException("BuildingDescription cannot be null");
        }
        this.description = description;
        this.x = x;
        this.y = y;
        this.operationalStatus = true;
        this.currentMaintenanceCost = description.getBaseMaintenanceCost();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
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
