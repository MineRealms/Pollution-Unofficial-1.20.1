package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.recipes.PORecipeMaps;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.item.PollutionItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

/**
 * Thaumcraft-facing chemistry recipes.
 *
 * <p>Port of the portable part of upstream {@code ThaumcraftRecipes}
 * (14 of 22). Substitutions: GTQT Thaumium -&gt; StainlessSteel, GTQT Mana -&gt;
 * InfusedAura, Sunnarium -&gt; Titanium (Sunnarium is not part of GTCEu Modern),
 * HOTCORE/BLANKCORE -&gt; the ported catalyst core items. The eight custom
 * wire-coil conversions live in {@code CoilRecipes} and are not duplicated
 * here. The solar-plate, catalyst-core and artificial-scabyst infusions of the
 * upstream file are registered by {@code InfusionRecipes} through TC4R's
 * infusion serializer (see {@code docs/TC4R_INFUSION_API.md}).</p>
 *
 * <p>// 跳过: 上游剩余两条纯奥术工作台配方（vis_resonator_efficient、
 * morphic_resonator_efficient、vis_battery_efficient）依赖未移植的
 * ItemsTC.visResonator / morphicResonator / BlocksTC.visBattery，
 * 且本移植版把过滤器等奥术配方改写为 GT 组装机配方。</p>
 *
 * <p><b>Newly ported maceration</b> (previously skipped):</p>
 * <ul>
 *   <li>// 上游: {@code ItemsTC.ingots} (Thaumcraft ingot meta) -&gt;
 *       本移植版: TC4R {@code TCItems.THAUMIUM_INGOT}</li>
 * </ul>
 *
 * <p>// 魔力钢打粉不在此注册: GTNN 自带
 * {@code gtceu:macerator/macerate_manasteel_ingot}，本移植版的重复配方
 * 会被 GT 配方查找表拒绝。</p>
 */
public final class ThaumcraftRecipes {

    private ThaumcraftRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        coreChemistry(provider);
        substrateChemistry(provider);
        macerations(provider);
    }

    /** 打粉：神秘锭（TC4R thaumium ingot -> StainlessSteel）。 */
    private static void macerations(Consumer<FinishedRecipe> provider) {
        // 上游: Botania ModItems.manaResource -> GTNN ManaSteel 打粉不在此注册:
        // GTNN 自带 gtceu:macerator/macerate_manasteel_ingot，重复配方会被
        // GT 配方查找表拒绝（原 pollution:macerator/thaumcraft/manasteel_dust）。

        // 上游: Thaumcraft ItemsTC.ingots -> 本移植版: TC4R thaumium ingot
        ItemStack thaumiumIngot = SafeItems.byId("thaumcraft", "thaumium_ingot", 1);
        if (thaumiumIngot.isEmpty()) {
            Pollution.LOGGER.warn("Thaumcraft: thaumcraft:thaumium_ingot is not registered, "
                    + "skipping thaumium_dust maceration");
            return;
        }
        GTRecipeBuilder.of(id("thaumium_dust"), GTRecipeTypes.MACERATOR_RECIPES)
                .inputItems(thaumiumIngot)
                .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.StainlessSteel, 1))
                .duration(10)
                .EUt(2)
                .save(provider);
    }

    private static void coreChemistry(Consumer<FinishedRecipe> provider) {
        GTRecipeBuilder.of(id("sunnarium"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                .inputFluids(PollutionMaterials.InfusedLight.getFluid(2304))
                .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Salisundus, 4))
                .notConsumable(PollutionItems.HOT_CATALYST_CORE.get())
                .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Titanium, 1))
                .duration(200)
                .EUt(GTValues.VA[GTValues.HV])
                .save(provider);

        GTRecipeBuilder.of(id("roughdraft"), GTRecipeTypes.MIXER_RECIPES)
                .inputFluids(GTMaterials.Mercury.getFluid(1000))
                .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Amethyst, 1))
                .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.CertusQuartz, 1))
                .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Opal, 1))
                .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Salt, 1))
                .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Sulfur, 1))
                .outputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Roughdraft, 6))
                .duration(600)
                .EUt(GTValues.VA[GTValues.MV])
                .save(provider);

        GTRecipeBuilder.of(id("substrate"), GTRecipeTypes.MIXER_RECIPES)
                .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Roughdraft, 6))
                .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.InfusedAir, 1))
                .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.InfusedFire, 1))
                .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.InfusedEntropy, 1))
                .outputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Substrate, 9))
                .duration(600)
                .EUt(GTValues.VA[GTValues.MV])
                .save(provider);

        GTRecipeBuilder.of(id("core_charging"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                .inputFluids(PollutionMaterials.InfusedWater.getFluid(9216))
                .inputFluids(PollutionMaterials.InfusedFire.getFluid(9216))
                .inputFluids(PollutionMaterials.InfusedOrder.getFluid(9216))
                .inputFluids(PollutionMaterials.InfusedAir.getFluid(9216))
                .inputItems(ChemicalHelper.get(TagPrefix.gem, PollutionMaterials.Valonite, 1))
                .notConsumable(PollutionItems.BLANK_CATALYST_CORE.get())
                .outputItems(PollutionItems.BLANK_CATALYST_CORE.get())
                .duration(3600)
                .EUt(GTValues.VA[GTValues.HV])
                .save(provider);
    }

    private static void substrateChemistry(Consumer<FinishedRecipe> provider) {
        alloyBlast(provider, "basic_substrate", 2700, 1200, GTValues.HV,
                ChemicalHelper.get(TagPrefix.dust, GTMaterials.Sulfur, 3),
                ChemicalHelper.get(TagPrefix.dust, GTMaterials.Salt, 2),
                ChemicalHelper.get(TagPrefix.dust, GTMaterials.Bismuth, 1),
                PollutionMaterials.BasicSubstrate, 1440, 4000);
        alloyBlast(provider, "basic_substrate_salis", 2700, 1200, GTValues.HV,
                ChemicalHelper.get(TagPrefix.dust, GTMaterials.Sulfur, 3),
                ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Salisundus, 2),
                ChemicalHelper.get(TagPrefix.dust, GTMaterials.Bismuth, 1),
                PollutionMaterials.BasicSubstrate, 5760, 4000);
        alloyBlast(provider, "advanced_substrate", 3600, 1200, GTValues.EV,
                ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.SulfoPlumbicSalt, 3),
                ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Salisundus, 2),
                ChemicalHelper.get(TagPrefix.dust, GTMaterials.Bismuth, 4),
                PollutionMaterials.AdvancedSubstrate, 1440, 1000);
        alloyBlast(provider, "advanced_substrate_syrmorite", 3600, 1200, GTValues.EV,
                ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.SulfoPlumbicSalt, 3),
                ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Salisundus, 2),
                ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Syrmorite, 4),
                PollutionMaterials.AdvancedSubstrate, 5760, 1000);
        alloyBlast(provider, "hyper_substrate", 3600, 2400, GTValues.LuV,
                ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Valonite, 3),
                ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Salisundus, 2),
                ChemicalHelper.get(TagPrefix.dust, GTMaterials.Bismuth, 4),
                PollutionMaterials.HyperSubstrate, 1440, 1000);
        alloyBlast(provider, "hyper_substrate_syrmorite", 3600, 2400, GTValues.LuV,
                ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Valonite, 3),
                ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Salisundus, 2),
                ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Syrmorite, 4),
                PollutionMaterials.HyperSubstrate, 5760, 1000);

        GTRecipeBuilder.of(id("thaummix"), GTRecipeTypes.MIXER_RECIPES)
                .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Syrmorite, 1))
                .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Octine, 1))
                .outputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Thaummix, 2))
                .duration(100)
                .EUt(GTValues.VA[GTValues.MV])
                .save(provider);

        GTRecipeBuilder.of(id("thaumium"), GTRecipeTypes.BLAST_RECIPES)
                .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Thaummix, 2))
                .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.StainlessSteel, 1))
                .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Redstone, 1))
                .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Copper, 1))
                .duration(400)
                .EUt(GTValues.VA[GTValues.MV])
                .addData("ebf_temp", 1800)
                .save(provider);
    }

    private static void alloyBlast(Consumer<FinishedRecipe> provider, String name, int temperature, int duration,
                                   int tier, net.minecraft.world.item.ItemStack first,
                                   net.minecraft.world.item.ItemStack second,
                                   net.minecraft.world.item.ItemStack third,
                                   com.gregtechceu.gtceu.api.data.chemical.material.Material output,
                                   int outputAmount, int manaAmount) {
        GTRecipeBuilder.of(id(name), PORecipeMaps.MAGIC_ALLOY_BLAST_RECIPES)
                .inputItems(first)
                .inputItems(second)
                .inputItems(third)
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(manaAmount))
                .outputFluids(output.getFluid(outputAmount))
                .duration(duration)
                .EUt(GTValues.VA[tier])
                .addData("ebf_temp", temperature)
                .save(provider);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("pollution", "thaumcraft/" + path);
    }
}
