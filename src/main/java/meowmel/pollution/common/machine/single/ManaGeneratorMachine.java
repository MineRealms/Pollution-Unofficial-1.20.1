package meowmel.pollution.common.machine.single;

import com.gregtechceu.gtceu.api.GTValues;
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
 *       {@code V[tier] * 64} buffer and emitter behaviour. The port now
 *       registers the tiered {@code mana_gen_recipes} entries
 *       ({@code BotaniaRecipes#manaGenRecipes}); the machine reads its own
 *       tier's entry rate ({@code V[tier]}) and caps its mana intake to that
 *       rate per tick, which is the same 1 mana = 1 EU conversion upstream
 *       applied unconditionally.</li>
 *   <li>1.12 Botania detected receivers with {@code instanceof IManaReceiver};
 *       the port implements Botania's {@link ManaReceiver} and is exposed
 *       through the {@code MANA_RECEIVER} capability by
 *       {@code ManaHatchCapabilityEvents}, so mana bursts and sparks can charge
 *       it.</li>
 * </ul>
 */
public class ManaGeneratorMachine extends PollutionEnergyMachine implements IManaHatch, ManaReceiver {

    private long manaIntakeThisTick;

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
        // by the container's side output condition. Reset the per-tick intake
        // budget granted by the mana_gen_recipes entry of this tier.
        manaIntakeThisTick = 0L;
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
        if (mana <= 0L || isFull()) {
            return;
        }
        // Per-tick intake rate from the tier's mana_gen_recipes entry (V[tier]).
        long rate = GTValues.V[Math.min(getTier(), GTValues.V.length - 1)];
        long remaining = Math.max(0L, rate - manaIntakeThisTick);
        if (remaining <= 0L) {
            return;
        }
        long accepted = Math.min(mana, remaining);
        energyContainer.addEnergy(accepted);
        manaIntakeThisTick += accepted;
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
