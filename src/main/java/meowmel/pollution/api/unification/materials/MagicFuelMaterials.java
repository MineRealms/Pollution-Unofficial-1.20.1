package meowmel.pollution.api.unification.materials;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.unification.PollutionMaterials;
import net.minecraft.resources.ResourceLocation;

/**
 * Fuel-chain materials of the magic fuel recipes.
 *
 * <p>Upstream produced these from the GTQT chemical chain. The port keeps the
 * materials (so the machine maps and fuel values survive) but produces them
 * from GTCEu-native intermediates, see {@code MagicFuelRecipes} and the
 * substitution table in the tracker.</p>
 */
public final class MagicFuelMaterials {

    private MagicFuelMaterials() {}

    public static void register() {
        PollutionMaterials.MagicNitrobenzene = new Material.Builder(id("magic_nitrobenzene"))
                .color(0xB03060)
                .fluid()
                .iconSet(MaterialIconSet.FLUID)
                .buildAndRegister();

        PollutionMaterials.InfernalBlazePropellant = new Material.Builder(id("infernal_blaze_propellant"))
                .color(0xFF6A00)
                .fluid()
                .iconSet(MaterialIconSet.FLUID)
                .buildAndRegister();

        PollutionMaterials.DragonPulseFuel = new Material.Builder(id("dragon_pulse_fuel"))
                .color(0x8A2BE2)
                .fluid()
                .iconSet(MaterialIconSet.FLUID)
                .buildAndRegister();
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, name);
    }
}
