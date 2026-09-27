package meowmel.pollution.common.machine.multiblock.astral;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
public final class CelestialCrystalGrowthArrayMachine extends AstralProcessingMachine {
    public CelestialCrystalGrowthArrayMachine(IMachineBlockEntity holder) { super(holder, true); }
    public static BlockPattern createPattern(MultiblockMachineDefinition definition) { return growth(definition); }

    @Override
    public net.minecraft.network.chat.Component getMagicRequirementFailure(com.gregtechceu.gtceu.api.recipe.GTRecipe recipe) {
        var failure = super.getMagicRequirementFailure(recipe);
        if (failure != null) return failure;
        return astralLensHatch == null || !astralLensHatch.hasConstellationDataWafer()
                ? net.minecraft.network.chat.Component.translatable("pollution.magic.failure.astral_wafer") : null;
    }
}
