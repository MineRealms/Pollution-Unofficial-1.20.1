package meowmel.pollution.common.item.astral;

import meowmel.pollution.api.amplification.MagicJeiHintResolver;
import meowmel.pollution.api.astral.AstralNbtHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Tooltip carrier for celestial calibration cores and constellation wafers:
 * port of upstream {@code ConstellationDataBehavior}.
 *
 * <p>Upstream resolved Astral Sorcery's {@code IConstellation}; the port is
 * id-based (see {@link AstralNbtHelper}), so the stored constellation id is
 * mapped through {@link MagicJeiHintResolver#constellationDisplayName(String)}
 * - the same stable display names the JEI amplification pages use - instead of
 * the native localized name. The function key and the "native NBT preserved"
 * note match upstream.</p>
 */
public class ConstellationDataItem extends Item {

    public ConstellationDataItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        String constellation = AstralNbtHelper.readConstellation(stack);
        if (constellation.isEmpty()) {
            tooltip.add(Component.translatable("pollution.astral_data.unattuned")
                    .withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        tooltip.add(Component.translatable("pollution.astral_data.constellation",
                        MagicJeiHintResolver.constellationDisplayName(constellation))
                .withStyle(ChatFormatting.AQUA));
        String function = stack.getOrCreateTag().getString(AstralNbtHelper.CELESTIAL_FUNCTION);
        if (!function.isEmpty()) {
            tooltip.add(Component.translatable("pollution.astral_data.function",
                            Component.translatable("pollution.astral_data.function." + function))
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        tooltip.add(Component.translatable("pollution.astral_data.nbt_preserved")
                .withStyle(ChatFormatting.GRAY));
    }
}
