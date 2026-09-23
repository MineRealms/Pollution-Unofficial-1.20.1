package meowmel.pollution.common.machine.multiblock.mana;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import meowmel.pollution.common.machine.multiblock.MagicMultiblockController;

/**
 * Base controller of the Botania mana multiblocks.
 *
 * <p>Upstream origin: {@code ManaMultiblockController} plus the mana handling
 * of {@code MagicRecipeMapMultiblockController} (1.12.2). Upstream cached one
 * input pool hatch and merged mana energy hatches into the controller's
 * {@code EnergyContainerList}; the port collects every part that implements
 * {@link meowmel.pollution.api.capability.IManaHatch} into a
 * {@link meowmel.pollution.api.capability.ManaHandlerList} after the structure
 * forms, and mana energy hatches additionally feed EU recipes through
 * GregTech's own energy recipe capability.</p>
 *
 * <p>Since the 2026-09-23 resource-system completion, the parent
 * {@link MagicMultiblockController} owns the mana / life essence / astral
 * lookups, the requirement checks and the {@code consumeMana} draw, so this
 * class no longer duplicates them. It remains as the shared Botania base (and
 * as the type hook for Botania-only behaviour when a ported machine needs it).</p>
 */
public abstract class ManaMultiblockController extends MagicMultiblockController {

    protected ManaMultiblockController(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }
}
