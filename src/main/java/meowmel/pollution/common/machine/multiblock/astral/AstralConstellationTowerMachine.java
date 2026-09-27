package meowmel.pollution.common.machine.multiblock.astral;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import hellfirepvp.astralsorcery.common.base.MoonPhase;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.SkyHandler;
import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import meowmel.pollution.common.machine.multiblock.AbstractDisplayMultiblockMachine;
import meowmel.pollution.common.starstream.StarstreamBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * A fixed-identity Astral tower. It buffers starlight locally and submits the
 * buffered channel energy to a linked Starstream core/relay using the public
 * network API, preserving the upstream tower's 20 tick sampling cadence.
 */
public final class AstralConstellationTowerMachine extends AbstractDisplayMultiblockMachine {
    private static final String TARGET = "StarstreamTarget";
    private static final String ENERGY = "ConstellationEnergy";
    private static final long CAPACITY = 4_194_304L;
    private static final long TRANSFER_PER_TICK = 512L;
    private static final long BASE_PER_TICK = 64L;
    private static final int SAMPLE_INTERVAL = 20;

    private final ConstellationTowerDefinition definition;
    private boolean workingEnabled = true;
    private BlockPos targetPos;
    private long energy;
    private long generation;
    private TickableSubscription tickSubscription;

    public AstralConstellationTowerMachine(IMachineBlockEntity holder, ConstellationTowerDefinition definition) {
        super(holder);
        this.definition = definition;
    }

    public ConstellationTowerDefinition getTowerDefinition() { return definition; }
    public boolean isWorkingEnabled() { return workingEnabled; }
    public void setWorkingEnabled(boolean enabled) { workingEnabled = enabled; markDirty(); }

    public boolean bindStarstreamTarget(BlockPos target) {
        if (target == null || getLevel() == null || !(getLevel().getBlockEntity(target) instanceof StarstreamBlockEntity node)
                || (node.getKind() != StarstreamBlockEntity.Kind.CORE && node.getKind() != StarstreamBlockEntity.Kind.RELAY)) {
            return false;
        }
        targetPos = target.immutable();
        markDirty();
        return true;
    }

    public void clearStarstreamTarget() {
        targetPos = null;
        markDirty();
    }

    public BlockPos getStarstreamTarget() { return targetPos; }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) tickSubscription = subscribeServerTick(this::tickTower);
    }

    @Override
    public void onUnload() {
        if (tickSubscription != null) tickSubscription.unsubscribe();
        tickSubscription = null;
        super.onUnload();
    }

    private void tickTower() {
        if (!(getLevel() instanceof ServerLevel level) || !isFormed()) return;
        if (targetPos != null && level.getBlockEntity(targetPos) instanceof StarstreamBlockEntity node) {
            String channel = definition.getId();
            long transfer = Math.min(TRANSFER_PER_TICK, energy);
            long accepted = node.receiveConstellationEnergy(channel, transfer, false);
            energy -= accepted;
        }
        if (getOffsetTimer() % SAMPLE_INTERVAL != 0L || !isWorkingEnabled()) return;
        BlockPos skyPos = getPos().above(7);
        if (!level.canSeeSky(skyPos)) { generation = 0L; return; }
        WorldContext context = SkyHandler.getContext(level);
        IConstellation constellation = ConstellationRegistry.getConstellation(
                ResourceLocation.fromNamespaceAndPath("astralsorcery", definition.getId()));
        if (context == null || constellation == null) { generation = 0L; return; }
        float distribution = Math.max(0.0F, Math.min(1.0F, context.getDistributionHandler().getDistribution(constellation)));
        boolean active = context.getConstellationHandler().isActiveCurrently(constellation, MoonPhase.fromWorld(level));
        long produced = Math.max(1L, Math.round(BASE_PER_TICK * (0.5D + distribution)
                * (active ? 4.0D : 1.0D))) * SAMPLE_INTERVAL;
        long accepted = Math.min(produced, CAPACITY - energy);
        energy += Math.max(0L, accepted);
        generation = accepted / SAMPLE_INTERVAL;
        markDirty();
    }

    @Override
    public void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        tag.putLong(ENERGY, Math.max(0L, Math.min(CAPACITY, energy)));
        tag.putBoolean("WorkingEnabled", workingEnabled);
        if (targetPos != null) tag.putLong(TARGET, targetPos.asLong());
    }

    @Override
    public void loadCustomPersistedData(CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        energy = Math.max(0L, Math.min(CAPACITY, tag.getLong(ENERGY)));
        workingEnabled = !tag.contains("WorkingEnabled") || tag.getBoolean("WorkingEnabled");
        targetPos = tag.contains(TARGET) ? BlockPos.of(tag.getLong(TARGET)) : null;
    }

    @Override
    public void addDisplayText(java.util.List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {
            textList.add(Component.translatable("pollution.machine.constellation_tower.display.constellation", definition.getEnglishName()));
            textList.add(Component.translatable("pollution.machine.constellation_tower.display.storage", energy, CAPACITY));
            textList.add(Component.translatable("pollution.machine.constellation_tower.display.generation", generation));
            textList.add(Component.translatable(targetPos == null
                    ? "pollution.starstream_network.status.unlinked" : "pollution.starstream_network.status.active"));
        }
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("CCCCC", "C   C", "C A C", "C C C", "C   C", "C   C", "CCCCC")
                .aisle("CCCCC", "C   C", "C A C", "C   C", "C   C", "C   C", "CCCCC")
                .aisle("CCCCC", "C   C", "C A C", "C S C", "C   C", "C   C", "CCCCC")
                .aisle("CCCCC", "C   C", "C A C", "C C C", "C   C", "C   C", "CCCCC")
                .aisle("CCCCC", "C   C", "C A C", "C C C", "C   C", "C   C", "CCCCC")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('C', Predicates.blocks(PollutionMagicBlocks.STARSTREAM_CASING.get()))
                .where('A', Predicates.blocks(PollutionMagicBlocks.CONSTELLATION_ANCHOR.get()))
                .where(' ', Predicates.any())
                .build();
    }
}
