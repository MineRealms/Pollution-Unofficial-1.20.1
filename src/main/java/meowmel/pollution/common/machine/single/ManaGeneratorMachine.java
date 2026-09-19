package meowmel.pollution.common.machine.single;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import meowmel.pollution.api.capability.IManaHatch;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import vazkii.botania.api.mana.ManaReceiver;

import java.util.List;

/**
 * Mana generator: stores Botania mana as GregTech energy and emits it.
 *
 * <p>Upstream origin: {@code ManaGeneratorTileEntity} (1.12.2, single block,
 * registered as {@code mana_gen_lv}..{@code mana_gen_iv}). Upstream extended
 * {@code SimpleGeneratorMetaTileEntity} with the (empty) {@code mana_gen_recipes}
 * map, implemented {@code IManaHatch} and simply added every received mana to
 * its energy container; the front face was the only energy output side.</p>
 *
 * <p>Deviations:</p>
 * <ul>
 *   <li>GregTech CEu Modern has no {@code SimpleGeneratorMetaTileEntity}; the
 *       machine extends the port's {@link PollutionEnergyMachine} with
 *       {@code isEnergyEmitter() = true}, which keeps the upstream
 *       {@code V[tier] * 64} buffer and emitter behaviour. The recipe map is
 *       not referenced because upstream registered no recipes for it.</li>
 *   <li>1.12 Botania detected receivers with {@code instanceof IManaReceiver};
 *       the port implements Botania's {@link ManaReceiver} and is exposed
 *       through the {@code MANA_RECEIVER} capability by
 *       {@code ManaHatchCapabilityEvents}, so mana bursts and sparks can charge
 *       it.</li>
 * </ul>
 */
public class ManaGeneratorMachine extends PollutionEnergyMachine implements IManaHatch, ManaReceiver {

    public ManaGeneratorMachine(IMachineBlockEntity info, int tier) {
        super(info, tier);
    }

    @Override
    protected boolean isEnergyEmitter() {
        return true;
    }

    @Override
    protected void pollutionTick() {
        // Mana is stored directly in the energy container; emission is handled
        // by the container's side output condition.
    }

    // ////////////////////////////////////
    // ***** IManaHatch *****//
    // ////////////////////////////////////

    @Override
    public long getMaxMana() {
        return energyContainer.getEnergyCapacity();
    }

    @Override
    public long getMana() {
        return energyContainer.getEnergyStored();
    }

    @Override
    public boolean isFull() {
        return getMana() >= getMaxMana();
    }

    @Override
    public void receiveMana(long mana) {
        if (mana > 0L && !isFull()) {
            energyContainer.addEnergy(mana);
        }
    }

    /** A generator refuses to give mana back, exactly like upstream. */
    @Override
    public boolean consumeMana(long amount, boolean simulate) {
        return false;
    }

    // ////////////////////////////////////
    // ***** Botania ManaReceiver *****//
    // ////////////////////////////////////

    @Override
    public Level getManaReceiverLevel() {
        return getLevel();
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return getPos();
    }

    @Override
    public int getCurrentMana() {
        return (int) Math.min(getMana(), Integer.MAX_VALUE);
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return !isFull();
    }

    @Override
    public void receiveMana(int mana) {
        receiveMana((long) mana);
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        textList.add(Component.literal("Mana: " + getMana() + " / " + getMaxMana()));
    }
}
