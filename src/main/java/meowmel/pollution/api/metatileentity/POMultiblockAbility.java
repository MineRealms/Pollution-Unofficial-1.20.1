package meowmel.pollution.api.metatileentity;

import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;

/**
 * Custom multiblock part abilities of the Pollution port.
 *
 * <p>Modern GregTech uses {@link PartAbility} only for structure-pattern
 * matching ({@code ability.isApplicable(block)}); the block-to-ability
 * registration happens automatically through
 * {@code MachineBuilder#abilities(...)}. Typed access to a part (vis buffer,
 * infused tank, ...) is done by filtering the controller's part list with the
 * corresponding interface from {@code meowmel.pollution.api.capability}.</p>
 */
public final class POMultiblockAbility {

    /** Vis hatch: see {@code IVisHatch}. */
    public static final PartAbility VIS_HATCH = new PartAbility("pollution_vis_hatch");

    /** Infused fluid hatch: fluid buffer for magic multiblocks. */
    public static final PartAbility INFUSED_FLUID_HATCH = new PartAbility("pollution_infused_fluid_hatch");

    private POMultiblockAbility() {}
}
