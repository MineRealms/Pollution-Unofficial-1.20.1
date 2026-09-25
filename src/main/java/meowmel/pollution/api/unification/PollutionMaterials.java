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

    // 曼苏斯/精灵流体（ElementMaterials 第二批）
    public static Material WhiteMansus;
    public static Material BlackMansus;
    public static Material Elven;
    public static Material Starrymansus;

    // ---- 六种魔法合金 / six aspect alloys ----------------------------------
    // Upstream: api/unification/materials/FirstDegreeMaterials.java
    public static Material Aertitanium;
    public static Material IgnisSteel;
    public static Material Aquasilver;
    public static Material Terracopper;
    public static Material Ordolead;
    public static Material Perditioaluminium;

    // 魔力/超导链（FirstDegreeMaterials 第二批）
    public static Material Impuremana;
    /** Pure Botania mana as a fluid (Life Activation Garden mode 1 output). */
    public static Material Mana;
    public static Material KQGold;
    public static Material CrudeLk99;
    public static Material MagicalSuperconductiveLiquid;
    public static Material BasicThaumicSuperconductor;
    public static Material AdvancedThaumicSuperconductor;

    // 灵气/精灵链（FirstDegreeMaterials 第三批）
    public static Material RichAura;
    public static Material ErichAura;
    public static Material ElvenElementium;

    // 感知/缚束/枢金属（ElementMaterials 第二批）
    public static Material SentientMetal;
    public static Material BindingMetal;
    public static Material ExistingNexus;
    public static Material FadingNexus;

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

    // Magic fuel chain (GTQT chain replaced by GTCEu-native intermediates)
    public static Material MagicNitrobenzene;
    public static Material InfernalBlazePropellant;
    public static Material DragonPulseFuel;

    // ---- 矿物材料 / ore materials -------------------------------------------
    // Upstream: api/unification/materials/OreMaterials.java
    // Syrmorite / Octine / Valonite already exist in SubstrateMaterials; their
    // ore/tool/rotor forms are registered in SubstrateMaterials.
    public static Material Scabyst;
    public static Material Cryolite;
    public static Material FlameCoal;
    public static Material DumbTin;
    public static Material MeltGold;
    public static Material AuthorityLead;
    public static Material Pyrargyrite;
    public static Material PlutoZinc;
    public static Material Dragonstone;
    public static Material PixieDust;
    public static Material ElvenQuartz;
    public static Material Amber;

    // Minimal SecondDegreeMaterials subset for previously skipped recipes
    // (upstream: api/unification/materials/SecondDegreeMaterials.java)
    public static Material LotusDust;
    public static Material EthylSilicate;
    public static Material RoughLlp;
    public static Material Llp;
    public static Material OilWithLlp;
    public static Material PureTar;
    public static Material SuperStickyTar;
    public static Material DimensionalTransformingAgent;

    // SecondDegreeMaterials second batch: filth / battery / kqt chains
    public static Material Filth;
    public static Material FilthWater;
    public static Material VoidWater;
    public static Material VoidMaterial;
    public static Material BasicBatteryHullAlloy;
    public static Material AdvancedBatteryHullAlloy;
    public static Material BasicBatteryContent;
    public static Material AdvancedBatteryContent;
    public static Material MagicalSulfoPlumbicSalt;
    public static Material AlchemicalResidue1;
    public static Material AlchemicalVapor1;
    public static Material AlchemicalResidue2;
    public static Material AlchemicalVapor2;
    public static Material AlchemicalResidue3;
    public static Material AlchemicalVapor3;
    public static Material AlchemicalResidue4;
    public static Material AlchemicalVapor4;
    public static Material AlchemicalResidue5;
    public static Material AlchemicalVapor5;
    public static Material AlchemicalResidue6;
    public static Material AlchemicalVapor6;
    public static Material MagicalTinSolution;
    public static Material MagicalStannousSulfateSolution;
    public static Material HighmanaStannousSulfate;
    public static Material ImpureMercuricSaltSolution;
    public static Material MercuricSaltSolution;
    public static Material MagicActivatedIronChlorideSolution;
    public static Material MagicActivatedFerrousChlorideEthanolSolution;
    public static Material PurifiedActivatedFerrousChlorideEthanolSolution;
    public static Material PurifiedActivatedFerrousChloride;
    public static Material SyrmoriteDopedMagicWaterSolution;
    public static Material UnformedEmbryoMagicWater;
    public static Material EmbryoMagicWater;
    public static Material UnstableDimensionalSilver;
    public static Material ImpureHyperdimensionalSilver;
    public static Material HyperdimensionalSilver;
    public static Material FerrousChloride;

    // SecondDegreeMaterials hachimi chain
    public static Material HydrazoicAcid;
    public static Material SodiumAzide;
    public static Material SodiumCyclopentadienide;
    public static Material HafnoceneDichloride;
    public static Material uOxoBisHafnoceneAzide;

    // HigherDegreeMaterials (upstream: api/unification/materials/HigherDegreeMaterials.java)
    public static Material AethericDarkSteel;
    public static Material IizunamaruElectrum;

    // Minimal MagicIntegrationMaterials subset for previously skipped recipes
    // (upstream: api/unification/materials/MagicIntegrationMaterials.java)
    public static Material OpticalGradeAquamarine;
    public static Material StarlightPollen;
    public static Material MoonlightResin;
    public static Material ArcaneInk;

    // Substrate / catalyst chemistry (ThaumcraftRecipes chain)
    public static Material Salisundus;
    public static Material Roughdraft;
    public static Material Substrate;
    public static Material Valonite;
    public static Material Syrmorite;
    public static Material Octine;
    public static Material Thaummix;
    public static Material SulfoPlumbicSalt;
    public static Material BasicSubstrate;
    public static Material AdvancedSubstrate;
    public static Material HyperSubstrate;
}
