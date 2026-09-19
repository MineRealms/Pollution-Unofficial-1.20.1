package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
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
 * <p><b>Item lookups</b>: every AE2 and GT item is resolved through
 * {@link SafeItems} instead of the {@code AEItems} / {@code AEBlocks} /
 * {@code AEParts} / {@code GTItems} static fields, which can still be null
 * while their owning class initialises. The 1.12.2 metadata indices from
 * {@code ae2Index} map to named definitions: material 16/17/18 = processor
 * prints, 22/23/24 = logic/engineering/calculation processor, 35-38 = item
 * storage components, 43/44 = formation/annihilation core; part 16/140 =
 * glass cable/quartz fiber, part 240/260 = import/export bus, part 460 = ME P2P
 * tunnel. The transparent glass cable is {@code ae2:fluix_glass_cable}.</p>
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
 *       -&gt; the GTCEu registry items {@code cpu_chip}, {@code ram_chip},
 *       {@code ulpic_chip}, {@code lpic_chip}</li>
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
        ItemStack quartzGlass = ae("quartz_glass", 1);
        ItemStack fluixDust = ae("fluix_dust", 2);
        ItemStack vibrantGlass = ae("quartz_vibrant_glass", 1);
        if (missing(quartzGlass, fluixDust, vibrantGlass)) {
            Pollution.LOGGER.warn("Skipping ae2/vibrant_quartz_glass: a required AE2 item is missing");
            return;
        }
        GTRecipeBuilder.of(id("vibrant_quartz_glass"), GTRecipeTypes.BLAST_RECIPES)
                .inputItems(quartzGlass)
                .inputItems(fluixDust)
                .outputItems(vibrantGlass)
                .duration(100)
                .blastFurnaceTemp(1800)
                .EUt(120)
                .save(provider);
    }

    /** 破坏核心 / 成型核心 */
    private static void cores(Consumer<FinishedRecipe> provider) {
        ItemStack fluixDust = ae("fluix_dust", 1);
        ItemStack annihilationCore = ae("annihilation_core", 16);
        ItemStack formationCore = ae("formation_core", 16);
        if (missing(fluixDust, annihilationCore, formationCore)) {
            Pollution.LOGGER.warn("Skipping the ae2 core group: a required AE2 item is missing");
            return;
        }
        GTRecipeBuilder.of(id("annihilation_core"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(ChemicalHelper.get(plate, GTMaterials.CertusQuartz, 6))
                .inputItems(fluixDust)
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(100))
                .outputItems(annihilationCore)
                .duration(160)
                .circuitMeta(1)
                .EUt(480)
                .save(provider);

        GTRecipeBuilder.of(id("formation_core"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(ChemicalHelper.get(plate, GTMaterials.NetherQuartz, 6))
                .inputItems(fluixDust)
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(100))
                .outputItems(formationCore)
                .duration(160)
                .circuitMeta(1)
                .EUt(480)
                .save(provider);
    }

    /** 三种电路板：逻辑 / 工程 / 运算处理器 */
    private static void processors(Consumer<FinishedRecipe> provider) {
        ItemStack logicPrint = ae("printed_logic_processor", 4);
        ItemStack engineeringPrint = ae("printed_engineering_processor", 4);
        ItemStack calculationPrint = ae("printed_calculation_processor", 4);
        ItemStack logicProcessor = ae("logic_processor", 16);
        ItemStack engineeringProcessor = ae("engineering_processor", 16);
        ItemStack calculationProcessor = ae("calculation_processor", 16);
        if (missing(logicPrint, engineeringPrint, calculationPrint, logicProcessor, engineeringProcessor,
                calculationProcessor)) {
            Pollution.LOGGER.warn("Skipping the ae2 processor group: a required AE2 item is missing");
            return;
        }
        GTRecipeBuilder.of(id("logic_processor"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(logicPrint)
                .inputItems(ChemicalHelper.get(plate, GTMaterials.RedAlloy, 1))
                .inputFluids(GTMaterials.HSSG.getFluid(144))
                .outputItems(logicProcessor)
                .duration(160)
                .EUt(480)
                .save(provider);

        GTRecipeBuilder.of(id("engineering_processor"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(engineeringPrint)
                .inputItems(ChemicalHelper.get(plate, GTMaterials.RedAlloy, 1))
                .inputFluids(GTMaterials.HSSG.getFluid(144))
                .outputItems(engineeringProcessor)
                .duration(160)
                .EUt(480)
                .save(provider);

        GTRecipeBuilder.of(id("calculation_processor"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(calculationPrint)
                .inputItems(ChemicalHelper.get(plate, GTMaterials.RedAlloy, 1))
                .inputFluids(GTMaterials.HSSG.getFluid(144))
                .outputItems(calculationProcessor)
                .duration(160)
                .EUt(480)
                .save(provider);
    }

    /** ME 接口 / 流体接口 / 输入输出总线 */
    private static void interfacesAndBuses(Consumer<FinishedRecipe> provider) {
        ItemStack formationCore = ae("formation_core", 1);
        ItemStack annihilationCore = ae("annihilation_core", 1);
        ItemStack meInterface = ae("interface", 16);
        ItemStack importBus = ae("import_bus", 8);
        ItemStack exportBus = ae("export_bus", 8);
        if (missing(formationCore, annihilationCore, meInterface, importBus, exportBus)) {
            Pollution.LOGGER.warn("Skipping the ae2 interface/bus group: a required AE2 item is missing");
            return;
        }
        GTRecipeBuilder.of(id("me_interface"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(ChemicalHelper.get(plate, GTMaterials.Titanium, 6))
                .inputItems(ChemicalHelper.get(frameGt, GTMaterials.TungstenSteel, 1))
                .inputItems(formationCore)
                .inputItems(annihilationCore)
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(100))
                .outputItems(meInterface)
                .duration(160)
                .circuitMeta(20)
                .EUt(1920)
                .save(provider);

        // 流体接口：AE2 15 已把流体支持并入普通 ME 接口
        GTRecipeBuilder.of(id("me_fluid_interface"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(ChemicalHelper.get(plate, GTMaterials.Titanium, 6))
                .inputItems(ChemicalHelper.get(frameGt, GTMaterials.NaquadahAlloy, 1))
                .inputItems(formationCore)
                .inputItems(annihilationCore)
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(100))
                .outputItems(meInterface)
                .duration(160)
                .circuitMeta(20)
                .EUt(1920)
                .save(provider);

        // 输入总线（流体总线在 AE2 15 中并入同一物品）
        GTRecipeBuilder.of(id("import_bus"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(ChemicalHelper.get(plate, GTMaterials.Titanium, 4))
                .inputItems(ChemicalHelper.get(frameGt, GTMaterials.TungstenSteel, 1))
                .inputItems(meInterface.copyWithCount(1))
                .inputItems(annihilationCore)
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(100))
                .outputItems(importBus)
                .duration(160)
                .circuitMeta(18)
                .EUt(480)
                .save(provider);

        // 输出总线（流体总线在 AE2 15 中并入同一物品）
        GTRecipeBuilder.of(id("export_bus"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(ChemicalHelper.get(plate, GTMaterials.Titanium, 4))
                .inputItems(ChemicalHelper.get(frameGt, GTMaterials.NaquadahAlloy, 1))
                .inputItems(meInterface.copyWithCount(1))
                .inputItems(formationCore)
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(100))
                .outputItems(exportBus)
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
        ItemStack cpuChip = SafeItems.gt("cpu_chip", 1);
        ItemStack ramChip = SafeItems.gt("ram_chip", 1);
        ItemStack ulpicChip = SafeItems.gt("ulpic_chip", 1);
        ItemStack lpicChip = SafeItems.gt("lpic_chip", 1);
        ItemStack logicProcessor1 = ae("logic_processor", 1);
        ItemStack logicProcessor2 = ae("logic_processor", 2);
        ItemStack logicProcessor4 = ae("logic_processor", 4);
        ItemStack logicProcessor8 = ae("logic_processor", 8);
        ItemStack calculationProcessor1 = ae("calculation_processor", 1);
        ItemStack calculationProcessor2 = ae("calculation_processor", 2);
        ItemStack calculationProcessor4 = ae("calculation_processor", 4);
        ItemStack calculationProcessor8 = ae("calculation_processor", 8);
        ItemStack component1k = ae("cell_component_1k", 4);
        ItemStack component4k = ae("cell_component_4k", 4);
        ItemStack component16k = ae("cell_component_16k", 4);
        ItemStack component64k = ae("cell_component_64k", 4);
        if (missing(cpuChip, ramChip, ulpicChip, lpicChip, logicProcessor1, logicProcessor2, logicProcessor4,
                logicProcessor8, calculationProcessor1, calculationProcessor2, calculationProcessor4,
                calculationProcessor8, component1k, component4k, component16k, component64k)) {
            Pollution.LOGGER.warn("Skipping the ae2 storage component group: a required AE2/GT item is missing");
            return;
        }
        GTRecipeBuilder.of(id("cell_component_1k"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(cpuChip)
                .inputItems(CustomTags.LV_CIRCUITS, 4)
                .inputItems(logicProcessor1)
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(100))
                .outputItems(component1k)
                .duration(320)
                .circuitMeta(22)
                .EUt(30)
                .save(provider);

        GTRecipeBuilder.of(id("cell_component_4k"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(ramChip)
                .inputItems(CustomTags.MV_CIRCUITS, 4)
                .inputItems(logicProcessor2)
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(200))
                .outputItems(component4k)
                .duration(320)
                .circuitMeta(22)
                .EUt(120)
                .save(provider);

        GTRecipeBuilder.of(id("cell_component_16k"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(ulpicChip)
                .inputItems(CustomTags.HV_CIRCUITS, 4)
                .inputItems(logicProcessor4)
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(400))
                .outputItems(component16k)
                .duration(320)
                .circuitMeta(22)
                .EUt(480)
                .save(provider);

        GTRecipeBuilder.of(id("cell_component_64k"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(lpicChip)
                .inputItems(CustomTags.EV_CIRCUITS, 4)
                .inputItems(logicProcessor8)
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(800))
                .outputItems(component64k)
                .duration(320)
                .circuitMeta(22)
                .EUt(1920)
                .save(provider);

        // 流体存储组件：AE2 15 中流体元件使用同一套 cell component
        GTRecipeBuilder.of(id("fluid_cell_component_1k"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(cpuChip)
                .inputItems(CustomTags.LV_CIRCUITS, 4)
                .inputItems(calculationProcessor1)
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(100))
                .outputItems(component1k)
                .duration(320)
                .circuitMeta(23)
                .EUt(30)
                .save(provider);

        GTRecipeBuilder.of(id("fluid_cell_component_4k"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(ramChip)
                .inputItems(CustomTags.MV_CIRCUITS, 4)
                .inputItems(calculationProcessor2)
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(200))
                .outputItems(component4k)
                .duration(320)
                .circuitMeta(23)
                .EUt(120)
                .save(provider);

        GTRecipeBuilder.of(id("fluid_cell_component_16k"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(ulpicChip)
                .inputItems(CustomTags.HV_CIRCUITS, 4)
                .inputItems(calculationProcessor4)
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(400))
                .outputItems(component16k)
                .duration(320)
                .circuitMeta(23)
                .EUt(480)
                .save(provider);

        GTRecipeBuilder.of(id("fluid_cell_component_64k"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(lpicChip)
                .inputItems(CustomTags.EV_CIRCUITS, 4)
                .inputItems(calculationProcessor8)
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(800))
                .outputItems(component64k)
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
        highTierComponent(provider, "item_cell_component_256k", 22, SafeItems.gt("mpic_chip", 1),
                CustomTags.IV_CIRCUITS, ae("logic_processor", 16), 1600, GTValues.IV, 1);
        highTierComponent(provider, "item_cell_component_1m", 22, SafeItems.gt("hpic_chip", 1),
                CustomTags.LuV_CIRCUITS, ae("logic_processor", 32), 3200, GTValues.LuV, 4);
        highTierComponent(provider, "item_cell_component_4m", 22, SafeItems.gt("uhpic_chip", 1),
                CustomTags.ZPM_CIRCUITS, ae("logic_processor", 64), 6400, GTValues.ZPM, 16);
        highTierComponent(provider, "item_cell_component_16m", 22, SafeItems.gt("nano_cpu_chip", 1),
                CustomTags.UV_CIRCUITS, ae("logic_processor", 64), 12800, GTValues.UV, 64);

        highTierComponent(provider, "fluid_cell_component_256k", 23, SafeItems.gt("mpic_chip", 1),
                CustomTags.IV_CIRCUITS, ae("calculation_processor", 16), 1600, GTValues.IV, 1);
        highTierComponent(provider, "fluid_cell_component_1m", 23, SafeItems.gt("hpic_chip", 1),
                CustomTags.LuV_CIRCUITS, ae("calculation_processor", 32), 3200, GTValues.LuV, 4);
        highTierComponent(provider, "fluid_cell_component_4m", 23, SafeItems.gt("uhpic_chip", 1),
                CustomTags.ZPM_CIRCUITS, ae("calculation_processor", 64), 6400, GTValues.ZPM, 16);
        highTierComponent(provider, "fluid_cell_component_16m", 23, SafeItems.gt("nano_cpu_chip", 1),
                CustomTags.UV_CIRCUITS, ae("calculation_processor", 64), 12800, GTValues.UV, 64);
    }

    private static void highTierComponent(Consumer<FinishedRecipe> provider, String name, int circuit, ItemStack chip,
                                          net.minecraft.tags.TagKey<net.minecraft.world.item.Item> circuitTag,
                                          ItemStack processor, int mana, int tier, int outputCount) {
        var aura = PollutionMaterials.InfusedAura;
        if (aura == null || !aura.hasFluid()) {
            Pollution.LOGGER.warn("Skipping ae2/{}: InfusedAura has no fluid", name);
            return;
        }
        ItemStack output = ae("cell_component_256k", outputCount);
        if (missing(chip, processor, output)) {
            Pollution.LOGGER.warn("Skipping ae2/{}: a required AE2/GT item is missing", name);
            return;
        }
        GTRecipeBuilder.of(id(name), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(chip)
                .inputItems(circuitTag, 4)
                .inputItems(processor)
                .inputFluids(aura.getFluid(mana))
                .outputItems(output)
                .duration(320)
                .circuitMeta(circuit)
                .EUt(GTValues.VA[tier])
                .save(provider);
    }

    /** 分子装配室 / P2P / 玻璃线缆 / ME 驱动器 / ME 控制器 */
    private static void networkBlocks(Consumer<FinishedRecipe> provider) {
        ItemStack quartzGlass4 = ae("quartz_glass", 4);
        ItemStack formationCore2 = ae("formation_core", 2);
        ItemStack annihilationCore2 = ae("annihilation_core", 2);
        ItemStack molecularAssembler = ae("molecular_assembler", 4);
        if (missing(quartzGlass4, formationCore2, annihilationCore2, molecularAssembler)) {
            Pollution.LOGGER.warn("Skipping ae2/molecular_assembler: a required AE2 item is missing");
        } else {
            GTRecipeBuilder.of(id("molecular_assembler"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                    .inputItems(quartzGlass4)
                    .inputItems(ChemicalHelper.get(frameGt, GTMaterials.TungstenSteel, 1))
                    .inputItems(formationCore2)
                    .inputItems(annihilationCore2)
                    .inputFluids(PollutionMaterials.InfusedAura.getFluid(100))
                    .outputItems(molecularAssembler)
                    .duration(400)
                    .circuitMeta(21)
                    .EUt(7680)
                    .save(provider);
        }

        ItemStack p2pCable = cable(2);
        ItemStack engineeringProcessor = ae("engineering_processor", 1);
        ItemStack p2pTunnel = ae("me_p2p_tunnel", 16);
        if (missing(p2pCable, engineeringProcessor, p2pTunnel)) {
            Pollution.LOGGER.warn("Skipping ae2/p2p_tunnel: a required AE2 item is missing");
        } else {
            GTRecipeBuilder.of(id("p2p_tunnel"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                    .inputItems(p2pCable)
                    .inputItems(ChemicalHelper.get(frameGt, GTMaterials.NaquadahAlloy, 1))
                    .inputItems(engineeringProcessor)
                    .inputFluids(PollutionMaterials.InfusedAura.getFluid(100))
                    .outputItems(p2pTunnel)
                    .duration(100)
                    .circuitMeta(21)
                    .EUt(1920)
                    .save(provider);
        }

        ItemStack quartzFiber16 = ae("quartz_fiber", 16);
        ItemStack fluixCrystal = ae("fluix_crystal", 1);
        ItemStack glassCable64 = cable(64);
        if (missing(quartzFiber16, fluixCrystal, glassCable64)) {
            Pollution.LOGGER.warn("Skipping ae2/glass_cable: a required AE2 item is missing");
        } else {
            GTRecipeBuilder.of(id("glass_cable"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                    .inputItems(quartzFiber16)
                    .inputItems(fluixCrystal)
                    .inputFluids(GTMaterials.HSSG.getFluid(144))
                    .outputItems(glassCable64)
                    .duration(100)
                    .EUt(480)
                    .save(provider);
        }

        ItemStack driveCable = cable(2);
        ItemStack driveLogic = ae("logic_processor", 2);
        ItemStack driveEngineering = ae("engineering_processor", 2);
        ItemStack drive = ae("drive", 8);
        if (missing(driveCable, driveLogic, driveEngineering, drive)) {
            Pollution.LOGGER.warn("Skipping ae2/drive: a required AE2 item is missing");
        } else {
            GTRecipeBuilder.of(id("drive"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                    .inputItems(ChemicalHelper.get(plate, GTMaterials.Titanium, 4))
                    .inputItems(ChemicalHelper.get(frameGt, GTMaterials.TungstenSteel, 1))
                    .inputItems(driveCable)
                    .inputItems(driveLogic)
                    .inputItems(driveEngineering)
                    .inputFluids(GTMaterials.HSSG.getFluid(144))
                    .outputItems(drive)
                    .duration(400)
                    .circuitMeta(20)
                    .EUt(1920)
                    .save(provider);
        }

        ItemStack controllerFiber = ae("quartz_fiber", 16);
        ItemStack controllerCalculation = ae("calculation_processor", 4);
        ItemStack controller = ae("controller", 8);
        if (missing(controllerFiber, controllerCalculation, controller)) {
            Pollution.LOGGER.warn("Skipping ae2/controller: a required AE2 item is missing");
        } else {
            GTRecipeBuilder.of(id("controller"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                    .inputItems(ChemicalHelper.get(plate, GTMaterials.TungstenSteel, 4))
                    .inputItems(ChemicalHelper.get(frameGt, GTMaterials.NaquadahAlloy, 1))
                    .inputItems(controllerFiber)
                    .inputItems(controllerCalculation)
                    .inputFluids(GTMaterials.HSSG.getFluid(144))
                    .outputItems(controller)
                    .duration(400)
                    .circuitMeta(20)
                    .EUt(1920)
                    .save(provider);
        }
    }

    /** Registry lookup for an AE2 item; empty when the item is not registered. */
    private static ItemStack ae(String path, int count) {
        return SafeItems.byId("ae2", path, count);
    }

    /** The transparent ME glass cable ({@code ae2:fluix_glass_cable}). */
    private static ItemStack cable(int count) {
        return SafeItems.byId("ae2", "fluix_glass_cable", count);
    }

    /** @return true when any of the given stacks is empty, so the recipe must be skipped */
    private static boolean missing(ItemStack... stacks) {
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("pollution", "ae2/" + path);
    }
}
