package meowmel.pollution.common.machine.multiblock.botania;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

/**
 * Botania recipe types of the Pollution port.
 *
 * <p>Upstream ({@code meowmel.pollution.api.recipes.PORecipeMaps}, 1.12.2)
 * declared these maps next to the Thaumcraft ones. {@code PORecipeMaps} is
 * owned by the Thaumcraft pass and is frozen after its own registration window,
 * so the Botania maps live here and are registered from the same
 * {@code GTRecipeType} register event
 * ({@code PollutionMachineEvents#onRecipeTypeRegister}).</p>
 *
 * <p>Re-created upstream maps (ids kept verbatim so recipe data transplants
 * 1:1):</p>
 * <ul>
 *   <li>{@code mana_petal_recipes} - 16 item in, 1 out</li>
 *   <li>{@code mana_rune_altar_recipes} - 10 item in, 1 out</li>
 *   <li>{@code pure_daisy_recipes} - 1 item in, 1 out</li>
 *   <li>{@code mana_infusion_recipes} - 2 item in, 1 out</li>
 *   <li>{@code mana_gen_recipes} - no IO slots, EU out</li>
 *   <li>{@code mana_to_eu} - 2/2 item, 2/2 fluid, EU out</li>
 *   <li>{@code dan_de_life_on} - 2/2 item, 2/2 fluid, EU out</li>
 * </ul>
 *
 * <p>Upstream {@code MANA_TURBINE_FUELS} already exists in the port as
 * {@code PORecipeMaps.MAGIC_TURBINE_FUELS} and is reused by the turbine
 * machines; it is not duplicated here.</p>
 */
public final class BotaniaRecipeMaps {

    private static final String GROUP = "pollution";

    public static final GTRecipeType MANA_PETAL_RECIPES = GTRecipeTypes.register("mana_petal_recipes", GROUP)
            .setMaxIOSize(16, 1, 0, 0)
            .setEUIO(IO.IN);

    public static final GTRecipeType MANA_RUNE_ALTAR_RECIPES = GTRecipeTypes.register("mana_rune_altar_recipes", GROUP)
            .setMaxIOSize(10, 1, 0, 0)
            .setEUIO(IO.IN);

    public static final GTRecipeType PURE_DAISY_RECIPES = GTRecipeTypes.register("pure_daisy_recipes", GROUP)
            .setMaxIOSize(1, 1, 0, 0)
            .setEUIO(IO.IN);

    public static final GTRecipeType MANA_INFUSION_RECIPES = GTRecipeTypes.register("mana_infusion_recipes", GROUP)
            .setMaxIOSize(2, 1, 0, 0)
            .setEUIO(IO.IN);

    /** Mana generator map: no slots, EU out. */
    public static final GTRecipeType MANA_GEN_RECIPES = GTRecipeTypes.register("mana_gen_recipes", GROUP)
            .setMaxIOSize(0, 0, 0, 0)
            .setEUIO(IO.OUT);

    /** Mana-to-EU fuel map (upstream FuelRecipeBuilder). */
    public static final GTRecipeType MANA_TO_EU = GTRecipeTypes.register("mana_to_eu", GROUP)
            .setMaxIOSize(2, 2, 2, 2)
            .setEUIO(IO.OUT);

    /** Dandelifeon fuel map (upstream FuelRecipeBuilder). */
    public static final GTRecipeType DAN_DE_LIFE_ON = GTRecipeTypes.register("dan_de_life_on", GROUP)
            .setMaxIOSize(2, 2, 2, 2)
            .setEUIO(IO.OUT);

    /**
     * Forces class initialisation during GregTech's recipe type register event,
     * before {@code GTRegistries.RECIPE_TYPES} freezes.
     */
    public static void init() {}

    private BotaniaRecipeMaps() {}
}
