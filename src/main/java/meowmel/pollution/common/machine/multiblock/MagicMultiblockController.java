package meowmel.pollution.common.machine.multiblock;

import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TooltipsPanel;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IVoidable;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import meowmel.pollution.api.amplification.MagicJeiHintResolver;
import meowmel.pollution.api.amplification.MagicMachineProfileRegistry;
import meowmel.pollution.api.amplification.MagicProcessTag;
import meowmel.pollution.api.capability.IAstralHatch;
import meowmel.pollution.api.capability.IBloodMagicHatch;
import meowmel.pollution.api.capability.IManaHatch;
import meowmel.pollution.api.capability.ITarotHatch;
import meowmel.pollution.api.capability.IVisHatch;
import meowmel.pollution.api.capability.ManaHandlerList;
import meowmel.pollution.api.magic.PollutionAspectMapping;
import meowmel.pollution.api.recipes.properties.AstralCondition;
import meowmel.pollution.api.recipes.properties.MagicRecipeProperties;
import meowmel.pollution.common.gui.MachineGuiWidgets;
import meowmel.pollution.common.machine.part.InfusedFluidHatchMachine;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Base controller of the magic multiblocks.
 *
 * <p>Upstream ({@code MagicRecipeMapMultiblockController}) extended the 1.12
 * {@code MultiblockRecipeMapController} and cached one vis hatch, one infused
 * fluid tank and the external-mod hatches (mana / blood / astral / tarot). The
 * port is built on modern {@link WorkableMultiblockMachine}: recipe handler
 * collection is done by GregTech itself, while the magic resources are read
 * directly from the parts collected here.</p>
 *
 * <p>Scope (2026-09-23): every recipe resource is wired. Thaumcraft vis and
 * infused fluids, Botania mana (aggregated from all {@link IManaHatch} parts
 * through a {@link ManaHandlerList}), Blood Magic life essence
 * ({@link IBloodMagicHatch}) and the tarot hatch are discovered on structure
 * formation, and the recipe gate checks them exactly like upstream
 * {@code checkMagicRequirements}. The astral lens hatch
 * ({@link IAstralHatch}) is validated against the recipe's
 * {@link AstralCondition}. Life essence and astral have no hatch machine in the
 * port yet, so the lookup simply finds none and the recipe fails with the
 * generic hatch failure message instead of crashing. The tarot hatch is handed
 * to the amplification engine, but its bonuses still require a calibrated
 * astral wafer (upstream gating).</p>
 *
 * <p>UI (2026-09-19): the controller implements {@link IFancyUIMachine} and
 * {@link IDisplayUIMachine} following GregTech's
 * {@code WorkableElectricMultiblockMachine}: a display panel with the working
 * status, energy usage, machine mode, parallels, progress and output lines,
 * plus energy / vis / infused-fluid bars. Every magic multiblock inherits this
 * screen, and the formed parts are exposed as fancy side tabs.</p>
 */
public abstract class MagicMultiblockController extends WorkableMultiblockMachine
        implements IFancyUIMachine, IDisplayUIMachine, IOverclockMachine, ITieredMachine {

    protected IVisHatch visHatch;
    protected InfusedFluidHatchMachine infusedFluidHatch;
    protected ITarotHatch tarotHatch;
    protected IBloodMagicHatch bloodMagicHatch;
    protected IAstralHatch astralLensHatch;
    protected ICoilType coilType;

    /** Aggregated Botania mana storage of the formed structure. */
    private ManaHandlerList manaHandler = new ManaHandlerList(List.of());

    /** Runtime cache of the energy hatches of the formed structure. */
    private EnergyContainerList energyContainer;

    private Set<Fluid> infusedFluids;

    protected MagicMultiblockController(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new MagicRecipeLogic(this);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        visHatch = null;
        infusedFluidHatch = null;
        tarotHatch = null;
        bloodMagicHatch = null;
        astralLensHatch = null;
        List<IManaHatch> manaHatches = new ArrayList<>();
        for (IMultiPart part : getParts()) {
            if (visHatch == null && part.self() instanceof IVisHatch hatch) {
                visHatch = hatch;
            }
            if (infusedFluidHatch == null && part.self() instanceof InfusedFluidHatchMachine hatch) {
                infusedFluidHatch = hatch;
            }
            if (tarotHatch == null && part.self() instanceof ITarotHatch hatch) {
                tarotHatch = hatch;
            }
            if (bloodMagicHatch == null && part.self() instanceof IBloodMagicHatch hatch) {
                bloodMagicHatch = hatch;
            }
            if (astralLensHatch == null && part.self() instanceof IAstralHatch hatch) {
                astralLensHatch = hatch;
            }
            if (part.self() instanceof IManaHatch hatch) {
                manaHatches.add(hatch);
            }
        }
        manaHandler = new ManaHandlerList(manaHatches);
        Object matchedCoil = getMultiblockState().getMatchContext().get("CoilType");
        coilType = matchedCoil instanceof ICoilType coil ? coil : null;
        energyContainer = createEnergyContainer();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        visHatch = null;
        infusedFluidHatch = null;
        tarotHatch = null;
        bloodMagicHatch = null;
        astralLensHatch = null;
        manaHandler = new ManaHandlerList(List.of());
        coilType = null;
        energyContainer = null;
        if (recipeLogic instanceof MagicRecipeLogic magicLogic) {
            magicLogic.resetMagicState();
        }
    }

    // ////////////////////////////////////
    // ***** Heating coils *****//
    // ////////////////////////////////////

    /** True when the formed structure contains GregTech heating coils. */
    public boolean hasCoil() {
        return coilType != null;
    }

    /**
     * Working temperature of the structure, taken from the matched GregTech
     * coil. Upstream added a custom coil-tier ramp because its coils were
     * custom blocks; the port uses the standard coil temperature instead.
     */
    public int getCurrentTemperature() {
        return coilType == null ? 0 : coilType.getCoilTemperature()
                + 100 * Math.max(0, getOverclockTier() - GTValues.MV);
    }

    @Override
    public long getOverclockVoltage() {
        EnergyContainerList energy = getEnergyContainer();
        long voltage = energy.getInputVoltage();
        return energy.getInputAmperage() == 1
                ? GTValues.VEX[GTUtil.getFloorTierByVoltage(voltage)] : voltage;
    }

    @Override
    public int getOverclockTier() {
        return GTUtil.getTierByVoltage(getOverclockVoltage());
    }

    @Override
    public int getMaxOverclockTier() { return getOverclockTier(); }

    @Override
    public int getTier() { return getOverclockTier(); }

    @Override
    public int getMinOverclockTier() { return getOverclockTier(); }

    @Override
    public void setOverclockTier(int tier) { }

    @Override
    public long getMaxVoltage() {
        EnergyContainerList energy = getEnergyContainer();
        long voltage = energy.getHighestInputVoltage();
        return energy.getNumHighestInputContainers() > 1
                ? GTValues.V[Math.min(GTValues.MAX, GTUtil.getTierByVoltage(voltage) + 1)] : voltage;
    }

    @Override
    public long getDisplayRecipeVoltage() {
        return getEnergyContainer().getHighestInputVoltage();
    }

    /** Standard GT electrical overclocking, including blast-furnace coil discounts. */
    public static ModifierFunction recipeModifier(MetaMachine machine, GTRecipe recipe) {
        if (!(machine instanceof MagicMultiblockController controller)
                || recipe.getInputEUt().isEmpty()) return ModifierFunction.IDENTITY;
        if (RecipeHelper.getRecipeEUtTier(recipe) > controller.getMaxOverclockTier()) {
            return ModifierFunction.cancel(Component.translatable("gtceu.recipe_modifier.insufficient_voltage"));
        }
        if (recipe.data.contains("ebf_temp")) {
            int required = recipe.data.getInt("ebf_temp");
            int temperature = controller.getCurrentTemperature();
            if (!controller.hasCoil() || required > temperature) {
                return ModifierFunction.cancel(Component.translatable("gtceu.recipe_modifier.coil_temperature_too_low"));
            }
            OverclockingLogic logic = (params, voltage) ->
                    OverclockingLogic.heatingCoilOC(params, voltage, required, temperature);
            return logic.getModifier(machine, recipe, controller.getOverclockVoltage()).compose(
                    ModifierFunction.builder().eutMultiplier(
                            OverclockingLogic.getCoilEUtDiscount(required, temperature)).build());
        }
        return OverclockingLogic.NON_PERFECT_OVERCLOCK.getModifier(
                machine, recipe, controller.getOverclockVoltage());
    }

    // ////////////////////////////////////
    // ***** Vis *****//
    // ////////////////////////////////////

    public int getVisCapacity() {
        return visHatch == null ? 0 : visHatch.getMaxVisStore();
    }

    public int getVisStore() {
        return visHatch == null ? 0 : visHatch.getVisStore();
    }

    public boolean consumeVis(int vis, boolean simulate) {
        if (vis <= 0) {
            return true;
        }
        return visHatch != null && visHatch.drainVis(vis, simulate);
    }

    // ////////////////////////////////////
    // ***** Infused fluids *****//
    // ////////////////////////////////////

    public boolean drainInfusedFluid(int amount, boolean simulate) {
        if (amount <= 0) {
            return true;
        }
        if (infusedFluidHatch == null) {
            return false;
        }
        FluidStack stored = infusedFluidHatch.tank.getFluidInTank(0);
        if (stored.isEmpty() || stored.getAmount() < amount || !isInfusedFluid(stored)) {
            return false;
        }
        if (!simulate) {
            infusedFluidHatch.tank.drain(amount, FluidAction.EXECUTE);
        }
        return true;
    }

    /** Accepts any fluid of a material that carries a Pollution aspect mapping. */
    public boolean isInfusedFluid(FluidStack stack) {
        if (infusedFluids == null) {
            Set<Fluid> fluids = new HashSet<>();
            for (var material : PollutionAspectMapping.all().keySet()) {
                if (material != null && material.hasFluid()) {
                    Fluid fluid = material.getFluid();
                    if (fluid != null) {
                        fluids.add(fluid);
                    }
                }
            }
            infusedFluids = fluids;
        }
        return infusedFluids.contains(stack.getFluid());
    }

    // ////////////////////////////////////
    // ***** Mana / life essence / astral *****//
    // ////////////////////////////////////

    /**
     * Drains the mana hatches discovered on the formed structure (Botania).
     * Upstream cached a single {@code manaPoolHatch}; the port keeps the
     * already-existing {@link ManaHandlerList} aggregation used by the Botania
     * controllers, so every installed mana hatch pools into one buffer.
     */
    public boolean consumeMana(long amount, boolean simulate) {
        return amount <= 0L || manaHandler.consumeMana(amount, simulate);
    }

    /** Aggregated mana storage of the formed structure; empty without a hatch. */
    public ManaHandlerList getManaHandler() {
        return manaHandler;
    }

    public long getMana() {
        return manaHandler.getMana();
    }

    public long getMaxMana() {
        return manaHandler.getMaxMana();
    }

    /**
     * Blood Magic provider of the formed structure, or {@code null} when the
     * port has no life essence hatch installed (lookup fails gracefully).
     */
    @Nullable
    public IBloodMagicHatch getBloodMagicHatch() {
        return bloodMagicHatch;
    }

    /**
     * Astral lens provider of the formed structure, or {@code null} when no
     * calibrated lens hatch is installed (lookup fails gracefully).
     */
    @Nullable
    public IAstralHatch getAstralLensHatch() {
        return astralLensHatch;
    }

    /** Drains life essence; returns false cleanly when no provider is installed. */
    public boolean consumeLifeEssence(int amount, boolean simulate) {
        if (amount <= 0) {
            return true;
        }
        return bloodMagicHatch != null && bloodMagicHatch.consumeLifeEssence(amount, simulate);
    }

    /**
     * Validates non-consumable magic authorizations before a recipe starts
     * (port of upstream {@code checkMagicRequirements}).
     *
     * <p>The tarot gate has two layers, matching upstream: an explicit
     * {@code TAROT} recipe property must be present in the hatch, and the
     * process tags {@code EXPERIMENTAL}, {@code MAGIC_CONVERSION},
     * {@code HIDDEN_RITUAL}, {@code RECYCLING} and {@code THREE_MAGIC_SYSTEMS}
     * additionally require their authorizing major arcana.</p>
     */
    public boolean checkMagicRequirements(GTRecipe recipe) {
        return getMagicRequirementFailure(recipe) == null;
    }

    /**
     * Reason the recipe cannot start or continue, or {@code null} when every
     * requirement is satisfied. The messages reuse the existing
     * {@code pollution.magic.failure.*} lang keys: a missing mana or life
     * essence provider reports its resource key, while every missing hatch or
     * unsatisfied astral/tarot gate reports the generic hatch key. The astral
     * branch is the port of upstream's
     * {@code astralLensHatch.matches(condition)} check, and returns the hatch
     * message instead of crashing when no lens exists.
     */
    @Nullable
    public Component getMagicRequirementFailure(GTRecipe recipe) {
        if (MagicRecipeProperties.getManaPerTick(recipe) > 0L && manaHandler.isEmpty()) {
            return Component.translatable("pollution.magic.failure.mana");
        }
        if (MagicRecipeProperties.getLifeEssencePerTick(recipe) > 0 && bloodMagicHatch == null) {
            return Component.translatable("pollution.magic.failure.life_essence");
        }
        if (MagicRecipeProperties.hasVisCost(recipe) && visHatch == null) {
            return Component.translatable("pollution.magic.failure.hatches");
        }
        if (MagicRecipeProperties.getInfusedFluidPerTick(recipe) > 0 && infusedFluidHatch == null) {
            return Component.translatable("pollution.magic.failure.hatches");
        }
        AstralCondition condition = MagicRecipeProperties.getAstralCondition(recipe);
        if (condition.isConfigured() && (astralLensHatch == null || !astralLensHatch.matches(condition))) {
            return Component.translatable("pollution.magic.failure.hatches");
        }
        String tarot = MagicRecipeProperties.getTarot(recipe);
        if (!tarot.isEmpty() && !hasTarot(tarot)) {
            return Component.translatable("pollution.magic.failure.hatches");
        }
        if (!checkTarotProcessGate(recipe)) {
            return Component.translatable("pollution.magic.failure.hatches");
        }
        return null;
    }

    /** Upstream tag gates: certain process domains require their authorizing card. */
    private boolean checkTarotProcessGate(GTRecipe recipe) {
        long tags = getMagicProcessTags(recipe);
        if (MagicProcessTag.hasAny(tags, MagicProcessTag.EXPERIMENTAL) && !hasTarot("the_fool")) {
            return false;
        }
        if (MagicProcessTag.hasAny(tags, MagicProcessTag.MAGIC_CONVERSION) && !hasTarot("the_magician")) {
            return false;
        }
        if (MagicProcessTag.hasAny(tags, MagicProcessTag.HIDDEN_RITUAL) && !hasTarot("the_high_priestess")) {
            return false;
        }
        if (MagicProcessTag.hasAny(tags, MagicProcessTag.RECYCLING)
                && !hasTarot("death") && !hasTarot("judgement")) {
            return false;
        }
        return !MagicProcessTag.hasAny(tags, MagicProcessTag.THREE_MAGIC_SYSTEMS) || hasTarot("the_world");
    }

    private boolean hasTarot(String tarotId) {
        return tarotHatch != null && tarotHatch.hasTarot(tarotId);
    }

    /**
     * Explicit recipe tags win; legacy recipes fall back to the machine's
     * registered profile so they still receive the safe first-batch bonuses.
     */
    public long getMagicProcessTags(GTRecipe recipe) {
        long tags = recipe == null ? 0L : MagicRecipeProperties.getProcessTagMask(recipe);
        if (tags != 0L) {
            return tags;
        }
        return MagicMachineProfileRegistry.getFallbackTags(getDefinition().getId());
    }

    /** Tarot hatch of the formed structure, or {@code null} when none is installed. */
    public ITarotHatch getTarotHatch() {
        return tarotHatch;
    }

    /** Keeps the non-consumable card / lens stable for one running recipe. */
    public void setMagicFocusLocked(boolean locked) {
        if (tarotHatch != null) {
            tarotHatch.setFocusLocked(locked);
        }
        if (astralLensHatch != null) {
            astralLensHatch.setFocusLocked(locked);
        }
    }

    // ////////////////////////////////////
    // ***** UI *****//
    // ////////////////////////////////////

    /** Energy hatches of the formed structure; mirrors {@code WorkableElectricMultiblockMachine}. */
    public EnergyContainerList getEnergyContainer() {
        if (energyContainer == null) {
            energyContainer = createEnergyContainer();
        }
        return energyContainer;
    }

    private EnergyContainerList createEnergyContainer() {
        List<IEnergyContainer> containers = new ArrayList<>();
        var handlers = getCapabilitiesFlat(IO.IN, EURecipeCapability.CAP);
        if (handlers.isEmpty()) {
            handlers = getCapabilitiesFlat(IO.OUT, EURecipeCapability.CAP);
        }
        for (IRecipeHandler<?> handler : handlers) {
            if (handler instanceof IEnergyContainer container) {
                containers.add(container);
            }
        }
        return new EnergyContainerList(containers);
    }

    public double getEnergyFill() {
        EnergyContainerList container = getEnergyContainer();
        long capacity = container.getEnergyCapacity();
        return capacity <= 0L ? 0.0D : (double) container.getEnergyStored() / (double) capacity;
    }

    public double getVisFill() {
        int capacity = getVisCapacity();
        return capacity <= 0 ? 0.0D : (double) getVisStore() / (double) capacity;
    }

    public double getInfusedFluidFill() {
        if (infusedFluidHatch == null) {
            return 0.0D;
        }
        int capacity = infusedFluidHatch.tank.getTankCapacity(0);
        return capacity <= 0 ? 0.0D :
                (double) infusedFluidHatch.tank.getFluidInTank(0).getAmount() / (double) capacity;
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(218, 208, this, entityPlayer).widget(new FancyMachineUIWidget(this, 218, 208));
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 210, 125);
        var screen = new DraggableScrollableWidgetGroup(4, 4, 138, 104);
        screen.setBackground(getScreenTexture());
        screen.addWidget(new LabelWidget(4, 5, self().getBlockState().getBlock().getDescriptionId()));
        screen.addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText)
                .textSupplier(isRemote() ? null : this::addDisplayText)
                .setMaxWidthLimit(128)
                .clickHandler(this::handleDisplayClick));
        group.addWidget(screen);
        group.addWidget(MachineGuiWidgets.progressBar(recipeLogic, 4, 110, 138, 11));
        group.addWidget(MachineGuiWidgets.fractionBar(this::getEnergyFill, 146, 4, 18, 117));
        group.addWidget(MachineGuiWidgets.fractionBar(this::getVisFill, 166, 4, 18, 117));
        group.addWidget(MachineGuiWidgets.fluidBar(this::getInfusedFluidFill, 186, 4, 18, 117));
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        int numParallels;
        boolean exact = false;
        if (recipeLogic.isActive() && recipeLogic.getLastRecipe() != null) {
            numParallels = recipeLogic.getLastRecipe().parallels;
            exact = true;
        } else {
            numParallels = getParallelHatch().map(IParallelHatch::getCurrentParallel).orElse(0);
        }

        MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(recipeLogic.isWorkingEnabled(), recipeLogic.isActive())
                .addEnergyUsageLine(getEnergyContainer())
                .addMachineModeLine(getRecipeType(), getRecipeTypes().length > 1)
                .addParallelsLine(numParallels, exact)
                .addWorkingStatusLine()
                .addProgressLine(recipeLogic)
                .addRecipeFailReasonLine(recipeLogic)
                .addOutputLines(recipeLogic.getLastRecipe());

        if (isFormed()) {
            textList.add(Component.literal("Vis: " + getVisStore() + " / " + getVisCapacity()));
            if (infusedFluidHatch != null) {
                FluidStack stored = infusedFluidHatch.tank.getFluidInTank(0);
                String fluidName = stored.isEmpty() ? "-" : stored.getDisplayName().getString();
                textList.add(Component.literal("Infused Fluid: " + fluidName + " " + stored.getAmount() + " / "
                        + infusedFluidHatch.tank.getTankCapacity(0)));
            }
            if (!manaHandler.isEmpty()) {
                textList.add(Component.translatable("pollution.machine.mana_plate.tier",
                        manaHandler.getTier(), getMana(), getMaxMana()));
            }
            if (bloodMagicHatch != null) {
                textList.add(Component.literal("Life Essence: " + bloodMagicHatch.getLifeEssence()
                        + " / " + bloodMagicHatch.getLifeEssenceCapacity()));
            }
            if (astralLensHatch != null) {
                String constellation = astralLensHatch.getFocusedConstellation();
                textList.add(Component.literal("Astral Focus: " + (constellation.isEmpty() ? "-" : constellation)));
            }
            if (hasCoil()) {
                textList.add(Component.translatable("gtceu.multiblock.blast_furnace.max_temperature",
                        getCurrentTemperature() + "K"));
            }
            if (tarotHatch != null) {
                String activeTarot = tarotHatch.getActiveTarot();
                textList.add(Component.translatable("pollution.machine.tarot_hatch.active",
                        activeTarot.isEmpty() ? "-" : MagicJeiHintResolver.tarotDisplayName(activeTarot)));
            }
        }
        IDisplayUIMachine.super.addDisplayText(textList);
    }

    @Override
    public List<IFancyUIProvider> getSubTabs() {
        return getParts().stream().filter(Objects::nonNull).map(IFancyUIProvider.class::cast).toList();
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        IVoidable.attachConfigurators(configuratorPanel, this);
        IFancyUIMachine.super.attachConfigurators(configuratorPanel);
    }

    @Override
    public void attachTooltips(TooltipsPanel tooltipsPanel) {
        for (IMultiPart part : getParts()) {
            part.attachFancyTooltipsToController(this, tooltipsPanel);
        }
    }
    public static ModifierFunction parallelModifier(MetaMachine machine, GTRecipe recipe) {
        if (!(machine instanceof LayeredMagicTowerMachine tower)) {
            return com.gregtechceu.gtceu.common.data.GTRecipeModifiers.PARALLEL_HATCH.getModifier(machine, recipe);
        }
        int maximum = tower.getParallelHatch().map(hatch -> hatch.getCurrentParallel()).orElse(1);
        maximum = tower.outputParallelLimit(recipe, maximum);
        if (maximum <= 0) return ModifierFunction.NULL;
        int parallels = com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic.getParallelAmount(machine, recipe, maximum);
        if (parallels <= 0) return ModifierFunction.NULL;
        return ModifierFunction.builder().modifyAllContents(
                com.gregtechceu.gtceu.api.recipe.content.ContentModifier.multiplier(parallels))
                .eutMultiplier(parallels).parallels(parallels).build();
    }
}
