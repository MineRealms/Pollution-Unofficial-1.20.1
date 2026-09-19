package meowmel.pollution.common.machine.part.mana;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import meowmel.pollution.api.capability.IManaHatch;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.ManaReceiver;

/**
 * Neighbour mana transfer for the mana hatches.
 *
 * <p>Upstream ({@code MetaTileEntityManaHatch#update}) tested
 * {@code tileEntity instanceof IManaReceiver} and additionally special-cased
 * neighbouring Pollution hatches. Botania 1.20.1 exposes mana receivers
 * through {@link BotaniaForgeCapabilities#MANA_RECEIVER}, so the port checks
 * for a neighbouring {@link IManaHatch} first (the uncapped internal path) and
 * falls back to the capability for Botania blocks.</p>
 *
 * <p>Deviation: the accepted amount is measured before / after the transfer
 * instead of removing the requested amount blindly like upstream did, so a
 * partial or throttled acceptance cannot destroy mana.</p>
 */
final class ManaReceiverLookup {

    static long pushMana(Level level, BlockPos pos, Direction side, long amount) {
        if (amount <= 0L) return 0L;
        if (MetaMachine.getMachine(level, pos) instanceof IManaHatch hatch
                && acceptsInternalMana(hatch)) {
            long before = hatch.getMana();
            hatch.receiveMana(amount);
            return Math.max(0L, hatch.getMana() - before);
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) return 0L;
        ManaReceiver receiver = blockEntity
                .getCapability(BotaniaForgeCapabilities.MANA_RECEIVER, side.getOpposite())
                .orElse(null);
        if (receiver == null || receiver.isFull()) return 0L;
        int before = receiver.getCurrentMana();
        receiver.receiveMana((int) Math.min(amount, Integer.MAX_VALUE));
        return Math.max(0L, (long) receiver.getCurrentMana() - before);
    }

    /**
     * Upstream only pushed into input hatches ({@code !isExportHatch} /
     * {@code !isExport}); without this guard an output hatch facing another
     * output hatch would fill it instead of a receiver.
     */
    private static boolean acceptsInternalMana(IManaHatch hatch) {
        if (hatch instanceof ManaHatchMachine manaHatch) {
            return !manaHatch.isExportHatch;
        }
        if (hatch instanceof ManaPoolHatchMachine poolHatch) {
            return !poolHatch.isExport;
        }
        return true;
    }

    private ManaReceiverLookup() {}
}
