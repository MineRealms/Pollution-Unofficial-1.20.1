package meowmel.pollution.dimension.worldgen;

import meowmel.pollution.Pollution;
import meowmel.pollution.dimension.worldgen.structure.UndergroundBridgePiece;
import meowmel.pollution.dimension.worldgen.structure.UndergroundBridgeStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Code-side worldgen registrations for the ported 1.12 fortress structure
 * ({@code POStructureManager}/{@code MapGenUndergroundBridge}), the custom
 * garden feature and the GTCEu ore veins.
 */
public final class PollutionStructures {

    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, Pollution.MOD_ID);

    public static final DeferredRegister<StructurePieceType> PIECE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, Pollution.MOD_ID);

    public static final RegistryObject<StructureType<UndergroundBridgeStructure>> UNDERGROUND_BRIDGE =
            STRUCTURE_TYPES.register("underground_bridge", () -> () -> UndergroundBridgeStructure.CODEC);

    public static final RegistryObject<StructurePieceType> UNDERGROUND_BRIDGE_PIECE =
            PIECE_TYPES.register("underground_bridge", () -> UndergroundBridgePiece::new);

    private PollutionStructures() {
    }

    public static void init(FMLJavaModLoadingContext context) {
        STRUCTURE_TYPES.register(context.getModEventBus());
        PIECE_TYPES.register(context.getModEventBus());
        PollutionFeatures.init(context);
        PollutionOreVeins.init(context);
    }
}
