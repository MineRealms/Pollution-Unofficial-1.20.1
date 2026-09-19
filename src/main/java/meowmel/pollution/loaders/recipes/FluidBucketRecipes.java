package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import meowmel.pollution.Pollution;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Consumer;

/**
 * Obtainability pass for the 123 Pollution fluid buckets ({@code GTBucketItem}).
 *
 * <p><b>Why a recipe family is needed.</b> GT buckets are normally obtained by
 * right-clicking a placed source block, or by the Canning Machine's container
 * mode. Pollution's GT fluids register no block form (see
 * {@code SecondDegreeMaterials.PureTar} for the documented reason: GT's fluid
 * block models would break datagen), so there is no source block to scoop and
 * the audit found every Pollution bucket recipe-less. GTCEu also ships no
 * generic "empty bucket + fluid" canner family - only a handful of its own
 * bucket recipes exist.</p>
 *
 * <p><b>Chosen solution.</b> For every Pollution material and every fluid
 * storage key the material actually carries, this class emits a
 * {@code gtceu:canner} recipe {@code empty bucket + 1000 mB fluid -> bucket}.
 * The canner machine is the intended machine for this family (the same map GT
 * uses for item canning), and the recipe makes the buckets visible in JEI.
 * The mapping is derived from the material registry, so the 123 existing
 * buckets are covered without hand-maintaining a list; materials without a
 * bucket item (or without that storage key) are skipped.</p>
 */
public final class FluidBucketRecipes {

    /** The fluid keys a bucket can exist for: primary liquid, gas, plasma and hot (molten). */
    private static final FluidStorageKey[] KEYS = {
            FluidStorageKeys.LIQUID,
            FluidStorageKeys.GAS,
            FluidStorageKeys.PLASMA,
            FluidStorageKeys.MOLTEN,
    };

    private FluidBucketRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        if (GTCEuAPI.materialManager == null) {
            Pollution.LOGGER.warn("[bucket] skipping the fluid bucket canning family: "
                    + "the GT material manager is not available");
            return;
        }
        int added = 0;
        for (Material material : GTCEuAPI.materialManager.getRegisteredMaterials()) {
            ResourceLocation materialId = GTCEuAPI.materialManager.getKey(material);
            if (materialId == null || !Pollution.MOD_ID.equals(materialId.getNamespace())
                    || !material.hasFluid()) {
                continue;
            }
            for (FluidStorageKey key : KEYS) {
                String fluidName = key.getRegistryNameFor(material);
                if (fluidName == null) {
                    continue;
                }
                Item bucket = ForgeRegistries.ITEMS.getValue(
                        ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, fluidName + "_bucket"));
                if (bucket == null) {
                    continue;
                }
                Fluid fluid = material.getFluid(key);
                if (fluid == null) {
                    continue;
                }
                GTRecipeBuilder.of(id(materialId.getPath() + "/" + fluidName), GTRecipeTypes.CANNER_RECIPES)
                        .inputItems(Items.BUCKET)
                        .inputFluids(new FluidStack(fluid, 1000))
                        .outputItems(new ItemStack(bucket))
                        .duration(20)
                        .EUt(8)
                        .save(provider);
                added++;
            }
        }
        Pollution.LOGGER.info("[bucket] registered {} fluid bucket canning recipes", added);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, "bucket/" + path);
    }
}
