package meowmel.pollution.common.item;

import com.tterrag.registrate.util.entry.ItemEntry;
import meowmel.pollution.compat.gtceu.PollutionGTAddon;
import net.minecraft.world.item.Item;

/**
 * Items of the Pollution port.
 *
 * <p>The 1.12 mod used GregTech's MetaItem system; the port registers plain
 * items and carries the upstream NBT contract on them (see
 * {@link PackagedAuraNode}). Models/textures are placeholders until the asset
 * pass.</p>
 */
public final class PollutionItems {

    /** Packaged aura node: the fuel/catalyst of the node machine family. */
    public static final ItemEntry<Item> PACKAGED_AURA_NODE = PollutionGTAddon.REGISTRATE
            .item("packaged_aura_node", Item::new)
            .lang("Packaged Aura Node")
            .register();

    /** Forces class initialisation from the mod constructor (own mod bus). */
    public static void init() {}

    private PollutionItems() {}
}
