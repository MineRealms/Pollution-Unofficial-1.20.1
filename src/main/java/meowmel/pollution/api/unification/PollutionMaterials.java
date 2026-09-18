package meowmel.pollution.api.unification;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

/**
 * Material registry mirror for the Pollution port.
 *
 * <p>Only materials ported so far are declared here. The remaining categories of
 * the 1.12.2 registry (see upstream {@code meowmel.pollution.api.unification.PollutionMaterials})
 * are tracked in {@code docs/MIGRATION_TRACKER.md} and will be added phase by phase.</p>
 */
public final class PollutionMaterials {

    private PollutionMaterials() {}

    // ---- 基础六要素 / six base aspects -------------------------------------
    // Upstream: api/unification/materials/ElementMaterials.java
    public static Material InfusedAir;
    public static Material InfusedFire;
    public static Material InfusedWater;
    public static Material InfusedEarth;
    public static Material InfusedEntropy;
    public static Material InfusedOrder;

    // ---- 六种魔法合金 / six aspect alloys ----------------------------------
    // Upstream: api/unification/materials/FirstDegreeMaterials.java
    public static Material Aertitanium;
    public static Material IgnisSteel;
    public static Material Aquasilver;
    public static Material Terracopper;
    public static Material Ordolead;
    public static Material Perditioaluminium;
}
