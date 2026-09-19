package meowmel.pollution.common.machine.multiblock;

import com.gregtechceu.gtceu.api.block.ICoilType;
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
import meowmel.pollution.api.capability.IVisHatch;
import meowmel.pollution.api.magic.PollutionAspectMapping;
import meowmel.pollution.api.recipes.properties.MagicRecipeProperties;
import meowmel.pollution.client.gui.MachineGuiWidgets;
import meowmel.pollution.common.machine.part.InfusedFluidHatchMachine;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

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
 * <p>Scope (2026-09-18): Thaumcraft-facing resources (vis, infused fluids) are
 * functional. Mana / life essence / astral / tarot stay reserved until their
 * systems are ported; recipes that ask for them fail the requirement check
 * exactly like upstream did when the hatch was missing.</p>
 *
 * <p>UI (2026-09-19): the controller implements {@link IFancyUIMachine} and
 * {@link IDisplayUIMachine} following GregTech's
 * {@code WorkableElectricMultiblockMachine}: a display panel with the working
 * status, energy usage, machine mode, parallels, progress and output lines,
 * plus energy / vis / infused-fluid bars. Every magic multiblock inherits this
 * screen, and the formed parts are exposed as fancy side tabs.</p>
 */
public abstract class MagicMultiblockController extends WorkableMultiblockMachine
        implements IFancyUIMachine, IDisplayUIMachine {

    protected IVisHatch visHatch;
    protected InfusedFluidHatchMachine infusedFluidHatch;
    protected ICoilType coilType;

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
        for (IMultiPart part : getParts()) {
            if (visHatch == null && part.self() instanceof IVisHatch hatch) {
                visHatch = hatch;
            }
            if (infusedFluidHatch == null && part.self() instanceof InfusedFluidHatchMachine hatch) {
                infusedFluidHatch = hatch;
            }
        }
        Object matchedCoil = getMultiblockState().getMatchContext().get("CoilType");
        coilType = matchedCoil instanceof ICoilType coil ? coil : null;
        energyContainer = createEnergyContainer();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        visHatch = null;
        infusedFluidHatch = null;
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
        return coilType == null ? 0 : coilType.getCoilTemperature();
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
    // ***** Reserved systems *****//
    // ////////////////////////////////////

    /** Botania mana hatch is not ported yet. */
    public boolean consumeMana(long amount, boolean simulate) {
        return amount <= 0;
    }

    /** Blood Magic life essence hatch is not ported yet. */
    public boolean consumeLifeEssence(int amount, boolean simulate) {
        return amount <= 0;
    }

    /**
     * Validates non-consumable magic authorizations before a recipe starts
     * (port of upstream {@code checkMagicRequirements}, Thaumcraft subset).
     */
    public boolean checkMagicRequirements(GTRecipe recipe) {
        if (MagicRecipeProperties.getManaPerTick(recipe) > 0) {
            return false;
        }
        if (MagicRecipeProperties.getLifeEssencePerTick(recipe) > 0) {
            return false;
        }
        if (MagicRecipeProperties.hasVisCost(recipe) && visHatch == null) {
            return false;
        }
        if (MagicRecipeProperties.getInfusedFluidPerTick(recipe) > 0 && infusedFluidHatch == null) {
            return false;
        }
        if (!MagicRecipeProperties.getTarot(recipe).isEmpty()) {
            return false;
        }
        return !recipe.data.contains(MagicRecipeProperties.ASTRAL_CONDITION);
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
            if (hasCoil()) {
                textList.add(Component.translatable("gtceu.multiblock.blast_furnace.max_temperature",
                        getCurrentTemperature() + "K"));
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
}
