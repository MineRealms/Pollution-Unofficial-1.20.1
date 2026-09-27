package meowmel.pollution.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import hellfirepvp.astralsorcery.common.crafting.RecipeTypesAS;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.astral.AstralCrystalNbtHelper;
import meowmel.pollution.common.machine.multiblock.MagicMultiblockController;
import meowmel.pollution.common.machine.multiblock.astral.AstralRecipeMaps;
import meowmel.pollution.common.machine.multiblock.astral.AstralRecipeOutputs;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.function.BooleanSupplier;

/** Exercises the registered machine modifiers and actual GT IO/tick logic; external sky/magic stores are fixtures. */
@GameTestHolder(Pollution.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AstralMachineGameTests {
    private AstralMachineGameTests() {}

    @GameTest(template = "platform", batch = "astral_machine", timeoutTicks = 800)
    public static void infuserKeepsInputNbtAndBlockedOutputConsumesNothing(GameTestHelper helper) {
        GTRecipe recipe = AstralRecipeMaps.INDUSTRIAL_STARLIGHT_INFUSER_RECIPES.getProxyRecipes().get(RecipeTypesAS.INFUSION.get())
                .stream().filter(r -> "copy_nbt".equals(r.data.getString(AstralRecipeOutputs.TRANSFORM))).findFirst().orElseThrow();
        Fixture machine = fixture(helper, "industrial_starlight_infuser", 1);
        machine.fill(recipe);
        ItemStack input = machine.itemsIn.getStackInSlot(0).copy();
        input.getOrCreateTag().putString("astral_tick_regression", "kept");
        machine.itemsIn.setStackInSlot(0, input.copy());
        int fluidBefore = machine.fluidsIn.getFluidInTank(0).getAmount();
        machine.itemsOut.setStackInSlot(0, new ItemStack(Items.STONE, 64));
        helper.assertTrue(!machine.getRecipeLogic().checkMatchedRecipeAvailable(recipe), "blocked output allowed an infusion to start");
        helper.assertTrue(ItemStack.isSameItemSameTags(input, machine.itemsIn.getStackInSlot(0))
                && input.getCount() == machine.itemsIn.getStackInSlot(0).getCount()
                && fluidBefore == machine.fluidsIn.getFluidInTank(0).getAmount(), "blocked output consumed an input");
        machine.itemsOut.setStackInSlot(0, ItemStack.EMPTY);
        helper.assertTrue(machine.getRecipeLogic().checkMatchedRecipeAvailable(recipe), "unblocked real infuser rejected native recipe");
        tickUntil(helper, machine, () -> !machine.itemsOut.getStackInSlot(0).isEmpty(), 700, () -> {
            ItemStack result = machine.itemsOut.getStackInSlot(0);
            helper.assertTrue("kept".equals(result.getOrCreateTag().getString("astral_tick_regression")), "completed infusion lost NBT");
            helper.assertTrue(machine.itemsIn.getStackInSlot(0).isEmpty(), "infusion did not consume its source");
            helper.succeed();
        });
    }

    @GameTest(template = "platform", batch = "astral_machine", timeoutTicks = 500)
    public static void lightwellProducesFluidWithoutConsumingCatalyst(GameTestHelper helper) {
        GTRecipe recipe = AstralRecipeMaps.INDUSTRIAL_LIGHTWELL_RECIPES.getProxyRecipes().get(RecipeTypesAS.WELL.get())
                .stream().filter(r -> r.getId().getPath().endsWith("starlight_aquamarine")).findFirst().orElseThrow();
        Fixture machine = fixture(helper, "industrial_lightwell", 1);
        machine.fill(recipe);
        ItemStack catalyst = machine.itemsIn.getStackInSlot(0).copy();
        helper.assertTrue(machine.getRecipeLogic().checkMatchedRecipeAvailable(recipe), "lightwell rejected native aquamarine recipe");
        tickUntil(helper, machine, () -> !machine.fluidsOut.getFluidInTank(0).isEmpty(), 400, () -> {
            helper.assertTrue(ItemStack.isSameItemSameTags(catalyst, machine.itemsIn.getStackInSlot(0))
                    && catalyst.getCount() == machine.itemsIn.getStackInSlot(0).getCount(), "lightwell consumed its catalyst");
            int expected = FluidRecipeCapability.CAP.of(recipe.getOutputContents(FluidRecipeCapability.CAP).get(0).content).getAmount();
            helper.assertTrue(machine.fluidsOut.getFluidInTank(0).getAmount() == expected, "lightwell output amount differs from imported native recipe");
            machine.getRecipeLogic().setWorkingEnabled(false);
            helper.succeed();
        });
    }

    @GameTest(template = "platform", batch = "astral_machine", timeoutTicks = 1200)
    public static void actualMachineModifiersPreserveSeedEmbryoAndGrowthLineage(GameTestHelper helper) {
        Fixture assembler = fixture(helper, "magic_assembler", 1);
        GTRecipe seedRecipe = recipe(helper, "crystals/seed");
        assembler.fill(seedRecipe);
        ItemStack rock = new ItemStack(ItemsAS.ROCK_CRYSTAL.get());
        CrystalAttributes.Builder.newBuilder(false).addProperty(CrystalPropertiesAS.Properties.PROPERTY_SIZE, 2)
                .addProperty(CrystalPropertiesAS.Properties.PROPERTY_PURITY, 2)
                .addProperty(CrystalPropertiesAS.Properties.PROPERTY_SHAPE, 2).build().store(rock);
        assembler.itemsIn.setStackInSlot(0, rock);
        helper.assertTrue(assembler.getRecipeLogic().checkMatchedRecipeAvailable(seedRecipe), "registered assembler modifier rejected native seed source");
        tickUntil(helper, assembler, () -> !assembler.itemsOut.getStackInSlot(0).isEmpty(), 300, () -> {
            ItemStack seed = assembler.itemsOut.getStackInSlot(0).copy();
            helper.assertTrue(AstralCrystalNbtHelper.isCrystalSeed(seed), "real machine emitted a blank seed");
            assembler.getRecipeLogic().resetRecipeLogic();
            GTRecipe embryoRecipe = recipe(helper, "crystals/embryo");
            assembler.fill(embryoRecipe);
            assembler.itemsIn.setStackInSlot(0, seed);
            helper.assertTrue(assembler.getRecipeLogic().checkMatchedRecipeAvailable(embryoRecipe), "registered assembler rejected the selected seed");
            tickUntil(helper, assembler, () -> !assembler.itemsOut.getStackInSlot(0).isEmpty(), 300, () -> {
                ItemStack embryo = assembler.itemsOut.getStackInSlot(0).copy();
                helper.assertTrue(AstralCrystalNbtHelper.isCrystalEmbryo(embryo), "real machine emitted a blank embryo");
                Fixture growth = fixture(helper, "celestial_crystal_growth_array", 2);
                GTRecipe growthRecipe = recipe(helper, "growth/vicio");
                growth.fill(growthRecipe);
                growth.itemsIn.setStackInSlot(0, embryo);
                helper.assertTrue(growth.getRecipeLogic().checkMatchedRecipeAvailable(growthRecipe), "registered growth modifier rejected native embryo");
                tickUntil(helper, growth, () -> !growth.itemsOut.getStackInSlot(0).isEmpty(), 500, () -> {
                    ItemStack crystal = growth.itemsOut.getStackInSlot(0);
                    helper.assertTrue(AstralCrystalNbtHelper.getOpticalQuality(crystal) > 0
                            && crystal.getTag().getCompound("poSourceCrystal").equals(seed.getTag().getCompound("poSourceCrystal")),
                            "real growth cycle lost source ancestry or optical quality");
                    helper.succeed();
                });
            });
        });
    }

    private static void tickUntil(GameTestHelper helper, Fixture machine, BooleanSupplier completed, int remaining, Runnable then) {
        helper.runAfterDelay(1, () -> {
            machine.getRecipeLogic().serverTick();
            if (completed.getAsBoolean()) then.run();
            else if (remaining <= 0) helper.fail("Astral recipe did not finish: " + machine.getRecipeLogic().getStatus());
            else tickUntil(helper, machine, completed, remaining - 1, then);
        });
    }

    private static GTRecipe recipe(GameTestHelper helper, String suffix) {
        return helper.getLevel().getRecipeManager().getRecipes().stream().filter(GTRecipe.class::isInstance).map(GTRecipe.class::cast)
                .filter(r -> r.getId().getNamespace().equals("pollution") && r.getId().getPath().endsWith("/astral/" + suffix))
                .findFirst().orElseThrow();
    }

    private static Fixture fixture(GameTestHelper helper, String id, int x) {
        BlockPos pos = helper.absolutePos(new BlockPos(x, 1, 1));
        var definition = GTRegistries.MACHINES.get(ResourceLocation.fromNamespaceAndPath("pollution", id));
        helper.getLevel().setBlockAndUpdate(pos, definition.getBlock().defaultBlockState());
        return new Fixture((IMachineBlockEntity) helper.getLevel().getBlockEntity(pos));
    }

    private static final class Fixture extends MagicMultiblockController {
        final NotifiableItemStackHandler itemsIn = new NotifiableItemStackHandler(this, 9, IO.IN);
        final NotifiableItemStackHandler itemsOut = new NotifiableItemStackHandler(this, 1, IO.OUT);
        final NotifiableFluidTank fluidsIn = new NotifiableFluidTank(this, 3, 64000, IO.IN);
        final NotifiableFluidTank fluidsOut = new NotifiableFluidTank(this, 1, 64000, IO.OUT);
        final NotifiableEnergyContainer power = NotifiableEnergyContainer.receiverContainer(this, 10_000_000_000L, GTValues.V[GTValues.UV], 16);

        Fixture(IMachineBlockEntity holder) {
            super(holder);
            isFormed = true;
            addHandlerList(RecipeHandlerList.of(IO.IN, itemsIn, fluidsIn, power));
            addHandlerList(RecipeHandlerList.of(IO.OUT, itemsOut, fluidsOut));
            power.setEnergyStored(10_000_000_000L);
        }

        void fill(GTRecipe recipe) {
            for (int slot = 0; slot < itemsIn.getSlots(); slot++) itemsIn.setStackInSlot(slot, ItemStack.EMPTY);
            itemsOut.setStackInSlot(0, ItemStack.EMPTY);
            for (int slot = 0; slot < fluidsIn.getTanks(); slot++) fluidsIn.setFluidInTank(slot, net.minecraftforge.fluids.FluidStack.EMPTY);
            var items = recipe.getInputContents(ItemRecipeCapability.CAP);
            for (int i = 0; i < items.size(); i++) itemsIn.setStackInSlot(i, ItemRecipeCapability.CAP.of(items.get(i).content).getItems()[0].copy());
            var fluids = recipe.getInputContents(FluidRecipeCapability.CAP);
            for (int i = 0; i < fluids.size(); i++) fluidsIn.setFluidInTank(i, FluidRecipeCapability.CAP.of(fluids.get(i).content).getStacks()[0].copy());
        }

        @Override public long getOverclockVoltage() { return GTValues.V[GTValues.UV]; }
        @Override public Component getMagicRequirementFailure(GTRecipe recipe) { return null; }
        @Override public boolean drainInfusedFluid(int amount, boolean simulate) { return true; }
        @Override public boolean consumeMana(long amount, boolean simulate) { return true; }
        @Override public boolean consumeVis(int amount, boolean simulate) { return true; }
    }
}
