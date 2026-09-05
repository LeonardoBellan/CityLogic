package kfclash.citylogic.domain.policies;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import kfclash.citylogic.ports.IBuildingState;

/**
 * Vocabolario dei tipi di edificio usato dalle ordinanze.
 * <p>
 * {@link IBuildingState#getType()} restituisce il nome della description
 * ("Factory", "House", "Park"), mentre i test del Core usano etichette
 * canoniche ("INDUSTRIAL", "PARK"): qui i due vocabolari vengono riconciliati
 * in un unico punto, così le policy non contengono letterali sparsi.
 */
public final class BuildingTypes {

    public static final String RESIDENTIAL = "RESIDENTIAL";
    public static final String INDUSTRIAL = "INDUSTRIAL";
    public static final String COMMERCIAL = "COMMERCIAL";
    public static final String PARK = "PARK";
    public static final String POWER_PLANT = "POWER_PLANT";
    public static final String ROAD = "ROAD";

    /** Tipi tassabili dall'ordinanza ambientale e mitigabili dai parchi. */
    public static final Set<String> POLLUTING = Set.of(INDUSTRIAL, POWER_PLANT);

    /** Tipi eleggibili ai sussidi ecologici. */
    public static final Set<String> GREEN = Set.of(PARK);

    /** Nomi del catalogo applicativo ricondotti alla categoria canonica. */
    private static final Map<String, String> ALIASES = Map.ofEntries(
            Map.entry("HOUSE", RESIDENTIAL),
            Map.entry("RESIDENTIAL", RESIDENTIAL),
            Map.entry("FACTORY", INDUSTRIAL),
            Map.entry("INDUSTRIAL", INDUSTRIAL),
            Map.entry("SHOP", COMMERCIAL),
            Map.entry("COMMERCIAL", COMMERCIAL),
            Map.entry("PARK", PARK),
            Map.entry("POWERPLANT", POWER_PLANT),
            Map.entry("POWER_PLANT", POWER_PLANT),
            Map.entry("ROAD", ROAD));

    private BuildingTypes() {
    }

    /** Categoria canonica dell'edificio, stringa vuota se sconosciuta. */
    public static String categoryOf(IBuildingState building) {
        if (building == null || building.getType() == null) {
            return "";
        }
        String normalized = building.getType().trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("[\\s-]+", "_");
        return ALIASES.getOrDefault(normalized, "");
    }

    public static boolean is(IBuildingState building, String category) {
        return categoryOf(building).equals(category);
    }

    public static boolean isAnyOf(IBuildingState building, Set<String> categories) {
        return categories.contains(categoryOf(building));
    }
}
