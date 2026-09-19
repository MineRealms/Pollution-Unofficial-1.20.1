package meowmel.pollution.common.machine.multiblock.mana;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import meowmel.pollution.api.capability.IManaHatch;
import meowmel.pollution.api.capability.ManaHandlerList;
import meowmel.pollution.api.recipes.properties.MagicRecipeProperties;
import meowmel.pollution.common.machine.multiblock.MagicMultiblockController;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Base controller of the Botania mana multiblocks.
 *
 * <p>Upstream origin: {@code ManaMultiblockController} plus the mana handling
 * of {@code MagicRecipeMapMultiblockController} (1.12.2). Upstream cached one
 * input pool hatch and merged mana energy hatches into the controller's
 * {@code EnergyContainerList}; the modern port collects every part that
 * implements {@link IManaHatch} into a {@link ManaHandlerList} after the
 * structure forms, and mana energy hatches additionally feed EU recipes through
 * GregTech's own energy recipe capability.</p>
 *
 * <p>Deviation: the parent {@link MagicMultiblockController} still rejects any
 * recipe with a mana cost (mana was reserved while this layer was unported).
 * This class overrides the requirement check so mana recipes pass once a mana
 * hatch is present; the remaining checks are repeated here because the parent
 * method cannot be asked to skip only the mana line.</p>
 */
public abstract class ManaMultiblockController extends MagicMultiblockController {

    private ManaHandlerList manaHandler = new ManaHandlerList(List.of());

    protected ManaMultiblockController(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
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

    @Override
    public boolean consumeMana(long amount, boolean simulate) {
        return amount <= 0L || manaHandler.consumeMana(amount, simulate);
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

    @Override
    public boolean checkMagicRequirements(GTRecipe recipe) {
        if (MagicRecipeProperties.getManaPerTick(recipe) <= 0L) {
            return super.checkMagicRequirements(recipe);
        }
        if (manaHandler.isEmpty()) {
            return false;
        }
        if (MagicRecipeProperties.getLifeEssencePerTick(recipe) > 0) {
            return false;
        }
        if (MagicRecipeProperties.hasVisCost(recipe) && visHatch == null) {
            return false;
        }
        if (MagicRecipeProperties.getInfusedFluidPerTick(recipe) > 0 && infusedFluidHatch == null) {
            return false;
        }
        if (!MagicRecipeProperties.getTarot(recipe).isEmpty()) {
            return false;
        }
        return !recipe.data.contains(MagicRecipeProperties.ASTRAL_CONDITION);
    }

    // ////////////////////////////////////
    // ***** UI *****//
    // ////////////////////////////////////

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {
            textList.add(Component.translatable("pollution.machine.mana_plate.tier",
                    getManaHandler().getTier(), getMana(), getMaxMana()));
        }
    }
}
