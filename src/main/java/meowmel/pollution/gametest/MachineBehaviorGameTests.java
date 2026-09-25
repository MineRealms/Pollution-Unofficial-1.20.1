package meowmel.pollution.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IExplosionMachine;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import dev.tc4port.thaumcraft.registry.TCFluids;
import meowmel.pollution.Pollution;
import meowmel.pollution.PollutionConfig;
import meowmel.pollution.api.pollution.PollutionEngine;
import meowmel.pollution.common.machine.single.MagicEnergyAbsorberMachine;
import meowmel.pollution.common.machine.single.ManaGeneratorMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import vazkii.botania.common.block.BotaniaBlocks;

@GameTestHolder(Pollution.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MachineBehaviorGameTests {
    private MachineBehaviorGameTests() {}

    @GameTest(template = "platform", batch = "fluid_containers", timeoutTicks = 100)
    public static void infusedHatchTransfersContainersWithoutVoidingBlockedOutput(GameTestHelper helper) {
        var pos = place(helper, "pollution:lv_infused_fluid_hatch");
        var hatch = (meowmel.pollution.common.machine.part.InfusedFluidHatchMachine) MetaMachine.getMachine(helper.getLevel(), pos);
        hatch.containerInput.setStackInSlot(0, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.WATER_BUCKET));
        hatch.containerOutput.setStackInSlot(0, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STONE, 64));
        helper.runAfterDelay(8, () -> {
            helper.assertTrue(hatch.tank.getFluidInTank(0).isEmpty()
                    && hatch.containerInput.getStackInSlot(0).is(net.minecraft.world.item.Items.WATER_BUCKET), "blocked output consumed a bucket");
            hatch.containerOutput.setStackInSlot(0, net.minecraft.world.item.ItemStack.EMPTY);
            helper.runAfterDelay(8, () -> {
                helper.assertTrue(hatch.tank.getFluidInTank(0).getAmount() == 1000
                        && hatch.containerOutput.getStackInSlot(0).is(net.minecraft.world.item.Items.BUCKET), "container did not empty into the hatch");
                var saved = helper.getLevel().getBlockEntity(pos).saveWithoutMetadata();
                hatch.tank.drainInternal(1000, net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                hatch.containerOutput.setStackInSlot(0, net.minecraft.world.item.ItemStack.EMPTY);
                helper.getLevel().getBlockEntity(pos).load(saved);
                helper.assertTrue(hatch.tank.getFluidInTank(0).getAmount() == 1000
                        && hatch.containerOutput.getStackInSlot(0).is(net.minecraft.world.item.Items.BUCKET), "save/load lost hatch inventory");
                hatch.containerOutput.setStackInSlot(0, net.minecraft.world.item.ItemStack.EMPTY);
                hatch.containerInput.setStackInSlot(0, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BUCKET));
                helper.runAfterDelay(8, () -> {
                    helper.assertTrue(hatch.tank.getFluidInTank(0).isEmpty()
                            && hatch.containerOutput.getStackInSlot(0).is(net.minecraft.world.item.Items.WATER_BUCKET), "container did not fill from the hatch");
                    helper.succeed();
                });
            });
        });
    }

    @GameTest(template = "machine_lab", batch = "persistence")
    public static void singleMachineInventoriesSurviveReload(GameTestHelper helper) {
        int x = 2;
        for (String id : new String[]{"source_charge", "lv_aspect_tank", "lv_flux_scrubber", "luv_small_node_generator"}) {
            var pos = helper.absolutePos(new BlockPos(x, 2, 2));
            x += 4;
            var definition = GTRegistries.MACHINES.get(ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, id));
            helper.getLevel().setBlockAndUpdate(pos, definition.getBlock().defaultBlockState());
            var machine = MetaMachine.getMachine(helper.getLevel(), pos);
            var inventories = machine.getTraits().stream()
                    .filter(com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler.class::isInstance)
                    .map(com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler.class::cast).toList();
            helper.assertTrue(!inventories.isEmpty(), "no inventory to test for " + id);
            for (var inventory : inventories) inventory.setStackInSlot(0, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND, 3));
            var blockEntity = helper.getLevel().getBlockEntity(pos);
            var saved = blockEntity.saveWithoutMetadata();
            for (var inventory : inventories) inventory.setStackInSlot(0, net.minecraft.world.item.ItemStack.EMPTY);
            blockEntity.load(saved);
            helper.assertTrue(inventories.stream().allMatch(inventory -> inventory.getStackInSlot(0).getCount() == 3),
                    "reload lost inventory contents for " + id);
        }
        helper.succeed();
    }

    private static BlockPos place(GameTestHelper helper, String id) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        var definition = GTRegistries.MACHINES.get(ResourceLocation.tryParse(id));
        helper.assertTrue(definition != null, "missing machine " + id);
        helper.getLevel().setBlockAndUpdate(pos, definition.getBlock().defaultBlockState());
        return pos;
    }

    @GameTest(template = "platform", batch = "absorber")
    public static void absorberPedestalsGenerateAndStop(GameTestHelper helper) {
        Block[] pedestals = {Blocks.DRAGON_EGG, BotaniaBlocks.manaPylon, BotaniaBlocks.gaiaPylon,
                BotaniaBlocks.gaiaHead, Blocks.STONE, BotaniaBlocks.naturaPylon};
        long[] rates = {GTValues.V[GTValues.HV], GTValues.V[GTValues.HV], GTValues.V[GTValues.EV],
                GTValues.V[GTValues.IV], 0, 0};
        for (int i = 0; i < pedestals.length; i++) {
            helper.assertTrue(MagicEnergyAbsorberMachine.generationVoltage(pedestals[i].defaultBlockState()) == rates[i],
                    "wrong generation rate for " + pedestals[i]);
        }
        BlockPos pos = place(helper, "pollution:hv_magic_energy_absorber");
        helper.getLevel().setBlockAndUpdate(pos.above(), BotaniaBlocks.manaPylon.defaultBlockState());
        helper.runAfterDelay(4, () -> {
            var energy = GTCapabilityHelper.getEnergyContainer(helper.getLevel(), pos, null);
            helper.assertTrue(energy != null && energy.getEnergyStored() > 0, "Mana Pylon did not generate EU");
            helper.getLevel().removeBlock(pos.above(), false);
            long stored = energy.getEnergyStored();
            helper.runAfterDelay(4, () -> {
                helper.assertTrue(energy.getEnergyStored() == stored, "generation continued without a pedestal");
                helper.succeed();
            });
        });
    }

    @GameTest(template = "platform", batch = "mana_generator")
    public static void manaIntakeRespectsWorldTicks(GameTestHelper helper) {
        BlockPos pos = place(helper, "pollution:lv_mana_generator");
        var machine = (ManaGeneratorMachine) MetaMachine.getMachine(helper.getLevel(), pos);
        machine.receiveMana(10000L);
        machine.receiveMana(10000L);
        helper.assertTrue(machine.getMana() == GTValues.V[GTValues.LV], "intake exceeded one tick's budget");
        helper.assertTrue(!machine.canReceiveManaFromBursts(), "exhausted receiver still accepts bursts");
        helper.runAfterDelay(2, () -> {
            machine.receiveMana(10000L);
            helper.assertTrue(machine.getMana() == 2 * GTValues.V[GTValues.LV], "budget did not reset on next tick");
            helper.succeed();
        });
    }

    @GameTest(template = "platform", batch = "flux_fuel")
    public static void fractionalFluxGeneratesEveryTick(GameTestHelper helper) {
        BlockPos pos = place(helper, "pollution:lv_flux_fuel_cell");
        var level = helper.getLevel();
        for (int x = 0; x < 3; x++) {
            level.setBlockAndUpdate(pos.offset(x - 1, 0, -1), TCFluids.FLUX_GOO.get().blockStateForQuanta(8));
        }
        helper.runAfterDelay(5, () -> {
            var energy = GTCapabilityHelper.getEnergyContainer(level, pos, null);
            helper.assertTrue(energy != null && energy.getEnergyStored() > GTValues.V[GTValues.LV],
                    "fractional flux consumption skipped generation until a whole quantum accumulated");
            helper.succeed();
        });
    }

    @GameTest(template = "platform", batch = "gt_explosion")
    public static void gregtechExplosionEmitsPollution(GameTestHelper helper) {
        BlockPos pos = place(helper, "gtceu:lv_macerator");
        var machine = MetaMachine.getMachine(helper.getLevel(), pos);
        helper.assertTrue(machine instanceof IExplosionMachine, "test machine has no explosion hook");
        double previous = PollutionEngine.get(helper.getLevel(), pos);
        ((IExplosionMachine) machine).doExplosion(2.0F);
        double added = PollutionEngine.get(helper.getLevel(), pos) - previous;
        helper.assertTrue(Math.abs(added - 2.0D * PollutionConfig.MUFFLER_POLLUTION_MULTIPLIER.get()) < 1.0E-6,
                "ordinary GT explosion was not attributed exactly once, actual=" + added);
        PollutionEngine.set(helper.getLevel(), pos, previous);
        helper.succeed();
    }
}
