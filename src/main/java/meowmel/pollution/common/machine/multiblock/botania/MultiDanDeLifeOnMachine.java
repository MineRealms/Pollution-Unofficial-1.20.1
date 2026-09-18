package meowmel.pollution.common.machine.multiblock.botania;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import meowmel.pollution.Pollution;
import meowmel.pollution.common.machine.multiblock.MagicRecipeLogic;
import meowmel.pollution.common.machine.multiblock.mana.ManaMultiblockController;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.botania.common.block.BotaniaBlocks;

import java.util.List;

/**
 * Life Activation Garden: plays Conway's Game of Life on a 31x31 cell-block
 * board and converts the dying cells into energy.
 *
 * <p>Upstream origin: {@code MetaTileEntityMultiDanDeLifeOn} (1.12.2, id
 * {@code pollution_multi_dan_de_life_on}). Every completed recipe step runs one
 * game round on the board above the controller (starting 15 blocks to the side
 * and 16 blocks into the structure, facing-dependent), Conway's rules decide
 * which Botania cell blocks live, and every cell that dies after a non-zero
 * lifespan adds
 * {@code coilLevel^1.5 * 2560000 * age^(1/3) + 22937600 * 2/3} to an internal
 * energy buffer. The buffer is flushed into the output energy hatch (mode 0) or
 * converted into mana fluid (mode 1); the mode is toggled with a sneak-right-click
 * screwdriver.</p>
 *
 * <p>Deviations:</p>
 * <ul>
 *   <li>The upstream {@code MultiblockFuelRecipeLogic} is replaced by
 *       {@link MagicRecipeLogic}; the game round runs from
 *       {@code onRecipeFinish}, so one round per completed recipe (upstream:
 *       one round per {@code progressTime > maxProgressTime}).</li>
 *   <li>Upstream's {@code pow(CellAge, 1/3)} used integer division and was
 *       therefore always 1; the port uses {@code 1.0 / 3.0} and keeps the
 *       documented formula.</li>
 *   <li>The upstream structure allowed neither item nor output fluid hatches,
 *       so its own fairy-dust recipe could never run and fluid mode could never
 *       output. The port adds {@code IMPORT_ITEMS} and {@code EXPORT_FLUIDS}
 *       positions to the {@code G} casing character; see
 *       {@link MultiDanDeLifeOnPatterns}.</li>
 *   <li>Fluid mode needs the unported mana material. The port resolves the
 *       {@code pollution:mana} fluid through {@link ForgeRegistries#FLUIDS} at
 *       runtime; while the material is absent the buffer simply stays stored
 *       and can still be drained in energy mode.</li>
 *   <li>The 1.12 {@code EntityPlayer.sendStatusMessage} calls map to
 *       {@code Player#displayClientMessage}.</li>
 * </ul>
 */
public class MultiDanDeLifeOnMachine extends ManaMultiblockController implements IDisplayUIMachine {

    /** Upstream game board is 31x31 cells, one block above the controller. */
    public static final int BOARD_SIZE = 31;

    private static final int MODE_ENERGY = 0;
    private static final int MODE_FLUID = 1;
    private static final int BOARD_OFFSET = 15;
    private static final int BOARD_DEPTH = 16;

    private static final ResourceLocation MANA_FLUID_ID = ResourceLocation
            .fromNamespaceAndPath(Pollution.MOD_ID, "mana");

    /** Upstream {@code CellAge[31][31]}, persisted. */
    private final int[][] cellAge = new int[BOARD_SIZE][BOARD_SIZE];
    /** Upstream {@code IsCellAlive[31][31]}, recomputed every round. */
    private final boolean[][] cellAlive = new boolean[BOARD_SIZE][BOARD_SIZE];

    private long energyBuffer;
    private int modeIndex;

    private TickableSubscription tickSubscription;

    public MultiDanDeLifeOnMachine(IMachineBlockEntity holder) {
        super(holder, BotaniaRecipeMaps.DAN_DE_LIFE_ON);
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new MultiDanDeLifeOnRecipeLogic(this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            tickSubscription = subscribeServerTick(this::tickBuffer);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (tickSubscription != null) {
            tickSubscription.unsubscribe();
            tickSubscription = null;
        }
    }

    // ////////////////////////////////////
    // ***** Energy buffer *****//
    // ////////////////////////////////////

    /** Flushes the internal buffer into the output energy hatch or mana tank. */
    private void tickBuffer() {
        if (!(getLevel() instanceof ServerLevel) || !isFormed() || energyBuffer <= 0L) {
            return;
        }
        if (modeIndex == MODE_ENERGY) {
            EnergyHatchPartMachine energy = findPart(EnergyHatchPartMachine.class);
            if (energy == null) {
                return;
            }
            long room = energy.energyContainer.getEnergyCapacity() - energy.energyContainer.getEnergyStored();
            if (room <= 0L) {
                return;
            }
            long moved = Math.min(room, energyBuffer);
            energy.energyContainer.addEnergy(moved);
            energyBuffer -= moved;
            markDirty();
            return;
        }
        FluidStack mana = manaFluid();
        if (mana.isEmpty()) {
            // The mana material is not ported yet: keep the buffer instead of
            // destroying it (documented deviation).
            return;
        }
        FluidHatchPartMachine output = findFluidOutput();
        if (output == null) {
            return;
        }
        long amount = Math.min(energyBuffer, Integer.MAX_VALUE);
        mana.setAmount((int) amount);
        int filled = output.tank.fill(mana, net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
        if (filled > 0) {
            energyBuffer -= filled;
            markDirty();
        }
    }

    private static FluidStack manaFluid() {
        var fluid = ForgeRegistries.FLUIDS.getValue(MANA_FLUID_ID);
        return fluid == null ? FluidStack.EMPTY : new FluidStack(fluid, 1);
    }

    // ////////////////////////////////////
    // ***** Game of Life *****//
    // ////////////////////////////////////

    /** Runs one Conway round on the cell-block board and banks the dead cells. */
    void runLifeStep() {
        if (!(getLevel() instanceof ServerLevel level) || !isFormed()) {
            return;
        }
        int controllerX = getPos().getX();
        int controllerY = getPos().getY();
        int controllerZ = getPos().getZ();
        int startX = controllerX;
        int startZ = controllerZ;
        switch (getFrontFacing()) {
            case NORTH -> {
                startX = controllerX - BOARD_OFFSET;
                startZ = controllerZ + BOARD_DEPTH;
            }
            case SOUTH -> {
                startX = controllerX - BOARD_OFFSET;
                startZ = controllerZ - (BOARD_SIZE + BOARD_OFFSET);
            }
            case WEST -> {
                startX = controllerX + BOARD_DEPTH;
                startZ = controllerZ - BOARD_OFFSET;
            }
            case EAST -> {
                startX = controllerX - (BOARD_SIZE + BOARD_OFFSET);
                startZ = controllerZ - BOARD_OFFSET;
            }
            default -> {
                return;
            }
        }
        int boardY = controllerY + 1;

        // Remove "breeding" cells that touch the board from outside.
        for (int x = -4; x < BOARD_SIZE + 4; x++) {
            for (int z = -4; z < BOARD_SIZE + 4; z++) {
                if (x >= 0 && x < BOARD_SIZE && z >= 0 && z < BOARD_SIZE) {
                    continue;
                }
                BlockPos pos = new BlockPos(startX + x, boardY, startZ + z);
                if (level.getBlockState(pos).is(BotaniaBlocks.cellBlock)) {
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                }
            }
        }

        for (int x = 0; x < BOARD_SIZE; x++) {
            for (int z = 0; z < BOARD_SIZE; z++) {
                cellAlive[x][z] = cellAliveAt(level, startX + x, boardY, startZ + z);
            }
        }

        for (int x = 0; x < BOARD_SIZE; x++) {
            for (int z = 0; z < BOARD_SIZE; z++) {
                BlockPos pos = new BlockPos(startX + x, boardY, startZ + z);
                if (cellAlive[x][z]) {
                    if (!level.getBlockState(pos).is(BotaniaBlocks.cellBlock)) {
                        level.setBlockAndUpdate(pos, BotaniaBlocks.cellBlock.defaultBlockState());
                    }
                    cellAge[x][z]++;
                } else {
                    if (level.getBlockState(pos).is(BotaniaBlocks.cellBlock)) {
                        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    }
                    if (cellAge[x][z] != 0) {
                        energyBuffer += energyPerAgeForCellDied(cellAge[x][z]);
                        cellAge[x][z] = 0;
                    }
                }
            }
        }
        markDirty();
    }

    private static boolean cellAliveAt(ServerLevel level, int x, int y, int z) {
        int liveNeighbors = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                if (level.getBlockState(new BlockPos(x + dx, y, z + dz)).is(BotaniaBlocks.cellBlock)) {
                    liveNeighbors++;
                }
            }
        }
        boolean alive = level.getBlockState(new BlockPos(x, y, z)).is(BotaniaBlocks.cellBlock);
        return alive ? liveNeighbors > 1 && liveNeighbors < 4 : liveNeighbors == 3;
    }

    /** Upstream {@code EnergyPerAgeForCellDied}, with the age exponent fixed. */
    private long energyPerAgeForCellDied(int age) {
        int coil = Math.max(1, coilLevel());
        return (long) (Math.pow(coil, 1.5D) * 2_560_000D * Math.pow(age, 1.0D / 3.0D)
                + 22_937_600D * 2.0D / 3.0D);
    }

    public int coilLevel() {
        return coilType == null ? 0 : coilType.getLevel();
    }

    // ////////////////////////////////////
    // ***** Mode toggle *****//
    // ////////////////////////////////////

    @Override
    protected InteractionResult onScrewdriverClick(Player player, InteractionHand hand, Direction facing,
                                                   BlockHitResult hitResult) {
        if (!isRemote() && player.isShiftKeyDown()) {
            if (!getRecipeLogic().isActive()) {
                modeIndex = modeIndex == MODE_ENERGY ? MODE_FLUID : MODE_ENERGY;
                player.displayClientMessage(Component.translatable("pollution.modeChanged.message"), true);
            } else {
                player.displayClientMessage(
                        Component.translatable("gregtech.multiblock.multiple_recipemaps.switch_message"), true);
            }
        }
        return InteractionResult.SUCCESS;
    }

    // ////////////////////////////////////
    // ***** UI *****//
    // ////////////////////////////////////

    @Override
    public void addDisplayText(List<Component> textList) {
        IDisplayUIMachine.super.addDisplayText(textList);
        if (isFormed()) {
            textList.add(Component.translatable("pollution.machine.pollution_multi_dan_de_life_on.mode",
                    Component.translatable("pollution.machine.pollution_multi_dan_de_life_on.mode" + modeIndex)));
            textList.add(Component.translatable("pollution.machine.pollution_multi_dan_de_life_on.buffer", energyBuffer));
            EnergyHatchPartMachine energy = findPart(EnergyHatchPartMachine.class);
            if (energy != null) {
                textList.add(Component.translatable("pollution.machine.pollution_multi_dan_de_life_on.energy",
                        energy.energyContainer.getEnergyStored(), energy.energyContainer.getEnergyCapacity()));
            }
        }
    }

    private <T> T findPart(Class<T> type) {
        for (var part : getParts()) {
            if (type.isInstance(part.self())) {
                return type.cast(part.self());
            }
        }
        return null;
    }

    private FluidHatchPartMachine findFluidOutput() {
        for (var part : getParts()) {
            if (part.self() instanceof FluidHatchPartMachine hatch
                    && PartAbility.EXPORT_FLUIDS.isApplicable(hatch.getBlockState().getBlock())) {
                return hatch;
            }
        }
        return null;
    }

    // ////////////////////////////////////
    // ***** Persistence *****//
    // ////////////////////////////////////

    @Override
    public void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        tag.putInt("GardenMode", modeIndex);
        tag.putLong("GardenEnergyBuffer", energyBuffer);
        int[] ages = new int[BOARD_SIZE * BOARD_SIZE];
        for (int x = 0; x < BOARD_SIZE; x++) {
            for (int z = 0; z < BOARD_SIZE; z++) {
                ages[x * BOARD_SIZE + z] = cellAge[x][z];
            }
        }
        tag.putIntArray("GardenCellAge", ages);
    }

    @Override
    public void loadCustomPersistedData(CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        modeIndex = tag.getInt("GardenMode");
        energyBuffer = tag.getLong("GardenEnergyBuffer");
        int[] ages = tag.getIntArray("GardenCellAge");
        if (ages.length == BOARD_SIZE * BOARD_SIZE) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                for (int z = 0; z < BOARD_SIZE; z++) {
                    cellAge[x][z] = ages[x * BOARD_SIZE + z];
                }
            }
        }
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return MultiDanDeLifeOnPatterns.create(definition);
    }

    /** Runs one game round whenever a recipe (one second) completes. */
    protected static class MultiDanDeLifeOnRecipeLogic extends MagicRecipeLogic {

        private final MultiDanDeLifeOnMachine garden;

        public MultiDanDeLifeOnRecipeLogic(MultiDanDeLifeOnMachine machine) {
            super(machine);
            this.garden = machine;
        }

        @Override
        public void onRecipeFinish() {
            super.onRecipeFinish();
            garden.runLifeStep();
        }
    }
}
