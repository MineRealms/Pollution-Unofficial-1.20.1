package meowmel.pollution.api.capability;

/**
 * Vis buffer exposed by the vis hatch multiblock part.
 *
 * <p>Ported from upstream {@code meowmel.pollution.api.capability.IVisHatch}.
 * A hatch slowly drains centivis from the Thaumcraft 4R network and stores it
 * for magic multiblocks to consume.</p>
 */
public interface IVisHatch {

    int getTier();

    int getVisStore();

    int getMaxVisStore();

    boolean drainVis(int amount, boolean simulate);
}
