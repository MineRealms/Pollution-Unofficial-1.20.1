package meowmel.pollution.loaders.recipes;

import appeng.api.util.AEColor;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.core.definitions.ItemDefinition;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.recipes.PORecipeMaps;
import meowmel.pollution.api.unification.PollutionMaterials;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.frameGt;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.plate;

/**
 * AE2 integration, port of upstream {@code AERecipes}.
 *
 * <p>Upstream held 33 GregTech recipes (1 blast furnace + 32 magic assembler,
 * all inside the disabled {@code common()}/{@code magic_assembler()} bodies).
 * The Thaumcraft arcane/infusion entries in the same upstream file belong to
 * {@code ThaumcraftRecipes} and are not duplicated here.</p>
 *
 * <p><b>AE2 APIs used</b> (verified with {@code javap} against
 * appliedenergistics2-forge 15.0.18): {@link AEItems}, {@link AEBlocks},
 * {@link AEParts} and their {@link ItemDefinition#stack(int)} accessors plus
 * {@code ColoredItemDefinition#stack(AEColor, int)} for cables. The 1.12.2
 * metadata indices from {@code ae2Index} map to named definitions:
 * material 16/17/18 = processor prints, 22/23/24 = logic/engineering/
 * calculation processor, 35-38 = item storage components, 43/44 =
 * formation/annihilation core; part 16/140 = glass cable/quartz fiber,
 * part 240/260 = import/export bus, part 460 = ME P2P tunnel.</p>
 *
 * <p><b>Material substitutions</b></p>
 * <ul>
 *   <li>GTQT Mana -&gt; {@code PollutionMaterials.InfusedAura}</li>
 *   <li>meowmelgold -&gt; {@code GTMaterials.TungstenSteel}</li>
 *   <li>hyperdimensional_silver -&gt; {@code GTMaterials.NaquadahAlloy}</li>
 *   <li>mansussteel -&gt; {@code GTMaterials.HSSG}</li>
 *   <li>GTQTMaterials.Fluix -&gt; AE2 {@code FLUIX_DUST} / {@code FLUIX_CRYSTAL}</li>
 *   <li>{@code MarkerMaterials.Tier} circuit inputs -&gt; GTCEu 7.5.3
 *       {@code CustomTags.*_CIRCUITS} item tags (MarkerMaterials.Tier no longer
 *       exists in GTCEu Modern)</li>
 *   <li>{@code MetaItems.CENTRAL_PROCESSING_UNIT}, {@code RANDOM_ACCESS_MEMORY},
 *       {@code ULTRA_LOW_POWER_INTEGRATED_CIRCUIT}, {@code LOW_POWER_INTEGRATED_CIRCUIT}
 *       -&gt; the {@code GTItems} equivalents</li>
 * </ul>
 *
 * <p><b>AE2 15.0.18 merges</b> (upstream items that no longer exist separately)</p>
 * <ul>
 *   <li>ME Fluid Interface block -&gt; {@code AEBlocks.INTERFACE}: the modern
 *       interface uses a key-agnostic config/storage inventory and handles
 *       fluids.</li>
 *   <li>Fluid storage components (material 54-57) -&gt; the shared
 *       {@code AEItems.CELL_COMPONENT_*} items; AE2 15 crafts its fluid cells
 *       from the same components as item cells.</li>
 *   <li>Fluid import/export buses (part 241/261) -&gt; the normal
 *       {@code AEParts.IMPORT_BUS} / {@code EXPORT_BUS}, whose GUI exposes the
 *       fluid mode. The two upstream fluid-bus recipes collapse into exact
 *       duplicates of the item-bus recipes and are skipped.</li>
 * </ul>
 *
 * <p><b>nae2 high-tier storage substitution</b></p>
 * <ul>
 *   <li>// 上游: nae2:material 19-22/24-27 (256k/1M/4M/16M item/fluid cell
 *       components) -> 本移植版: AE2 {@code CELL_COMPONENT_256K}. AE2 15.4.10
 *       only ships components up to 256k; the 1M/4M/16M tiers keep their
 *       relative storage scale by outputting 4x/16x/64x of the 256k component.
 *       The upstream {@code GTQTMetaItems.NANO_POWER_IC} UV input maps to
 *       {@code GTItems.NANO_CENTRAL_PROCESSING_UNIT}.</li>
 * </ul>
 *
 * <p><b>Skipped recipes</b> (2 of 33)</p>
 * <ul>
 *   <li>2 fluid bus recipes: duplicates after the fluid bus merge (see above).</li>
 * </ul>
 */
public final class AERecipes {

    private AERecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        chargedQuartzGlass(provider);
        cores(provider);
        processors(provider);
        interfacesAndBuses(provider);
        storageComponents(provider);
        storageComponentsHighTier(provider);
        networkBlocks(provider);
    }

    /** 聚能石英玻璃：石英玻璃 + 萤石粉 -> 充能石英玻璃 */
    private static void chargedQuartzGlass(Consumer<FinishedRecipe> provider) {
        GTRecipeBuilder.of(id("vibrant_quartz_glass"), GTRecipeTypes.BLAST_RECIPES)
                .inputItems(ae(AEBlocks.QUARTZ_GLASS, 1))
                .inputItems(ae(AEItems.FLUIX_DUST, 2))
                .outputItems(ae(AEBlocks.QUARTZ_VIBRANT_GLASS, 1))
                .duration(100)
                .blastFurnaceTemp(1800)
                .EUt(120)
                .save(provider);
    }

    /** 破坏核心 / 成型核心 */
    private static void cores(Consumer<FinishedRecipe> provider) {
        GTRecipeBuilder.of(id("annihilation_core"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(ChemicalHelper.get(plate, GTMaterials.CertusQuartz, 6))
                .inputItems(ae(AEItems.FLUIX_DUST, 1))
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(100))
                .outputItems(ae(AEItems.ANNIHILATION_CORE, 16))
                .duration(160)
                .circuitMeta(1)
                .EUt(480)
                .save(provider);

        GTRecipeBuilder.of(id("formation_core"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(ChemicalHelper.get(plate, GTMaterials.NetherQuartz, 6))
                .inputItems(ae(AEItems.FLUIX_DUST, 1))
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(100))
                .outputItems(ae(AEItems.FORMATION_CORE, 16))
                .duration(160)
                .circuitMeta(1)
                .EUt(480)
                .save(provider);
    }

    /** 三种电路板：逻辑 / 工程 / 运算处理器 */
    private static void processors(Consumer<FinishedRecipe> provider) {
        GTRecipeBuilder.of(id("logic_processor"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(ae(AEItems.LOGIC_PROCESSOR_PRINT, 4))
                .inputItems(ChemicalHelper.get(plate, GTMaterials.RedAlloy, 1))
                .inputFluids(GTMaterials.HSSG.getFluid(144))
                .outputItems(ae(AEItems.LOGIC_PROCESSOR, 16))
                .duration(160)
                .EUt(480)
                .save(provider);

        GTRecipeBuilder.of(id("engineering_processor"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(ae(AEItems.ENGINEERING_PROCESSOR_PRINT, 4))
                .inputItems(ChemicalHelper.get(plate, GTMaterials.RedAlloy, 1))
                .inputFluids(GTMaterials.HSSG.getFluid(144))
                .outputItems(ae(AEItems.ENGINEERING_PROCESSOR, 16))
                .duration(160)
                .EUt(480)
                .save(provider);

        GTRecipeBuilder.of(id("calculation_processor"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(ae(AEItems.CALCULATION_PROCESSOR_PRINT, 4))
                .inputItems(ChemicalHelper.get(plate, GTMaterials.RedAlloy, 1))
                .inputFluids(GTMaterials.HSSG.getFluid(144))
                .outputItems(ae(AEItems.CALCULATION_PROCESSOR, 16))
                .duration(160)
                .EUt(480)
                .save(provider);
    }

    /** ME 接口 / 流体接口 / 输入输出总线 */
    private static void interfacesAndBuses(Consumer<FinishedRecipe> provider) {
        GTRecipeBuilder.of(id("me_interface"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(ChemicalHelper.get(plate, GTMaterials.Titanium, 6))
                .inputItems(ChemicalHelper.get(frameGt, GTMaterials.TungstenSteel, 1))
                .inputItems(ae(AEItems.FORMATION_CORE, 1))
                .inputItems(ae(AEItems.ANNIHILATION_CORE, 1))
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(100))
                .outputItems(ae(AEBlocks.INTERFACE, 16))
                .duration(160)
                .circuitMeta(20)
                .EUt(1920)
                .save(provider);

        // 流体接口：AE2 15 已把流体支持并入普通 ME 接口
        GTRecipeBuilder.of(id("me_fluid_interface"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(ChemicalHelper.get(plate, GTMaterials.Titanium, 6))
                .inputItems(ChemicalHelper.get(frameGt, GTMaterials.NaquadahAlloy, 1))
                .inputItems(ae(AEItems.FORMATION_CORE, 1))
                .inputItems(ae(AEItems.ANNIHILATION_CORE, 1))
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(100))
                .outputItems(ae(AEBlocks.INTERFACE, 16))
                .duration(160)
                .circuitMeta(20)
                .EUt(1920)
                .save(provider);

        // 输入总线（流体总线在 AE2 15 中并入同一物品）
        GTRecipeBuilder.of(id("import_bus"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(ChemicalHelper.get(plate, GTMaterials.Titanium, 4))
                .inputItems(ChemicalHelper.get(frameGt, GTMaterials.TungstenSteel, 1))
                .inputItems(ae(AEBlocks.INTERFACE, 1))
                .inputItems(ae(AEItems.ANNIHILATION_CORE, 1))
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(100))
                .outputItems(ae(AEParts.IMPORT_BUS, 8))
                .duration(160)
                .circuitMeta(18)
                .EUt(480)
                .save(provider);

        // 输出总线（流体总线在 AE2 15 中并入同一物品）
        GTRecipeBuilder.of(id("export_bus"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(ChemicalHelper.get(plate, GTMaterials.Titanium, 4))
                .inputItems(ChemicalHelper.get(frameGt, GTMaterials.NaquadahAlloy, 1))
                .inputItems(ae(AEBlocks.INTERFACE, 1))
                .inputItems(ae(AEItems.FORMATION_CORE, 1))
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(100))
                .outputItems(ae(AEParts.EXPORT_BUS, 8))
                .duration(160)
                .circuitMeta(19)
                .EUt(480)
                .save(provider);
    }

    /**
     * 存储组件。上游的流体存储组件（material 54-57）在 AE2 15 中已并入共享的
     * item cell component，因此两组配方分别使用逻辑处理器 / 运算处理器产出同一物品。
     */
    private static void storageComponents(Consumer<FinishedRecipe> provider) {
        GTRecipeBuilder.of(id("cell_component_1k"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(GTItems.CENTRAL_PROCESSING_UNIT.asStack(1))
                .inputItems(CustomTags.LV_CIRCUITS, 4)
                .inputItems(ae(AEItems.LOGIC_PROCESSOR, 1))
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(100))
                .outputItems(ae(AEItems.CELL_COMPONENT_1K, 4))
                .duration(320)
                .circuitMeta(22)
                .EUt(30)
                .save(provider);

        GTRecipeBuilder.of(id("cell_component_4k"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(GTItems.RANDOM_ACCESS_MEMORY.asStack(1))
                .inputItems(CustomTags.MV_CIRCUITS, 4)
                .inputItems(ae(AEItems.LOGIC_PROCESSOR, 2))
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(200))
                .outputItems(ae(AEItems.CELL_COMPONENT_4K, 4))
                .duration(320)
                .circuitMeta(22)
                .EUt(120)
                .save(provider);

        GTRecipeBuilder.of(id("cell_component_16k"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(GTItems.ULTRA_LOW_POWER_INTEGRATED_CIRCUIT.asStack(1))
                .inputItems(CustomTags.HV_CIRCUITS, 4)
                .inputItems(ae(AEItems.LOGIC_PROCESSOR, 4))
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(400))
                .outputItems(ae(AEItems.CELL_COMPONENT_16K, 4))
                .duration(320)
                .circuitMeta(22)
                .EUt(480)
                .save(provider);

        GTRecipeBuilder.of(id("cell_component_64k"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(GTItems.LOW_POWER_INTEGRATED_CIRCUIT.asStack(1))
                .inputItems(CustomTags.EV_CIRCUITS, 4)
                .inputItems(ae(AEItems.LOGIC_PROCESSOR, 8))
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(800))
                .outputItems(ae(AEItems.CELL_COMPONENT_64K, 4))
                .duration(320)
                .circuitMeta(22)
                .EUt(1920)
                .save(provider);

        // 流体存储组件：AE2 15 中流体元件使用同一套 cell component
        GTRecipeBuilder.of(id("fluid_cell_component_1k"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(GTItems.CENTRAL_PROCESSING_UNIT.asStack(1))
                .inputItems(CustomTags.LV_CIRCUITS, 4)
                .inputItems(ae(AEItems.CALCULATION_PROCESSOR, 1))
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(100))
                .outputItems(ae(AEItems.CELL_COMPONENT_1K, 4))
                .duration(320)
                .circuitMeta(23)
                .EUt(30)
                .save(provider);

        GTRecipeBuilder.of(id("fluid_cell_component_4k"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(GTItems.RANDOM_ACCESS_MEMORY.asStack(1))
                .inputItems(CustomTags.MV_CIRCUITS, 4)
                .inputItems(ae(AEItems.CALCULATION_PROCESSOR, 2))
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(200))
                .outputItems(ae(AEItems.CELL_COMPONENT_4K, 4))
                .duration(320)
                .circuitMeta(23)
                .EUt(120)
                .save(provider);

        GTRecipeBuilder.of(id("fluid_cell_component_16k"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(GTItems.ULTRA_LOW_POWER_INTEGRATED_CIRCUIT.asStack(1))
                .inputItems(CustomTags.HV_CIRCUITS, 4)
                .inputItems(ae(AEItems.CALCULATION_PROCESSOR, 4))
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(400))
                .outputItems(ae(AEItems.CELL_COMPONENT_16K, 4))
                .duration(320)
                .circuitMeta(23)
                .EUt(480)
                .save(provider);

        GTRecipeBuilder.of(id("fluid_cell_component_64k"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(GTItems.LOW_POWER_INTEGRATED_CIRCUIT.asStack(1))
                .inputItems(CustomTags.EV_CIRCUITS, 4)
                .inputItems(ae(AEItems.CALCULATION_PROCESSOR, 8))
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(800))
                .outputItems(ae(AEItems.CELL_COMPONENT_64K, 4))
                .duration(320)
                .circuitMeta(23)
                .EUt(1920)
                .save(provider);
    }

    /**
     * 256k+ 存储组件。上游 nae2 元件在本整合包中不存在；以 AE2 15 最大号的
     * {@code CELL_COMPONENT_256K} 替代，1M/4M/16M 档按 4x/16x/64x 输出保持
     * 相对容量梯度。// 上游: nae2:material 19-22 (物品) / 24-27 (流体) ->
     * 本移植版: appliedenergistics2:cell_component_256k
     */
    private static void storageComponentsHighTier(Consumer<FinishedRecipe> provider) {
        highTierComponent(provider, "item_cell_component_256k", 22, GTItems.POWER_INTEGRATED_CIRCUIT.asStack(),
                CustomTags.IV_CIRCUITS, ae(AEItems.LOGIC_PROCESSOR, 16), 1600, GTValues.IV, 1);
        highTierComponent(provider, "item_cell_component_1m", 22, GTItems.HIGH_POWER_INTEGRATED_CIRCUIT.asStack(),
                CustomTags.LuV_CIRCUITS, ae(AEItems.LOGIC_PROCESSOR, 32), 3200, GTValues.LuV, 4);
        highTierComponent(provider, "item_cell_component_4m", 22,
                GTItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT.asStack(),
                CustomTags.ZPM_CIRCUITS, ae(AEItems.LOGIC_PROCESSOR, 64), 6400, GTValues.ZPM, 16);
        highTierComponent(provider, "item_cell_component_16m", 22, GTItems.NANO_CENTRAL_PROCESSING_UNIT.asStack(),
                CustomTags.UV_CIRCUITS, ae(AEItems.LOGIC_PROCESSOR, 64), 12800, GTValues.UV, 64);

        highTierComponent(provider, "fluid_cell_component_256k", 23, GTItems.POWER_INTEGRATED_CIRCUIT.asStack(),
                CustomTags.IV_CIRCUITS, ae(AEItems.CALCULATION_PROCESSOR, 16), 1600, GTValues.IV, 1);
        highTierComponent(provider, "fluid_cell_component_1m", 23, GTItems.HIGH_POWER_INTEGRATED_CIRCUIT.asStack(),
                CustomTags.LuV_CIRCUITS, ae(AEItems.CALCULATION_PROCESSOR, 32), 3200, GTValues.LuV, 4);
        highTierComponent(provider, "fluid_cell_component_4m", 23,
                GTItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT.asStack(),
                CustomTags.ZPM_CIRCUITS, ae(AEItems.CALCULATION_PROCESSOR, 64), 6400, GTValues.ZPM, 16);
        highTierComponent(provider, "fluid_cell_component_16m", 23, GTItems.NANO_CENTRAL_PROCESSING_UNIT.asStack(),
                CustomTags.UV_CIRCUITS, ae(AEItems.CALCULATION_PROCESSOR, 64), 12800, GTValues.UV, 64);
    }

    private static void highTierComponent(Consumer<FinishedRecipe> provider, String name, int circuit, ItemStack chip,
                                          net.minecraft.tags.TagKey<net.minecraft.world.item.Item> circuitTag,
                                          ItemStack processor, int mana, int tier, int outputCount) {
        var aura = PollutionMaterials.InfusedAura;
        if (aura == null || !aura.hasFluid()) {
            Pollution.LOGGER.warn("Skipping ae2/{}: InfusedAura has no fluid", name);
            return;
        }
        GTRecipeBuilder.of(id(name), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(chip)
                .inputItems(circuitTag, 4)
                .inputItems(processor)
                .inputFluids(aura.getFluid(mana))
                .outputItems(ae(AEItems.CELL_COMPONENT_256K, outputCount))
                .duration(320)
                .circuitMeta(circuit)
                .EUt(GTValues.VA[tier])
                .save(provider);
    }

    /** 分子装配室 / P2P / 玻璃线缆 / ME 驱动器 / ME 控制器 */
    private static void networkBlocks(Consumer<FinishedRecipe> provider) {
        GTRecipeBuilder.of(id("molecular_assembler"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(ae(AEBlocks.QUARTZ_GLASS, 4))
                .inputItems(ChemicalHelper.get(frameGt, GTMaterials.TungstenSteel, 1))
                .inputItems(ae(AEItems.FORMATION_CORE, 2))
                .inputItems(ae(AEItems.ANNIHILATION_CORE, 2))
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(100))
                .outputItems(ae(AEBlocks.MOLECULAR_ASSEMBLER, 4))
                .duration(400)
                .circuitMeta(21)
                .EUt(7680)
                .save(provider);

        GTRecipeBuilder.of(id("p2p_tunnel"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(cable(2))
                .inputItems(ChemicalHelper.get(frameGt, GTMaterials.NaquadahAlloy, 1))
                .inputItems(ae(AEItems.ENGINEERING_PROCESSOR, 1))
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(100))
                .outputItems(ae(AEParts.ME_P2P_TUNNEL, 16))
                .duration(100)
                .circuitMeta(21)
                .EUt(1920)
                .save(provider);

        GTRecipeBuilder.of(id("glass_cable"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(ae(AEParts.QUARTZ_FIBER, 16))
                .inputItems(ae(AEItems.FLUIX_CRYSTAL, 1))
                .inputFluids(GTMaterials.HSSG.getFluid(144))
                .outputItems(cable(64))
                .duration(100)
                .EUt(480)
                .save(provider);

        GTRecipeBuilder.of(id("drive"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(ChemicalHelper.get(plate, GTMaterials.Titanium, 4))
                .inputItems(ChemicalHelper.get(frameGt, GTMaterials.TungstenSteel, 1))
                .inputItems(cable(2))
                .inputItems(ae(AEItems.LOGIC_PROCESSOR, 2))
                .inputItems(ae(AEItems.ENGINEERING_PROCESSOR, 2))
                .inputFluids(GTMaterials.HSSG.getFluid(144))
                .outputItems(ae(AEBlocks.DRIVE, 8))
                .duration(400)
                .circuitMeta(20)
                .EUt(1920)
                .save(provider);

        GTRecipeBuilder.of(id("controller"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(ChemicalHelper.get(plate, GTMaterials.TungstenSteel, 4))
                .inputItems(ChemicalHelper.get(frameGt, GTMaterials.NaquadahAlloy, 1))
                .inputItems(ae(AEParts.QUARTZ_FIBER, 16))
                .inputItems(ae(AEItems.CALCULATION_PROCESSOR, 4))
                .inputFluids(GTMaterials.HSSG.getFluid(144))
                .outputItems(ae(AEBlocks.CONTROLLER, 8))
                .duration(400)
                .circuitMeta(20)
                .EUt(1920)
                .save(provider);
    }

    private static ItemStack ae(ItemDefinition<?> definition, int count) {
        return definition.stack(count);
    }

    private static ItemStack cable(int count) {
        return AEParts.GLASS_CABLE.stack(AEColor.TRANSPARENT, count);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("pollution", "ae2/" + path);
    }
}
