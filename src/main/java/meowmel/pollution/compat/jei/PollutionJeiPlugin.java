package meowmel.pollution.compat.jei;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.amplification.MagicJeiHintResolver;
import meowmel.pollution.api.amplification.MagicMachineProfileRegistry;
import meowmel.pollution.api.amplification.MagicProcessTag;
import meowmel.pollution.common.block.PollutionMiscBlocks;
import meowmel.pollution.common.machine.PollutionMachines;
import meowmel.pollution.common.machine.part.InfusedFluidHatchMachine;
import meowmel.pollution.common.machine.part.mana.ManaPoolHatchMachine;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * JEI plugin of the Pollution port.
 *
 * <p>JEI 15.56 supports plain custom categories, so the port documents the
 * pollution-facing machines (vis generator, flux scrubber, flux fuel cell and
 * the mineral extractor), the magic hatches and the magic amplification rules
 * in info categories. The custom GT recipe maps (magic blast smelter,
 * greenhouse, industrial infusion, Botania maps, ...) already receive
 * categories from GregTech's own JEI plugin; their magic recipe properties are
 * added to those pages by {@link MagicRecipeDataInfos}, the modern replacement
 * for upstream's {@code MagicPropertyRecipeUI}.</p>
 *
 * <p>Scope of the upstream port:</p>
 * <ul>
 *   <li>{@code MagicGuideUI} / {@code MagicPropertyRecipeUI} - ported as
 *       {@link MagicAmplificationInfoCategory} plus
 *       {@link MagicRecipeDataInfos}.</li>
 *   <li>vis / mana hatch information - upstream only shipped item tooltips, the
 *       port adds {@link MagicHatchInfoCategory} on top of them.</li>
 *   <li>starstream and astral recipe categories - <b>not ported</b>: the
 *       starstream network (tower / relay / obelisk tiles, wireless terminals)
 *       and the astral celestial machines with their recipe maps
 *       ({@code celestial_observation}, {@code industrial_starlight_infuser},
 *       ...) are deferred in {@code PORecipeMaps}, so there is no recipe map to
 *       attach a category to. The ported astral data items and the
 *       constellation effects are surfaced through
 *       {@link MagicAmplificationInfoCategory} instead.</li>
 * </ul>
 *
 * <p>The whole plugin is optional-safe: it lives in {@code compat.jei}, is only
 * discovered through the {@code @JeiPlugin} annotation when JEI is loaded and
 * is never referenced from the mod entry point or from common code.</p>
 */
@JeiPlugin
public final class PollutionJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, "jei_plugin");
    }

    /**
     * Debug-only handle consumed by {@code JeiDebugDumper}. It is only ever
     * populated while JEI is loaded, so it stays null in runs without JEI and
     * the dumper never touches it in that case.
     */
    private static volatile IJeiRuntime runtime;

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
    }

    public static IJeiRuntime getRuntime() {
        return runtime;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new PollutionInfoCategory(guiHelper, fallbackIcon()));
        registration.addRecipeCategories(new MagicAmplificationInfoCategory(guiHelper, fallbackIcon()));
        registration.addRecipeCategories(new MagicHatchInfoCategory(guiHelper, fallbackIcon()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(PollutionInfoCategory.RECIPE_TYPE, machineInfoRecipes());
        registration.addRecipes(MagicAmplificationInfoCategory.RECIPE_TYPE, magicAmplificationRecipes());
        registration.addRecipes(MagicHatchInfoCategory.RECIPE_TYPE, magicHatchRecipes());
        MagicRecipeDataInfos.install();
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        List<ItemStack> machineCatalysts = new ArrayList<>();
        addStack(machineCatalysts, machineStack(PollutionMachines.VIS_GENERATOR, 1));
        addStack(machineCatalysts, machineStack(PollutionMachines.FLUX_SCRUBBER, 4));
        addStack(machineCatalysts, machineStack(PollutionMachines.FLUX_FUEL_CELL, 4));
        machineCatalysts.add(new ItemStack(PollutionMiscBlocks.MINERAL_EXTRACTOR.get()));
        if (!machineCatalysts.isEmpty()) {
            registration.addRecipeCatalysts(PollutionInfoCategory.RECIPE_TYPE,
                    machineCatalysts.toArray(new ItemStack[0]));
        }

        List<ItemStack> magicCatalysts = new ArrayList<>();
        addStack(magicCatalysts, singleStack(PollutionMachines.MAGIC_MACERATOR));
        addStack(magicCatalysts, singleStack(PollutionMachines.INDUSTRIAL_INFUSION));
        addStack(magicCatalysts, singleStack(PollutionMachines.MAGIC_FUSION_REACTOR));
        addStack(magicCatalysts, singleStack(PollutionMachines.MAGIC_LARGE_TURBINE));
        if (!magicCatalysts.isEmpty()) {
            registration.addRecipeCatalysts(MagicAmplificationInfoCategory.RECIPE_TYPE,
                    magicCatalysts.toArray(new ItemStack[0]));
        }

        List<ItemStack> hatchCatalysts = new ArrayList<>();
        addStack(hatchCatalysts, machineStack(PollutionMachines.VIS_HATCH, 1));
        addStack(hatchCatalysts, machineStack(PollutionMachines.INFUSED_FLUID_HATCH, 1));
        addStack(hatchCatalysts, machineStack(PollutionMachines.MANA_INPUT_HATCH_1A, 1));
        addStack(hatchCatalysts, machineStack(PollutionMachines.MANA_OUTPUT_HATCH_1A, 1));
        addStack(hatchCatalysts, machineStack(PollutionMachines.MANA_POOL_INPUT_HATCH,
                ManaPoolHatchMachine.PoolType.NORMAL.ordinal()));
        addStack(hatchCatalysts, machineStack(PollutionMachines.MANA_POOL_OUTPUT_HATCH,
                ManaPoolHatchMachine.PoolType.NORMAL.ordinal()));
        addStack(hatchCatalysts, machineStack(PollutionMachines.FLUX_MUFFLER, 1));
        if (!hatchCatalysts.isEmpty()) {
            registration.addRecipeCatalysts(MagicHatchInfoCategory.RECIPE_TYPE,
                    hatchCatalysts.toArray(new ItemStack[0]));
        }
    }

    // ////////////////////////////////////
    // ***** machine info pages *****//
    // ////////////////////////////////////

    private static List<PollutionInfoRecipe> machineInfoRecipes() {
        List<PollutionInfoRecipe> recipes = new ArrayList<>();

        addInfo(recipes, machineStack(PollutionMachines.VIS_GENERATOR, 1), List.of(
                Component.literal("抽取灵气转化为 EU 与工业污染"),
                Component.literal("输出随灵气缓冲量提升")));

        addInfo(recipes, machineStack(PollutionMachines.FLUX_SCRUBBER, 4), List.of(
                Component.literal("消耗 EU 清理 16 格内的咒波"),
                Component.literal("降低灵气污染")));

        addInfo(recipes, machineStack(PollutionMachines.FLUX_FUEL_CELL, 4), List.of(
                Component.literal("燃烧周围咒波发电"),
                Component.literal("咒波超过上限时会爆炸")));

        addInfo(recipes, new ItemStack(PollutionMiscBlocks.MINERAL_EXTRACTOR.get()), List.of(
                Component.literal("用混沌与魔法源质开采周围矿物"),
                Component.literal("右键打开界面，可切换实体矿/虚拟产物")));

        return recipes;
    }

    // ////////////////////////////////////
    // ***** magic amplification pages *****//
    // ////////////////////////////////////

    /**
     * One page per ported magic machine, using the fallback tag profiles of
     * {@link MagicMachineProfileRegistry} and the effect text of
     * {@link MagicJeiHintResolver}. Machines whose profile exists upstream but
     * is not ported (starstream / astral) are skipped.
     */
    private static List<PollutionInfoRecipe> magicAmplificationRecipes() {
        List<PollutionInfoRecipe> recipes = new ArrayList<>();
        addAmplification(recipes, PollutionMachines.MAGIC_MACERATOR, "magic_macerator");
        addAmplification(recipes, PollutionMachines.MAGIC_BENDER, "magic_bender");
        addAmplification(recipes, PollutionMachines.MAGIC_CUTTER, "magic_cutter");
        addAmplification(recipes, PollutionMachines.MAGIC_EXTRUDER, "magic_extruder");
        addAmplification(recipes, PollutionMachines.MAGIC_WIRE_MILL, "magic_wiremill");
        addAmplification(recipes, PollutionMachines.MAGIC_ELECTROLYZER, "magic_electrolyzer");
        addAmplification(recipes, PollutionMachines.MAGIC_MIXER, "magic_mixer");
        addAmplification(recipes, PollutionMachines.MAGIC_ASSEMBLER, "magic_assembler");
        addAmplification(recipes, PollutionMachines.MAGIC_CENTRIFUGE, "magic_centrifuge");
        addAmplification(recipes, PollutionMachines.MAGIC_CHEMICAL_BATH, "magic_chemical_bath");
        addAmplification(recipes, PollutionMachines.MAGIC_SIFTER, "magic_sifter");
        addAmplification(recipes, PollutionMachines.MAGIC_SOLIDIFIER, "magic_solidifier");
        addAmplification(recipes, PollutionMachines.MAGIC_BREWERY, "magic_brewery");
        addAmplification(recipes, PollutionMachines.MAGIC_CHEMICAL_REACTOR, "magic_chemical_reactor");
        addAmplification(recipes, PollutionMachines.MAGIC_AUTOCLAVE, "magic_autoclave");
        addAmplification(recipes, PollutionMachines.MAGIC_GREEN_HOUSE, "magic_green_house");
        addAmplification(recipes, PollutionMachines.MAGIC_DISTILLERY, "magic_distillery");
        addAmplification(recipes, PollutionMachines.INDUSTRIAL_INFUSION, "industrial_infusion");
        addAmplification(recipes, PollutionMachines.MAGIC_FUSION_REACTOR, "magic_fusion_reactor");
        addAmplification(recipes, PollutionMachines.MAGIC_ELECTRIC_BLAST_FURNACE,
                "magic_electric_blast_furnace");
        addAmplification(recipes, PollutionMachines.MAGIC_ALLOY_BLAST, "magic_alloy_blast");
        // Deferred with their systems, so no page is generated:
        // industrial_starlight_infuser, industrial_lightwell,
        // celestial_observation_array, celestial_calibration_matrix.
        return recipes;
    }

    private static void addAmplification(List<PollutionInfoRecipe> recipes, MachineDefinition machine,
                                         String profilePath) {
        if (machine == null) {
            return;
        }
        ItemStack icon = machine.asStack();
        if (icon.isEmpty()) {
            return;
        }
        long mask = MagicMachineProfileRegistry.getProfiles().getOrDefault(profilePath, 0L);
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("pollution.jei.magic_amplification.tags",
                MagicProcessTag.describeMask(mask)));

        List<String> constellationHints = MagicJeiHintResolver.constellationHints(mask);
        if (!constellationHints.isEmpty()) {
            lines.add(Component.translatable("pollution.jei.magic_amplification.constellation"));
            for (String hint : limit(constellationHints, 3)) {
                lines.add(Component.literal(hint));
            }
        }
        List<String> tarotHints = MagicJeiHintResolver.tarotHints(mask);
        if (!tarotHints.isEmpty()) {
            lines.add(Component.translatable("pollution.jei.magic_amplification.tarot"));
            for (String hint : limit(tarotHints, 4)) {
                lines.add(Component.literal(hint));
            }
        }
        lines.add(Component.translatable("pollution.jei.magic_amplification.footer"));
        recipes.add(new PollutionInfoRecipe(icon, lines));
    }

    private static List<String> limit(List<String> values, int maximum) {
        return values.size() <= maximum ? values : values.subList(0, maximum);
    }

    // ////////////////////////////////////
    // ***** magic hatch pages *****//
    // ////////////////////////////////////

    private static List<PollutionInfoRecipe> magicHatchRecipes() {
        List<PollutionInfoRecipe> recipes = new ArrayList<>();

        addInfo(recipes, machineStack(PollutionMachines.VIS_HATCH, 1), List.of(
                Component.translatable("pollution.machine.vis_hatch.tooltip.capacity", 2000),
                Component.translatable("pollution.machine.vis_hatch.tooltip.drain"),
                Component.translatable("pollution.machine.vis_hatch.tooltip.buffer", 1)));

        addInfo(recipes, machineStack(PollutionMachines.INFUSED_FLUID_HATCH, 1), List.of(
                Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity",
                        InfusedFluidHatchMachine.getTankCapacity(1)),
                Component.translatable("pollution.machine.infused_fluid_hatch.tooltip")));

        addInfo(recipes, machineStack(PollutionMachines.MANA_INPUT_HATCH_1A, 1), List.of(
                Component.translatable("pollution.machine.mana_hatch.tooltip"),
                Component.translatable("gtceu.universal.tooltip.voltage_in",
                        GTValues.V[GTValues.LV], GTValues.VNF[GTValues.LV]),
                Component.translatable("gtceu.universal.tooltip.amperage_in_till", 1)));

        addInfo(recipes, machineStack(PollutionMachines.MANA_OUTPUT_HATCH_1A, 1), List.of(
                Component.translatable("pollution.machine.mana_hatch.tooltip"),
                Component.translatable("gtceu.universal.tooltip.voltage_out",
                        GTValues.V[GTValues.LV], GTValues.VNF[GTValues.LV]),
                Component.translatable("gtceu.universal.tooltip.amperage_out_till", 1)));

        addInfo(recipes, machineStack(PollutionMachines.WIRELESS_MANA_INPUT_HATCH_1A, 1), List.of(
                Component.translatable("pollution.machine.mana_hatch.tooltip"),
                Component.translatable("pollution.machine.wireless_mana_hatch.tooltip")));

        ManaPoolHatchMachine.PoolType poolType = ManaPoolHatchMachine.PoolType.NORMAL;
        List<Component> poolLines = List.of(
                Component.translatable("pollution.machine.mana_pool_hatch.type",
                        Component.translatable("pollution.machine.mana_pool_hatch.type." + poolType.getName())),
                Component.translatable("pollution.machine.mana_pool_hatch.capacity", poolType.getCapacity()),
                Component.translatable("pollution.machine.mana_pool_hatch.transfer", poolType.getTransferRate()));

        addInfo(recipes, machineStack(PollutionMachines.MANA_POOL_INPUT_HATCH, poolType.ordinal()),
                combine(poolLines, Component.translatable("pollution.machine.mana_pool_input_hatch.tooltip")));
        addInfo(recipes, machineStack(PollutionMachines.MANA_POOL_OUTPUT_HATCH, poolType.ordinal()),
                combine(poolLines, Component.translatable("pollution.machine.mana_pool_output_hatch.tooltip")));
        addInfo(recipes, machineStack(PollutionMachines.WIRELESS_MANA_POOL_INPUT_HATCH, poolType.ordinal()),
                combine(poolLines,
                        Component.translatable("pollution.machine.mana_pool_input_hatch.tooltip"),
                        Component.translatable("pollution.machine.wireless_mana_pool_hatch.tooltip")));

        addInfo(recipes, machineStack(PollutionMachines.FLUX_MUFFLER, 1), List.of(
                Component.translatable("pollution.machine.flux_muffler.tooltip"),
                Component.translatable("pollution.machine.flux_muffler.tooltip.recovery", 0)));

        return recipes;
    }

    // ////////////////////////////////////
    // ***** helpers *****//
    // ////////////////////////////////////

    private static List<Component> combine(List<Component> base, Component... extra) {
        List<Component> lines = new ArrayList<>(base);
        for (Component line : extra) {
            lines.add(line);
        }
        return lines;
    }

    private static void addInfo(List<PollutionInfoRecipe> recipes, ItemStack icon, List<Component> lines) {
        if (icon.isEmpty()) {
            return;
        }
        recipes.add(new PollutionInfoRecipe(icon, lines));
    }

    private static ItemStack machineStack(MachineDefinition[] family, int index) {
        if (family == null || index < 0 || index >= family.length || family[index] == null) {
            return ItemStack.EMPTY;
        }
        return family[index].asStack();
    }

    private static ItemStack singleStack(MachineDefinition machine) {
        return machine == null ? ItemStack.EMPTY : machine.asStack();
    }

    private static void addStack(List<ItemStack> stacks, ItemStack stack) {
        if (!stack.isEmpty()) {
            stacks.add(stack);
        }
    }

    private static ItemStack fallbackIcon() {
        ItemStack visGenerator = machineStack(PollutionMachines.VIS_GENERATOR, 1);
        return visGenerator.isEmpty() ? new ItemStack(PollutionMiscBlocks.MINERAL_EXTRACTOR.get()) : visGenerator;
    }
}
