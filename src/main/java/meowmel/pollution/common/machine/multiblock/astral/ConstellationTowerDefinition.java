package meowmel.pollution.common.machine.multiblock.astral;

import java.util.Locale;

/** Fixed Astral identities used by tower channels and controller recipes. */
public enum ConstellationTowerDefinition {
    AEVITAS("aevitas", "Aevitas"), EVORSIO("evorsio", "Evorsio"), ARMARA("armara", "Armara"),
    DISCIDIA("discidia", "Discidia"), VICIO("vicio", "Vicio"), MINERALIS("mineralis", "Mineralis"),
    FORNAX("fornax", "Fornax"), HOROLOGIUM("horologium", "Horologium"), LUCERNA("lucerna", "Lucerna"),
    OCTANS("octans", "Octans"), BOOTES("bootes", "Bootes"), PELOTRIO("pelotrio", "Pelotrio"),
    GELU("gelu", "Gelu"), ULTERIA("ulteria", "Ulteria"), ALCARA("alcara", "Alcara"), VORUX("vorux", "Vorux");
    private final String id;
    private final String englishName;
    ConstellationTowerDefinition(String id, String englishName) { this.id = id; this.englishName = englishName; }
    public String getId() { return id; }
    public String getEnglishName() { return englishName; }
    public static ConstellationTowerDefinition fromId(String id) {
        if (id == null) return null;
        for (ConstellationTowerDefinition d : values()) if (d.id.equalsIgnoreCase(id)) return d;
        return null;
    }
    @Override public String toString() { return id.toLowerCase(Locale.ROOT); }
}
