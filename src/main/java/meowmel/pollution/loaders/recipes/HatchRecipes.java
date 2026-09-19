package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.recipes.PORecipeMaps;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.item.PollutionItems;
import meowmel.pollution.common.machine.PollutionMachines;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import static meowmel.pollution.loaders.recipes.InfusionRecipes.aspects;
import static meowmel.pollution.loaders.recipes.InfusionRecipes.ing;
import static meowmel.pollution.loaders.recipes.InfusionRecipes.infusion;
import static meowmel.pollution.loaders.recipes.InfusionRecipes.machine;

/**
 * Obtainable crafting recipes for the magic multiblock hatches/parts that had
 * none (upstream {@code BotaniaRecipes.manahatch}, {@code MufflerHatchRecipes}
 * and the shaped {@code MachineRecipes} entries for the infused fluid hatch).
 *
 * <p>Covered, LV..UV (tiers 1..8):</p>
 * <ul>
 *   <li>mana input hatches 4A/16A/64A and mana output hatches 1A/4A/16A/64A,
 *       assembled from the matching GT energy hatch (the 1A mana input hatch
 *       recipes already ship in {@link BotaniaRecipes} and are not repeated);</li>
 *   <li>wireless mana input/output hatches of every amperage, upgraded from
 *       the matching wired hatch with a mana resonance coil, a Botania spark
 *       and an AE2 wireless receiver (ender eye fallback);</li>
 *   <li>aspect tanks, flux mufflers and infused fluid hatches.</li>
 * </ul>
 *
 * <p><b>GT base substitutions.</b> GTCEu 7.5.3 only registers the hi-amp
 * energy hatches from EV on ({@code ENERGY_INPUT_HATCH_4A/16A} and the 64A
 * {@code SUBSTATION_ENERGY_INPUT_HATCH}/{@code ...OUTPUT_HATCH}; there is no
 * {@code ENERGY_*_HATCH_64A} array). Below EV the recipe therefore combines
 * the tier's 1A energy hatch with the same wire GT itself uses for its own
 * hi-amp hatch recipes ({@code wireGtQuadruple} for 4A, {@code wireGtOctal}
 * for 16A, {@code wireGtHex} for 64A), so every tier 1..8 stays obtainable
 * without colliding in GT's recipe lookup DB. The 1A mana output hatch recipe
 * additionally consumes a Botania mana diamond because it would otherwise be
 * input-identical to the existing mana pool output hatch recipes at LV/LuV
 * (the same conflict the port already solved for the pool input hatch).</p>
 *
 * <p><b>Ingredient enrichment.</b> Every family carries tier-scaled flavour on
 * top of its GT base: mana powder at LV, an elemental rune from MV, thaumium
 * at HV/EV, a tier plate from EV, void ingots from IV, gaia ingots from LuV,
 * plus a SolderingAlloy bath from EV (see
 * {@link #addHatchEnrichment(List, int)}). The enriched lists stay at or below
 * the magic assembler recipe type's 9 item + 3 fluid slot budget, and the
 * extra stacks are optional — a missing item only makes the recipe leaner.</p>
 *
 * <p><b>High tiers.</b> IV..UV additionally get one Thaumcraft infusion-altar
 * recipe per family (mana in 1A, mana out 1A, wireless mana in 1A, aspect
 * tank, flux muffler, infused fluid hatch), scaling the aspect cost with the
 * tier and filling all 8 pedestals with the TC4R/Botania items that are
 * actually registered ({@code thaumcraft:thaumium_ingot},
 * {@code thaumcraft:void_ingot}, {@code thaumcraft:primordial_pearl},
 * {@code botania:terrasteel_ingot}, {@code botania:gaia_ingot}); a missing item
 * only skips the affected recipe.</p>
 */
public final class HatchRecipes {

    private static final int FIRST_TIER = GTValues.LV;
    /** UHV is covered too: GTCEu 7.5.3 registers the UHV energy/muffler/fluid hatches and the
     *  {@code uhv_sensor}/{@code uhv_emitter} components, and the port's machine arrays carry UHV. */
    private static final int LAST_TIER = GTValues.UHV;

    /** Amperages of the mana input families that still need recipes (1A ships in BotaniaRecipes). */
    private static final int[] MANA_INPUT_AMPS = { 4, 16, 64 };
    private static final int[] MANA_OUTPUT_AMPS = { 1, 4, 16, 64 };

    private HatchRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        int manaInput = manaInputHatches(provider);
        int manaOutput = manaOutputHatches(provider);
        int wireless = wirelessManaHatches(provider);
        int tanks = aspectTanks(provider);
        int mufflers = fluxMufflers(provider);
        int infused = infusedFluidHatches(provider);
        int tarot = tarotHatch(provider);
        int infusions = highTierInfusions(provider);
        Pollution.LOGGER.info("[hatch] registered {} mana input hatch recipes (4A/16A/64A)", manaInput);
        Pollution.LOGGER.info("[hatch] registered {} mana output hatch recipes (1A/4A/16A/64A)", manaOutput);
        Pollution.LOGGER.info("[hatch] registered {} wireless mana hatch recipes", wireless);
        Pollution.LOGGER.info("[hatch] registered {} aspect tank recipes", tanks);
        Pollution.LOGGER.info("[hatch] registered {} flux muffler recipes", mufflers);
        Pollution.LOGGER.info("[hatch] registered {} infused fluid hatch recipes", infused);
        Pollution.LOGGER.info("[hatch] registered {} tarot hatch recipes", tarot);
        Pollution.LOGGER.info("[hatch] registered {} high-tier hatch infusion recipes", infusions);
    }

    // ////////////////////////////////////
    // ***** mana energy hatches *****//
    // ////////////////////////////////////

    private static int manaInputHatches(Consumer<FinishedRecipe> provider) {
        ItemStack rune = botania("rune_mana", 1);
        if (rune.isEmpty()) {
            Pollution.LOGGER.warn("[hatch] skipping all mana input hatch recipes: botania:rune_mana is missing");
            return 0;
        }
        int added = 0;
        for (int tier = FIRST_TIER; tier <= LAST_TIER; tier++) {
            for (int amps : MANA_INPUT_AMPS) {
                if (manaHatch(provider, true, amps, tier,
                        at(manaInputFamily(amps), tier), rune, tierComponent(tier, "sensor", 2))) {
                    added++;
                }
            }
        }
        return added;
    }

    private static int manaOutputHatches(Consumer<FinishedRecipe> provider) {
        ItemStack rune = botania("rune_mana", 1);
        if (rune.isEmpty()) {
            Pollution.LOGGER.warn("[hatch] skipping all mana output hatch recipes: botania:rune_mana is missing");
            return 0;
        }
        int added = 0;
        for (int tier = FIRST_TIER; tier <= LAST_TIER; tier++) {
            for (int amps : MANA_OUTPUT_AMPS) {
                if (manaHatch(provider, false, amps, tier,
                        at(manaOutputFamily(amps), tier), rune, tierComponent(tier, "emitter", 2))) {
                    added++;
                }
            }
        }
        return added;
    }

    private static boolean manaHatch(Consumer<FinishedRecipe> provider, boolean input, int amps, int tier,
                                     MachineDefinition result, ItemStack rune, ItemStack sensor) {
        String family = input ? "mana input" : "mana output";
        if (result == null) {
            Pollution.LOGGER.warn("[hatch] skipping {} hatch {}A {}: machine is not registered",
                    family, amps, tierName(tier));
            return false;
        }
        if (rune.isEmpty() || sensor.isEmpty()) {
            Pollution.LOGGER.warn("[hatch] skipping {} hatch {}A {}: botania:rune_mana or the tier "
                    + "sensor/emitter is missing", family, amps, tierName(tier));
            return false;
        }
        MachineDefinition hiAmp = amps == 1 ? null : hiAmpEnergyHatch(input, amps, tier);
        MachineDefinition base = hiAmp != null ? hiAmp
                : at(input ? GTMachines.ENERGY_INPUT_HATCH : GTMachines.ENERGY_OUTPUT_HATCH, tier);
        if (base == null) {
            Pollution.LOGGER.warn("[hatch] skipping {} hatch {}A {}: no GT energy hatch for this tier",
                    family, amps, tierName(tier));
            return false;
        }
        // GT only registers hi-amp hatches from EV on; below that the matching
        // wire is combined with the tier's 1A hatch, mirroring GT's own recipes.
        ItemStack wire = amps > 1 && hiAmp == null ? ampWire(amps, tier) : ItemStack.EMPTY;
        if (amps > 1 && hiAmp == null && wire.isEmpty()) {
            Pollution.LOGGER.warn("[hatch] skipping {} hatch {}A {}: the amp wire is missing",
                    family, amps, tierName(tier));
            return false;
        }
        ItemStack gear = ChemicalHelper.get(TagPrefix.gear, gearMaterial(tier), 2);
        if (gear.isEmpty()) {
            Pollution.LOGGER.warn("[hatch] skipping {} hatch {}A {}: the tier gear is missing",
                    family, amps, tierName(tier));
            return false;
        }
        // The 1A mana output hatch recipe would otherwise be input-identical to
        // the existing mana pool output hatch recipes at LV and LuV, which GT's
        // recipe lookup DB rejects. Like the port's mana pool input fix, the
        // recipe consumes an extra mana diamond to stay distinct.
        ItemStack diamond = !input && amps == 1 ? botania("mana_diamond", 1) : ItemStack.EMPTY;
        if (!input && amps == 1 && diamond.isEmpty()) {
            Pollution.LOGGER.warn("[hatch] skipping {} hatch {}A {}: the distinguishing "
                    + "botania:mana_diamond is missing", family, amps, tierName(tier));
            return false;
        }
        List<ItemStack> components = new ArrayList<>();
        components.add(base.asStack());
        components.add(rune);
        components.add(gear);
        components.add(sensor);
        if (!wire.isEmpty()) {
            components.add(wire);
        }
        if (!diamond.isEmpty()) {
            components.add(diamond);
        }
        addHatchEnrichment(components, tier);
        GTRecipeBuilder builder = GTRecipeBuilder.of(
                id("mana_" + (input ? "input" : "output") + "_hatch_" + amps + "a/" + tierName(tier)),
                PORecipeMaps.MAGIC_ASSEMBLER_RECIPES);
        for (ItemStack component : components) {
            builder.inputItems(component);
        }
        builder.inputFluids(PollutionMaterials.InfusedAura.getFluid(1000));
        FluidStack solder = hatchSolder(tier);
        if (!solder.isEmpty()) {
            builder.inputFluids(solder);
        }
        builder.outputItems(result)
                .duration(100)
                .EUt(GTValues.VA[tier])
                .save(provider);
        return true;
    }

    // ////////////////////////////////////
    // ***** wireless mana hatches *****//
    // ////////////////////////////////////

    private static int wirelessManaHatches(Consumer<FinishedRecipe> provider) {
        ItemStack coil = PollutionItems.MANA_RESONANCE_COIL.asStack();
        ItemStack spark = botania("spark", 1);
        ItemStack link = SafeItems.byId("ae2", "wireless_receiver", 1);
        if (link.isEmpty()) {
            link = new ItemStack(Items.ENDER_EYE);
        }
        if (coil.isEmpty() || spark.isEmpty() || link.isEmpty()) {
            Pollution.LOGGER.warn("[hatch] skipping all wireless mana hatch recipes: the resonance coil, "
                    + "botania:spark or the wireless link item is missing");
            return 0;
        }
        int added = 0;
        for (int tier = FIRST_TIER; tier <= LAST_TIER; tier++) {
            for (int amps : MANA_OUTPUT_AMPS) {
                if (wirelessHatch(provider, true, amps, tier, coil, spark, link)) {
                    added++;
                }
                if (wirelessHatch(provider, false, amps, tier, coil, spark, link)) {
                    added++;
                }
            }
        }
        return added;
    }

    private static boolean wirelessHatch(Consumer<FinishedRecipe> provider, boolean input, int amps, int tier,
                                         ItemStack coil, ItemStack spark, ItemStack link) {
        String direction = input ? "input" : "output";
        MachineDefinition wired = at(input ? manaInputFamily(amps) : manaOutputFamily(amps), tier);
        MachineDefinition result = at(input ? wirelessInputFamily(amps) : wirelessOutputFamily(amps), tier);
        if (wired == null || result == null) {
            Pollution.LOGGER.warn("[hatch] skipping wireless mana {} hatch {}A {}: machine is not registered",
                    direction, amps, tierName(tier));
            return false;
        }
        if (coil.isEmpty() || spark.isEmpty() || link.isEmpty()) {
            Pollution.LOGGER.warn("[hatch] skipping wireless mana {} hatch {}A {}: a required item is missing",
                    direction, amps, tierName(tier));
            return false;
        }
        List<ItemStack> components = new ArrayList<>();
        components.add(wired.asStack());
        components.add(coil);
        components.add(spark);
        components.add(link);
        addHatchEnrichment(components, tier);
        if (tier >= GTValues.EV) {
            addIfPresent(components, tierComponent(tier, "emitter", 2));
        }
        GTRecipeBuilder builder = GTRecipeBuilder.of(
                id("wireless_mana_" + direction + "_hatch_" + amps + "a/" + tierName(tier)),
                PORecipeMaps.MAGIC_ASSEMBLER_RECIPES);
        for (ItemStack component : components) {
            builder.inputItems(component);
        }
        builder.inputFluids(PollutionMaterials.InfusedAura.getFluid(2000));
        FluidStack solder = hatchSolder(tier);
        if (!solder.isEmpty()) {
            builder.inputFluids(solder);
        }
        builder.outputItems(result)
                .duration(200)
                .EUt(GTValues.VA[tier])
                .save(provider);
        return true;
    }

    // ////////////////////////////////////
    // ***** aspect tanks *****//
    // ////////////////////////////////////

    private static int aspectTanks(Consumer<FinishedRecipe> provider) {
        ItemStack jar = firstOf(
                SafeItems.byId("thaumcraft", "warded_jar", 1),
                SafeItems.byId("thaumcraft", "void_jar", 1),
                SafeItems.byId("thaumcraft", "void_ingot", 2));
        ItemStack focus = firstOf(
                SafeItems.byId("botania", "mana_pearl", 1),
                SafeItems.byId("botania", "mana_diamond", 1));
        ItemStack glass = firstOf(
                SafeItems.byId("gtceu", "tempered_glass", 1),
                new ItemStack(Items.GLASS));
        if (jar.isEmpty() || focus.isEmpty()) {
            Pollution.LOGGER.warn("[hatch] skipping all aspect tank recipes: the thaumcraft jar/phial or "
                    + "the botania mana pearl/diamond is missing");
            return 0;
        }
        int added = 0;
        for (int tier = FIRST_TIER; tier <= LAST_TIER; tier++) {
            MachineDefinition result = at(PollutionMachines.ASPECT_TANK, tier);
            MachineDefinition base = at(GTMachines.FLUID_IMPORT_HATCH, tier);
            if (result == null || base == null) {
                Pollution.LOGGER.warn("[hatch] skipping aspect tank {}: machine is not registered",
                        tierName(tier));
                continue;
            }
            List<ItemStack> components = new ArrayList<>();
            components.add(base.asStack());
            components.add(jar);
            components.add(focus);
            components.add(glass);
            addHatchEnrichment(components, tier);
            GTRecipeBuilder builder = GTRecipeBuilder.of(id("aspect_tank/" + tierName(tier)),
                    PORecipeMaps.MAGIC_ASSEMBLER_RECIPES);
            for (ItemStack component : components) {
                builder.inputItems(component);
            }
            builder.inputFluids(PollutionMaterials.InfusedAura.getFluid(1000));
            if (tier >= GTValues.HV) {
                builder.inputFluids(PollutionMaterials.InfusedMagic.getFluid(500));
            }
            builder.outputItems(result)
                    .duration(100)
                    .EUt(GTValues.VA[tier])
                    .save(provider);
            added++;
        }
        return added;
    }

    // ////////////////////////////////////
    // ***** flux mufflers *****//
    // ////////////////////////////////////

    private static int fluxMufflers(Consumer<FinishedRecipe> provider) {
        ItemStack goo = SafeItems.byId("thaumcraft", "flux_goo", 2);
        ItemStack catalyst = botania("alchemy_catalyst", 1);
        if (goo.isEmpty()) {
            Pollution.LOGGER.warn("[hatch] skipping all flux muffler recipes: thaumcraft:flux_goo is missing");
            return 0;
        }
        int added = 0;
        for (int tier = FIRST_TIER; tier <= LAST_TIER; tier++) {
            MachineDefinition result = at(PollutionMachines.FLUX_MUFFLER, tier);
            MachineDefinition base = at(GTMachines.MUFFLER_HATCH, tier);
            if (result == null || base == null) {
                Pollution.LOGGER.warn("[hatch] skipping flux muffler {}: machine is not registered",
                        tierName(tier));
                continue;
            }
            List<ItemStack> components = new ArrayList<>();
            components.add(base.asStack());
            components.add(goo);
            if (!catalyst.isEmpty()) {
                components.add(catalyst);
            }
            addHatchEnrichment(components, tier);
            GTRecipeBuilder builder = GTRecipeBuilder.of(
                    id("flux_muffler/" + tierName(tier)), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES);
            for (ItemStack component : components) {
                builder.inputItems(component);
            }
            builder.inputFluids(PollutionMaterials.InfusedTaint.getFluid(500));
            if (tier >= GTValues.HV) {
                builder.inputFluids(PollutionMaterials.InfusedAura.getFluid(500));
            }
            builder.outputItems(result)
                    .duration(100)
                    .EUt(GTValues.VA[tier])
                    .save(provider);
            added++;
        }
        return added;
    }

    // ////////////////////////////////////
    // ***** infused fluid hatches *****//
    // ////////////////////////////////////

    private static int infusedFluidHatches(Consumer<FinishedRecipe> provider) {
        ItemStack rune = botania("rune_mana", 1);
        if (rune.isEmpty()) {
            Pollution.LOGGER.warn("[hatch] skipping all infused fluid hatch recipes: botania:rune_mana is missing");
            return 0;
        }
        int added = 0;
        for (int tier = FIRST_TIER; tier <= LAST_TIER; tier++) {
            MachineDefinition result = at(PollutionMachines.INFUSED_FLUID_HATCH, tier);
            MachineDefinition base = at(GTMachines.FLUID_IMPORT_HATCH, tier);
            ItemStack gear = ChemicalHelper.get(TagPrefix.gear, gearMaterial(tier), 2);
            if (result == null || base == null || gear.isEmpty()) {
                Pollution.LOGGER.warn("[hatch] skipping infused fluid hatch {}: machine or gear is missing",
                        tierName(tier));
                continue;
            }
            List<ItemStack> components = new ArrayList<>();
            components.add(base.asStack());
            components.add(rune);
            components.add(gear);
            addHatchEnrichment(components, tier);
            GTRecipeBuilder builder = GTRecipeBuilder.of(id("infused_fluid_hatch/" + tierName(tier)),
                    PORecipeMaps.MAGIC_ASSEMBLER_RECIPES);
            for (ItemStack component : components) {
                builder.inputItems(component);
            }
            builder.inputFluids(PollutionMaterials.InfusedAura.getFluid(1000));
            if (tier >= GTValues.HV) {
                builder.inputFluids(PollutionMaterials.InfusedMagic.getFluid(500));
            }
            builder.outputItems(result)
                    .duration(100)
                    .EUt(GTValues.VA[tier])
                    .save(provider);
            added++;
        }
        return added;
    }

    // ////////////////////////////////////
    // ***** tarot hatch *****//
    // ////////////////////////////////////

    /**
     * Upstream {@code MagicHatchRecipes.registerTarotHatch}: an LV assembler
     * recipe combining the tier hull, sensor and field generator with the
     * blank card and arcane ink, plus a Thaumcraft infusion-altar alternative.
     * The upstream {@code ItemsTC.salisMundus} emblem maps to Salisundus dust.
     */
    private static int tarotHatch(Consumer<FinishedRecipe> provider) {
        MachineDefinition result = PollutionMachines.TAROT_HATCH;
        MachineDefinition base = at(GTMachines.HULL, GTValues.LV);
        ItemStack card = SafeItems.of(PollutionItems.BLANK_TAROT_CARD);
        ItemStack ink = SafeItems.of(PollutionItems.ARCANE_INK_CAPSULE);
        ItemStack sensor = tierComponent(GTValues.LV, "sensor", 1);
        ItemStack fieldGenerator = SafeItems.gt("lv_field_generator", 1);
        ItemStack circuit = SafeItems.of(PollutionItems.MAGIC_CIRCUIT_LV);
        ItemStack salis = ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Salisundus, 1);
        if (result == null || base == null || card.isEmpty() || ink.isEmpty()
                || sensor.isEmpty() || fieldGenerator.isEmpty() || circuit.isEmpty() || salis.isEmpty()) {
            Pollution.LOGGER.warn("[hatch] skipping tarot hatch recipe: a required component is missing");
            return 0;
        }

        List<ItemStack> components = new ArrayList<>();
        components.add(base.asStack());
        components.add(sensor);
        components.add(fieldGenerator);
        components.add(card);
        components.add(ink);
        components.add(salis);
        components.add(circuit);
        GTRecipeBuilder builder = GTRecipeBuilder.of(id("tarot_hatch"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES);
        for (ItemStack component : components) {
            builder.inputItems(component);
        }
        builder.inputFluids(PollutionMaterials.InfusedMagic.getFluid(144));
        builder.outputItems(result)
                .duration(240)
                .EUt(GTValues.VA[GTValues.MV])
                .save(provider);

        int added = 1;
        if (infusion(provider, "hatch_tarot_hatch/iv", machine(result), 8,
                card,
                aspects("cognitio", 8, "praecantatio", 16, "permutatio", 8, "sensus", 8, "spiritus", 8),
                ing(ink), ing(salis), ing(circuit), ing(sensor), ing(fieldGenerator))) {
            added++;
        }
        return added;
    }

    // ////////////////////////////////////
    // ***** high-tier infusions *****//
    // ////////////////////////////////////

    /**
     * IV..UV infusion-altar alternatives for six hatch families. Aspect costs
     * scale with the tier (IV: 8 per primal, UV: 20 per primal + 40
     * praecantatio) and every family fills all 8 pedestals with the tier's
     * sensor/emitter, thaumium, void ingots, terrasteel/gaia and a Botania
     * rune/diamond accent. Every component is checked by
     * {@link InfusionRecipes#infusion}; a missing item only skips that recipe.
     */
    private static int highTierInfusions(Consumer<FinishedRecipe> provider) {
        ItemStack rune = botania("rune_mana", 2);
        ItemStack spark = botania("spark", 2);
        ItemStack coil = PollutionItems.MANA_RESONANCE_COIL.asStack(2);
        ItemStack terrasteel = botania("terrasteel_ingot", 1);
        ItemStack gaia = botania("gaia_ingot", 1);
        ItemStack manaPearl = botania("mana_pearl", 1);
        ItemStack manaDiamond = botania("mana_diamond", 1);
        ItemStack endoflame = botania("endoflame", 1);
        ItemStack thaumium = SafeItems.byId("thaumcraft", "thaumium_ingot", 2);
        ItemStack voidIngot = SafeItems.byId("thaumcraft", "void_ingot", 1);
        ItemStack pearl = SafeItems.byId("thaumcraft", "primordial_pearl", 1);
        ItemStack eldritch = SafeItems.byId("thaumcraft", "eldritch_object", 1);
        ItemStack alumentum = SafeItems.byId("thaumcraft", "alumentum", 2);
        ItemStack phial = SafeItems.byId("thaumcraft", "essence_phial", 1);
        ItemStack jar = firstOf(
                SafeItems.byId("thaumcraft", "warded_jar", 1),
                SafeItems.byId("thaumcraft", "void_jar", 1));
        ItemStack goo = SafeItems.byId("thaumcraft", "flux_goo", 2);
        ItemStack catalyst = botania("alchemy_catalyst", 1);
        ItemStack glass = firstOf(
                SafeItems.byId("gtceu", "tempered_glass", 1),
                new ItemStack(Items.GLASS));
        ItemStack link = SafeItems.byId("ae2", "wireless_receiver", 1);
        if (link.isEmpty()) {
            link = new ItemStack(Items.ENDER_EYE);
        }

        int added = 0;
        for (int tier = GTValues.IV; tier <= LAST_TIER; tier++) {
            String name = tierName(tier);
            int primal = 4 * (tier - GTValues.HV); // IV 8 .. UV 20
            int praecantatio = 2 * primal;
            int instability = 4 + 2 * (tier - GTValues.IV);

            // 魔力输入仓 1A
            if (infusion(provider, "hatch_mana_input_1a/" + name,
                    machine(at(PollutionMachines.MANA_INPUT_HATCH_1A, tier)), instability,
                    machine(at(GTMachines.ENERGY_INPUT_HATCH, tier)),
                    aspects("aer", primal, "ignis", primal, "aqua", primal, "terra", primal,
                            "ordo", primal, "praecantatio", praecantatio, "potentia", primal),
                    pedestals(List.of(ing(rune), ing(tierComponent(tier, "sensor", 2)), ing(thaumium),
                            ing(voidIngot), ing(terrasteel), ing(pearl)), manaDiamond, phial))) {
                added++;
            }

            // 魔力输出仓 1A
            if (infusion(provider, "hatch_mana_output_1a/" + name,
                    machine(at(PollutionMachines.MANA_OUTPUT_HATCH_1A, tier)), instability,
                    machine(at(GTMachines.ENERGY_OUTPUT_HATCH, tier)),
                    aspects("aer", primal, "ignis", primal, "aqua", primal, "terra", primal,
                            "ordo", primal, "praecantatio", praecantatio, "potentia", primal),
                    pedestals(List.of(ing(rune), ing(tierComponent(tier, "emitter", 2)), ing(thaumium),
                            ing(voidIngot), ing(terrasteel), ing(pearl)), manaDiamond, endoflame))) {
                added++;
            }

            // 无线魔力输入仓 1A
            if (infusion(provider, "hatch_wireless_mana_input_1a/" + name,
                    machine(at(PollutionMachines.WIRELESS_MANA_INPUT_HATCH_1A, tier)), instability,
                    machine(at(PollutionMachines.MANA_INPUT_HATCH_1A, tier)),
                    aspects("aer", primal, "ignis", primal, "aqua", primal, "terra", primal,
                            "ordo", primal, "praecantatio", praecantatio, "potentia", primal,
                            "auram", Math.max(1, primal / 2)),
                    pedestals(List.of(ing(coil), ing(spark), ing(link), ing(thaumium),
                            ing(voidIngot), ing(gaia), ing(pearl)), manaDiamond))) {
                added++;
            }

            // 要素储罐
            if (infusion(provider, "hatch_aspect_tank/" + name,
                    machine(at(PollutionMachines.ASPECT_TANK, tier)), instability,
                    machine(at(GTMachines.FLUID_IMPORT_HATCH, tier)),
                    aspects("permutatio", primal, "vacuos", primal, "aqua", primal, "ordo", primal,
                            "praecantatio", praecantatio, "auram", Math.max(1, primal / 2),
                            "cognitio", Math.max(1, primal / 2)),
                    pedestals(List.of(ing(jar), ing(manaPearl), ing(glass), ing(thaumium),
                            ing(voidIngot), ing(eldritch)), manaDiamond, phial))) {
                added++;
            }

            // 通量消声仓
            if (infusion(provider, "hatch_flux_muffler/" + name,
                    machine(at(PollutionMachines.FLUX_MUFFLER, tier)), instability,
                    machine(at(GTMachines.MUFFLER_HATCH, tier)),
                    aspects("vitium", primal, "perditio", primal, "ignis", primal, "aqua", primal,
                            "praecantatio", praecantatio, "vacuos", Math.max(1, primal / 2)),
                    pedestals(List.of(ing(goo), ing(catalyst), ing(thaumium), ing(voidIngot),
                            ing(eldritch), ing(pearl)), alumentum, endoflame))) {
                added++;
            }

            // 注魔流体仓
            if (infusion(provider, "hatch_infused_fluid/" + name,
                    machine(at(PollutionMachines.INFUSED_FLUID_HATCH, tier)), instability,
                    machine(at(GTMachines.FLUID_IMPORT_HATCH, tier)),
                    aspects("aqua", primal, "vitreus", primal, "permutatio", primal,
                            "praecantatio", praecantatio, "ordo", Math.max(1, primal / 2),
                            "motus", Math.max(1, primal / 2)),
                    pedestals(List.of(ing(rune), ing(phial), ing(thaumium), ing(voidIngot),
                            ing(terrasteel), ing(pearl)), manaDiamond, glass))) {
                added++;
            }
        }
        return added;
    }

    // ////////////////////////////////////
    // ***** helpers *****//
    // ////////////////////////////////////

    /**
     * Tier-scaled flavour on top of the GT base: mana powder at LV, one
     * elemental rune from MV, thaumium at HV/EV, a tier plate from EV, void
     * ingots from IV and gaia ingots from LuV. Every stack is optional: a
     * missing item only makes the recipe leaner.
     */
    private static void addHatchEnrichment(List<ItemStack> components, int tier) {
        if (tier == GTValues.LV) {
            addIfPresent(components, botania("mana_powder", 4));
            return;
        }
        addIfPresent(components, tierRune(tier));
        if (tier == GTValues.HV || tier == GTValues.EV) {
            addIfPresent(components, SafeItems.byId("thaumcraft", "thaumium_ingot", 2));
        }
        if (tier >= GTValues.EV) {
            addIfPresent(components, ChemicalHelper.get(TagPrefix.plate, tierPlateMaterial(tier), 4));
        }
        if (tier >= GTValues.IV) {
            addIfPresent(components, SafeItems.byId("thaumcraft", "void_ingot", 2));
        }
        if (tier >= GTValues.LuV) {
            addIfPresent(components, botania("gaia_ingot", 1));
        }
    }

    /** The SolderingAlloy bath added to the assembler hatches from EV on (empty below EV). */
    private static FluidStack hatchSolder(int tier) {
        if (tier < GTValues.EV) {
            return FluidStack.EMPTY;
        }
        return fluid(GTMaterials.SolderingAlloy, tier >= GTValues.IV ? 288 : 144);
    }

    /** LV Steel, MV Aluminium, HV StainlessSteel, EV Titanium, IV TungstenSteel, LuV NaquadahAlloy, ZPM Tritanium, UV Neutronium. */
    private static Material tierPlateMaterial(int tier) {
        return switch (tier) {
            case GTValues.LV -> GTMaterials.Steel;
            case GTValues.MV -> GTMaterials.Aluminium;
            case GTValues.HV -> GTMaterials.StainlessSteel;
            case GTValues.EV -> GTMaterials.Titanium;
            case GTValues.IV -> GTMaterials.TungstenSteel;
            case GTValues.LuV -> GTMaterials.NaquadahAlloy;
            case GTValues.ZPM -> GTMaterials.Tritanium;
            default -> GTMaterials.Neutronium;
        };
    }

    /** The tier's Botania rune accent (elemental runes from MV on). */
    private static ItemStack tierRune(int tier) {
        return switch (tier) {
            case GTValues.MV -> botania("rune_air", 1);
            case GTValues.HV -> botania("rune_earth", 1);
            case GTValues.EV -> botania("rune_water", 1);
            case GTValues.IV -> botania("rune_fire", 1);
            case GTValues.LuV -> botania("rune_spring", 1);
            case GTValues.ZPM -> botania("rune_summer", 1);
            default -> botania("rune_winter", 1);
        };
    }

    /** Appends the optional stacks (a missing item never fails the recipe). */
    private static void addIfPresent(List<ItemStack> components, ItemStack stack) {
        if (!stack.isEmpty()) {
            components.add(stack);
        }
    }

    /**
     * Builds the 8-pedestal component array of the high-tier infusions: the
     * required core plus as many optional flavour stacks as fit.
     */
    private static Ingredient[] pedestals(List<Ingredient> core, ItemStack... optional) {
        List<Ingredient> components = new ArrayList<>(core);
        for (ItemStack stack : optional) {
            if (components.size() >= 8) {
                break;
            }
            if (!stack.isEmpty()) {
                components.add(ing(stack));
            }
        }
        return components.toArray(new Ingredient[0]);
    }

    private static ItemStack botania(String path, int count) {
        return SafeItems.byId("botania", path, count);
    }

    /** @return the fluid stack, or an empty stack when the material has no fluid in this GTCEu build */
    private static FluidStack fluid(Material material, int amount) {
        if (material == null || !material.hasFluid()) {
            return FluidStack.EMPTY;
        }
        return material.getFluid(amount);
    }

    private static MachineDefinition[] manaInputFamily(int amps) {
        return switch (amps) {
            case 1 -> PollutionMachines.MANA_INPUT_HATCH_1A;
            case 4 -> PollutionMachines.MANA_INPUT_HATCH_4A;
            case 16 -> PollutionMachines.MANA_INPUT_HATCH_16A;
            case 64 -> PollutionMachines.MANA_INPUT_HATCH_64A;
            default -> null;
        };
    }

    private static MachineDefinition[] manaOutputFamily(int amps) {
        return switch (amps) {
            case 1 -> PollutionMachines.MANA_OUTPUT_HATCH_1A;
            case 4 -> PollutionMachines.MANA_OUTPUT_HATCH_4A;
            case 16 -> PollutionMachines.MANA_OUTPUT_HATCH_16A;
            case 64 -> PollutionMachines.MANA_OUTPUT_HATCH_64A;
            default -> null;
        };
    }

    private static MachineDefinition[] wirelessInputFamily(int amps) {
        return switch (amps) {
            case 1 -> PollutionMachines.WIRELESS_MANA_INPUT_HATCH_1A;
            case 4 -> PollutionMachines.WIRELESS_MANA_INPUT_HATCH_4A;
            case 16 -> PollutionMachines.WIRELESS_MANA_INPUT_HATCH_16A;
            case 64 -> PollutionMachines.WIRELESS_MANA_INPUT_HATCH_64A;
            default -> null;
        };
    }

    private static MachineDefinition[] wirelessOutputFamily(int amps) {
        return switch (amps) {
            case 1 -> PollutionMachines.WIRELESS_MANA_OUTPUT_HATCH_1A;
            case 4 -> PollutionMachines.WIRELESS_MANA_OUTPUT_HATCH_4A;
            case 16 -> PollutionMachines.WIRELESS_MANA_OUTPUT_HATCH_16A;
            case 64 -> PollutionMachines.WIRELESS_MANA_OUTPUT_HATCH_64A;
            default -> null;
        };
    }

    /** GT only registers the hi-amp hatches from EV on; null below that. */
    private static MachineDefinition hiAmpEnergyHatch(boolean input, int amps, int tier) {
        return switch (amps) {
            case 4 -> at(input ? GTMachines.ENERGY_INPUT_HATCH_4A : GTMachines.ENERGY_OUTPUT_HATCH_4A, tier);
            case 16 -> at(input ? GTMachines.ENERGY_INPUT_HATCH_16A : GTMachines.ENERGY_OUTPUT_HATCH_16A, tier);
            case 64 -> at(input ? GTMachines.SUBSTATION_ENERGY_INPUT_HATCH : GTMachines.SUBSTATION_ENERGY_OUTPUT_HATCH,
                    tier);
            default -> null;
        };
    }

    /** The wire GT itself combines with a 1A hatch to build the hi-amp hatch. */
    private static ItemStack ampWire(int amps, int tier) {
        TagPrefix prefix = switch (amps) {
            case 4 -> TagPrefix.wireGtQuadruple;
            case 16 -> TagPrefix.wireGtOctal;
            case 64 -> TagPrefix.wireGtHex;
            default -> null;
        };
        return prefix == null ? ItemStack.EMPTY : ChemicalHelper.get(prefix, tierWireMaterial(tier), 2);
    }

    private static Material tierWireMaterial(int tier) {
        return switch (tier) {
            case GTValues.LV -> GTMaterials.Tin;
            case GTValues.MV -> GTMaterials.Copper;
            case GTValues.HV -> GTMaterials.Gold;
            case GTValues.EV -> GTMaterials.Aluminium;
            case GTValues.IV -> GTMaterials.Tungsten;
            case GTValues.LuV -> GTMaterials.NiobiumTitanium;
            case GTValues.ZPM -> GTMaterials.VanadiumGallium;
            default -> GTMaterials.YttriumBariumCuprate;
        };
    }

    /** Matches the existing BotaniaRecipes convention: HSSG through IV, TungstenSteel above. */
    private static Material gearMaterial(int tier) {
        return tier <= GTValues.IV ? GTMaterials.HSSG : GTMaterials.TungstenSteel;
    }

    private static ItemStack tierComponent(int tier, String kind, int count) {
        return SafeItems.gt(tierName(tier) + "_" + kind, count);
    }

    private static MachineDefinition at(MachineDefinition[] family, int tier) {
        return family == null || tier < 0 || tier >= family.length ? null : family[tier];
    }

    private static ItemStack firstOf(ItemStack... stacks) {
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static String tierName(int tier) {
        return GTValues.VN[tier].toLowerCase(Locale.ROOT);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, "hatch/" + path);
    }
}
