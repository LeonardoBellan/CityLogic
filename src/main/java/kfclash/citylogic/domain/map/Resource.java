package kfclash.citylogic.domain.map;

import java.util.Objects;

/**
 * Minimal compatibility type used by older tests and examples.
 */
public record Resource(String name, int amount) {
    public Resource {
        Objects.requireNonNull(name, "name cannot be null");
    }
}
