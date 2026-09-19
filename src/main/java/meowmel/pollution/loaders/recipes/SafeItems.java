package meowmel.pollution.loaders.recipes;

import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

/**
 * Safe access to items during recipe registration.
 *
 * <p>Recipe registration runs inside GT's {@code AddPackFindersEvent} callback.
 * Directly dereferencing other mods' static item fields (e.g.
 * {@code GTItems.FIELD_GENERATOR_UHV}) can observe {@code null} because the
 * owning class may still be initialising (fields are assigned in declaration
 * order) or because the item is not registered on this pack. Both cases used to
 * crash the game with an NPE while building recipes.</p>
 *
 * <p>This helper never throws: a missing item resolves to
 * {@link ItemStack#EMPTY} and the caller is expected to guard or skip the
 * recipe.</p>
 */
public final class SafeItems {

    private SafeItems() {}

    /** Null-safe {@link ItemEntry#asStack(int)}. */
    public static ItemStack of(@Nullable ItemEntry<?> entry, int count) {
        return entry == null ? ItemStack.EMPTY : entry.asStack(count);
    }

    /** Null-safe {@link ItemEntry#asStack()}. */
    public static ItemStack of(@Nullable ItemEntry<?> entry) {
        return of(entry, 1);
    }

    /** Registry lookup by namespace/path; empty stack when absent. */
    public static ItemStack byId(String namespace, String path, int count) {
        Item item = ForgeRegistries.ITEMS.getValue(
                ResourceLocation.fromNamespaceAndPath(namespace, path));
        return item == null ? ItemStack.EMPTY : new ItemStack(item, count);
    }

    /** GTCEu item lookup by path; empty stack when absent. */
    public static ItemStack gt(String path, int count) {
        return byId("gtceu", path, count);
    }
}
