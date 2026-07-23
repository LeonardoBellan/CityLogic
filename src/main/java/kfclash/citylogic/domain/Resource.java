package kfclash.citylogic.domain;

public final class Resource {
    private final String type;
    private final int amount;

    public Resource(String type, int amount) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Resource type cannot be null or blank");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Resource amount cannot be negative");
        }
        this.type = type;
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Resource)) {
            return false;
        }
        Resource other = (Resource) obj;
        return amount == other.amount && type.equals(other.type);
    }

    @Override
    public int hashCode() {
        return 31 * type.hashCode() + amount;
    }

    @Override
    public String toString() {
        return "Resource{" + "type='" + type + '\'' + ", amount=" + amount + '}';
    }
}
