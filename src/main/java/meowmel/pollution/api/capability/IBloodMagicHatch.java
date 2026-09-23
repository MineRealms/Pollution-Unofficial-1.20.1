package meowmel.pollution.api.capability;

/**
 * A Blood Magic orb backed source of life essence for a multiblock.
 *
 * <p>Upstream origin: {@code meowmel.pollution.api.capability.IBloodMagicHatch}
 * (1.12.2). The method set is unchanged. The port has no Blood Magic hatch
 * machine yet, so the magic controller discovers this interface from the formed
 * parts and fails the recipe requirement when none is installed (the same
 * behaviour upstream had before its hatch content landed).</p>
 */
public interface IBloodMagicHatch {

    int getLifeEssence();

    int getLifeEssenceCapacity();

    boolean consumeLifeEssence(int amount, boolean simulate);
}
