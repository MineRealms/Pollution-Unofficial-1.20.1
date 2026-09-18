package meowmel.pollution.api.recipes;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

/**
 * GregTech recipe types of the Pollution port.
 *
 * <p>Upstream ({@code RecipeMapBuilder}) defined 20+ maps. The port keeps the
 * upstream names verbatim so recipe data can be transplanted 1:1, and only
 * carries the Thaumcraft-facing subset so far. The magic processing machines
 * (MagicMacerator, MagicMixer, ...) reuse GregTech's own recipe types and do not
 * need an entry here; these are the custom maps.</p>
 *
 * <p>Deferred with their systems: Botania ({@code dan_de_life_on},
 * {@code mana_to_eu}, {@code mana_gen_recipes}, {@code mana_infusion_recipes},
 * {@code mana_rune_altar_recipes}, {@code mana_petal_recipes},
 * {@code pure_daisy_recipes}), Astral ({@code magic_meteors_recipes},
 * {@code industrial_starlight_infuser_recipes}, {@code industrial_lightwell_recipes},
 * {@code celestial_observation}, {@code celestial_calibration},
 * {@code crystal_cultivation}, {@code celestial_crystal_growth}) and the
 * static JEI guide maps.</p>
 *
 * <p>UI details of the upstream maps (slot overlays, progress bars, sounds,
 * MagicPropertyRecipeUI) are pending the modern recipe-UI pass.</p>
 */
public final class PORecipeMaps {

    private static final String GROUP = "pollution";

    public static final GTRecipeType MAGIC_ALLOY_BLAST_RECIPES = GTRecipeTypes.register("magic_blast_smelter", GROUP)
            .setMaxIOSize(9, 0, 3, 1)
            .setEUIO(IO.IN);

    public static final GTRecipeType STOVE_RECIPES = GTRecipeTypes.register("stove", GROUP)
            .setMaxIOSize(2, 1, 0, 0)
            .setEUIO(IO.IN);

    public static final GTRecipeType MAGIC_FUSION_REACTOR = GTRecipeTypes.register("magic_fusion_reactor", GROUP)
            .setMaxIOSize(0, 0, 1, 2)
            .setEUIO(IO.IN);

    public static final GTRecipeType MAGIC_CHEMICAL_REACTOR_RECIPES = GTRecipeTypes
            .register("magic_chemical_reactor", GROUP)
            .setMaxIOSize(5, 4, 5, 4)
            .setEUIO(IO.IN);

    public static final GTRecipeType MAGIC_ASSEMBLER_RECIPES = GTRecipeTypes.register("magic_assembler", GROUP)
            .setMaxIOSize(9, 1, 3, 0)
            .setEUIO(IO.IN);

    public static final GTRecipeType MAGIC_GREENHOUSE_RECIPES = GTRecipeTypes.register("magic_greenhouse", GROUP)
            .setMaxIOSize(4, 4, 1, 1)
            .setEUIO(IO.IN);

    /** Magic turbine fuel map: one fluid in, one fluid out, EU out. */
    public static final GTRecipeType MAGIC_TURBINE_FUELS = GTRecipeTypes.register("magic_turbine", GROUP)
            .setMaxIOSize(0, 0, 1, 1)
            .setEUIO(IO.OUT);

    public static final GTRecipeType FORGE_ALCHEMY_RECIPES = GTRecipeTypes.register("forge_alchemy", GROUP)
            .setMaxIOSize(9, 3, 6, 3)
            .setEUIO(IO.IN);

    public static final GTRecipeType NODE_MAGIC_FUSION_RECIPES = GTRecipeTypes.register("node_magic_fusion", GROUP)
            .setMaxIOSize(0, 0, 3, 3)
            .setEUIO(IO.IN);

    public static final GTRecipeType INDUSTRIAL_INFUSION_RECIPES = GTRecipeTypes
            .register("industrial_infusion_recipes", GROUP)
            .setMaxIOSize(25, 1, 8, 0)
            .setEUIO(IO.IN);

    /**
     * Forces class initialisation at a safe time.
     *
     * <p>{@code GTRecipeTypes#register} writes into GregTech's own registry, which
     * is frozen before the machine {@code RegisterEvent} fires. Recipe types
     * must therefore be created during mod construction (same window GregTech
     * itself uses), never from the machine registration listener.</p>
     */
    public static void init() {}

    private PORecipeMaps() {}
}
