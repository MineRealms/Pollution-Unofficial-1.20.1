package meowmel.pollution.api.unification;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialRegistryEvent;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.magic.PollutionAspectMapping;
import meowmel.pollution.api.unification.materials.ElementMaterials;
import meowmel.pollution.api.unification.materials.FirstDegreeMaterials;
import meowmel.pollution.api.unification.materials.InfusedMaterials;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Material registration for GregTech CEu Modern.
 *
 * <p>{@code IGTAddon#registerMaterials()} is deprecated for removal since 7.5.x; the
 * sanctioned replacement is {@link MaterialRegistryEvent} (create the addon registry)
 * followed by {@link MaterialEvent} (create the materials). Both are mod-bus events.</p>
 */
@Mod.EventBusSubscriber(modid = Pollution.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class PollutionMaterialEvents {

    private PollutionMaterialEvents() {}

    @SubscribeEvent
    public static void onMaterialRegistry(MaterialRegistryEvent event) {
        GTCEuAPI.materialManager.createRegistry(Pollution.MOD_ID);
        Pollution.LOGGER.debug("Created Pollution material registry");
    }

    @SubscribeEvent
    public static void onMaterial(MaterialEvent event) {
        ElementMaterials.register();
        FirstDegreeMaterials.register();
        InfusedMaterials.register();
        meowmel.pollution.api.unification.materials.MagicFuelMaterials.register();
        meowmel.pollution.api.unification.materials.SubstrateMaterials.register();
        meowmel.pollution.api.unification.materials.OreMaterials.register();
        meowmel.pollution.api.unification.materials.SecondDegreeMaterials.register();
        meowmel.pollution.api.unification.materials.HigherDegreeMaterials.register();
        meowmel.pollution.api.unification.materials.MagicIntegrationMaterials.register();
        meowmel.pollution.api.unification.materials.MaterialPropertyAddition.init();
        PollutionAspectMapping.init();
        TCAspectAddons.init();
        Pollution.LOGGER.info("Registered Pollution materials: 6 base aspects, 4 nexus metals, 34 compound aspects, "
                + "6 aspect alloys, 11 ore materials, kqt/superconductor/battery/filth/hachimi intermediates");
    }
}
