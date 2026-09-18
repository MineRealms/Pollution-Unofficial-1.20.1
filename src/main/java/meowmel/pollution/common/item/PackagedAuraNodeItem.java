package meowmel.pollution.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Packaged aura node: tooltip port of upstream {@code PollutionMetaItem1#addInformation}.
 *
 * <p>Upstream printed every NBT entry of the node stack as {@code key: value}.
 * The port keeps the same contract through {@link PackagedAuraNode}; the six
 * essence keys and the tier/type strings are shown in a deterministic order so
 * the tooltip is stable.</p>
 */
public class PackagedAuraNodeItem extends Item {

    public PackagedAuraNodeItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        if (tag == null || tag.isEmpty()) {
            return;
        }
        tooltip.add(Component.translatable("pollution.item.packaged_aura_node.header").withStyle(ChatFormatting.GRAY));
        if (tag.contains(PackagedAuraNode.TAG_TIER)) {
            tooltip.add(line(PackagedAuraNode.TAG_TIER, tag.getString(PackagedAuraNode.TAG_TIER)));
        }
        if (tag.contains(PackagedAuraNode.TAG_TYPE)) {
            tooltip.add(line(PackagedAuraNode.TAG_TYPE, tag.getString(PackagedAuraNode.TAG_TYPE)));
        }
        for (String key : new String[]{
                PackagedAuraNode.ESSENCE_AIR, PackagedAuraNode.ESSENCE_FIRE, PackagedAuraNode.ESSENCE_WATER,
                PackagedAuraNode.ESSENCE_EARTH, PackagedAuraNode.ESSENCE_ORDER, PackagedAuraNode.ESSENCE_ENTROPY}) {
            if (tag.contains(key)) {
                tooltip.add(line(key, Integer.toString(tag.getInt(key))));
            }
        }
    }

    private static Component line(String key, String value) {
        return Component.literal(key + ": " + value).withStyle(ChatFormatting.GRAY);
    }
}
