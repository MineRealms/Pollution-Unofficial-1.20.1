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

    // ---- 复合要素材料 / compound aspect materials --------------------------
    // Upstream: api/unification/materials/InfusedMaterials.java
    public static Material InfusedCrystal;
    public static Material InfusedLife;
    public static Material InfusedDeath;
    public static Material InfusedSoul;
    public static Material InfusedWeapon;
    public static Material InfusedMetal;
    public static Material InfusedEnergy;
    public static Material InfusedInstrument;
    public static Material InfusedExchange;
    public static Material InfusedMagic;
    public static Material InfusedAlchemy;
    public static Material InfusedCold;
    public static Material InfusedAura;
    public static Material InfusedLight;
    public static Material InfusedCraft;
    public static Material InfusedVoid;
    public static Material InfusedMotion;
    public static Material InfusedTaint;
    public static Material InfusedDark;
    public static Material InfusedAlien;
    public static Material InfusedFly;
    public static Material InfusedPlant;
    public static Material InfusedMechanics;
    public static Material InfusedTrap;
    public static Material InfusedUndead;
    public static Material InfusedThought;
    public static Material InfusedSense;
    public static Material InfusedAnimal;
    public static Material InfusedHuman;
    public static Material InfusedGreed;
    public static Material InfusedArmor;
    public static Material InfusedSpatio;
    public static Material InfusedTempus;
    public static Material InfusedTinctura;
}
