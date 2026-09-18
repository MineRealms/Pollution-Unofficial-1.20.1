package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.tterrag.registrate.util.entry.ItemEntry;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.recipes.PORecipeMaps;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.item.PollutionItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

/**
 * Forge alchemy recipes ({@code pollution:forge_alchemy} map).
 *
 * <p>Port of the portable subset of upstream
 * {@code meowmel.pollution.loaders.recipes.ForgeAlchemyRecipes} (1.12.2). The
 * upstream file uses Pollution's own recipe map and no Forbidden Magic class;
 * the addon's alchemy content is datapack JSON, so no addon API call is needed
 * here (see {@code docs/PHASE6C_API.md} §2.3).</p>
 *
 * <p>Ported (15 recipes): the nine basic metal triads, the three advanced
 * noble/rare-earth triads, the advanced-substrate Syrmorite/Octine/Valonite
 * transmutation and the two six-aspect-alloy transmutations.</p>
 *
 * <p>Documented substitutions / skips:</p>
 * <ul>
 *   <li>GTQT {@code Mana} fluid is replaced by {@code InfusedAura}, the same
 *       substitution used by {@code ThaumcraftRecipes}.</li>
 *   <li>Upstream meta-item philosopher stones (damage 150/151) are the port's
 *       plain items {@code stone_of_philosopher_1}/{@code _2}; recipes are
 *       skipped if the item is absent.</li>
 *   <li>Skipped: the three philosopher-stone upgrades, the
 *       DimensionalTransformingAgent chain, HyperdimensionalSilver, KQGold,
 *       Terrasteel, ElvenElementium, Manasteel/Thaumium/Mansussteel,
 *       IizunamaruElectrum, AethericDarkSteel and BloodOfAvernus. They need
 *       materials or fluids that are not ported (Black/White/Starrymansus,
 *       Sentient/BindingMetal, Existing/FadingNexus, GTQT Mana/Thaumium/
 *       VoidMetal, Blood Magic life essence, Botania runes).</li>
 * </ul>
 */
public final class ForgeAlchemyRecipes {

    private static final int CHANCE = 3333;
    private static final int BOOST = 500;
    private static final int FLUID_AMOUNT = 2304;
    private static final int MANA_AMOUNT = 1000;

    private ForgeAlchemyRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        basicMetalTransmutations(provider);
        advancedMetalTransmutations(provider);
        substrateTransmutations(provider);
    }

    /** Nine triads from BasicSubstrate, stone tier 1, HV. */
    private static void basicMetalTransmutations(Consumer<FinishedRecipe> provider) {
        ItemStack stone = philosopherStone(1);
        if (stone.isEmpty()) {
            return;
        }
        basicTriad(provider, stone, 1, 3600, 2500, 1920,
                GTMaterials.Lead, GTMaterials.Tin, GTMaterials.Iron);
        basicTriad(provider, stone, 2, 3600, 3000, 1920,
                GTMaterials.Zinc, GTMaterials.Nickel, GTMaterials.Cobalt);
        basicTriad(provider, stone, 3, 3600, 3500, 1920,
                GTMaterials.Copper, GTMaterials.Silver, GTMaterials.Gold);
        basicTriad(provider, stone, 4, 4500, 4000, 1920,
                GTMaterials.Manganese, GTMaterials.Molybdenum, GTMaterials.Neodymium);
        basicTriad(provider, stone, 5, 4500, 4500, 1920,
                GTMaterials.Gallium, GTMaterials.Vanadium, GTMaterials.Chromium);
        basicTriad(provider, stone, 6, 4500, 5000, 1920,
                GTMaterials.Antimony, GTMaterials.Beryllium, GTMaterials.Bismuth);
        basicTriad(provider, stone, 7, 5400, 9000, 7680,
                GTMaterials.Aluminium, GTMaterials.Titanium, GTMaterials.Tungsten);
        basicTriad(provider, stone, 8, 5400, 9500, 7680,
                GTMaterials.Niobium, GTMaterials.Tantalum, GTMaterials.Yttrium);
        basicTriad(provider, stone, 9, 5400, 10000, 7680,
                GTMaterials.Thorium, GTMaterials.Uranium238, GTMaterials.Plutonium241);
    }

    private static void basicTriad(Consumer<FinishedRecipe> provider, ItemStack stone, int circuit,
                                   int temperature, int duration, int eu, Material... outputs) {
        GTRecipeBuilder builder = GTRecipeBuilder
                .of(id("metal/" + circuit), PORecipeMaps.FORGE_ALCHEMY_RECIPES)
                .inputFluids(PollutionMaterials.BasicSubstrate.getFluid(144))
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(MANA_AMOUNT))
                .notConsumable(stone)
                .circuitMeta(circuit)
                .blastFurnaceTemp(temperature)
                .duration(duration)
                .EUt(eu);
        for (Material output : outputs) {
            builder.chancedOutput(output.getFluid(FLUID_AMOUNT), CHANCE, BOOST);
        }
        builder.save(provider);
    }

    /** Three triads from AdvancedSubstrate, stone tier 2, EV+. */
    private static void advancedMetalTransmutations(Consumer<FinishedRecipe> provider) {
        ItemStack stone = philosopherStone(2);
        if (stone.isEmpty()) {
            return;
        }
        advancedTriad(provider, stone, 2, 6300, 14000,
                GTMaterials.Platinum, GTMaterials.Palladium, GTMaterials.Ruthenium);
        advancedTriad(provider, stone, 3, 6300, 14500,
                GTMaterials.Rhodium, GTMaterials.Iridium, GTMaterials.Osmium);
        advancedTriad(provider, stone, 4, 6300, 15000,
                GTMaterials.Cerium, GTMaterials.Europium, GTMaterials.Samarium);
    }

    private static void advancedTriad(Consumer<FinishedRecipe> provider, ItemStack stone, int circuit,
                                      int temperature, int duration, Material... outputs) {
        GTRecipeBuilder builder = GTRecipeBuilder
                .of(id("metal_advanced/" + circuit), PORecipeMaps.FORGE_ALCHEMY_RECIPES)
                .inputFluids(PollutionMaterials.AdvancedSubstrate.getFluid(144))
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(4000))
                .notConsumable(stone)
                .circuitMeta(circuit)
                .blastFurnaceTemp(temperature)
                .duration(duration)
                .EUt(30720);
        for (Material output : outputs) {
            builder.chancedOutput(output.getFluid(FLUID_AMOUNT), CHANCE, BOOST);
        }
        builder.save(provider);
    }

    /** Advanced-substrate specials and the two six-aspect-alloy transmutations. */
    private static void substrateTransmutations(Consumer<FinishedRecipe> provider) {
        ItemStack basicStone = philosopherStone(1);
        if (!basicStone.isEmpty()) {
            // 赛摩铜 / 炽焰铁 / 法罗钠
            GTRecipeBuilder.of(id("substrate/twilight"), PORecipeMaps.FORGE_ALCHEMY_RECIPES)
                    .inputFluids(PollutionMaterials.AdvancedSubstrate.getFluid(144))
                    .inputFluids(PollutionMaterials.InfusedAura.getFluid(MANA_AMOUNT))
                    .notConsumable(basicStone)
                    .chancedOutput(PollutionMaterials.Syrmorite.getFluid(FLUID_AMOUNT), CHANCE, BOOST)
                    .chancedOutput(PollutionMaterials.Octine.getFluid(FLUID_AMOUNT), CHANCE, BOOST)
                    .chancedOutput(ChemicalHelper.get(TagPrefix.gem, PollutionMaterials.Valonite, 16),
                            CHANCE, BOOST)
                    .circuitMeta(1)
                    .blastFurnaceTemp(5400)
                    .duration(10000)
                    .EUt(7680)
                    .save(provider);

            // 律动钛 / 定坤铜 / 司辰铅
            GTRecipeBuilder.of(id("substrate/aer_terra_ordo"), PORecipeMaps.FORGE_ALCHEMY_RECIPES)
                    .inputFluids(PollutionMaterials.BasicSubstrate.getFluid(144))
                    .inputFluids(PollutionMaterials.InfusedAura.getFluid(MANA_AMOUNT))
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Titanium, 4))
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Copper, 4))
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Lead, 4))
                    .notConsumable(basicStone)
                    .chancedOutput(PollutionMaterials.Aertitanium.getFluid(FLUID_AMOUNT), CHANCE, BOOST)
                    .chancedOutput(PollutionMaterials.Terracopper.getFluid(FLUID_AMOUNT), CHANCE, BOOST)
                    .chancedOutput(PollutionMaterials.Ordolead.getFluid(FLUID_AMOUNT), CHANCE, BOOST)
                    .circuitMeta(20)
                    .blastFurnaceTemp(3600)
                    .duration(2000)
                    .EUt(1920)
                    .save(provider);

            // 残日钢 / 捩花银 / 无极铝
            GTRecipeBuilder.of(id("substrate/ignis_aqua_perditio"), PORecipeMaps.FORGE_ALCHEMY_RECIPES)
                    .inputFluids(PollutionMaterials.BasicSubstrate.getFluid(144))
                    .inputFluids(PollutionMaterials.InfusedAura.getFluid(MANA_AMOUNT))
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Steel, 4))
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Silver, 4))
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Aluminium, 4))
                    .notConsumable(basicStone)
                    .chancedOutput(PollutionMaterials.IgnisSteel.getFluid(FLUID_AMOUNT), CHANCE, BOOST)
                    .chancedOutput(PollutionMaterials.Aquasilver.getFluid(FLUID_AMOUNT), CHANCE, BOOST)
                    .chancedOutput(PollutionMaterials.Perditioaluminium.getFluid(FLUID_AMOUNT), CHANCE, BOOST)
                    .circuitMeta(20)
                    .blastFurnaceTemp(3600)
                    .duration(2000)
                    .EUt(1920)
                    .save(provider);
        }
    }

    private static ItemStack philosopherStone(int tier) {
        ItemEntry<Item> entry = PollutionItems.get("stone_of_philosopher_" + tier);
        if (entry == null) {
            Pollution.LOGGER.warn("Forge alchemy: stone_of_philosopher_{} is not registered, "
                    + "skipping its recipes", tier);
            return ItemStack.EMPTY;
        }
        return new ItemStack(entry.get());
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("pollution", "forge_alchemy/" + path);
    }
}
