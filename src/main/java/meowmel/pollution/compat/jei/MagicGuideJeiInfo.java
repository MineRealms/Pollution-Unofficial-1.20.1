package meowmel.pollution.compat.jei;

import meowmel.pollution.common.item.PollutionItems;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Item information pages for the four static handbook pages from 1.12.
 *
 * <p>They are intentionally JEI item descriptions rather than executable GT
 * recipes: the celestial machines and Astral recipe maps are not part of the
 * 1.20.1 port. The final line on every celestial page says so explicitly.</p>
 */
public final class MagicGuideJeiInfo {

    private MagicGuideJeiInfo() {}

    public static void register(IRecipeRegistration registration) {
        add(registration, List.of(PollutionItems.ROCK_CRYSTAL_SEED.asStack()),
                "seed", 6);
        add(registration, List.of(PollutionItems.CELESTIAL_CRYSTAL_EMBRYO.asStack()),
                "embryo", 7);
        add(registration, List.of(PollutionItems.CONSTELLATION_DATA_WAFER.asStack()),
                "wafer", 20);
        add(registration, tarotStacks(), "tarot", 27);
    }

    private static void add(IRecipeRegistration registration, List<ItemStack> stacks,
                            String page, int lines) {
        List<Component> text = new ArrayList<>(lines);
        for (int i = 1; i <= lines; i++) {
            text.add(Component.translatable("pollution.magic.guide." + page + "." + i));
        }
        registration.addItemStackInfo(stacks, text.toArray(new Component[0]));
    }

    private static List<ItemStack> tarotStacks() {
        return List.of(
                PollutionItems.TAROT_THE_FOOL.asStack(),
                PollutionItems.TAROT_THE_MAGICIAN.asStack(),
                PollutionItems.TAROT_THE_HIGH_PRIESTESS.asStack(),
                PollutionItems.TAROT_THE_EMPRESS.asStack(),
                PollutionItems.TAROT_THE_EMPEROR.asStack(),
                PollutionItems.TAROT_THE_HIGHOPHANT.asStack(),
                PollutionItems.TAROT_THE_LOVERS.asStack(),
                PollutionItems.TAROT_THE_CHARIOT.asStack(),
                PollutionItems.TAROT_THE_STRENGTH.asStack(),
                PollutionItems.TAROT_THE_HERMIT.asStack(),
                PollutionItems.TAROT_THE_WHEEL_OF_FORTUNE.asStack(),
                PollutionItems.TAROT_JUSTICE.asStack(),
                PollutionItems.TAROT_THE_HANGED_MAN.asStack(),
                PollutionItems.TAROT_DEATH.asStack(),
                PollutionItems.TAROT_TEMPERANCE.asStack(),
                PollutionItems.TAROT_THE_DEVIL.asStack(),
                PollutionItems.TAROT_THE_TOWER.asStack(),
                PollutionItems.TAROT_THE_STAR.asStack(),
                PollutionItems.TAROT_THE_MOON.asStack(),
                PollutionItems.TAROT_THE_SUN.asStack(),
                PollutionItems.TAROT_JUDGEMENT.asStack(),
                PollutionItems.TAROT_THE_WORLD.asStack());
    }
}
