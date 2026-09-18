package meowmel.pollution.compat.jei;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import meowmel.pollution.Pollution;
import meowmel.pollution.common.block.PollutionMiscBlocks;
import meowmel.pollution.common.machine.PollutionMachines;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * JEI plugin: machine pollution information category.
 *
 * <p>JEI 15.56 supports plain custom categories, so the port documents the
 * pollution-facing machines (vis generator, flux scrubber, flux fuel cell and
 * the mineral extractor) in a single {@link PollutionInfoCategory} instead of
 * the recipe-heavy pages GTCEu provides for its own machines.</p>
 */
@JeiPlugin
public final class PollutionJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new PollutionInfoCategory(
                registration.getJeiHelpers().getGuiHelper(), fallbackIcon()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(PollutionInfoCategory.RECIPE_TYPE, machineInfoRecipes());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        List<ItemStack> catalysts = new ArrayList<>();
        addStack(catalysts, machineStack(PollutionMachines.VIS_GENERATOR, 1));
        addStack(catalysts, machineStack(PollutionMachines.FLUX_SCRUBBER, 4));
        addStack(catalysts, machineStack(PollutionMachines.FLUX_FUEL_CELL, 4));
        catalysts.add(new ItemStack(PollutionMiscBlocks.MINERAL_EXTRACTOR.get()));
        registration.addRecipeCatalysts(PollutionInfoCategory.RECIPE_TYPE, catalysts.toArray(new ItemStack[0]));
    }

    // ////////////////////////////////////
    // ***** info pages *****//
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
