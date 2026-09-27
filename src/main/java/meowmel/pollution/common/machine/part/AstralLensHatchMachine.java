package meowmel.pollution.common.machine.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import hellfirepvp.astralsorcery.common.base.MoonPhase;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.SkyHandler;
import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import meowmel.pollution.api.astral.AstralCrystalNbtHelper;
import meowmel.pollution.api.astral.AstralNbtHelper;
import meowmel.pollution.api.capability.IAstralHatch;
import meowmel.pollution.api.recipes.properties.AstralCondition;
import meowmel.pollution.common.item.PollutionItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

/** A non-consumable lens focus that reads Astral Sorcery's live server sky. */
public class AstralLensHatchMachine extends MagicItemHatchMachine implements IAstralHatch {

    public AstralLensHatchMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier, tier >= GTValues.LuV ? 2 : 1);
    }

    @Override
    protected boolean isAcceptedStack(ItemStack stack) {
        return AstralNbtHelper.readNativeConstellation(stack) != null;
    }

    @Override
    protected boolean isAcceptedStack(int slot, ItemStack stack) {
        return slot == 0 ? isAcceptedStack(stack)
                : getTier() >= GTValues.LuV && AstralCrystalNbtHelper.isCultivatedCrystal(stack);
    }

    private IConstellation getFocus() {
        return AstralNbtHelper.readNativeConstellation(getFocusStack());
    }

    private WorldContext getSky() {
        return getLevel() instanceof ServerLevel level ? SkyHandler.getContext(level) : null;
    }

    @Override
    public String getFocusedConstellation() {
        IConstellation focus = getFocus();
        return focus == null ? "" : focus.getSimpleName().toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean hasConstellationDataWafer() {
        return getFocusStack().is(PollutionItems.CONSTELLATION_DATA_WAFER.get()) && getFocus() != null;
    }

    @Override
    public int getOpticalCrystalQuality() {
        return getTier() >= GTValues.LuV
                ? AstralCrystalNbtHelper.getOpticalQuality(getAuxiliaryStack(1)) : 0;
    }

    @Override
    public double getOpticalCrystalStrengthBonus() {
        int quality = getOpticalCrystalQuality();
        if (quality < 50) return 0.0D;
        if (quality < 70) return 0.05D;
        if (quality < 85) return 0.10D;
        return 0.20D;
    }

    @Override
    public boolean isSkyVisible() {
        return getLevel() instanceof ServerLevel level && level.canSeeSky(getPos().above());
    }

    @Override
    public boolean isNight() {
        return getLevel() instanceof ServerLevel level && !level.isDay();
    }

    @Override
    public boolean isFocusedConstellationActive() {
        IConstellation focus = getFocus();
        WorldContext sky = getSky();
        return focus != null && sky != null && getLevel() instanceof ServerLevel level
                && sky.getConstellationHandler().getLastTrackedDay() >= 0
                && sky.getConstellationHandler().isActiveCurrently(focus, MoonPhase.fromWorld(level));
    }

    @Override
    public float getFocusedDistribution() {
        IConstellation focus = getFocus();
        WorldContext sky = getSky();
        return focus == null || sky == null ? 0.0F : sky.getDistributionHandler().getDistribution(focus);
    }

    @Override
    public String getMoonPhase() {
        return getSky() != null && getLevel() instanceof ServerLevel level
                ? MoonPhase.fromWorld(level).name().toLowerCase(Locale.ROOT) : "";
    }

    @Override
    public String getCelestialEvent() {
        WorldContext sky = getSky();
        if (sky == null) return "";
        var events = sky.getCelestialEventHandler();
        if (events.getSolarEclipse().isActiveNow()) return "solar_eclipse";
        if (events.getLunarEclipse().isActiveNow()) return "lunar_eclipse";
        if (events.getStarFallEvent().isActiveNow()) return "star_fall";
        return "";
    }

    @Override
    public boolean matches(AstralCondition condition) {
        if (condition == null || !condition.isConfigured()) return true;
        if (getFocus() == null || !isSkyVisible() || getSky() == null) return false;
        if (condition.isNightRequired() && !isNight()) return false;
        if (!condition.getMoonPhase().isEmpty()
                && !condition.getMoonPhase().equals(getMoonPhase())) return false;
        if (!condition.getCelestialEvent().isEmpty()
                && !condition.getCelestialEvent().equals(getCelestialEvent())) return false;
        if (!condition.getConstellation().isEmpty()) {
            IConstellation required = AstralNbtHelper.findConstellation(condition.getConstellation());
            if (required == null || required != getFocus() || !isFocusedConstellationActive()) return false;
        }
        return getFocusedDistribution() >= condition.getMinimumDistribution();
    }
}
