package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.recipes.PORecipeMaps;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import meowmel.pollution.common.item.PollutionItems;
import meowmel.pollution.common.machine.PollutionMachines;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import static meowmel.pollution.loaders.recipes.InfusionRecipes.aspects;
import static meowmel.pollution.loaders.recipes.InfusionRecipes.ing;
import static meowmel.pollution.loaders.recipes.InfusionRecipes.infusion;
import static meowmel.pollution.loaders.recipes.InfusionRecipes.machine;

/**
 * Obtainable crafting recipes for the machines/controllers that had none
 * (upstream registered these in {@code MachineRecipes}, {@code MagicGCYMRecipes}
 * and {@code NodeFusionRecipes}, but the port only carried the machine code).
 *
 * <p><b>Design split (requested).</b></p>
 * <ul>
 *   <li><b>Early machines (tier &lt;= HV)</b> use the TC4R infusion altar
 *       ({@link InfusionRecipes#infusion}); the central item is the tier's GT
 *       hull and the pedestals mix Botania and Thaumcraft items, with the
 *       aspect cost scaled by tier (LV: 4 per primal + 2 perditio + 4
 *       praecantatio, HV: 10 + 6 + 15) and instability 2/3/5. Covered: mana
 *       generators LV..HV and the source charge (LV).</li>
 *   <li><b>Advanced machines (tier &gt;= EV)</b> use the magic assembler
 *       ({@link PORecipeMaps#MAGIC_ASSEMBLER_RECIPES}) in GT-industrial style:
 *       tier hull/machine casing + magic circuits + two tier components
 *       (field generator/emitter/pump) + gears/frames/plates/rotors +
 *       infused fluids + GT fluids + catalysts. Covered: mana generators
 *       EV/IV, small node generators LuV..UV, the three node fusion reactors,
 *       the magic mega turbine, the magic greenhouse, the Terra distillery,
 *       the node washer, the mana plate and the life activation garden.</li>
 * </ul>
 *
 * <p><b>Input budget.</b> The infusion helper accepts up to 8 pedestals (the
 * task contract); every early recipe fills all 8. The magic assembler recipe
 * type is registered with {@code setMaxIOSize(9, 1, 3, 0)}, so advanced
 * recipes stay at 9 item types + 3 fluids (12 distinct inputs) — GT only warns
 * when this is exceeded, but the recipe UI/JEI only renders the declared slot
 * count.</p>
 *
 * <p><b>Substitutions</b> follow the port conventions: no new items are
 * invented; every stack goes through {@link SafeItems} and a missing item only
 * skips the affected recipe. Upstream-specific machines that are not ported
 * (GTFO greenhouse, GTQT chemical plant, POHyper casings) are replaced by their
 * ported equivalents ({@code GTMultiMachines.DISTILLATION_TOWER},
 * {@link PollutionMagicBlocks#VOID_PRISM}, ...).</p>
 */
public final class MachineRecipes {

    /** The four mana generator tiers LV..IV are covered (upstream registered 1..5). */
    private static final int MANA_GENERATOR_LAST = GTValues.IV;

    private MachineRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        int mana = manaGenerators(provider);
        int charge = sourceCharge(provider);
        int nodeGenerators = smallNodeGenerators(provider);
        int reactors = nodeFusionReactors(provider);
        int megaTurbine = magicMegaTurbine(provider);
        int greenHouse = magicGreenHouse(provider);
        int distillery = botDistillery(provider);
        int washer = nodeWasher(provider);
        int plate = manaPlate(provider);
        int garden = lifeGarden(provider);
        Pollution.LOGGER.info("[machine] registered {} mana generator recipes (LV..IV)", mana);
        Pollution.LOGGER.info("[machine] registered {} source charge recipes", charge);
        Pollution.LOGGER.info("[machine] registered {} small node generator recipes (LuV..UV)", nodeGenerators);
        Pollution.LOGGER.info("[machine] registered {} node fusion reactor recipes (LuV/ZPM/UV)", reactors);
        Pollution.LOGGER.info("[machine] registered {} magic mega turbine recipes", megaTurbine);
        Pollution.LOGGER.info("[machine] registered {} magic greenhouse recipes", greenHouse);
        Pollution.LOGGER.info("[machine] registered {} Terra distillery recipes", distillery);
        Pollution.LOGGER.info("[machine] registered {} node washer recipes", washer);
        Pollution.LOGGER.info("[machine] registered {} mana plate recipes", plate);
        Pollution.LOGGER.info("[machine] registered {} life activation garden recipes", garden);
    }

    // ////////////////////////////////////
    // ***** mana generators *****//
    // ////////////////////////////////////

    /**
     * LV..HV infusion (central = tier hull), EV/IV magic assembler upgrades.
     * Every infusion fills all 8 pedestals: rune_mana, mana_pearl, the tier
     * field generator, the tier gear plus four tier-flavoured Botania/Thaumcraft
     * items (diamond + endoflame at LV, air rune + void ingot + alumentum at
     * MV, earth rune + terrasteel + void ingot + phial at HV).
     * // 上游: mana_gen_lv..iv 无配方（1.12 只注册了机器）。
     */
    private static int manaGenerators(Consumer<FinishedRecipe> provider) {
        ItemStack rune = botania("rune_mana", 1);
        ItemStack runeAir = botania("rune_air", 1);
        ItemStack runeEarth = botania("rune_earth", 1);
        ItemStack diamond = botania("mana_diamond", 1);
        ItemStack pearl = botania("mana_pearl", 1);
        ItemStack endoflame = botania("endoflame", 1);
        ItemStack terrasteel = botania("terrasteel_ingot", 1);
        ItemStack resonator = thaumcraft("resonator", 1);
        ItemStack thaumium = thaumcraft("thaumium_ingot", 1);
        ItemStack voidIngot = thaumcraft("void_ingot", 1);
        ItemStack alumentum = thaumcraft("alumentum", 1);
        ItemStack phial = thaumcraft("essence_phial", 1);
        if (anyEmpty(rune, pearl, resonator)) {
            Pollution.LOGGER.warn("[machine] skipping all mana generator recipes: "
                    + "a Botania/Thaumcraft component is missing");
            return 0;
        }
        int added = 0;
        for (int tier = GTValues.LV; tier <= GTValues.HV; tier++) {
            MachineDefinition result = at(PollutionMachines.MANA_GENERATOR, tier);
            MachineDefinition hull = at(GTMachines.HULL, tier);
            ItemStack fieldGenerator = tierComponent(tier, "field_generator", 2);
            ItemStack gear = gtPart(TagPrefix.gear, manaGearMaterial(tier), 2);
            List<ItemStack> components = new ArrayList<>();
            components.add(rune);
            components.add(pearl);
            components.add(fieldGenerator);
            components.add(gear);
            switch (tier) {
                case GTValues.LV -> {
                    components.add(diamond);
                    components.add(resonator);
                    components.add(thaumium);
                    components.add(endoflame);
                }
                case GTValues.MV -> {
                    components.add(runeAir);
                    components.add(resonator);
                    components.add(voidIngot);
                    components.add(alumentum);
                }
                default -> {
                    components.add(runeEarth);
                    components.add(terrasteel);
                    components.add(voidIngot);
                    components.add(phial);
                }
            }
            if (result == null || hull == null || anyEmpty(components.toArray(new ItemStack[0]))) {
                Pollution.LOGGER.warn("[machine] skipping mana generator {}: machine, GT hull or "
                        + "a component is missing", tierName(tier));
                continue;
            }
            List<Ingredient> pedestals = new ArrayList<>();
            for (ItemStack component : components) {
                pedestals.add(ing(component));
            }
            Map<String, Integer> aspectMap = primalAspects(tier);
            aspectMap.put("potentia", 4 + 4 * (tier - GTValues.LV));
            aspectMap.put("auram", 2 + 4 * (tier - GTValues.LV));
            if (infusion(provider, "machine/mana_generator/" + tierName(tier), machine(result),
                    infusionInstability(tier), machine(hull), aspectMap,
                    pedestals.toArray(new Ingredient[0]))) {
                added++;
            }
        }
        for (int tier = GTValues.EV; tier <= MANA_GENERATOR_LAST; tier++) {
            if (manaGeneratorUpgrade(provider, tier)) {
                added++;
            }
        }
        return added;
    }

    /** EV/IV: magic assembler upgrade built on the tier hull (IV also on the EV generator). */
    private static boolean manaGeneratorUpgrade(Consumer<FinishedRecipe> provider, int tier) {
        MachineDefinition result = at(PollutionMachines.MANA_GENERATOR, tier);
        MachineDefinition hull = at(GTMachines.HULL, tier);
        MachineDefinition previous = tier == GTValues.EV ? null
                : at(PollutionMachines.MANA_GENERATOR, tier - 1);
        boolean heavy = tier >= GTValues.IV;
        ItemStack circuit = magicCircuit(tier);
        ItemStack coil = SafeItems.of(PollutionItems.MANA_RESONANCE_COIL, heavy ? 2 : 1);
        ItemStack rune = botania("rune_mana", heavy ? 4 : 2);
        ItemStack terrasteel = heavy ? ItemStack.EMPTY : botania("terrasteel_ingot", 2);
        ItemStack gaia = heavy ? botania("gaia_ingot", 2) : ItemStack.EMPTY;
        ItemStack pixie = heavy ? botania("pixie_dust", 4) : ItemStack.EMPTY;
        ItemStack endoflame = heavy ? ItemStack.EMPTY : botania("endoflame", 4);
        ItemStack gear = heavy ? ItemStack.EMPTY : gtPart(TagPrefix.gear, GTMaterials.HSSG, 4);
        ItemStack fieldGenerator = tierComponent(tier, "field_generator", heavy ? 4 : 2);
        ItemStack emitter = tierComponent(tier, "emitter", heavy ? 4 : 2);
        FluidStack aura = fluid(PollutionMaterials.InfusedAura, heavy ? 4000 : 2000);
        FluidStack magic = fluid(PollutionMaterials.InfusedMagic, heavy ? 2000 : 1000);
        FluidStack light = heavy ? fluid(PollutionMaterials.InfusedLight, 1000) : FluidStack.EMPTY;
        FluidStack solder = heavy ? FluidStack.EMPTY : fluid(GTMaterials.SolderingAlloy, 288);
        List<ItemStack> required = new ArrayList<>();
        required.add(circuit);
        required.add(coil);
        required.add(rune);
        required.add(fieldGenerator);
        required.add(emitter);
        if (heavy) {
            required.add(gaia);
            required.add(pixie);
        } else {
            required.add(terrasteel);
            required.add(endoflame);
            required.add(gear);
        }
        if (result == null || hull == null || anyEmpty(required.toArray(new ItemStack[0]))
                || aura.isEmpty() || magic.isEmpty()
                || (heavy && (previous == null || light.isEmpty()))) {
            Pollution.LOGGER.warn("[machine] skipping mana generator {}: a machine or component is missing",
                    tierName(tier));
            return false;
        }
        GTRecipeBuilder builder = GTRecipeBuilder.of(id("mana_generator/" + tierName(tier)),
                        PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(hull)
                .inputItems(circuit)
                .inputItems(coil)
                .inputItems(rune)
                .inputItems(fieldGenerator)
                .inputItems(emitter)
                .inputFluids(aura)
                .inputFluids(magic);
        if (heavy) {
            builder.inputItems(previous);
            builder.inputItems(gaia);
            builder.inputItems(pixie);
            builder.inputFluids(light);
        } else {
            builder.inputItems(terrasteel);
            builder.inputItems(gear);
            builder.inputItems(endoflame);
            if (!solder.isEmpty()) {
                builder.inputFluids(solder);
            }
        }
        builder.outputItems(result)
                .duration(heavy ? 400 : 300)
                .EUt(GTValues.VA[tier])
                .save(provider);
        return true;
    }

    // ////////////////////////////////////
    // ***** source charge *****//
    // ////////////////////////////////////

    /**
     * LV infusion; the water ring is the item the machine charges. All 8
     * pedestals are filled (rune, diamond, pearl, resonator, thaumium, LV pump,
     * LV sensor, water ring) and the aspects add aqua/permutatio/motus on top of
     * the LV primal spread.
     * // 上游: source_charge 无配方（1.12 只注册了机器）。
     */
    private static int sourceCharge(Consumer<FinishedRecipe> provider) {
        MachineDefinition result = PollutionMachines.SOURCE_CHARGE;
        MachineDefinition hull = at(GTMachines.HULL, GTValues.LV);
        ItemStack rune = botania("rune_mana", 1);
        ItemStack diamond = botania("mana_diamond", 1);
        ItemStack pearl = botania("mana_pearl", 1);
        ItemStack resonator = thaumcraft("resonator", 1);
        ItemStack thaumium = thaumcraft("thaumium_ingot", 1);
        ItemStack pump = tierComponent(GTValues.LV, "electric_pump", 1);
        ItemStack sensor = tierComponent(GTValues.LV, "sensor", 1);
        ItemStack ring = SafeItems.of(PollutionItems.BAUBLES_WATER_RING);
        if (result == null || hull == null || anyEmpty(rune, diamond, pearl, resonator, thaumium, pump, sensor, ring)) {
            Pollution.LOGGER.warn("[machine] skipping source charge: machine, GT hull or component is missing");
            return 0;
        }
        Map<String, Integer> aspectMap = primalAspects(GTValues.LV);
        aspectMap.put("aqua", 8);
        aspectMap.put("permutatio", 4);
        aspectMap.put("motus", 4);
        aspectMap.put("potentia", 4);
        return infusion(provider, "machine/source_charge", machine(result), 2, machine(hull), aspectMap,
                ing(rune), ing(diamond), ing(pearl), ing(resonator), ing(thaumium),
                ing(pump), ing(sensor), ing(ring)) ? 1 : 0;
    }

    // ////////////////////////////////////
    // ***** small node generators *****//
    // ////////////////////////////////////

    /**
     * LuV..UV (UHV intentionally skipped). Mirrors the upstream shaped recipe
     * (hull + large node generator + emitter + circuit + vis hatch + field
     * generator) as a magic assembler recipe and adds a packaged aura node, a
     * tier frame, a tier-flavoured rune and node fluids.
     * // 上游: MachineRecipes.SMALL_NODE_GENERATOR_*（有序合成）。
     */
    private static int smallNodeGenerators(Consumer<FinishedRecipe> provider) {
        MachineDefinition largeNodeGenerator = PollutionMachines.LARGE_NODE_GENERATOR;
        if (largeNodeGenerator == null) {
            Pollution.LOGGER.warn("[machine] skipping all small node generator recipes: "
                    + "the large node generator is not registered");
            return 0;
        }
        int added = 0;
        for (int tier = GTValues.LuV; tier <= GTValues.UV; tier++) {
            int rank = tier - GTValues.LuV; // LuV 0, ZPM 1, UV 2
            MachineDefinition result = at(PollutionMachines.SMALL_NODE_GENERATOR, tier);
            MachineDefinition hull = at(GTMachines.HULL, tier);
            MachineDefinition visHatch = at(PollutionMachines.VIS_HATCH, tier);
            ItemStack circuit = magicCircuit(tier);
            ItemStack emitter = tierComponent(tier, "emitter", 2 * (rank + 1));
            ItemStack fieldGenerator = tierComponent(tier, "field_generator", 2 * (rank + 1));
            ItemStack frame = gtPart(TagPrefix.frameGt, nodeFrameMaterial(tier), 4);
            ItemStack auraNode = SafeItems.of(PollutionItems.PACKAGED_AURA_NODE, 2 << rank);
            ItemStack rune = switch (rank) {
                case 0 -> botania("rune_mana", 2);
                case 1 -> botania("rune_spring", 4);
                default -> botania("rune_summer", 8);
            };
            FluidStack aura = fluid(PollutionMaterials.InfusedAura, 4000 * (rank + 1));
            FluidStack light = fluid(PollutionMaterials.InfusedLight, 2000 * (rank + 1));
            FluidStack third = rank == 0 ? fluid(PollutionMaterials.InfusedMagic, 1000)
                    : fluid(PollutionMaterials.InfusedDark, 1000 * rank);
            if (result == null || hull == null || visHatch == null
                    || anyEmpty(circuit, emitter, fieldGenerator, frame, auraNode, rune)
                    || aura.isEmpty() || light.isEmpty() || third.isEmpty()) {
                Pollution.LOGGER.warn("[machine] skipping small node generator {}: "
                        + "a machine or component is missing", tierName(tier));
                continue;
            }
            GTRecipeBuilder.of(id("small_node_generator/" + tierName(tier)),
                            PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                    .inputItems(hull)
                    .inputItems(largeNodeGenerator)
                    .inputItems(visHatch)
                    .inputItems(circuit)
                    .inputItems(emitter)
                    .inputItems(fieldGenerator)
                    .inputItems(frame)
                    .inputItems(auraNode)
                    .inputItems(rune)
                    .inputFluids(aura)
                    .inputFluids(light)
                    .inputFluids(third)
                    .outputItems(result)
                    .duration(600 * (rank + 1))
                    .EUt(GTValues.VA[tier])
                    .save(provider);
            added++;
        }
        return added;
    }

    // ////////////////////////////////////
    // ***** node fusion reactors *****//
    // ////////////////////////////////////

    /**
     * LuV/ZPM/UV. Port of the upstream magic-assembler chain (POHyper casings
     * -&gt; void prism, Starrymansus -&gt; InfusedAura): the LuV reactor is built
     * from the beam core and the superconducting coil, the ZPM/UV ones upgrade
     * the previous reactor and climb the frame material (NaquadahAlloy,
     * Tritanium, Neutronium). // 上游: NodeFusionRecipes.NODE_FUSION_REACTOR[0..2]。
     */
    private static int nodeFusionReactors(Consumer<FinishedRecipe> provider) {
        int added = 0;
        for (int tier = GTValues.LuV; tier <= GTValues.UV; tier++) {
            int rank = tier - GTValues.LuV; // LuV 0, ZPM 1, UV 2
            MachineDefinition result = switch (tier) {
                case GTValues.LuV -> PollutionMachines.NODE_FUSION_REACTOR_LUV;
                case GTValues.ZPM -> PollutionMachines.NODE_FUSION_REACTOR_ZPM;
                default -> PollutionMachines.NODE_FUSION_REACTOR_UV;
            };
            MachineDefinition previous = switch (tier) {
                case GTValues.LuV -> null;
                case GTValues.ZPM -> PollutionMachines.NODE_FUSION_REACTOR_LUV;
                default -> PollutionMachines.NODE_FUSION_REACTOR_ZPM;
            };
            ItemStack voidPrism = PollutionMagicBlocks.VOID_PRISM.asStack(4 * (rank + 1));
            ItemStack beamCore = PollutionMagicBlocks.BEAM_CORE_1.asStack(8 * (rank + 1));
            ItemStack circuit = magicCircuit(tier);
            ItemStack starryRune = SafeItems.of(PollutionItems.STARRY_RUNE, rank + 1);
            ItemStack auraNode = SafeItems.of(PollutionItems.PACKAGED_AURA_NODE, 2 << rank);
            ItemStack fieldGenerator = tierComponent(tier, "field_generator", 4 * (rank + 1));
            ItemStack frame = gtPart(TagPrefix.frameGt, nodeFrameMaterial(tier), 4);
            ItemStack coil = SafeItems.gt("superconducting_coil", 4 * (rank + 1));
            ItemStack coreOfIdea = rank == 0 ? pollutionItem("core_of_idea", 2) : ItemStack.EMPTY;
            Material firstMaterial = switch (tier) {
                case GTValues.LuV -> PollutionMaterials.KQGold;
                case GTValues.ZPM -> PollutionMaterials.BindingMetal;
                default -> PollutionMaterials.ExistingNexus;
            };
            Material secondMaterial = switch (tier) {
                case GTValues.LuV -> PollutionMaterials.InfusedLight;
                case GTValues.ZPM -> PollutionMaterials.SentientMetal;
                default -> PollutionMaterials.FadingNexus;
            };
            FluidStack first = fluid(firstMaterial, 8000);
            FluidStack second = fluid(secondMaterial, 8000);
            FluidStack third = rank == 0 ? fluid(PollutionMaterials.InfusedDark, 8000)
                    : fluid(PollutionMaterials.InfusedAura, rank == 1 ? 8000 : 16000);
            if (result == null || anyEmpty(voidPrism, beamCore, circuit, starryRune, auraNode,
                    fieldGenerator, frame, coil) || first.isEmpty() || second.isEmpty() || third.isEmpty()
                    || (rank == 0 && coreOfIdea.isEmpty())) {
                Pollution.LOGGER.warn("[machine] skipping node fusion reactor {}: "
                        + "a machine or component is missing", tierName(tier));
                continue;
            }
            GTRecipeBuilder builder = GTRecipeBuilder.of(id("node_fusion_reactor/" + tierName(tier)),
                            PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                    .inputItems(voidPrism)
                    .inputItems(beamCore)
                    .inputItems(circuit)
                    .inputItems(starryRune)
                    .inputItems(auraNode)
                    .inputItems(fieldGenerator)
                    .inputItems(frame)
                    .inputItems(coil)
                    .inputFluids(first)
                    .inputFluids(second)
                    .inputFluids(third);
            if (rank == 0) {
                builder.inputItems(coreOfIdea);
            } else {
                builder.inputItems(previous);
            }
            builder.outputItems(result)
                    .duration(1200 + rank * 400)
                    .EUt(GTValues.VA[tier])
                    .save(provider);
            added++;
        }
        return added;
    }

    // ////////////////////////////////////
    // ***** magic mega turbine *****//
    // ////////////////////////////////////

    /**
     * IV. Upgrades the ported large magic turbine with the hot spell prism, the
     * segregation catalyst, a TungstenSteel rotor and the IV emitter, mirroring
     * the upstream {@code large_magic_turbine} infusion at one tier higher.
     * // 上游: MagicGCYMRecipes.large_magic_turbine（注魔，EV）。
     */
    private static int magicMegaTurbine(Consumer<FinishedRecipe> provider) {
        MachineDefinition result = PollutionMachines.MAGIC_MEGA_TURBINE;
        MachineDefinition largeTurbine = PollutionMachines.MAGIC_LARGE_TURBINE;
        ItemStack hotPrism = PollutionMagicBlocks.SPELL_PRISM_HOT.asStack(4);
        ItemStack circuit = magicCircuit(GTValues.IV);
        ItemStack starryRune = SafeItems.of(PollutionItems.STARRY_RUNE);
        ItemStack catalyst = SafeItems.of(PollutionItems.SEGREGATION_CATALYST_CORE);
        ItemStack frame = gtPart(TagPrefix.frameGt, GTMaterials.NaquadahAlloy, 8);
        ItemStack gear = gtPart(TagPrefix.gear, GTMaterials.TungstenSteel, 8);
        ItemStack rotor = gtPart(TagPrefix.rotor, GTMaterials.TungstenSteel, 4);
        ItemStack fieldGenerator = tierComponent(GTValues.IV, "field_generator", 4);
        FluidStack aura = fluid(PollutionMaterials.InfusedAura, 8000);
        FluidStack fire = fluid(PollutionMaterials.InfusedFire, 4000);
        FluidStack lubricant = fluid(GTMaterials.Lubricant, 4000);
        if (result == null || largeTurbine == null
                || anyEmpty(hotPrism, circuit, starryRune, catalyst, frame, gear, rotor, fieldGenerator)
                || aura.isEmpty() || fire.isEmpty() || lubricant.isEmpty()) {
            Pollution.LOGGER.warn("[machine] skipping magic mega turbine: a machine or component is missing");
            return 0;
        }
        GTRecipeBuilder.of(id("magic_mega_turbine"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(largeTurbine)
                .inputItems(hotPrism)
                .inputItems(circuit)
                .inputItems(starryRune)
                .inputItems(catalyst)
                .inputItems(frame)
                .inputItems(gear)
                .inputItems(rotor)
                .inputItems(fieldGenerator)
                .inputFluids(aura)
                .inputFluids(fire)
                .inputFluids(lubricant)
                .outputItems(result)
                .duration(1200)
                .EUt(GTValues.VA[GTValues.IV])
                .save(provider);
        return 1;
    }

    // ////////////////////////////////////
    // ***** magic greenhouse *****//
    // ////////////////////////////////////

    /**
     * EV. Port of the upstream {@code magic_greenhouse} infusion: GTFO's
     * greenhouse is not in the pack, so the controller is assembled from the EV
     * hull, the magic control assembly, the water spell prism, the beam core
     * and Botania plant material. // 上游: MagicGCYMRecipes.magic_greenhouse（注魔）。
     */
    private static int magicGreenHouse(Consumer<FinishedRecipe> provider) {
        MachineDefinition result = PollutionMachines.MAGIC_GREEN_HOUSE;
        MachineDefinition hull = at(GTMachines.HULL, GTValues.EV);
        ItemStack control = SafeItems.of(PollutionItems.MAGIC_CONTROL_ASSEMBLY);
        ItemStack circuit = magicCircuit(GTValues.EV);
        ItemStack valonite = ChemicalHelper.get(TagPrefix.gem, PollutionMaterials.Valonite, 4);
        ItemStack waterPrism = PollutionMagicBlocks.SPELL_PRISM_WATER.asStack(4);
        ItemStack beamCore = PollutionMagicBlocks.BEAM_CORE_3.asStack();
        ItemStack endoflame = botania("endoflame", 4);
        ItemStack manaPearl = botania("mana_pearl", 4);
        ItemStack fieldGenerator = tierComponent(GTValues.EV, "field_generator", 4);
        FluidStack plant = fluid(PollutionMaterials.InfusedPlant, 4000);
        FluidStack aura = fluid(PollutionMaterials.InfusedAura, 4000);
        FluidStack earth = fluid(PollutionMaterials.InfusedEarth, 2000);
        if (result == null || hull == null || anyEmpty(control, circuit, valonite, waterPrism, beamCore,
                endoflame, manaPearl, fieldGenerator) || plant.isEmpty() || aura.isEmpty() || earth.isEmpty()) {
            Pollution.LOGGER.warn("[machine] skipping magic greenhouse: a machine or component is missing");
            return 0;
        }
        GTRecipeBuilder.of(id("magic_green_house"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(hull)
                .inputItems(control)
                .inputItems(circuit)
                .inputItems(valonite)
                .inputItems(waterPrism)
                .inputItems(beamCore)
                .inputItems(endoflame)
                .inputItems(manaPearl)
                .inputItems(fieldGenerator)
                .inputFluids(plant)
                .inputFluids(aura)
                .inputFluids(earth)
                .outputItems(result)
                .duration(800)
                .EUt(GTValues.VA[GTValues.EV])
                .save(provider);
        return 1;
    }

    // ////////////////////////////////////
    // ***** Terra distillery *****//
    // ////////////////////////////////////

    /**
     * EV. Terra-watertight shell around a GT distillation tower, matching the
     * upstream Terra casing structure and adding an EV pump plus lubricant.
     * // 上游: bot_distillery 无配方。
     */
    private static int botDistillery(Consumer<FinishedRecipe> provider) {
        MachineDefinition result = PollutionMachines.BOT_DISTILLERY;
        MachineDefinition distillationTower = GTMultiMachines.DISTILLATION_TOWER;
        ItemStack terraCasing = PollutionMagicBlocks.TERRA_WATERTIGHT_CASING.asStack(8);
        ItemStack glass = PollutionMagicBlocks.CAMINATED_GLASS.asStack(8);
        ItemStack circuit = magicCircuit(GTValues.EV);
        ItemStack control = SafeItems.of(PollutionItems.MAGIC_CONTROL_ASSEMBLY);
        ItemStack terrasteel = botania("terrasteel_ingot", 4);
        ItemStack rune = botania("rune_mana", 4);
        ItemStack pump = tierComponent(GTValues.EV, "electric_pump", 4);
        ItemStack fieldGenerator = tierComponent(GTValues.EV, "field_generator", 2);
        FluidStack water = fluid(PollutionMaterials.InfusedWater, 8000);
        FluidStack aura = fluid(PollutionMaterials.InfusedAura, 4000);
        FluidStack lubricant = fluid(GTMaterials.Lubricant, 1000);
        if (result == null || distillationTower == null
                || anyEmpty(terraCasing, glass, circuit, control, terrasteel, rune, pump, fieldGenerator)
                || water.isEmpty() || aura.isEmpty() || lubricant.isEmpty()) {
            Pollution.LOGGER.warn("[machine] skipping Terra distillery: a machine or component is missing");
            return 0;
        }
        GTRecipeBuilder.of(id("bot_distillery"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(distillationTower)
                .inputItems(terraCasing)
                .inputItems(glass)
                .inputItems(circuit)
                .inputItems(control)
                .inputItems(terrasteel)
                .inputItems(rune)
                .inputItems(pump)
                .inputItems(fieldGenerator)
                .inputFluids(water)
                .inputFluids(aura)
                .inputFluids(lubricant)
                .outputItems(result)
                .duration(1000)
                .EUt(GTValues.VA[GTValues.EV])
                .save(provider);
        return 1;
    }

    // ////////////////////////////////////
    // ***** node washer *****//
    // ////////////////////////////////////

    /**
     * LuV. Node rebirth pool: beam core basin, void prism walls and the
     * stabilization frame, washed by the node fluids and circulated by an LuV
     * pump. // 上游: node_washer 无配方。
     */
    private static int nodeWasher(Consumer<FinishedRecipe> provider) {
        MachineDefinition result = PollutionMachines.NODE_WASHER;
        ItemStack beamCore = PollutionMagicBlocks.BEAM_CORE_2.asStack(2);
        ItemStack voidPrism = PollutionMagicBlocks.VOID_PRISM.asStack(4);
        ItemStack circuit = magicCircuit(GTValues.LuV);
        ItemStack starryRune = SafeItems.of(PollutionItems.STARRY_RUNE);
        ItemStack auraNode = SafeItems.of(PollutionItems.PACKAGED_AURA_NODE, 2);
        ItemStack stabilization = SafeItems.of(PollutionItems.NODE_STABILIZATION_FRAME);
        ItemStack pump = tierComponent(GTValues.LuV, "electric_pump", 4);
        ItemStack fieldGenerator = tierComponent(GTValues.LuV, "field_generator", 4);
        ItemStack rune = botania("rune_mana", 4);
        FluidStack water = fluid(PollutionMaterials.InfusedWater, 16000);
        FluidStack aura = fluid(PollutionMaterials.InfusedAura, 8000);
        FluidStack magic = fluid(PollutionMaterials.InfusedMagic, 4000);
        if (result == null || anyEmpty(beamCore, voidPrism, circuit, starryRune, auraNode,
                stabilization, pump, fieldGenerator, rune)
                || water.isEmpty() || aura.isEmpty() || magic.isEmpty()) {
            Pollution.LOGGER.warn("[machine] skipping node washer: a machine or component is missing");
            return 0;
        }
        GTRecipeBuilder.of(id("node_washer"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(beamCore)
                .inputItems(voidPrism)
                .inputItems(circuit)
                .inputItems(starryRune)
                .inputItems(auraNode)
                .inputItems(stabilization)
                .inputItems(pump)
                .inputItems(fieldGenerator)
                .inputItems(rune)
                .inputFluids(water)
                .inputFluids(aura)
                .inputFluids(magic)
                .outputItems(result)
                .duration(1200)
                .EUt(GTValues.VA[GTValues.LuV])
                .save(provider);
        return 1;
    }

    // ////////////////////////////////////
    // ***** mana plate *****//
    // ////////////////////////////////////

    /**
     * IV. Eleven-by-eleven mana-basic floor accelerator, assembled from the
     * mana casings, the resonance coils and an InfusedOrder bath.
     * // 上游: mana_plate 无配方。
     */
    private static int manaPlate(Consumer<FinishedRecipe> provider) {
        MachineDefinition result = PollutionMachines.MANA_PLATE;
        ItemStack manaBasic = PollutionMagicBlocks.MANA_BASIC.asStack(16);
        ItemStack circuit = magicCircuit(GTValues.IV);
        ItemStack resonanceCoil = SafeItems.of(PollutionItems.MANA_RESONANCE_COIL, 4);
        ItemStack starryRune = SafeItems.of(PollutionItems.STARRY_RUNE);
        ItemStack naturalCoil = SafeItems.of(PollutionItems.NATURAL_INFUSED_COIL, 2);
        ItemStack fieldGenerator = tierComponent(GTValues.IV, "field_generator", 4);
        ItemStack gaia = botania("gaia_ingot", 4);
        ItemStack pixie = botania("pixie_dust", 8);
        ItemStack rune = botania("rune_mana", 4);
        FluidStack aura = fluid(PollutionMaterials.InfusedAura, 16000);
        FluidStack magic = fluid(PollutionMaterials.InfusedMagic, 8000);
        FluidStack order = fluid(PollutionMaterials.InfusedOrder, 4000);
        if (result == null || anyEmpty(manaBasic, circuit, resonanceCoil, starryRune, naturalCoil,
                fieldGenerator, gaia, pixie, rune)
                || aura.isEmpty() || magic.isEmpty() || order.isEmpty()) {
            Pollution.LOGGER.warn("[machine] skipping mana plate: a machine or component is missing");
            return 0;
        }
        GTRecipeBuilder.of(id("mana_plate"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(manaBasic)
                .inputItems(circuit)
                .inputItems(resonanceCoil)
                .inputItems(starryRune)
                .inputItems(naturalCoil)
                .inputItems(fieldGenerator)
                .inputItems(gaia)
                .inputItems(pixie)
                .inputItems(rune)
                .inputFluids(aura)
                .inputFluids(magic)
                .inputFluids(order)
                .outputItems(result)
                .duration(1600)
                .EUt(GTValues.VA[GTValues.IV])
                .save(provider);
        return 1;
    }

    // ////////////////////////////////////
    // ***** life activation garden *****//
    // ////////////////////////////////////

    /**
     * LuV. Conway garden controller: beam core 4, Botania cell blocks, the
     * evolution catalyst core and the Dandelifeon flower, grown in the mana 5
     * casing. // 上游: pollution_multi_dan_de_life_on 无配方。
     */
    private static int lifeGarden(Consumer<FinishedRecipe> provider) {
        MachineDefinition result = PollutionMachines.MULTI_DAN_DE_LIFE_ON;
        ItemStack beamCore = PollutionMagicBlocks.BEAM_CORE_4.asStack();
        ItemStack circuit = magicCircuit(GTValues.LuV);
        ItemStack starryRune = SafeItems.of(PollutionItems.STARRY_RUNE, 2);
        ItemStack cellBlock = botania("cell_block", 16);
        ItemStack evolutionCore = SafeItems.of(PollutionItems.EVOLUTION_CATALYST_CORE);
        ItemStack gaia = botania("gaia_ingot", 4);
        ItemStack dandelifeon = botania("dandelifeon", 4);
        ItemStack fieldGenerator = tierComponent(GTValues.LuV, "field_generator", 4);
        ItemStack mana5 = PollutionMagicBlocks.MANA_5.asStack(4);
        FluidStack life = fluid(PollutionMaterials.InfusedLife, 16000);
        FluidStack aura = fluid(PollutionMaterials.InfusedAura, 8000);
        FluidStack plant = fluid(PollutionMaterials.InfusedPlant, 4000);
        if (result == null || anyEmpty(beamCore, circuit, starryRune, cellBlock, evolutionCore,
                gaia, dandelifeon, fieldGenerator, mana5)
                || life.isEmpty() || aura.isEmpty() || plant.isEmpty()) {
            Pollution.LOGGER.warn("[machine] skipping life activation garden: a machine or component is missing");
            return 0;
        }
        GTRecipeBuilder.of(id("pollution_multi_dan_de_life_on"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(beamCore)
                .inputItems(circuit)
                .inputItems(starryRune)
                .inputItems(cellBlock)
                .inputItems(evolutionCore)
                .inputItems(gaia)
                .inputItems(dandelifeon)
                .inputItems(fieldGenerator)
                .inputItems(mana5)
                .inputFluids(life)
                .inputFluids(aura)
                .inputFluids(plant)
                .outputItems(result)
                .duration(1600)
                .EUt(GTValues.VA[GTValues.LuV])
                .save(provider);
        return 1;
    }

    // ////////////////////////////////////
    // ***** helpers *****//
    // ////////////////////////////////////

    /**
     * LV 4, MV 7, HV 10 per primal plus 2/4/6 perditio and 4/8/15 praecantatio.
     * The callers add the machine-specific extras (potentia/auram/aqua/...).
     */
    private static Map<String, Integer> primalAspects(int tier) {
        int primal = switch (tier) {
            case GTValues.LV -> 4;
            case GTValues.MV -> 7;
            default -> 10;
        };
        int perditio = switch (tier) {
            case GTValues.LV -> 2;
            case GTValues.MV -> 4;
            default -> 6;
        };
        int praecantatio = switch (tier) {
            case GTValues.LV -> 4;
            case GTValues.MV -> 8;
            default -> 15;
        };
        return aspects("aer", primal, "ignis", primal, "aqua", primal, "terra", primal,
                "ordo", primal, "perditio", perditio, "praecantatio", praecantatio);
    }

    /** LV 2, MV 3, HV 5, matching the low end of the InfusionRecipes convention. */
    private static int infusionInstability(int tier) {
        return switch (tier) {
            case GTValues.LV -> 2;
            case GTValues.MV -> 3;
            default -> 5;
        };
    }

    /** LV Steel, MV Aluminium, HV StainlessSteel: the early mana generator gears. */
    private static Material manaGearMaterial(int tier) {
        return switch (tier) {
            case GTValues.LV -> GTMaterials.Steel;
            case GTValues.MV -> GTMaterials.Aluminium;
            default -> GTMaterials.StainlessSteel;
        };
    }

    /** NaquadahAlloy -> Tritanium -> Neutronium: the node family structural frame. */
    private static Material nodeFrameMaterial(int tier) {
        return switch (tier) {
            case GTValues.ZPM -> GTMaterials.Tritanium;
            case GTValues.UV -> GTMaterials.Neutronium;
            default -> GTMaterials.NaquadahAlloy;
        };
    }

    private static ItemStack magicCircuit(int tier) {
        return switch (tier) {
            case GTValues.LV -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_LV);
            case GTValues.MV -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_MV);
            case GTValues.HV -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_HV);
            case GTValues.EV -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_EV);
            case GTValues.IV -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_IV);
            case GTValues.LuV -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_LUV);
            case GTValues.ZPM -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_ZPM);
            case GTValues.UV -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_UV);
            default -> ItemStack.EMPTY;
        };
    }

    private static ItemStack tierComponent(int tier, String kind, int count) {
        return SafeItems.gt(tierName(tier) + "_" + kind, count);
    }

    private static ItemStack gtPart(TagPrefix prefix, Material material, int count) {
        return ChemicalHelper.get(prefix, material, count);
    }

    private static ItemStack botania(String path, int count) {
        return SafeItems.byId("botania", path, count);
    }

    private static ItemStack thaumcraft(String path, int count) {
        return SafeItems.byId("thaumcraft", path, count);
    }

    private static ItemStack pollutionItem(String name, int count) {
        return SafeItems.of(PollutionItems.get(name), count);
    }

    /** @return the fluid stack, or an empty stack when the material has no fluid in this GTCEu build */
    private static FluidStack fluid(Material material, int amount) {
        if (material == null || !material.hasFluid()) {
            return FluidStack.EMPTY;
        }
        return material.getFluid(amount);
    }

    private static boolean anyEmpty(ItemStack... stacks) {
        for (ItemStack stack : stacks) {
            if (stack == null || stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static MachineDefinition at(MachineDefinition[] family, int tier) {
        return family == null || tier < 0 || tier >= family.length ? null : family[tier];
    }

    private static String tierName(int tier) {
        return GTValues.VN[tier].toLowerCase(Locale.ROOT);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, "machine/" + path);
    }
}
