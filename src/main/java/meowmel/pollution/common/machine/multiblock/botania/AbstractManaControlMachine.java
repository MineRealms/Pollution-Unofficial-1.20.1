package meowmel.pollution.common.machine.multiblock.botania;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import meowmel.pollution.api.capability.IManaHatch;
import meowmel.pollution.api.capability.ManaHandlerList;
import meowmel.pollution.common.machine.multiblock.AbstractDisplayMultiblockMachine;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Base of the passive Botania controllers (no recipe map of their own).
 *
 * <p>Upstream {@code MetaTileEntityManaPlate} and
 * {@code MetaTileEntityBotGasCollector} extended
 * {@code MetaTileEntityBaseWithControl} and kept their own
 * {@code ManaHandlerList} built from {@code getAbilities(MANA_INPUT_POOL)}.
 * The port keeps that shape: this base collects every part implementing
 * {@link IManaHatch} after the structure forms and exposes the same
 * {@code consumeMana} contract as the recipe-driven
 * {@code ManaMultiblockController}.</p>
 *
 * <p>The screen comes from {@link AbstractDisplayMultiblockMachine}; this base
 * adds the shared mana storage line to the display.</p>
 */
public abstract class AbstractManaControlMachine extends AbstractDisplayMultiblockMachine {

    private ManaHandlerList manaHandler = new ManaHandlerList(List.of());

    protected AbstractManaControlMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        List<IManaHatch> hatches = new ArrayList<>();
        for (IMultiPart part : getParts()) {
            if (part.self() instanceof IManaHatch hatch) {
                hatches.add(hatch);
            }
        }
        manaHandler = new ManaHandlerList(hatches);
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        manaHandler = new ManaHandlerList(List.of());
    }

    public ManaHandlerList getManaHandler() {
        return manaHandler;
    }

    public long getMana() {
        return manaHandler.getMana();
    }

    public long getMaxMana() {
        return manaHandler.getMaxMana();
    }

    public boolean consumeMana(long amount, boolean simulate) {
        return amount <= 0L || manaHandler.consumeMana(amount, simulate);
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {
            textList.add(Component.translatable("pollution.machine.mana_plate.tier",
                    getManaHandler().getTier(), getMana(), getMaxMana()));
        }
    }
}
