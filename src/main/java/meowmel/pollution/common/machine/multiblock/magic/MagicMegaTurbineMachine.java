package meowmel.pollution.common.machine.multiblock.magic;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import meowmel.pollution.api.metatileentity.POMultiblockAbility;
import meowmel.pollution.common.block.PollutionMagicBlocks;

/**
 * IV magic mega turbine: twelve rotor holders and sixteen times the large turbine output.
 * <p>High power triples the target output and matching fuel demand, can be changed only
 * while idle, and persists with the controller. GT handles speed, efficiency and wear.</p>
 */
public class MagicMegaTurbineMachine extends AbstractMagicTurbineMachine {
    private static final com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder FIELD_HOLDER =
            new com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder(MagicMegaTurbineMachine.class, MANAGED_FIELD_HOLDER);
    @com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
    @com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced
    private boolean highPower;

    @Override public com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder getFieldHolder() { return FIELD_HOLDER; }
    public boolean isHighPower() { return highPower; }
    public void setHighPower(boolean enabled) {
        if (getRecipeLogic().isActive()) return;
        highPower = enabled;
        getRecipeLogic().markLastRecipeDirty();
        markDirty();
    }
    @Override public long getOverclockVoltage() { return super.getOverclockVoltage() * (highPower ? 3 : 1); }
    @Override public void attachConfigurators(com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel panel) {
        super.attachConfigurators(panel);
        panel.attachConfigurators(new com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton.Toggle(
                com.gregtechceu.gtceu.api.gui.GuiTextures.BUTTON_POWER.getSubTexture(0, 0, 1, 0.5),
                com.gregtechceu.gtceu.api.gui.GuiTextures.BUTTON_POWER.getSubTexture(0, 0.5, 1, 0.5),
                this::isHighPower, (click, enabled) -> setHighPower(enabled))
                .setTooltipsSupplier(enabled -> java.util.List.of(net.minecraft.network.chat.Component.translatable(
                        "pollution.machine.turbine.high_power", enabled))));
    }

    public MagicMegaTurbineMachine(IMachineBlockEntity holder) {
        this(holder, com.gregtechceu.gtceu.api.GTValues.IV);
    }

    protected MagicMegaTurbineMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier, 12, 16);
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return createPattern(definition, PollutionMagicBlocks.SPELL_PRISM_HOT.get(),
                PollutionMagicBlocks.STAINLESS_STEEL_GEARBOX.get());
    }

    protected static BlockPattern createPattern(MultiblockMachineDefinition definition,
                                                net.minecraft.world.level.block.Block casing,
                                                net.minecraft.world.level.block.Block gearbox) {
        return FactoryBlockPattern.start()
                .aisle("CCCCCCC", "CCCCCCC", "CCMMMCC", "CCMMMCC", "CCMMMCC", "CCCCCCC", "CCCCCCC")
                .aisle("CCCCCCC", "RGGGGGR", "CCCCCCC", "CCCCCCC", "CCCCCCC", "RGGGGGR", "CCCCCCC")
                .aisle("CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC")
                .aisle("CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC")
                .aisle("CCCCCCC", "RGGGGGR", "CCCCCCC", "CCCCCCC", "CCCCCCC", "RGGGGGR", "CCCCCCC")
                .aisle("CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC")
                .aisle("CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC")
                .aisle("CCCCCCC", "RGGGGGR", "CCCCCCC", "CCCCCCC", "CCCCCCC", "RGGGGGR", "CCCCCCC")
                .aisle("CCCCCCC", "CAAAAAC", "CAAAAAC", "CAASAAC", "CAAAAAC", "CAAAAAC", "CCCCCCC")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('C', Predicates.blocks(casing))
                .where('G', Predicates.blocks(gearbox))
                .where('R', rotorsAtLeast(definition.getTier()))
                .where('M', Predicates.abilities(PartAbility.MUFFLER))
                .where('A', Predicates.blocks(casing)
                        .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMinGlobalLimited(1).setMaxGlobalLimited(4))
                        .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMinGlobalLimited(1).setMaxGlobalLimited(4))
                        .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(1))
                        .or(Predicates.abilities(PartAbility.OUTPUT_ENERGY).setMaxGlobalLimited(3))
                        .or(Predicates.abilities(POMultiblockAbility.MANA_OUTPUT_HATCH).setMaxGlobalLimited(8))
                        .or(Predicates.abilities(POMultiblockAbility.TAROT_HATCH).setMaxGlobalLimited(1))
                        .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                .build();
    }
}
