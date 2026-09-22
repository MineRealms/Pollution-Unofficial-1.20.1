package meowmel.pollution.common.machine.multiblock.node;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import meowmel.pollution.common.item.PackagedAuraNode;
import meowmel.pollution.common.item.PollutionItems;
import meowmel.pollution.common.machine.multiblock.AbstractDisplayMultiblockMachine;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;
import java.util.Random;

/**
 * Node producer: packs a new aura node every few seconds, consuming EU and
 * infused energy. Node tiers/types/probabilities follow the upstream tables.
 *
 * <p>Upstream placed the node item variants into a GT MetaItem; the port uses
 * the plain {@code packaged_aura_node} item with the same NBT contract.</p>
 */
public class NodeProducerMachine extends AbstractDisplayMultiblockMachine {

    private final Random random = new Random();

    private TickableSubscription tickSubscription;
    private int timer;
    private int euTier = 1;
    private int duration = 30;
    private int infusedCost = 144;

    public NodeProducerMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            tickSubscription = subscribeServerTick(this::tickProducer);
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

    private void tickProducer() {
        if (!(getLevel() instanceof ServerLevel) || !isFormed()) {
            return;
        }
        EnergyHatchPartMachine energy = findPart(EnergyHatchPartMachine.class);
        FluidHatchPartMachine fluids = findPart(FluidHatchPartMachine.class);
        ItemBusPartMachine output = findPart(ItemBusPartMachine.class);
        if (energy == null || fluids == null || output == null) {
            return;
        }

        long voltage = Math.max(1, energy.energyContainer.getInputVoltage());
        euTier = Math.max(1, (int) Math.ceil(Math.log((double) voltage / 32) / Math.log(4) + 1));
        if (euTier <= 3) {
            return;
        }
        duration = Math.max(1, (int) Math.ceil(30.0F / (euTier - 3)));
        infusedCost = 144 * (1 << Math.max(0, euTier - 4));

        if (energy.energyContainer.getEnergyStored() < voltage) {
            return;
        }

        FluidStack infusedEnergy = PollutionMaterials.InfusedEnergy.getFluid(infusedCost);
        if (!infusedEnergy.isEmpty()
                && fluids.tank.drain(infusedEnergy, IFluidHandler.FluidAction.SIMULATE).getAmount() < infusedCost) {
            return;
        }

        ItemStack preview = createRandomNode();
        if (!ItemHandlerHelper.insertItemStacked(output.getInventory().storage, preview, true).isEmpty()) {
            timer = 0;
        } else {
            energy.energyContainer.changeEnergy(-voltage);
            if (!infusedEnergy.isEmpty()) {
                fluids.tank.drain(infusedEnergy, IFluidHandler.FluidAction.EXECUTE);
            }
        }

        if (getOffsetTimer() % 20 == 0) {
            timer++;
            if (timer >= duration) {
                timer = 0;
                ItemHandlerHelper.insertItemStacked(output.getInventory().storage, createRandomNode(), false);
            }
        }
    }

    private ItemStack createRandomNode() {
        ItemStack node = new ItemStack(PollutionItems.PACKAGED_AURA_NODE.get());
        double tierProbability = random.nextDouble() * 100;
        double typeProbability = random.nextDouble() * 100;

        String tier;
        if (tierProbability < 60) {
            tier = "Normal";
        } else if (tierProbability < 80) {
            tier = "Withering";
        } else if (tierProbability < 85) {
            tier = "Bright";
        } else {
            tier = "Pale";
        }

        String type;
        if (typeProbability < 60) {
            type = "Standard";
        } else if (typeProbability < 70) {
            type = "Ominous";
        } else if (typeProbability < 80) {
            type = "Pure";
        } else if (typeProbability < 95) {
            type = "Concussive";
        } else {
            type = "Voracious";
        }

        node.getOrCreateTag().putString(PackagedAuraNode.TAG_TIER, tier);
        node.getOrCreateTag().putString(PackagedAuraNode.TAG_TYPE, type);
        int coilLevel = hasCoil() ? Math.max(1, getCoilLevel()) : 1;
        PackagedAuraNode.setEssence(node, PackagedAuraNode.ESSENCE_AIR, gaussian(1.0));
        PackagedAuraNode.setEssence(node, PackagedAuraNode.ESSENCE_FIRE, gaussian(0.9 + 0.1 * coilLevel));
        PackagedAuraNode.setEssence(node, PackagedAuraNode.ESSENCE_WATER, gaussian(1.0));
        PackagedAuraNode.setEssence(node, PackagedAuraNode.ESSENCE_EARTH, gaussian(1.0));
        PackagedAuraNode.setEssence(node, PackagedAuraNode.ESSENCE_ORDER, gaussian(0.6 + 0.1 * euTier));
        PackagedAuraNode.setEssence(node, PackagedAuraNode.ESSENCE_ENTROPY, gaussian(1.0));
        return node;
    }

    private int gaussian(double bound) {
        return Math.min(Math.abs((int) Math.ceil(random.nextGaussian() * 100 * bound)), 1000);
    }

    /** Coil level of the structure; 0 when no GregTech heating coils are present. */
    private int getCoilLevel() {
        var coil = getMultiblockState().getMatchContext().get("CoilType");
        if (coil instanceof com.gregtechceu.gtceu.api.block.ICoilType coilType) {
            return coilType.getLevel();
        }
        return 0;
    }

    private boolean hasCoil() {
        return getCoilLevel() > 0;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {
            textList.add(Component.translatable("pollution.machine.node_producer_duration",
                    timer + " / " + duration + " t"));
            textList.add(Component.translatable("pollution.machine.node_producer_infusedcost",
                    infusedCost + " mB"));
        }
    }

    private <T> T findPart(Class<T> type) {
        for (IMultiPart part : getParts()) {
            if (type.isInstance(part.self())) {
                return type.cast(part.self());
            }
        }
        return null;
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("                    ", "                    ", "                    ", "                    ",
                        "                    ", "                    ", "                    ", "      A     A       ",
                        "      B     B       ", "      B     B       ", "      B     B       ", "      A     A       ",
                        "                    ", "                    ", "                    ", "                    ")
                .aisle("    CCCCCCCCCCC     ", "    BBBBBBBBBBB     ", "    BBBBBBBBBBB     ", "    CCCCCCCCCCC     ",
                        "                    ", "      B     B       ", "      D     E       ", "     AAA   AAA      ",
                        "     B B   B B      ", "     B B   B B      ", "     B B   B B      ", "     AAA   AAA      ",
                        "      A     A       ", "      D     E       ", "                    ", "                    ")
                .aisle("   CFFFFFFFFFFFCGFCF", "   CGGGGGGGGGGGC  B ", "   C           C  B ", "   C           C  B ",
                        "   C  B     B  C  B ", "   C           C  B ", "   C           C  B ", "   C  A     A  C GAG",
                        "      B     B     A ", "      B     B       ", "      B     B       ", "      A     A       ",
                        "                    ", "                    ", "                    ", "                    ")
                .aisle("  CFAAAAAAAAAAACGCCC", "   AGAAAAAAAAAGA BHB", "   A           A BHB", "   A  B     B  A BHB",
                        "   A           A BHB", "   A           A BHB", "   A           A BHB", "  GA           AGAAA",
                        "                 AAA", "                  A ", "                  H ", "                    ",
                        "                    ", "                    ", "                    ", "                    ")
                .aisle(" CFAAAAAAAAAAAACGFCF", "  GFGAIFFIFFIAGA  B ", "  GF  B     B  B  B ", "  GF           B  B ",
                        "  GA           B  B ", "  GA           B  B ", "  GA           A  B ", " GAFA         AFAGAG",
                        "   B           B  A ", "   B           B    ", "   B           B    ", "   B           B    ",
                        "   B           B    ", "   A           A    ", "                    ", "                    ")
                .aisle("XFAAAAAAAAAAAAACG F ", "  BGGAFIEIEIFAGA    ", "  F            B    ", "  F            B    ",
                        "  A            B    ", "  A            B    ", "  A            A    ", " AFHFA       AFHFAG ",
                        "  B B         B B   ", "  B B         B B   ", "  B B         B B   ", "  B B         B B   ",
                        "  B B         B B   ", "  AFA         AAA   ", "   A           A    ", "   H           H    ")
                .aisle("XFAAAAAAAAAAAAACGFCF", " FBGGAFEIIIEFAGA  B ", " BB      A     B  B ", "  H            B  B ",
                        "  A            B  B ", "  H            B  B ", "  B            A  B ", " BAFA         AFAGAG",
                        "   B           B  A ", "   B           B    ", "   B           B    ", "   B           B    ",
                        "   B           B    ", "   A           A    ", "                    ", "                    ")
                .aisle("XFAAAAAAAAAAAAACGCCC", " SBGGAIIIIIIIAGA BHB", " FB     AOA    B BHB", "  A     B B    B BHB",
                        "  H    B   B   B BHB", "  A   B     B  B BHB", "  B  B       B A BHB", " BBAB         BAGAAA",
                        "                 AAA", "                  A ", "                  H ", "                    ",
                        "                    ", "                    ", "                    ", "                    ")
                .aisle("XFAAAAAAAAAAAAACGFCF", " FBGGAFEIIIEFAGA  B ", " BB      A     B  B ", "  H            B  B ",
                        "  A            B  B ", "  H            B  B ", "  B            A  B ", " BAFA         AFAGAG",
                        "   B           B  A ", "   B           B    ", "   B           B    ", "   B           B    ",
                        "   B           B    ", "   A           A    ", "                    ", "                    ")
                .aisle("XFAAAAAAAAAAAAACG F ", "  BGGAFIEIEIFAGA    ", "  F            B    ", "  F            B    ",
                        "  A            B    ", "  A            B    ", "  A            A    ", " AFHFA       AFHFAG ",
                        "  B B         B B   ", "  B B         B B   ", "  B B         B B   ", "  B B         B B   ",
                        "  B B         B     ", "  AFA         AAA   ", "   A           A    ", "   H           H    ")
                .aisle(" CFAAAAAAAAAAAACGFCF", "  GFGAIFFIFFIAGA  B ", "  GF  B     B  B  B ", "  GF           B  B ",
                        "  GA           B  B ", "  GA           B  B ", "  GA           A  B ", " GAFA         AFAGAG",
                        "   B           B  A ", "   B           B    ", "   B           B    ", "   B           B    ",
                        "   B           B    ", "   A           A    ", "                    ", "                    ")
                .aisle("  CFAAAAAAAAAAACGCCC", "   AGAAAAAAAAAGA BHB", "   A           A BHB", "   A  B     B  A BHB",
                        "   A           A BHB", "   A           A BHB", "   A           A BHB", "  GA           AGAAA",
                        "                 AAA", "                  A ", "                  H ", "                    ",
                        "                    ", "                    ", "                    ", "                    ")
                .aisle("   CFFFFFFFFFFFCGFCF", "   CGGGGGGGGGGGC  B ", "   C           C  B ", "   C           C  B ",
                        "   C  B     B  C  B ", "   C           C  B ", "   C           C  B ", "   C  A     A  C GAG",
                        "      B     B     A ", "      B     B       ", "      B     B       ", "      A     A       ",
                        "                    ", "                    ", "                    ", "                    ")
                .aisle("    CCCCCCCCCCC     ", "    BBBBBBBBBBB     ", "    BBBBBBBBBBB     ", "    CCCCCCCCCCC     ",
                        "                    ", "      B     B       ", "      K     L       ", "     AAA   AAA      ",
                        "     B B   B B      ", "     B B   B B      ", "     B B   B B      ", "     AAA   AAA      ",
                        "      A     A       ", "      K     L       ", "                    ", "                    ")
                .aisle("                    ", "                    ", "                    ", "                    ",
                        "                    ", "                    ", "                    ", "      A     A       ",
                        "      B     B       ", "      B     B       ", "      B     B       ", "      A     A       ",
                        "                    ", "                    ", "                    ", "                    ")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('X', Predicates.blocks(GTBlocks.FUSION_CASING.get())
                        .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setExactLimit(1))
                        .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setExactLimit(1))
                        .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                .where('O', Predicates.abilities(PartAbility.EXPORT_ITEMS).setExactLimit(1))
                .where('I', Predicates.heatingCoils())
                .where('A', Predicates.blocks(GTBlocks.FUSION_CASING.get()))
                .where('B', Predicates.blocks(PollutionMagicBlocks.LAMINATED_GLASS.get()))
                .where('C', Predicates.blocks(PollutionMagicBlocks.VOID_PRISM.get()))
                .where('D', Predicates.blocks(PollutionMagicBlocks.BEAM_CORE_0.get()))
                .where('E', Predicates.blocks(PollutionMagicBlocks.BEAM_CORE_1.get()))
                .where('F', Predicates.blocks(PollutionMagicBlocks.SPELL_PRISM.get()))
                .where('G', Predicates.blocks(PollutionMagicBlocks.TUNGSTENSTEEL_GEARBOX.get()))
                .where('H', Predicates.blocks(PollutionMagicBlocks.BEAM_CORE_4.get()))
                .where('K', Predicates.blocks(PollutionMagicBlocks.BEAM_CORE_2.get()))
                .where('L', Predicates.blocks(PollutionMagicBlocks.BEAM_CORE_3.get()))
                .where(' ', Predicates.any())
                .build();
    }
}
