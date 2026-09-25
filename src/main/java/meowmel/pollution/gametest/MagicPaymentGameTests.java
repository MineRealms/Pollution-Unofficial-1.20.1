package meowmel.pollution.gametest;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.recipes.properties.MagicRecipeProperties;
import meowmel.pollution.common.machine.multiblock.MagicMultiblockController;
import meowmel.pollution.common.machine.multiblock.MagicRecipeLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Pollution.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MagicPaymentGameTests {
    private MagicPaymentGameTests() {}

    @GameTest(template = "platform", batch = "magic_payment")
    public static void voltageAndCoilsChangeRealRecipeCosts(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        var definition = GTRegistries.MACHINES.get(ResourceLocation.tryParse("pollution:magic_macerator"));
        helper.getLevel().setBlockAndUpdate(pos, definition.getBlock().defaultBlockState());
        var controller = new PaymentFixture((IMachineBlockEntity) helper.getLevel().getBlockEntity(pos));
        GTRecipe recipe = GTRecipeTypes.MACERATOR_RECIPES.recipeBuilder("pollution:overclock_regression")
                .EUt(32).duration(200).buildRawRecipe();
        GTRecipe overclocked = MagicMultiblockController.recipeModifier(controller, recipe).apply(recipe);
        helper.assertTrue(overclocked != null && overclocked.duration == 50
                        && overclocked.getInputEUt().getTotalEU() == 512,
                "HV did not perform two standard overclocks on an LV recipe");
        recipe.data.putInt("ebf_temp", 1800);
        GTRecipe hot = MagicMultiblockController.recipeModifier(controller, recipe).apply(recipe);
        helper.assertTrue(hot != null && hot.duration < overclocked.duration
                        && hot.getInputEUt().getTotalEU() < overclocked.getInputEUt().getTotalEU(),
                "surplus coil heat did not improve speed and energy efficiency");
        recipe.data.putInt("ebf_temp", 99999);
        helper.assertTrue(MagicMultiblockController.recipeModifier(controller, recipe).apply(recipe) == null,
                "recipe exceeded the available coil temperature");
        helper.succeed();
    }

    @GameTest(template = "platform", batch = "magic_payment")
    public static void failedMagicPaymentIsAtomicAndVisSurvivesReload(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        var definition = GTRegistries.MACHINES.get(ResourceLocation.tryParse("pollution:magic_macerator"));
        helper.getLevel().setBlockAndUpdate(pos, definition.getBlock().defaultBlockState());
        var controller = new PaymentFixture((IMachineBlockEntity) helper.getLevel().getBlockEntity(pos));
        var logic = (MagicRecipeLogic) controller.getRecipeLogic();
        GTRecipe recipe = GTRecipeTypes.MACERATOR_RECIPES.recipeBuilder("pollution:payment_regression")
                .duration(20).buildRawRecipe();
        recipe.tickInputs.clear();
        recipe.tickOutputs.clear();
        recipe.data.putInt(MagicRecipeProperties.INFUSED_FLUID_PER_TICK, 5);
        recipe.data.putLong(MagicRecipeProperties.MANA_PER_TICK, 10);
        recipe.data.putInt(MagicRecipeProperties.VIS_PER_CRAFT, 7);
        helper.assertTrue(!logic.handleTickRecipe(recipe).isSuccess(), "missing mana was accepted");
        helper.assertTrue(controller.fluid == 30 && controller.vis == 10,
                "a failed tick consumed other magic resources");
        controller.mana = 100;
        var funded = logic.handleTickRecipe(recipe);
        helper.assertTrue(funded.isSuccess(), "funded tick was rejected: " + funded.reason().getString());
        helper.assertTrue(controller.fluid == 25 && controller.mana == 90 && controller.vis == 3,
                "first tick did not pay the expected resources");
        CompoundTag saved = new CompoundTag();
        logic.saveCustomPersistedData(saved, false);
        logic.resetMagicState();
        logic.loadCustomPersistedData(saved);
        helper.assertTrue(logic.handleTickRecipe(recipe).isSuccess(), "reload charged the craft's vis a second time");
        helper.assertTrue(controller.fluid == 20 && controller.mana == 80 && controller.vis == 3,
                "restored tick did not preserve one-time vis payment");
        helper.succeed();
    }

    /** Independent stores make partial deductions observable without relying on a recipe search. */
    private static final class PaymentFixture extends MagicMultiblockController {
        int fluid = 30;
        long mana;
        int vis = 10;

        PaymentFixture(IMachineBlockEntity holder) { super(holder); }
        @Override public long getOverclockVoltage() { return 512; }
        @Override public boolean hasCoil() { return true; }
        @Override public int getCurrentTemperature() { return 5400; }
        @Override public Component getMagicRequirementFailure(GTRecipe recipe) { return null; }
        @Override public boolean drainInfusedFluid(int amount, boolean simulate) {
            if (fluid < amount) return false;
            if (!simulate) fluid -= amount;
            return true;
        }
        @Override public boolean consumeMana(long amount, boolean simulate) {
            if (mana < amount) return false;
            if (!simulate) mana -= amount;
            return true;
        }
        @Override public boolean consumeVis(int amount, boolean simulate) {
            if (vis < amount) return false;
            if (!simulate) vis -= amount;
            return true;
        }
    }
}
