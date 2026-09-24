package meowmel.pollution.common.starstream;

import meowmel.pollution.Pollution;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Own registry for the network endpoints; keeps Starstream independent of GT machine registration. */
public final class StarstreamBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Pollution.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Pollution.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Pollution.MOD_ID);
    private static RegistryObject<Block> block(String id, StarstreamBlockEntity.Kind kind, MapColor color) {
        RegistryObject<Block> entry = BLOCKS.register(id, () -> new StarstreamBlock(BlockBehaviour.Properties.of().mapColor(color)
                .strength(8, 80).requiresCorrectToolForDrops(), kind));
        ITEMS.register(id, () -> new BlockItem(entry.get(), new Item.Properties()));
        return entry;
    }
    public static final RegistryObject<Block> OPERATION_CORE = block("starstream_operation_core", StarstreamBlockEntity.Kind.OPERATION, MapColor.COLOR_PURPLE);
    public static final RegistryObject<Block> RELAY = block("starstream_relay", StarstreamBlockEntity.Kind.RELAY, MapColor.COLOR_BLUE);
    public static final RegistryObject<Block> INTERDIMENSIONAL_RELAY = block("starstream_interdimensional_relay", StarstreamBlockEntity.Kind.GATEWAY, MapColor.COLOR_CYAN);
    public static final RegistryObject<Block> CHUNK_ANCHOR = block("starstream_chunk_anchor", StarstreamBlockEntity.Kind.ANCHOR, MapColor.COLOR_LIGHT_BLUE);
    public static final RegistryObject<Block> NEXUS_CORE = block("starstream_nexus_core", StarstreamBlockEntity.Kind.CORE, MapColor.COLOR_PURPLE);
    public static final RegistryObject<BlockEntityType<StarstreamBlockEntity>> BLOCK_ENTITY = BLOCK_ENTITIES.register(
            "starstream_network", () -> BlockEntityType.Builder.of(StarstreamBlockEntity::new,
                    OPERATION_CORE.get(), RELAY.get(), INTERDIMENSIONAL_RELAY.get(), CHUNK_ANCHOR.get(), NEXUS_CORE.get()).build(null));
    public static void init(IEventBus bus) { BLOCKS.register(bus); ITEMS.register(bus); BLOCK_ENTITIES.register(bus); }
    private StarstreamBlocks() {}
}
