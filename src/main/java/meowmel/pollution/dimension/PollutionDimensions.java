package meowmel.pollution.dimension;

import meowmel.pollution.Pollution;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * Registry keys used by portals and dimension data.
 * <p>Underground and Alfheim have custom generators and biome maps bound to the world
 * seed. Serialized legacy biome maps retain their old seed semantics. Fog/sky colors
 * are biome effects; Alfheim's client effects restore the 164-block cloud height and
 * Botania Garden of Glass sky. Demiplane keeps overworld terrain, as upstream.</p>
 * <p>Blood retains its existing datapack terrain and is excluded from this port pass.</p>
 */
public final class PollutionDimensions {

    /** Upstream id 43, {@code AlfheimWorld}. */
    public static final ResourceKey<Level> ALFHEIM = key("alfheim");

    /** Upstream id 42, {@code BloodWorld}. */
    public static final ResourceKey<Level> BLOOD = key("blood");

    /** Upstream id 40, {@code DimensionDemiplane}. */
    public static final ResourceKey<Level> DEMIPLANE = key("demiplane");

    /** Upstream id 41, {@code UndergroundWorlds}; first registered dimension. */
    public static final ResourceKey<Level> UNDERGROUND = key("underground");

    private static ResourceKey<Level> key(String name) {
        return ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, name));
    }

    private PollutionDimensions() {}
}
