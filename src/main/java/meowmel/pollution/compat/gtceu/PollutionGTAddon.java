package meowmel.pollution.compat.gtceu;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import meowmel.pollution.Pollution;

/**
 * GregTech CEu Modern addon entry point. Discovered automatically through the
 * {@link GTAddon} annotation scan.
 */
@GTAddon
public final class PollutionGTAddon implements IGTAddon {

    private final GTRegistrate registrate = GTRegistrate.create(Pollution.MOD_ID);

    @Override
    public GTRegistrate getRegistrate() {
        return registrate;
    }

    @Override
    public void initializeAddon() {
        registrate.registerRegistrate();
        Pollution.LOGGER.debug("Pollution GregTech CEu addon initialized");
    }

    @Override
    public String addonModId() {
        return Pollution.MOD_ID;
    }
}
