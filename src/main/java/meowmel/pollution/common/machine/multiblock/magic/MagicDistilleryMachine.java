package meowmel.pollution.common.machine.multiblock.magic;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import meowmel.pollution.common.machine.multiblock.MagicMultiblockController;
import meowmel.pollution.common.machine.multiblock.MagicStructureElements;
import meowmel.pollution.common.machine.multiblock.LayeredMagicTowerMachine;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.capability.IFluidHandler;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.*;

/**
 * Magic distillery.
 *
 * <p>One to twelve vertical separation layers, with exactly one output hatch
 * per layer. Distillation fractions leave from bottom to top; distillery mode
 * uses the lowest output. Magic inputs remain on the base casing.</p>
 */
public class MagicDistilleryMachine extends LayeredMagicTowerMachine {

    public MagicDistilleryMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    public Material getMaterial() {
        return PollutionMaterials.InfusedWater;
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return createPattern(definition, false);
    }

    public static List<com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo> createShapes(
            MultiblockMachineDefinition definition) {
        return java.util.stream.IntStream.rangeClosed(1, 12).mapToObj(layers ->
                new com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo(
                        createPattern(definition, true).getPreview(new int[] {1, layers, 1, 1}))).toList();
    }

    private static BlockPattern createPattern(MultiblockMachineDefinition definition, boolean preview) {
        return FactoryBlockPattern.start(RIGHT, FRONT, DOWN)
                .aisle("#####", "#ZZZ#", "#ZCZ#", "#ZZZ#", "#####")
                .aisle(preview ? "##O##" : "##X##", "#XAX#", "XAPAX", "#XAX#", "##X##")
                .setRepeatable(1, 12)
                .aisle("#YSY#", "YAAAY", "YATAY", "YAAAY", "#YYY#")
                .aisle("#YYY#", "YYYYY", "YYYYY", "YYYYY", "#YYY#")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('Y', Predicates.blocks(PollutionMagicBlocks.SPELL_PRISM_COLD.get())
                        .or(MagicStructureElements.magicHatches(false, false,
                                GTRecipeTypes.DISTILLATION_RECIPES, GTRecipeTypes.DISTILLERY_RECIPES)))
                .where('X', Predicates.blocks(PollutionMagicBlocks.SPELL_PRISM_COLD.get())
                        .or(preview ? Predicates.blocks(PollutionMagicBlocks.SPELL_PRISM_COLD.get())
                                : Predicates.abilities(PartAbility.EXPORT_FLUIDS)
                                .setMinLayerLimited(1).setMaxLayerLimited(1)))
                .where('O', Predicates.abilities(PartAbility.EXPORT_FLUIDS))
                .where('Z', Predicates.blocks(PollutionMagicBlocks.SPELL_PRISM_COLD.get()))
                // 上游: hatch('C', MUFFLER_HATCH)（无全局上限）-> 本移植版: GT 消声仓能力
                .where('C', Predicates.abilities(PartAbility.MUFFLER))
                .where('P', Predicates.blocks(PollutionMagicBlocks.TITANIUM_PIPE.get()))
                .where('A', Predicates.blocks(PollutionMagicBlocks.BEAM_CORE_2.get()))
                .where('T', Predicates.blocks(PollutionMagicBlocks.TITANIUM_GEARBOX.get()))
                .where('#', Predicates.any())
                .build();
    }
}
