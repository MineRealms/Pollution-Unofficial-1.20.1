package meowmel.pollution.common.machine;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.recipes.PORecipeMaps;
import net.minecraft.resources.ResourceLocation;

/**
 * GregTech registration listeners.
 *
 * <p>GregTech posts {@code GTCEuAPI.RegisterEvent} for recipe types inside
 * {@code GTRecipeTypes.init()} and for machines at the end of
 * {@code GTMachines} initialisation, each immediately before freezing the
 * corresponding registry. Those are the only windows where addons can register
 * types/machines with all GregTech data available. Subscribed with
 * {@code modBus.addGenericListener(Type.class, ...)}.</p>
 */
public final class PollutionMachineEvents {

    private PollutionMachineEvents() {}

    /**
     * Recipe types must be created here, not during mod construction: GregTech's
     * own registries are frozen before addon constructors run, and the machine
     * event happens after {@code RECIPE_TYPES.freeze()}.
     */
    public static void onRecipeTypeRegister(GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType> event) {
        PORecipeMaps.init();
        meowmel.pollution.common.machine.multiblock.botania.BotaniaRecipeMaps.init();
        Pollution.LOGGER.info("Registered Pollution recipe types");
    }

    public static void onMachineRegister(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        PollutionMachines.register();
        Pollution.LOGGER.info("Registered Pollution machine definitions");
    }
}
