package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.tterrag.registrate.util.entry.ItemEntry;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.recipes.PORecipeMaps;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import meowmel.pollution.common.item.PollutionItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.Consumer;

/**
 * Magic chemical chain, port of the portable subset of upstream
 * {@code meowmel.pollution.loaders.recipes.MagicChemicalRecipes} (32 of 135
 * recipes).
 *
 * <p><b>Material substitutions</b> (task substitution table plus the ones
 * already established by {@code ForgeAlchemyRecipes}/{@code BotaniaRecipes}):</p>
 * <ul>
 *   <li>KQGold -&gt; TungstenSteel</li>
 *   <li>HyperdimensionalSilver -&gt; NaquadahAlloy</li>
 *   <li>IizunamaruElectrum -&gt; Electrum (as in {@code BotaniaRecipes})</li>
 *   <li>BlackMansus / WhiteMansus / Starrymansus / Impuremana -&gt; InfusedAura</li>
 * </ul>
 *
 * <p><b>Modern API substitutions</b></p>
 * <ul>
 *   <li>The GTQT {@code BACTERIAL_VAT_RECIPES} map does not exist in the port;
 *       the slime-breeding recipes run on the port's magic greenhouse
 *       ({@link PORecipeMaps#MAGIC_GREENHOUSE_RECIPES}), the same substitution
 *       {@code BotaniaRecipes} uses for the GTQT vat. The biomass -&gt; diesel
 *       recipe carries two fluid inputs and does not fit that map's single
 *       fluid slot, so it is skipped.</li>
 *   <li>Upstream meta-items are the port's plain items
 *       ({@link PollutionItems#get(String)}); a recipe is skipped with a log
 *       line when its philosopher stone is absent.</li>
 * </ul>
 *
 * <p><b>Skipped (103 recipes)</b></p>
 * <ul>
 *   <li>Philosopher stone 3/4 duplication: SentientMetal / ExistingNexus
 *       blocks unported.</li>
 *   <li>Evolution core and the three ultimate-catalyst plastic recipes:
 *       DimensionalTransformingAgent unported.</li>
 *   <li>Magic distillation: Impuremana and GTQT Mana both substitute to
 *       InfusedAura, so the recipe would distil a fluid into itself.</li>
 *   <li>EthylSilicate/LLP chain (6): EthylSilicate, LotusDust, RoughLlp and
 *       Llp unported.</li>
 *   <li>Paradox matter, coking-core infusion and the two wood-coking recipes:
 *       Thaumcraft items and the 1.12 infusion API are not available in TC4R
 *       (see {@code TC4RBridge}).</li>
 *   <li>Sugar -&gt; HMF and the two HMF -&gt; MethylFormate recipes:
 *       Crotonaldehyde, ZirconiumTetrachloride and MethylFormate are GTQT
 *       materials.</li>
 *   <li>Zombie brain (ItemsTC.brain) and the GTFO banana: items unported.</li>
 *   <li>Valonite/Octine/Syrmorite philosopher-stone transmutations:
 *       DimensionalTransformingAgent unported.</li>
 *   <li>The whole kqt chain (31), superconductor chain (5), battery chain (20),
 *       filth chain (6), hachimi chain (5) and blood chain (6): their
 *       intermediates (MagicalSulfoPlumbicSalt, CrudeLk99,
 *       BasicBatteryHullAlloy/Content, Filth/VoidWater/VoidMaterial,
 *       HydrazoicAcid/SodiumAzide, Blood Magic life essence and the blood
 *       culture fluids) are unported.</li>
 * </ul>
 */
public final class MagicChemicalRecipes {

    private MagicChemicalRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        philosopherStones(provider);
        catalystCores(provider);
        generalChemistry(provider);
        slimeBreeding(provider);
        beamCores(provider);
        kqtChain(provider);
    }

    // ////////////////////////////////////
    // ***** philosopher stones *****//
    // ////////////////////////////////////

    private static void philosopherStones(Consumer<FinishedRecipe> provider) {
        ItemStack stone1 = philosopherStone(1);
        ItemStack stone2 = philosopherStone(2);

        FluidStack alchemy = fluid(PollutionMaterials.InfusedAlchemy, 14400);
        if (!stone1.isEmpty() && alchemy != null) {
            GTRecipeBuilder.of(id("philosopher_stone_1"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .notConsumable(stone1)
                    .inputItems(ChemicalHelper.get(TagPrefix.block, GTMaterials.TungstenSteel, 1))
                    .inputFluids(alchemy)
                    .outputItems(stone1.copy())
                    .duration(10000)
                    .EUt(GTValues.VA[GTValues.HV])
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/philosopher_stone_1: stone item or InfusedAlchemy missing");
        }

        FluidStack aura = fluid(PollutionMaterials.InfusedAura, 20000);
        if (!stone2.isEmpty() && aura != null) {
            GTRecipeBuilder.of(id("philosopher_stone_2"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .notConsumable(stone2)
                    .inputItems(ChemicalHelper.get(TagPrefix.block, GTMaterials.Electrum, 1))
                    .inputFluids(aura)
                    .outputItems(stone2.copy())
                    .duration(10000)
                    .EUt(7680)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/philosopher_stone_2: stone item or InfusedAura missing");
        }
    }

    // ////////////////////////////////////
    // ***** catalyst cores *****//
    // ////////////////////////////////////

    private static void catalystCores(Consumer<FinishedRecipe> provider) {
        catalystCore(provider, "hot_catalyst_core", PollutionMaterials.InfusedFire,
                PollutionItems.HOT_CATALYST_CORE);
        catalystCore(provider, "cold_catalyst_core", PollutionMaterials.InfusedCold,
                PollutionItems.COLD_CATALYST_CORE);
        catalystCore(provider, "integration_catalyst_core", PollutionMaterials.InfusedMagic,
                PollutionItems.INTEGRATION_CATALYST_CORE);
        catalystCore(provider, "segregation_catalyst_core", PollutionMaterials.InfusedMagic,
                PollutionItems.SEGREGATION_CATALYST_CORE);
    }

    private static void catalystCore(Consumer<FinishedRecipe> provider, String name, Material aspect,
                                     ItemEntry<Item> core) {
        FluidStack essence = fluid(aspect, 2304);
        if (essence == null) {
            Pollution.LOGGER.warn("Skipping magic_chemical/{}: {} has no fluid", name, aspect);
            return;
        }
        GTRecipeBuilder.of(id(name), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                .inputItems(PollutionItems.BLANK_CATALYST_CORE.asStack())
                .inputFluids(essence)
                .notConsumable(core.asStack())
                .outputItems(core.asStack())
                .duration(3600)
                .EUt(GTValues.VA[GTValues.HV])
                .save(provider);
    }

    // ////////////////////////////////////
    // ***** general chemistry *****//
    // ////////////////////////////////////

    private static void generalChemistry(Consumer<FinishedRecipe> provider) {
        // Potassium nitrite route (upstream label; outputs saltpeter + sulfuric acid)
        FluidStack water = fluid(GTMaterials.Water, 1000);
        FluidStack sulfurDioxide = fluid(GTMaterials.SulfurDioxide, 3000);
        FluidStack sulfuricAcid = fluid(GTMaterials.SulfuricAcid, 3000);
        if (water != null && sulfurDioxide != null && sulfuricAcid != null) {
            GTRecipeBuilder.of(id("potassium_nitrite"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Saltpeter, 2))
                    .inputFluids(water)
                    .inputFluids(sulfurDioxide)
                    .notConsumable(PollutionItems.HOT_CATALYST_CORE.asStack())
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Saltpeter, 2))
                    .outputFluids(sulfuricAcid)
                    .duration(200)
                    .EUt(GTValues.VA[GTValues.LV])
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/potassium_nitrite: a required fluid is missing");
        }

        // Oilsands -> graphene + methane + water
        FluidStack methane = fluid(GTMaterials.Methane, 9600);
        FluidStack byproductWater = fluid(GTMaterials.Water, 4800);
        if (methane != null && byproductWater != null) {
            GTRecipeBuilder.of(id("oilsands_graphene"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Oilsands, 4))
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Graphene, 1))
                    .outputFluids(methane)
                    .outputFluids(byproductWater)
                    .duration(200)
                    .EUt(30720)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/oilsands_graphene: a required fluid is missing");
        }

        // 10H2O + N2 + S -> H2SO4 + 2HNO3 + 8H2
        FluidStack water10 = fluid(GTMaterials.Water, 10000);
        FluidStack nitrogen = fluid(GTMaterials.Nitrogen, 1000);
        FluidStack acid = fluid(GTMaterials.SulfuricAcid, 1000);
        FluidStack nitricAcid = fluid(GTMaterials.NitricAcid, 2000);
        FluidStack hydrogen = fluid(GTMaterials.Hydrogen, 8000);
        if (water10 != null && nitrogen != null && acid != null && nitricAcid != null && hydrogen != null) {
            GTRecipeBuilder.of(id("sulfur_nitrogen_water"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Sulfur, 1))
                    .inputFluids(water10)
                    .inputFluids(nitrogen)
                    .outputFluids(acid)
                    .outputFluids(nitricAcid)
                    .outputFluids(hydrogen)
                    .duration(200)
                    .EUt(GTValues.VA[GTValues.EV])
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/sulfur_nitrogen_water: a required fluid is missing");
        }

        // 6NaCl + 2C + 3N2 + 8H2O + 5H2 -> 2NaOH + 2Na2CO3 + 6NH4Cl
        FluidStack water8 = fluid(GTMaterials.Water, 8000);
        FluidStack nitrogen3 = fluid(GTMaterials.Nitrogen, 3000);
        FluidStack hydrogen5 = fluid(GTMaterials.Hydrogen, 5000);
        if (water8 != null && nitrogen3 != null && hydrogen5 != null) {
            GTRecipeBuilder.of(id("salt_carbon_nitrogen"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Carbon, 2))
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Salt, 6))
                    .inputFluids(water8)
                    .inputFluids(nitrogen3)
                    .inputFluids(hydrogen5)
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.SodiumHydroxide, 2))
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.SodaAsh, 2))
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.AmmoniumChloride, 6))
                    .duration(200)
                    .EUt(GTValues.VA[GTValues.EV])
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/salt_carbon_nitrogen: a required fluid is missing");
        }

        // Hydrogen peroxide
        FluidStack peroxideWater = fluid(GTMaterials.Water, 2000);
        FluidStack oxygen = fluid(GTMaterials.Oxygen, 1000);
        FluidStack hydrogenPeroxide = fluid(GTMaterials.HydrogenPeroxide, 2000);
        if (peroxideWater != null && oxygen != null && hydrogenPeroxide != null) {
            GTRecipeBuilder.of(id("hydrogen_peroxide"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .notConsumable(PollutionItems.INTEGRATION_CATALYST_CORE.asStack())
                    .inputFluids(peroxideWater)
                    .inputFluids(oxygen)
                    .outputFluids(hydrogenPeroxide)
                    .duration(200)
                    .EUt(GTValues.VA[GTValues.EV])
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/hydrogen_peroxide: a required fluid is missing");
        }

        // World salt (philosopher stone 1)
        ItemStack stone1 = philosopherStone(1);
        if (!stone1.isEmpty()) {
            GTRecipeBuilder.of(id("salisundus"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .notConsumable(stone1)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Salt, 10))
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Salisundus, 1))
                    .duration(1000)
                    .EUt(GTValues.VA[GTValues.LV])
                    .save(provider);

            // Redstone / electrotine conversion
            GTRecipeBuilder.of(id("electrotine_to_redstone"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .notConsumable(stone1.copy())
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Electrotine, 64))
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Redstone, 64))
                    .duration(100)
                    .EUt(GTValues.VA[GTValues.LV])
                    .save(provider);
            GTRecipeBuilder.of(id("redstone_to_electrotine"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .notConsumable(stone1.copy())
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Redstone, 64))
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Electrotine, 64))
                    .duration(1000)
                    .EUt(GTValues.VA[GTValues.LV])
                    .save(provider);

            // Seawater bromine extraction
            FluidStack saltWater = fluid(GTMaterials.SaltWater, 16000);
            FluidStack bromine = fluid(GTMaterials.Bromine, 100);
            FluidStack iodine = fluid(GTMaterials.Iodine, 10);
            if (saltWater != null && bromine != null && iodine != null) {
                GTRecipeBuilder.of(id("seawater_bromine"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                        .notConsumable(stone1.copy())
                        .inputFluids(saltWater)
                        .outputFluids(bromine)
                        .outputFluids(iodine)
                        .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Salt, 16))
                        .duration(120)
                        .EUt(GTValues.VA[GTValues.EV])
                        .save(provider);
            } else {
                Pollution.LOGGER.warn("Skipping magic_chemical/seawater_bromine: a required fluid is missing");
            }

            // Acetone
            FluidStack acetoneMethane = fluid(GTMaterials.Methane, 3000);
            FluidStack acetoneWater = fluid(GTMaterials.Water, 1000);
            FluidStack acetone = fluid(GTMaterials.Acetone, 1000);
            FluidStack acetoneHydrogen = fluid(GTMaterials.Hydrogen, 4000);
            if (acetoneMethane != null && acetoneWater != null && acetone != null && acetoneHydrogen != null) {
                GTRecipeBuilder.of(id("acetone"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                        .notConsumable(stone1.copy())
                        .inputFluids(acetoneMethane)
                        .inputFluids(acetoneWater)
                        .outputFluids(acetone)
                        .outputFluids(acetoneHydrogen)
                        .circuitMeta(20)
                        .duration(120)
                        .EUt(GTValues.VA[GTValues.HV])
                        .save(provider);
            } else {
                Pollution.LOGGER.warn("Skipping magic_chemical/acetone: a required fluid is missing");
            }

            // Toluene + octane
            FluidStack octaneMethane = fluid(GTMaterials.Methane, 9000);
            FluidStack benzene = fluid(GTMaterials.Benzene, 1000);
            FluidStack octane = fluid(GTMaterials.Octane, 1000);
            FluidStack toluene = fluid(GTMaterials.Toluene, 1000);
            FluidStack octaneHydrogen = fluid(GTMaterials.Hydrogen, 8000);
            if (octaneMethane != null && benzene != null && octane != null && toluene != null
                    && octaneHydrogen != null) {
                GTRecipeBuilder.of(id("toluene_octane"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                        .notConsumable(stone1.copy())
                        .notConsumable(PollutionItems.COKING_CATALYST_CORE.asStack())
                        .inputFluids(octaneMethane)
                        .inputFluids(benzene)
                        .outputFluids(octane)
                        .outputFluids(toluene)
                        .outputFluids(octaneHydrogen)
                        .duration(120)
                        .EUt(GTValues.VA[GTValues.EV])
                        .save(provider);
            } else {
                Pollution.LOGGER.warn("Skipping magic_chemical/toluene_octane: a required fluid is missing");
            }
        } else {
            Pollution.LOGGER.warn("Skipping the philosopher-stone-1 chemistry group: stone item missing");
        }
    }

    // ////////////////////////////////////
    // ***** slime breeding *****//
    // ////////////////////////////////////

    private static void slimeBreeding(Consumer<FinishedRecipe> provider) {
        FluidStack biomass = fluid(GTMaterials.Biomass, 200);
        FluidStack heavyOil = fluid(GTMaterials.OilHeavy, 1000);
        FluidStack glycerol = fluid(GTMaterials.Glycerol, 1000);
        FluidStack glue = fluid(GTMaterials.Glue, 1000);
        FluidStack rubber = fluid(GTMaterials.Rubber, 1000);
        if (biomass != null && heavyOil != null && glycerol != null && glue != null && rubber != null) {
            GTRecipeBuilder.of(id("slime/oil"), PORecipeMaps.MAGIC_GREENHOUSE_RECIPES)
                    .inputFluids(biomass)
                    .notConsumable(PollutionItems.TAR_SLIME.asStack())
                    .outputFluids(heavyOil)
                    .duration(60)
                    .EUt(GTValues.VA[GTValues.LV])
                    .save(provider);
            GTRecipeBuilder.of(id("slime/sugar"), PORecipeMaps.MAGIC_GREENHOUSE_RECIPES)
                    .inputFluids(fluid(GTMaterials.Biomass, 200))
                    .notConsumable(PollutionItems.SUGAR_SLIME.asStack())
                    .outputItems(Items.SUGAR, 16)
                    .duration(60)
                    .EUt(GTValues.VA[GTValues.LV])
                    .save(provider);
            GTRecipeBuilder.of(id("slime/glycerol"), PORecipeMaps.MAGIC_GREENHOUSE_RECIPES)
                    .inputFluids(fluid(GTMaterials.Biomass, 200))
                    .notConsumable(PollutionItems.GLYCEROL_SLIME.asStack())
                    .outputFluids(glycerol)
                    .duration(60)
                    .EUt(GTValues.VA[GTValues.LV])
                    .save(provider);
            GTRecipeBuilder.of(id("slime/glue"), PORecipeMaps.MAGIC_GREENHOUSE_RECIPES)
                    .inputFluids(fluid(GTMaterials.Biomass, 200))
                    .notConsumable(PollutionItems.GLUE_SLIME.asStack())
                    .outputFluids(glue)
                    .duration(60)
                    .EUt(GTValues.VA[GTValues.LV])
                    .save(provider);
            GTRecipeBuilder.of(id("slime/rubber"), PORecipeMaps.MAGIC_GREENHOUSE_RECIPES)
                    .inputFluids(fluid(GTMaterials.Biomass, 200))
                    .notConsumable(PollutionItems.RUBBER_SLIME.asStack())
                    .outputFluids(rubber)
                    .duration(60)
                    .EUt(GTValues.VA[GTValues.LV])
                    .save(provider);

            // Other slimes: tar slime + the matching medium
            GTRecipeBuilder.of(id("slime/tar_to_sugar"), GTRecipeTypes.CHEMICAL_RECIPES)
                    .inputItems(PollutionItems.TAR_SLIME.asStack())
                    .inputItems(Items.SUGAR, 4)
                    .outputItems(PollutionItems.SUGAR_SLIME.asStack())
                    .duration(100)
                    .EUt(GTValues.VA[GTValues.LV])
                    .save(provider);
            GTRecipeBuilder.of(id("slime/tar_to_glue"), GTRecipeTypes.CHEMICAL_RECIPES)
                    .inputItems(PollutionItems.TAR_SLIME.asStack())
                    .inputFluids(glue)
                    .outputItems(PollutionItems.GLUE_SLIME.asStack())
                    .duration(100)
                    .EUt(GTValues.VA[GTValues.LV])
                    .save(provider);
            GTRecipeBuilder.of(id("slime/tar_to_glycerol"), GTRecipeTypes.CHEMICAL_RECIPES)
                    .inputItems(PollutionItems.TAR_SLIME.asStack())
                    .inputFluids(glycerol)
                    .outputItems(PollutionItems.GLYCEROL_SLIME.asStack())
                    .duration(100)
                    .EUt(GTValues.VA[GTValues.LV])
                    .save(provider);
            GTRecipeBuilder.of(id("slime/tar_to_rubber"), GTRecipeTypes.CHEMICAL_RECIPES)
                    .inputItems(PollutionItems.TAR_SLIME.asStack())
                    .inputFluids(rubber)
                    .outputItems(PollutionItems.RUBBER_SLIME.asStack())
                    .duration(100)
                    .EUt(GTValues.VA[GTValues.LV])
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping the magic_chemical slime-breeding group: a required fluid is missing");
        }
    }

    // ////////////////////////////////////
    // ***** beam cores *****//
    // ////////////////////////////////////

    private static void beamCores(Consumer<FinishedRecipe> provider) {
        ItemStack stone1 = philosopherStone(1);
        FluidStack energy = fluid(PollutionMaterials.InfusedEnergy, 576);
        if (stone1.isEmpty() || energy == null) {
            Pollution.LOGGER.warn("Skipping the magic_chemical beam-core group: stone item or InfusedEnergy missing");
            return;
        }
        ItemStack frame = ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.NaquadahAlloy, 1);
        beamCore(provider, stone1, frame, energy, 1, PollutionMagicBlocks.BEAM_CORE_0.asStack());
        beamCore(provider, stone1, frame, energy, 2, PollutionMagicBlocks.BEAM_CORE_1.asStack());
        beamCore(provider, stone1, frame, energy, 3, PollutionMagicBlocks.BEAM_CORE_2.asStack());
        beamCore(provider, stone1, frame, energy, 4, PollutionMagicBlocks.BEAM_CORE_3.asStack());
        beamCore(provider, stone1, frame, energy, 5, PollutionMagicBlocks.BEAM_CORE_4.asStack());
    }

    private static void beamCore(Consumer<FinishedRecipe> provider, ItemStack stone, ItemStack frame,
                                 FluidStack energy, int circuit, ItemStack output) {
        GTRecipeBuilder.of(id("beam_core_" + (circuit - 1)), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                .notConsumable(stone.copy())
                .inputItems(frame.copy())
                .inputFluids(energy)
                .outputItems(output)
                .circuitMeta(circuit)
                .duration(1000)
                .EUt(GTValues.VA[GTValues.HV])
                .save(provider);
    }

    // ////////////////////////////////////
    // ***** kqt chain (portable head) *****//
    // ////////////////////////////////////

    private static void kqtChain(Consumer<FinishedRecipe> provider) {
        // Galena + world salt -> sulfo-plumbic salt; the rest of the kqt chain is unported
        GTRecipeBuilder.of(id("sulfo_plumbic_salt"), GTRecipeTypes.MIXER_RECIPES)
                .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Salisundus, 1))
                .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Galena, 2))
                .outputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.SulfoPlumbicSalt, 3))
                .duration(300)
                .EUt(GTValues.VA[GTValues.HV])
                .save(provider);
    }

    // ////////////////////////////////////
    // ***** helpers *****//
    // ////////////////////////////////////

    private static ItemStack philosopherStone(int tier) {
        ItemEntry<Item> entry = PollutionItems.get("stone_of_philosopher_" + tier);
        if (entry == null) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(entry.get());
    }

    /** @return the fluid stack, or null when the material has no fluid in this GTCEu build */
    private static FluidStack fluid(Material material, int amount) {
        if (material == null || !material.hasFluid()) {
            return null;
        }
        return material.getFluid(amount);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("pollution", "magic_chemical/" + path);
    }
}
