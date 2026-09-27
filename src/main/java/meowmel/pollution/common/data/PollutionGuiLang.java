package meowmel.pollution.common.data;

import com.tterrag.registrate.providers.RegistrateLangProvider;

public final class PollutionGuiLang {
    private PollutionGuiLang() {}
    public static void add(RegistrateLangProvider provider) {
        provider.add("pollution.starstream_linker.unlinked", "Starstream link cleared");
        provider.add("pollution.machine.magic_turbine.tooltip", "Generates EU from magical turbine fuels.");
        provider.add("pollution.machine.large_mana_turbine.tooltip.1", "Generates EU from mana fluids; requires a rotor.");
        provider.add("pollution.machine.large_mana_turbine.tooltip.2", "Use the matching mana casing and a dynamo hatch.");
        provider.add("pollution.jei.recipe.astral.sky", "%s / %s / sky >= %s%%");
        provider.add("pollution.jei.recipe.astral.any", "Any constellation");
        provider.add("pollution.jei.recipe.astral.night", "Night");
        provider.add("pollution.jei.recipe.astral.any_time", "Any time");
        provider.add("pollution.jei.recipe.astral.quality", "Cultivated crystal quality >= %s%%");
        provider.add("pollution.jei.recipe.astral.inherit", "Retains the input crystal / item properties");
        provider.add("pollution.ui.coil_level", "Heating coil level: %s");
        provider.add("pollution.ui.frame_level", "Containment frame level: %s");
        provider.add("pollution.ui.mansus_progress", "Starry Mansus progress: %s / %s");
        provider.add("pollution.ui.fusion_heat", "Fusion heat: %s EU");
        provider.add("pollution.ui.fusion_buffer", "Fusion startup buffer: %s / %s EU");
        provider.add("pollution.ui.progress", "Progress: %s / %s");
        provider.add("pollution.ui.essence_work", "Working: %s; infused fire: %s mB/t");
        provider.add("pollution.ui.essence_focus", "Focus mode: %s");
        provider.add("pollution.ui.enabled_channel", "On (%s)");
        provider.add("pollution.ui.yes", "Yes");
        provider.add("pollution.ui.no", "No");

        provider.add("gtceu.industrial_starlight_infuser_recipes", "Industrial Starlight Infusion");
        provider.add("gtceu.industrial_lightwell_recipes", "Industrial Lightwell");
        provider.add("gtceu.celestial_observation", "Celestial Observation");
        provider.add("gtceu.celestial_calibration", "Celestial Calibration");
        provider.add("gtceu.celestial_crystal_growth", "Celestial Crystal Growth");
        provider.add("pollution.ui.vis", "Vis: %s / %s");
        provider.add("pollution.ui.infused", "Infused fluid: %s, %s / %s mB");
        provider.add("pollution.ui.life", "Life essence: %s / %s");
        provider.add("pollution.ui.focus", "Astral focus: %s");
        provider.add("pollution.ui.formed", "Structure formed");
        provider.add("pollution.ui.unformed", "Structure incomplete");
        provider.add("pollution.magic.failure.astral_wafer", "A matching constellation data wafer is required in the Astral lens hatch.");
        provider.add("pollution.ui.astral.title", "Constellation and amplification");
        provider.add("pollution.ui.astral.no_wafer", "Insert a constellation data wafer to enable amplification.");
        provider.add("pollution.ui.astral.base", "Wafer: %s%%");
        provider.add("pollution.ui.astral.optics", "Lens: +%s%%; quality: %s%%");
        provider.add("pollution.ui.astral.sky", "Active sky: %s");
        provider.add("pollution.ui.astral.preview", "Idle preview");
        provider.add("pollution.ui.astral.running", "Current recipe");
        provider.add("pollution.ui.astral.speed", "Time: -%s%%; EU/t: -%s%%");
        provider.add("pollution.ui.astral.magic", "Magic cost: -%s%%; parallel: +%s");
        provider.add("pollution.ui.astral.output", "Output: +%s%%; reroll: +%s%%");
        provider.add("pollution.ui.astral.catalyst", "Catalyst save: %s%%; heat: +%s K");
        provider.add("pollution.machine.industrial_starlight_infuser.pool", "After terminal construction, fill all 24 spaces of the 5x5 pool around the infuser with liquid starlight.");
        provider.add("pollution.machine.industrial_lightwell.native", "Processes Astral Sorcery lightwell catalysts without consuming them.");
        provider.add("pollution.machine.astral.processing", "Uses the Astral lens hatch and preserves constellation / crystal properties.");
    }
}
