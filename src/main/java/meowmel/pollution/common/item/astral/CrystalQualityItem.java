package meowmel.pollution.common.item.astral;

import meowmel.pollution.api.astral.AstralCrystalNbtHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Tooltip carrier for rock crystal seeds, embryos and cultivated crystals:
 * port of upstream {@code CrystalQualityBehavior}.
 *
 * <p>Reads the quality data owned by {@link AstralCrystalNbtHelper}, which the
 * port keeps as the serialized contract for the industrial crystal chain.</p>
 */
public class CrystalQualityItem extends Item {

    public CrystalQualityItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int purity = AstralCrystalNbtHelper.getPurity(stack);
        int stability = AstralCrystalNbtHelper.getStability(stack);
        if (purity <= 0 && stability <= 0) {
            tooltip.add(Component.translatable("pollution.crystal_quality.unselected")
                    .withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        tooltip.add(Component.translatable("pollution.crystal_quality.purity", purity)
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("pollution.crystal_quality.stability", stability)
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        if (AstralCrystalNbtHelper.isCrystalEmbryo(stack)) {
            tooltip.add(Component.translatable("pollution.crystal_quality.embryo")
                    .withStyle(ChatFormatting.BLUE));
        } else if (AstralCrystalNbtHelper.isCultivatedCrystal(stack)) {
            tooltip.add(Component.translatable("pollution.crystal_quality.cultivated")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("pollution.crystal_quality.grade",
                            AstralCrystalNbtHelper.getCultivationGrade(stack),
                            AstralCrystalNbtHelper.getOpticalQuality(stack))
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }
}
