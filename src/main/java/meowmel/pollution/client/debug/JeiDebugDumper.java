package meowmel.pollution.client.debug;

import meowmel.pollution.Pollution;
import meowmel.pollution.compat.jei.PollutionJeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = Pollution.MOD_ID, value = Dist.CLIENT)
public final class JeiDebugDumper {

    private static final String JEI_MOD_ID = "jei";
    private static final String RECIPES_GUI_CLASS = "mezz.jei.gui.recipes.RecipesGui";
    private static final String DUMP_FILE_NAME = "pollution-jei-dump.txt";
    private static final long DUMP_INTERVAL_MS = 5000L;

    private static long lastDumpMs = 0L;

    private JeiDebugDumper() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!ModList.get().isLoaded(JEI_MOD_ID)) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen == null
                || !RECIPES_GUI_CLASS.equals(minecraft.screen.getClass().getName())) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastDumpMs < DUMP_INTERVAL_MS) {
            return;
        }
        try {
            JeiAccess.dump();
        } catch (Throwable throwable) {
            writeFailure(throwable);
        } finally {
            lastDumpMs = now;
        }
    }

    private static Path dumpFile() {
        return FMLPaths.CONFIGDIR.get().resolve(DUMP_FILE_NAME);
    }

    private static void writeFailure(Throwable throwable) {
        StringBuilder builder = new StringBuilder();
        builder.append("=== Pollution JEI debug dump FAILED ===\n");
        builder.append("timestamp: ").append(ZonedDateTime.now()).append('\n');
        builder.append("screen: ").append(screenName()).append('\n');
        builder.append("error: ").append(throwable).append('\n');
        for (StackTraceElement element : throwable.getStackTrace()) {
            builder.append("    at ").append(element).append('\n');
        }
        Throwable cause = throwable.getCause();
        if (cause != null) {
            builder.append("caused by: ").append(cause).append('\n');
        }
        write(builder.toString());
        Pollution.LOGGER.warn("[Pollution] JEI debug dump failed", throwable);
    }

    private static void write(String content) {
        try {
            Path file = dumpFile();
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(file, content, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
        } catch (IOException ioException) {
            Pollution.LOGGER.error("[Pollution] Could not write JEI debug dump", ioException);
        }
    }

    private static String screenName() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.screen == null ? "null" : minecraft.screen.getClass().getName();
    }

    private static final class JeiAccess {

        private static final String GTCEU_NAMESPACE = "gtceu";
        private static final int MAX_RECIPES_PER_CATEGORY = 300;
        private static final int MAX_RECIPES_TOTAL = 2000;

        private JeiAccess() {}

        static void dump() {
            StringBuilder out = new StringBuilder();
            IJeiRuntime runtime = PollutionJeiPlugin.getRuntime();

            out.append("=== Pollution JEI debug dump ===\n");
            out.append("timestamp: ").append(ZonedDateTime.now()).append('\n');
            out.append("screen: ").append(screenName()).append('\n');
            out.append("jei runtime: ").append(runtime == null ? "NOT AVAILABLE" : "available").append('\n');
            out.append("game dir: ").append(FMLPaths.GAMEDIR.get()).append('\n');
            out.append("config dir: ").append(FMLPaths.CONFIGDIR.get()).append('\n');
            out.append('\n');

            if (runtime != null) {
                dumpCategoriesAndRecipes(out, runtime);
                dumpIngredientUnderMouse(out, runtime);
            } else {
                out.append("## JEI runtime not available, category/recipe dump skipped\n\n");
            }

            dumpBrokenItemNames(out);
            dumpBrokenFluidNames(out);

            write(out.toString());
        }

        private static void dumpCategoriesAndRecipes(StringBuilder out, IJeiRuntime runtime) {
            IRecipeManager recipeManager = runtime.getRecipeManager();
            IIngredientManager ingredientManager = runtime.getIngredientManager();
            IFocusGroup emptyFocusGroup = runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup();

            List<IRecipeCategory<?>> categories;
            try {
                categories = recipeManager.createRecipeCategoryLookup().get().toList();
            } catch (Throwable throwable) {
                out.append("!! could not list recipe categories: ").append(throwable).append('\n');
                return;
            }

            out.append("## Registered recipe categories: ").append(categories.size()).append('\n');
            for (IRecipeCategory<?> category : categories) {
                RecipeType<?> recipeType = category.getRecipeType();
                out.append("- ").append(recipeType.getUid()).append(" | ")
                        .append(category.getTitle().getString()).append('\n');
            }
            out.append('\n');

            out.append("## Recipes (cap ").append(MAX_RECIPES_PER_CATEGORY)
                    .append(" per category, ").append(MAX_RECIPES_TOTAL).append(" total)\n");

            int dumpedRecipes = 0;
            for (IRecipeCategory<?> category : categories) {
                if (dumpedRecipes >= MAX_RECIPES_TOTAL) {
                    out.append("!! global recipe cap reached, remaining categories skipped\n");
                    break;
                }
                RecipeType<?> recipeType = category.getRecipeType();
                boolean relevant;
                try {
                    relevant = isRelevantCategory(recipeManager, recipeType);
                } catch (Throwable throwable) {
                    out.append("!! relevance check failed for ").append(recipeType.getUid())
                            .append(": ").append(throwable).append('\n');
                    continue;
                }
                if (!relevant) {
                    continue;
                }
                int budget = Math.min(MAX_RECIPES_PER_CATEGORY, MAX_RECIPES_TOTAL - dumpedRecipes);
                dumpedRecipes += dumpCategory(out, recipeManager, castCategory(category),
                        emptyFocusGroup, ingredientManager, budget);
            }
            out.append("## recipe dump finished, recipes dumped: ").append(dumpedRecipes).append("\n\n");
        }

        private static boolean isRelevantCategory(IRecipeManager recipeManager, RecipeType<?> recipeType) {
            ResourceLocation uid = recipeType.getUid();
            String namespace = uid.getNamespace();
            if (Pollution.MOD_ID.equals(namespace) || GTCEU_NAMESPACE.equals(namespace)) {
                return true;
            }
            return recipeManager.createRecipeCatalystLookup(recipeType).getItemStack()
                    .filter(stack -> !stack.isEmpty())
                    .map(stack -> ForgeRegistries.ITEMS.getKey(stack.getItem()))
                    .filter(key -> key != null && Pollution.MOD_ID.equals(key.getNamespace()))
                    .findAny()
                    .isPresent();
        }

        @SuppressWarnings("unchecked")
        private static <T> IRecipeCategory<T> castCategory(IRecipeCategory<?> category) {
            return (IRecipeCategory<T>) category;
        }

        private static <T> int dumpCategory(StringBuilder out, IRecipeManager recipeManager,
                                            IRecipeCategory<T> category, IFocusGroup focusGroup,
                                            IIngredientManager ingredientManager, int budget) {
            RecipeType<T> recipeType = category.getRecipeType();
            out.append("\n### ").append(recipeType.getUid()).append(" | ")
                    .append(category.getTitle().getString()).append('\n');

            List<T> recipes;
            try {
                recipes = recipeManager.createRecipeLookup(recipeType).get().limit(budget).toList();
            } catch (Throwable throwable) {
                out.append("  !! recipe lookup failed: ").append(throwable).append('\n');
                return 0;
            }
            out.append("  recipes dumped: ").append(recipes.size()).append('\n');

            for (T recipe : recipes) {
                out.append("  - ").append(recipeName(category, recipe)).append('\n');
                try {
                    Optional<IRecipeLayoutDrawable<T>> layout =
                            recipeManager.createRecipeLayoutDrawable(category, recipe, focusGroup);
                    if (layout.isEmpty()) {
                        out.append("      <no layout drawable>\n");
                        continue;
                    }
                    for (IRecipeSlotView slot : layout.get().getRecipeSlotsView().getSlotViews()) {
                        out.append("      [").append(slot.getRole()).append("] ");
                        if (slot.isEmpty()) {
                            out.append("(empty)\n");
                            continue;
                        }
                        List<ITypedIngredient<?>> ingredients = slot.getAllIngredientsList();
                        for (int i = 0; i < ingredients.size(); i++) {
                            if (i > 0) {
                                out.append(", ");
                            }
                            out.append(describeIngredient(ingredients.get(i), ingredientManager));
                        }
                        out.append('\n');
                    }
                } catch (Throwable throwable) {
                    out.append("      <layout error: ").append(throwable).append(">\n");
                }
            }
            return recipes.size();
        }

        private static <T> String recipeName(IRecipeCategory<T> category, T recipe) {
            try {
                ResourceLocation registryName = category.getRegistryName(recipe);
                if (registryName != null) {
                    return registryName.toString();
                }
            } catch (Throwable ignored) {
            }
            return String.valueOf(recipe);
        }

        private static void dumpIngredientUnderMouse(StringBuilder out, IJeiRuntime runtime) {
            out.append("## Ingredient under mouse\n");
            try {
                Optional<ITypedIngredient<?>> underMouse =
                        runtime.getIngredientListOverlay().getIngredientUnderMouse();
                if (underMouse.isPresent()) {
                    out.append("- ").append(describeIngredient(underMouse.get(),
                            runtime.getIngredientManager())).append('\n');
                } else {
                    out.append("- (none)\n");
                }
            } catch (Throwable throwable) {
                out.append("- !! ").append(throwable).append('\n');
            }
            out.append('\n');
        }

        private static String describeIngredient(ITypedIngredient<?> typedIngredient,
                                                 IIngredientManager ingredientManager) {
            Optional<ItemStack> itemStack = typedIngredient.getIngredient(VanillaTypes.ITEM_STACK);
            if (itemStack.isPresent()) {
                ItemStack stack = itemStack.get();
                return "item{" + ForgeRegistries.ITEMS.getKey(stack.getItem())
                        + " | desc=" + stack.getDescriptionId()
                        + " | name=" + stack.getHoverName().getString()
                        + " | count=" + stack.getCount() + "}";
            }
            Optional<FluidStack> fluidStack = typedIngredient.getIngredient(ForgeTypes.FLUID_STACK);
            if (fluidStack.isPresent()) {
                FluidStack stack = fluidStack.get();
                String descriptionId;
                try {
                    descriptionId = stack.getFluid().getFluidType().getDescriptionId();
                } catch (Throwable throwable) {
                    descriptionId = "<error: " + throwable + ">";
                }
                return "fluid{" + ForgeRegistries.FLUIDS.getKey(stack.getFluid())
                        + " | desc=" + descriptionId
                        + " | name=" + stack.getDisplayName().getString()
                        + " | amount=" + stack.getAmount() + "}";
            }
            Object raw = typedIngredient.getIngredient();
            String displayName;
            try {
                IIngredientHelper<Object> helper = ingredientManager.getIngredientHelper(raw);
                displayName = helper.getDisplayName(raw);
            } catch (Throwable throwable) {
                displayName = String.valueOf(raw);
            }
            return "other{" + typedIngredient.getType().getUid()
                    + " | class=" + (raw == null ? "null" : raw.getClass().getName())
                    + " | name=" + displayName + "}";
        }

        private static void dumpBrokenItemNames(StringBuilder out) {
            out.append("## Broken / untranslated item names\n");
            int total = 0;
            int flagged = 0;
            for (Item item : ForgeRegistries.ITEMS) {
                total++;
                ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
                try {
                    ItemStack stack = new ItemStack(item);
                    String name = stack.getHoverName().getString();
                    String descriptionId = stack.getDescriptionId();
                    if (isBrokenName(name, descriptionId)) {
                        flagged++;
                        out.append("- ").append(key).append(" | ").append(descriptionId)
                                .append(" | ").append(name).append('\n');
                    }
                } catch (Throwable throwable) {
                    flagged++;
                    out.append("- ").append(key).append(" | <error: ").append(throwable).append(">\n");
                }
            }
            out.append("items checked: ").append(total).append(", flagged: ").append(flagged).append("\n\n");
        }

        private static void dumpBrokenFluidNames(StringBuilder out) {
            out.append("## Broken / untranslated fluid names\n");
            int total = 0;
            int flagged = 0;
            for (Fluid fluid : ForgeRegistries.FLUIDS) {
                total++;
                ResourceLocation key = ForgeRegistries.FLUIDS.getKey(fluid);
                try {
                    FluidStack stack = new FluidStack(fluid, 1000);
                    String name = stack.getDisplayName().getString();
                    String descriptionId = fluid.getFluidType().getDescriptionId();
                    if (isBrokenName(name, descriptionId)) {
                        flagged++;
                        out.append("- ").append(key).append(" | ").append(descriptionId)
                                .append(" | ").append(name).append('\n');
                    }
                } catch (Throwable throwable) {
                    flagged++;
                    out.append("- ").append(key).append(" | <error: ").append(throwable).append(">\n");
                }
            }
            out.append("fluids checked: ").append(total).append(", flagged: ").append(flagged).append("\n\n");
        }

        private static boolean isBrokenName(String name, String descriptionId) {
            if (name == null || name.isEmpty()) {
                return true;
            }
            if (name.equals(descriptionId)) {
                return true;
            }
            return name.startsWith("item.")
                    || name.startsWith("block.")
                    || name.startsWith("material.")
                    || name.startsWith("fluid.");
        }
    }
}
