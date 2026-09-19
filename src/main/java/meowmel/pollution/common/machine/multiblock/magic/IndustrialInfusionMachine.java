package meowmel.pollution.common.machine.multiblock.magic;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import dev.tc4port.thaumcraft.api.research.ResearchApi;
import dev.tc4port.thaumcraft.api.research.ResearchKey;
import meowmel.pollution.api.recipes.properties.MagicRecipeProperties;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.machine.multiblock.MagicMultiblockController;
import meowmel.pollution.common.machine.multiblock.MagicRecipeLogic;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import java.util.UUID;

/**
 * Industrial infusion: the GT-ified Thaumcraft infusion altar.
 *
 * <p>Upstream gated recipes on the bound player's Thaumcraft research and
 * exposed the diagnostic texts in its UI. The port gates on the machine
 * owner's research through {@code ResearchApi.isComplete}; when the owner is
 * missing or offline the gate is skipped (server-friendly fallback), and an
 * unknown research key never blocks.</p>
 *
 * <p>Structure deviation: upstream additionally accepted the blood-magic,
 * astral-lens and tarot hatches on the void-prism casing (0..1 each); none of
 * those abilities is registered in the port, so they are not accepted.</p>
 */
public class IndustrialInfusionMachine extends MagicMultiblockController {

    public IndustrialInfusionMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    public Material getMaterial() {
        return PollutionMaterials.InfusedMagic;
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new IndustrialInfusionRecipeLogic(this);
    }

    boolean ownerHasResearch(String researchKey) {
        if (!(getLevel() instanceof ServerLevel level)) {
            return true;
        }
        UUID owner = getOwnerUUID();
        if (owner == null) {
            return true;
        }
        var player = level.getServer().getPlayerList().getPlayer(owner);
        if (player == null) {
            return true;
        }
        try {
            return ResearchApi.isComplete(player, ResearchKey.parse(researchKey));
        } catch (RuntimeException exception) {
            return true;
        }
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return IndustrialInfusionPatterns.create(definition);
    }

    /** Recipe logic that adds the research gate on top of the magic checks. */
    static final class IndustrialInfusionRecipeLogic extends MagicRecipeLogic {

        private final IndustrialInfusionMachine machine;

        IndustrialInfusionRecipeLogic(IndustrialInfusionMachine machine) {
            super(machine);
            this.machine = machine;
        }

        @Override
        protected ActionResult checkRecipe(GTRecipe recipe) {
            ActionResult result = super.checkRecipe(recipe);
            if (!result.isSuccess()) {
                return result;
            }
            String research = MagicRecipeProperties.getThaumcraftResearch(recipe);
            if (!research.isEmpty() && !machine.ownerHasResearch(research)) {
                return ActionResult.fail(
                        Component.translatable("pollution.magic.failure.research", research), null, null);
            }
            return ActionResult.SUCCESS;
        }
    }
}
