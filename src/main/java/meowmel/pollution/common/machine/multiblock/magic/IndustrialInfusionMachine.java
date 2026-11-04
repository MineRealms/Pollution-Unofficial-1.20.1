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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import java.util.HashSet;
import java.util.Set;

import java.util.UUID;

/**
 * Industrial infusion: the GT-ified Thaumcraft infusion altar.
 *
 * <p>Upstream gated recipes on the bound player's Thaumcraft research and
 * exposed the diagnostic texts in its UI. The port gates on the machine
 * owner's research through {@code ResearchApi.isComplete}. Only research previously verified for
 * this owner may run while they are offline; missing ownership or invalid research fails closed.</p>
 *
 * <p>Structure deviation: upstream additionally accepted the blood-magic,
 * astral-lens and tarot hatches on the void-prism casing (0..1 each); none of
 * those abilities is registered in the port, so they are not accepted.</p>
 */
public class IndustrialInfusionMachine extends MagicMultiblockController {

    private UUID researchOwner;
    private final Set<String> verifiedResearch = new HashSet<>();

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
        if (!(getLevel() instanceof ServerLevel level)) return false;
        UUID owner = getOwnerUUID();
        if (owner == null) return false;
        if (!owner.equals(researchOwner)) {
            researchOwner = owner;
            verifiedResearch.clear();
            markDirty();
        }
        var player = level.getServer().getPlayerList().getPlayer(owner);
        if (player == null) return verifiedResearch.contains(researchKey);
        try {
            boolean complete = ResearchApi.isComplete(player, ResearchKey.parse(researchKey));
            boolean changed = complete ? verifiedResearch.add(researchKey) : verifiedResearch.remove(researchKey);
            if (changed) markDirty();
            return complete;
        } catch (RuntimeException exception) {
            if (verifiedResearch.remove(researchKey)) markDirty();
            return false;
        }
    }

    @Override
    public void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        if (researchOwner != null && !forDrop) {
            tag.putUUID("VerifiedResearchOwner", researchOwner);
            ListTag keys = new ListTag();
            verifiedResearch.stream().sorted().forEach(key -> keys.add(StringTag.valueOf(key)));
            tag.put("VerifiedResearch", keys);
        }
    }

    @Override
    public void loadCustomPersistedData(CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        researchOwner = tag.hasUUID("VerifiedResearchOwner") ? tag.getUUID("VerifiedResearchOwner") : null;
        verifiedResearch.clear();
        if (researchOwner != null) {
            ListTag keys = tag.getList("VerifiedResearch", Tag.TAG_STRING);
            for (int i = 0; i < keys.size(); i++) verifiedResearch.add(keys.getString(i));
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
