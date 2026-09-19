package meowmel.pollution.api.recipes.properties;

import meowmel.pollution.Pollution;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Stable recipe / hatch ids for the 22 major arcana already registered by
 * Pollution.
 *
 * <p>Upstream stored the cards as metadata values 301..322 of one meta item and
 * mapped them through a fixed id table. The port registers one item per card,
 * so the same table is keyed by registry path. The three cards whose registry
 * name carries a {@code the_} prefix that the upstream id does not
 * ({@code the_death} -&gt; {@code death}, {@code the_temperance} -&gt;
 * {@code temperance}, {@code the_judgement} -&gt; {@code judgement}) keep the
 * upstream ids so recipes, JEI and the amplification engine stay
 * compatible.</p>
 *
 * <p>All lookups are registry based and never throw: an unregistered card
 * simply resolves to {@code null} / {@link ItemStack#EMPTY}, so the tarot hatch
 * and the recipes stay safe when a card is missing.</p>
 */
public final class TarotCards {

    /** Registry path / stable id pairs, in upstream metadata order. */
    private static final String[][] CARDS = {
            { "the_fool", "the_fool" },
            { "the_magician", "the_magician" },
            { "the_high_priestess", "the_high_priestess" },
            { "the_empress", "the_empress" },
            { "the_emperor", "the_emperor" },
            { "the_highophant", "the_highophant" },
            { "the_lovers", "the_lovers" },
            { "the_chariot", "the_chariot" },
            { "the_strength", "the_strength" },
            { "the_hermit", "the_hermit" },
            { "the_wheel_of_fortune", "the_wheel_of_fortune" },
            { "the_justice", "the_justice" },
            { "the_hanged_man", "the_hanged_man" },
            { "the_death", "death" },
            { "the_temperance", "temperance" },
            { "the_devil", "the_devil" },
            { "the_tower", "the_tower" },
            { "the_star", "the_star" },
            { "the_moon", "the_moon" },
            { "the_sun", "the_sun" },
            { "the_judgement", "judgement" },
            { "the_world", "the_world" }
    };

    /** Stable ids of the 22 major arcana in upstream order (immutable). */
    public static final List<String> IDS;

    static {
        List<String> ids = new ArrayList<>(CARDS.length);
        for (String[] card : CARDS) {
            ids.add(card[1]);
        }
        IDS = List.copyOf(ids);
    }

    private TarotCards() {}

    public static boolean isTarot(ItemStack stack) {
        return getId(stack) != null;
    }

    /** Stable major-arcana id of the stack, or {@code null} when it is not a card. */
    @Nullable
    public static String getId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (key == null || !Pollution.MOD_ID.equals(key.getNamespace())) {
            return null;
        }
        return idForPath(key.getPath());
    }

    public static boolean matches(ItemStack stack, String tarotId) {
        String active = getId(stack);
        return active != null && tarotId != null && active.equals(normalize(tarotId));
    }

    /** The registered card stack for an id, or {@link ItemStack#EMPTY} when the card is absent. */
    public static ItemStack getStack(String tarotId) {
        String path = pathForId(normalize(tarotId));
        if (path == null) {
            return ItemStack.EMPTY;
        }
        Item item = ForgeRegistries.ITEMS.getValue(
                ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, path));
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    public static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    @Nullable
    private static String idForPath(String path) {
        for (String[] card : CARDS) {
            if (card[0].equals(path)) {
                return card[1];
            }
        }
        return null;
    }

    @Nullable
    private static String pathForId(String id) {
        for (String[] card : CARDS) {
            if (card[1].equals(id)) {
                return card[0];
            }
        }
        return null;
    }
}
