package meowmel.pollution.gametest;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import meowmel.pollution.Pollution;
import meowmel.pollution.common.machine.PollutionMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Pollution.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MultiblockFormationGameTests {
    private MultiblockFormationGameTests() {}

    static MultiblockControllerMachine build(GameTestHelper helper, MultiblockMachineDefinition definition) {
        return build(helper, definition, 0);
    }

    static MultiblockControllerMachine build(GameTestHelper helper, MultiblockMachineDefinition definition, int shape) {
        var preview = definition.getMatchingShapes().get(shape).getBlocks();
        BlockPos base = helper.absolutePos(new BlockPos(1, 1, 1));
        MultiblockControllerMachine controller = null;
        for (int x = 0; x < preview.length; x++) {
            for (int y = 0; y < preview[x].length; y++) {
                for (int z = 0; z < preview[x][y].length; z++) {
                    var info = preview[x][y][z];
                    if (info == null) continue;
                    BlockPos pos = base.offset(x, y, z);
                    helper.getLevel().setBlockAndUpdate(pos, info.getBlockState());
                    if (info.getBlockState().is(definition.getBlock())) {
                        controller = (MultiblockControllerMachine) MetaMachine.getMachine(helper.getLevel(), pos);
                    }
                }
            }
        }
        helper.assertTrue(controller != null, "preview has no controller: " + definition.getId());
        controller.setFrontFacing(Direction.NORTH);
        controller.setUpwardsFacing(Direction.NORTH);
        helper.assertTrue(controller.checkPatternWithLock(), "preview does not form: " + definition.getId()
                + ", error=" + (controller.getMultiblockState().error == null ? "none" :
                controller.getMultiblockState().error.getErrorInfo().getString())
                + ", candidates=" + (controller.getMultiblockState().error == null ? "none" :
                controller.getMultiblockState().error.getCandidates())
                + ", pos=" + controller.getMultiblockState().getPos());
        controller.onStructureFormed();
        helper.assertTrue(controller.isFormed(), "controller did not form");
        return controller;
    }

    @GameTest(template = "machine_lab", batch = "multiblock")
    public static void magicDistilleryPreviewForms(GameTestHelper helper) {
        build(helper, PollutionMachines.MAGIC_DISTILLERY);
        helper.succeed();
    }

    @GameTest(template = "machine_lab", batch = "multiblock")
    public static void terraDistilleryPreviewForms(GameTestHelper helper) {
        build(helper, PollutionMachines.BOT_DISTILLERY);
        helper.succeed();
    }

    @GameTest(template = "machine_lab", batch = "tower")
    public static void twelveLayerTowersKeepFractionsSeparate(GameTestHelper helper) {
        var tower = (meowmel.pollution.common.machine.multiblock.LayeredMagicTowerMachine)
                build(helper, PollutionMachines.MAGIC_DISTILLERY, 11);
        var outputs = tower.getParts().stream()
                .filter(part -> com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.EXPORT_FLUIDS.isApplicable(part.self().getBlockState().getBlock()))
                .map(part -> (com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine) part.self())
                .sorted(java.util.Comparator.comparingInt(part -> part.getPos().getY())).toList();
        helper.assertTrue(outputs.size() == 12, "maximum-height preview has missing layers");
        var recipe = com.gregtechceu.gtceu.common.data.GTRecipeTypes.DISTILLATION_RECIPES.recipeBuilder("pollution:fraction_routing_test")
                .duration(1).outputFluids(new net.minecraftforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER, 100))
                .outputFluids(new net.minecraftforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.LAVA, 200)).buildRawRecipe();
        tower.getRecipeLogic().setupRecipe(recipe);
        helper.assertTrue(tower.getRecipeLogic().getLastRecipe() != null, "tower rejected the routing test");
        tower.getRecipeLogic().onRecipeFinish();
        helper.assertTrue(outputs.get(0).tank.getFluidInTank(0).getFluid() == net.minecraft.world.level.material.Fluids.WATER
                && outputs.get(0).tank.getFluidInTank(0).getAmount() == 100, "first fraction did not reach the lowest layer");
        helper.assertTrue(outputs.get(1).tank.getFluidInTank(0).getFluid() == net.minecraft.world.level.material.Fluids.LAVA
                && outputs.get(1).tank.getFluidInTank(0).getAmount() == 200, "second fraction mixed with the first");
        helper.succeed();
    }

    @GameTest(template = "machine_lab", batch = "tower")
    public static void maximumTerraTowerPreviewForms(GameTestHelper helper) {
        build(helper, PollutionMachines.BOT_DISTILLERY, 11);
        helper.succeed();
    }

    @GameTest(template = "machine_lab", batch = "tower")
    public static void blockedTowerRetainsAllFractionsUntilSpaceReturns(GameTestHelper helper) {
        var tower = (meowmel.pollution.common.machine.multiblock.LayeredMagicTowerMachine)
                build(helper, PollutionMachines.MAGIC_DISTILLERY, 1);
        var outputs = tower.getParts().stream()
                .filter(part -> com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.EXPORT_FLUIDS.isApplicable(part.self().getBlockState().getBlock()))
                .map(part -> (com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine) part.self())
                .sorted(java.util.Comparator.comparingInt(part -> part.getPos().getY())).toList();
        var recipe = com.gregtechceu.gtceu.common.data.GTRecipeTypes.DISTILLATION_RECIPES.recipeBuilder("pollution:blocked_fraction_test")
                .duration(1).outputFluids(new net.minecraftforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER, 100))
                .outputFluids(new net.minecraftforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.LAVA, 200)).buildRawRecipe();
        tower.getRecipeLogic().setupRecipe(recipe);
        outputs.get(1).tank.fillInternal(new net.minecraftforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1),
                net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
        helper.assertTrue(tower.outputParallelLimit(recipe, 16) == 0, "parallel calculation ignored an incompatible fraction tank");
        tower.getRecipeLogic().onRecipeFinish();
        helper.assertTrue(tower.getRecipeLogic().getLastRecipe() != null && tower.getRecipeLogic().isWaiting(), "blocked recipe was discarded");
        helper.assertTrue(outputs.get(0).tank.getFluidInTank(0).isEmpty(), "blocked recipe partially emitted its first fraction");
        outputs.get(1).tank.drainInternal(1, net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
        tower.getRecipeLogic().onRecipeFinish();
        helper.assertTrue(outputs.get(0).tank.getFluidInTank(0).getAmount() == 100
                && outputs.get(1).tank.getFluidInTank(0).getAmount() == 200, "unblocking lost or duplicated a fraction");
        helper.succeed();
    }

    @GameTest(template = "machine_lab", batch = "rotors")
    public static void megaTurbineRequiresAllRotorsAndPersistsHighPower(GameTestHelper helper) {
        var turbine = (meowmel.pollution.common.machine.multiblock.magic.MagicMegaTurbineMachine)
                build(helper, PollutionMachines.MAGIC_MEGA_TURBINE);
        helper.assertTrue(!turbine.hasRotor() && turbine.getOverclockVoltage() == 0, "empty turbine produces power");
        var rotors = turbine.getParts().stream()
                .filter(com.gregtechceu.gtceu.common.machine.multiblock.part.RotorHolderPartMachine.class::isInstance)
                .map(com.gregtechceu.gtceu.common.machine.multiblock.part.RotorHolderPartMachine.class::cast).toList();
        helper.assertTrue(rotors.size() == 12, "mega turbine has the wrong rotor count");
        for (var holder : rotors) {
            var rotor = com.gregtechceu.gtceu.common.data.GTItems.TURBINE_ROTOR.asStack();
            com.gregtechceu.gtceu.common.item.TurbineRotorBehaviour.getBehaviour(rotor)
                    .setPartMaterial(rotor, meowmel.pollution.api.unification.PollutionMaterials.Syrmorite);
            holder.setRotorStack(rotor);
        }
        helper.assertTrue(turbine.hasRotor(), "complete rotor set was rejected; check preview holder tiers");
        long normal = turbine.getOverclockVoltage();
        helper.assertTrue(normal > 0, "rotor power is not applied");
        turbine.setHighPower(true);
        helper.assertTrue(turbine.getOverclockVoltage() == normal * 3, "high power does not triple the target output");
        var blockEntity = helper.getLevel().getBlockEntity(turbine.getPos());
        var saved = blockEntity.saveWithoutMetadata();
        blockEntity.load(saved);
        helper.assertTrue(turbine.isHighPower(), "high-power setting did not survive save/load");
        turbine.onWorking();
        helper.assertTrue(rotors.stream().allMatch(rotor -> rotor.getRotorSpeed() > 0), "not all rotor holders accelerate");
        rotors.get(11).setRotorStack(net.minecraft.world.item.ItemStack.EMPTY);
        helper.assertTrue(!turbine.hasRotor() && !turbine.onWorking() && turbine.getOverclockVoltage() == 0,
                "turbine kept running with a missing rotor");
        helper.succeed();
    }

    @GameTest(template = "machine_lab", batch = "bath", timeoutTicks = 100)
    public static void bathRefillsOnlyWhenItCanPayForAWholeBucket(GameTestHelper helper) {
        var bath = (meowmel.pollution.common.machine.multiblock.magic.MagicChemicalBathMachine)
                build(helper, PollutionMachines.MAGIC_CHEMICAL_BATH);
        java.util.List<BlockPos> cells = bath.getMultiblockState().getMatchContext().get("pollution.bath.water_cells");
        helper.assertTrue(cells.size() == 25, "bath recorded the wrong water area");
        for (BlockPos pos : cells) helper.getLevel().setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
        var hatch = bath.getParts().stream()
                .filter(part -> com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.IMPORT_FLUIDS.isApplicable(part.self().getBlockState().getBlock()))
                .map(part -> (com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine) part.self()).findFirst().orElseThrow();
        hatch.tank.fill(new net.minecraftforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER, 999),
                net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
        helper.runAfterDelay(8, () -> {
            helper.assertTrue(cells.stream().noneMatch(pos -> helper.getLevel().getFluidState(pos).isSource()), "bath created water without a whole bucket");
            hatch.tank.fill(new net.minecraftforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1),
                    net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
            helper.runAfterDelay(8, () -> {
                helper.assertTrue(cells.stream().filter(pos -> helper.getLevel().getFluidState(pos).isSource()).count() == 1,
                        "one bucket did not create exactly one source");
                helper.assertTrue(hatch.tank.getFluidInTank(0).isEmpty(), "bath failed to debit its water input");
                helper.succeed();
            });
        });
    }

    @GameTest(template = "machine_lab", batch = "multiblock")
    public static void magicBathPreviewForms(GameTestHelper helper) {
        build(helper, PollutionMachines.MAGIC_CHEMICAL_BATH);
        helper.succeed();
    }

    @GameTest(template = "machine_lab", batch = "multiblock")
    public static void magicTurbinePreviewForms(GameTestHelper helper) {
        build(helper, PollutionMachines.MAGIC_LARGE_TURBINE);
        helper.succeed();
    }

    @GameTest(template = "machine_lab", batch = "multiblock")
    public static void megaManaTurbinePreviewForms(GameTestHelper helper) {
        build(helper, PollutionMachines.MEGA_MANA_ROTOR_TURBINE);
        helper.succeed();
    }

    @GameTest(template = "machine_lab", batch = "battery")
    public static void magicBatteryStoresEnergyWithFullOutputsAndCanPause(GameTestHelper helper) {
        var battery = (meowmel.pollution.common.machine.multiblock.magic.MagicBatteryMachine)
                build(helper, PollutionMachines.MAGIC_BATTERY);
        var inputDefinition = com.gregtechceu.gtceu.api.registry.GTRegistries.MACHINES.get(
                net.minecraft.resources.ResourceLocation.tryParse("gtceu:iv_energy_input_hatch"));
        helper.assertTrue(inputDefinition != null, "missing battery test energy hatch");
        BlockPos casing = null;
        for (BlockPos candidate : BlockPos.betweenClosed(helper.absolutePos(new BlockPos(1, 1, 1)),
                helper.absolutePos(new BlockPos(25, 25, 25)))) {
            if (helper.getLevel().getBlockState(candidate).is(
                    meowmel.pollution.common.block.PollutionMagicBlocks.MAGIC_BATTERY_CASING.get())) {
                casing = candidate.immutable();
                break;
            }
        }
        helper.assertTrue(casing != null, "battery preview contains no casing");
        battery.onStructureInvalid();
        helper.getLevel().setBlockAndUpdate(casing, inputDefinition.getBlock().defaultBlockState());
        helper.assertTrue(battery.checkPatternWithLock(), "battery rejected an input hatch");
        battery.onStructureFormed();
        var input = com.gregtechceu.gtceu.api.capability.GTCapabilityHelper.getEnergyContainer(helper.getLevel(), casing, null);
        helper.assertTrue(input != null, "input has no energy capability");
        for (var part : battery.getParts()) {
            if (part instanceof com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine hatch
                    && com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.OUTPUT_ENERGY.isApplicable(hatch.getBlockState().getBlock())) {
                hatch.energyContainer.changeEnergy(hatch.energyContainer.getEnergyCapacity());
            }
        }
        input.changeEnergy(10000);
        helper.runAfterDelay(5, () -> {
            long stored = battery.getStoredEnergy();
            helper.assertTrue(stored > 0 && stored + input.getEnergyStored() == 10000,
                    "battery storage=" + stored + ", input=" + input.getEnergyStored() + ", formed=" + battery.isFormed());
            helper.assertTrue(battery.getCapacity() == 250000, "tier-1 battery has incorrect capacity");
            battery.setWorkingEnabled(false);
            helper.runAfterDelay(3, () -> {
                helper.assertTrue(battery.getStoredEnergy() == stored, "disabled battery still transfers energy");
                var blockEntity = helper.getLevel().getBlockEntity(battery.getPos());
                var saved = blockEntity.saveWithoutMetadata();
                blockEntity.load(saved);
                helper.assertTrue(battery.getStoredEnergy() == stored && !battery.isWorkingEnabled(),
                        "battery save/load lost storage or control state");
                helper.succeed();
            });
        });
    }
}
