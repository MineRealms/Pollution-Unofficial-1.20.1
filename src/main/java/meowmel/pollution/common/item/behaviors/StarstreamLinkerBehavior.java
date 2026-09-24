package meowmel.pollution.common.item.behaviors;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Starstream linker behaviour - portable half of upstream
 * {@code meowmel.pollution.common.items.behaviors.StarstreamLinkerBehavior}.
 *
 * <p>Upstream attached this behaviour to the {@code starstream_linker} meta
 * item (id 571). The modern linker delegates network selection and endpoint
 * binding to the registered Starstream blocks while retaining the original
 * mode and NBT names.</p>
 *
 * <p>What is ported here is the network-independent half, ready for the future
 * item class to delegate to:</p>
 * <ul>
 *   <li>the INPUT / NETWORK mode flag in the item NBT (upstream key
 *       {@code StarstreamLinkMode}, including the legacy {@code OUTPUT}
 *       spelling that still reads as NETWORK);</li>
 *   <li>the sneak + right-click-air mode toggle;</li>
 *   <li>the mode / usage tooltip lines.</li>
 * </ul>
 *
 * <p>Astral constellation tower producers remain an optional integration: the
 * network accepts their channel energy through its public API, but does not
 * invent a survival source when Astral is absent.</p>
 */
public final class StarstreamLinkerBehavior {

    /** Upstream NBT keys, kept verbatim so ported saves stay compatible. */
    public static final String TAG_MODE = "StarstreamLinkMode";
    public static final String MODE_INPUT = "INPUT";
    public static final String MODE_NETWORK = "NETWORK";
    public static final String LEGACY_MODE_OUTPUT = "OUTPUT";

    private StarstreamLinkerBehavior() {}

    /** Reads the current mode, mapping the legacy {@code OUTPUT} spelling to NETWORK. */
    public static String getMode(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return MODE_INPUT;
        }
        String mode = tag.getString(TAG_MODE);
        return MODE_NETWORK.equals(mode) || LEGACY_MODE_OUTPUT.equals(mode) ? MODE_NETWORK : MODE_INPUT;
    }

    public static boolean isNetworkMode(ItemStack stack) {
        return MODE_NETWORK.equals(getMode(stack));
    }

    /**
     * Upstream sneak + right-click-air behaviour: flips INPUT and NETWORK,
     * clears any pending selection and returns the new mode.
     */
    public static String toggleMode(ItemStack stack) {
        String next = isNetworkMode(stack) ? MODE_INPUT : MODE_NETWORK;
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(TAG_MODE, next);
        tag.remove("StarstreamSelectedNetwork");
        tag.remove("StarstreamSelectedSource");
        return next;
    }

    /** Status message key for a mode, used by the future item class. */
    public static String modeMessageKey(String mode) {
        return MODE_NETWORK.equals(mode)
                ? "pollution.item.starstream_linker.mode.network"
                : "pollution.item.starstream_linker.mode.input";
    }

    /**
     * Tooltip lines of the upstream behaviour.
     */
    public static void appendTooltip(ItemStack stack, List<Component> tooltip) {
        boolean network = isNetworkMode(stack);
        tooltip.add(Component.translatable(modeMessageKey(getMode(stack)))
                .withStyle(network ? ChatFormatting.GOLD : ChatFormatting.GREEN));
        tooltip.add(Component.translatable("pollution.item.starstream_linker.tooltip.toggle")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(network
                        ? "pollution.item.starstream_linker.tooltip.network"
                        : "pollution.item.starstream_linker.tooltip.input")
                .withStyle(ChatFormatting.DARK_AQUA));
    }
}
