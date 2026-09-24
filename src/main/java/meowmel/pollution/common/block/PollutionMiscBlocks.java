package meowmel.pollution.common.block;

import com.tterrag.registrate.util.entry.BlockEntry;
import meowmel.pollution.Pollution;
import meowmel.pollution.common.block.tile.FleshHeartBlock;
import meowmel.pollution.common.block.tile.FleshHeartBlockEntity;
import meowmel.pollution.common.block.tile.MineralExtractorBlock;
import meowmel.pollution.common.block.tile.MineralExtractorBlockEntity;
import meowmel.pollution.common.block.tile.PortalBlock;
import meowmel.pollution.common.item.MineralExtractorBlockItem;
import meowmel.pollution.compat.gtceu.PollutionGTAddon;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

/**
 * Miscellaneous Pollution blocks and their block entities.
 *
 * <p>Registers the 1.12 {@code PollutionBlocksInit} portal and flesh heart plus
 * the separately registered mineral extractor: {@code pollution:portal},
 * {@code pollution:flesh_heart} and {@code pollution:mineral_extractor}. The
 * blocks are registrate entries on the shared Pollution GT registrate like the
 * casing and plant batches; the block entity types use a plain Forge
 * {@link DeferredRegister} and are bound to the mod bus from
 * {@link #init(FMLJavaModLoadingContext)}.</p>
 *
 * <p>Deviations from upstream: {@code BlockPOPortal} had no item, so
 * {@code pollution:portal} has none either; the flesh heart and mineral
 * extractor keep their upstream block items.</p>
 */
public final class PollutionMiscBlocks {

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Pollution.MOD_ID);

    // ////////////////////////////////////
    // ***** blocks *****//
    // ////////////////////////////////////

    public static final BlockEntry<PortalBlock> PORTAL = PollutionGTAddon.REGISTRATE
            .block("portal", PortalBlock::new)
            .blockstate((context, provider) -> {
            })
            .properties(properties -> properties
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(-1.0F, 3600000.0F)
                    .sound(SoundType.GLASS)
                    .lightLevel(state -> 11)
                    .noOcclusion())
            .register();

    public static final BlockEntry<FleshHeartBlock> FLESH_HEART = PollutionGTAddon.REGISTRATE
            .block("flesh_heart", FleshHeartBlock::new)
            .blockstate((context, provider) -> {
            })
            .properties(properties -> properties
                    .mapColor(MapColor.COLOR_RED)
                    .strength(5.0F, 10.0F)
                    .sound(SoundType.SLIME_BLOCK)
                    .lightLevel(state -> 4))
            .simpleItem()
            .register();

    /** Upstream {@code BlockFlesh}: soft meat block, trunk/body material of the flesh tree. */
    public static final BlockEntry<Block> FLESH_BLOCK = PollutionGTAddon.REGISTRATE
            .block("flesh_block", Block::new)
            .blockstate((context, provider) -> {
            })
            .properties(properties -> properties
                    .mapColor(MapColor.COLOR_RED)
                    .strength(1.5F, 2.0F)
                    .sound(SoundType.SLIME_BLOCK))
            .simpleItem()
            .register();

    public static final BlockEntry<MineralExtractorBlock> MINERAL_EXTRACTOR = PollutionGTAddon.REGISTRATE
            .block("mineral_extractor", MineralExtractorBlock::new)
            .blockstate((context, provider) -> {
            })
            .properties(properties -> properties
                    .mapColor(MapColor.METAL)
                    .strength(4.0F, 12.0F)
                    .sound(SoundType.METAL)
                    .lightLevel(state -> 10)
                    .requiresCorrectToolForDrops())
            .item(MineralExtractorBlockItem::new)
            .model((context, provider) -> {
                // Keep the hand-authored builtin/entity model and its transforms.
            })
            .build()
            .register();

    // ////////////////////////////////////
    // ***** block entities *****//
    // ////////////////////////////////////

    public static final RegistryObject<BlockEntityType<FleshHeartBlockEntity>> FLESH_HEART_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("flesh_heart",
                    () -> BlockEntityType.Builder
                            .of(FleshHeartBlockEntity::new, FLESH_HEART.get())
                            .build(null));

    public static final RegistryObject<BlockEntityType<MineralExtractorBlockEntity>> MINERAL_EXTRACTOR_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("mineral_extractor",
                    () -> BlockEntityType.Builder
                            .of(MineralExtractorBlockEntity::new, MINERAL_EXTRACTOR.get())
                            .build(null));

    /** Registration names, used for the language keys. */
    public static final List<String> ALL_NAMES = List.of("portal", "flesh_heart", "flesh_block", "mineral_extractor");

    /** "flesh_heart" -&gt; "Flesh Heart". */
    public static String displayName(String name) {
        StringBuilder display = new StringBuilder();
        for (String part : name.split("_")) {
            if (display.length() > 0) {
                display.append(' ');
            }
            display.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return display.toString();
    }

    /** Registers the block entity types on the mod bus and forces block class init. */
    public static void init(FMLJavaModLoadingContext context) {
        BLOCK_ENTITY_TYPES.register(context.getModEventBus());
    }

    private PollutionMiscBlocks() {}
}
