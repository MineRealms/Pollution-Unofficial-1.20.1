package meowmel.pollution.common.machine.multiblock.astral;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.FluidsAS;
import meowmel.pollution.api.metatileentity.POMultiblockAbility;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import meowmel.pollution.common.machine.multiblock.MagicMultiblockController;
import meowmel.pollution.common.machine.multiblock.MagicStructureElements;
import net.minecraft.network.chat.Component;

/** The five upstream Astral processing structures, with their original dimensions. */
public class AstralProcessingMachine extends MagicMultiblockController {
    private final boolean advancedLens;

    public AstralProcessingMachine(IMachineBlockEntity holder, boolean advancedLens) {
        super(holder);
        this.advancedLens = advancedLens;
    }


    @Override
    public Component getMagicRequirementFailure(GTRecipe recipe) {
        if (advancedLens && (astralLensHatch == null || astralLensHatch.getTier() < GTValues.LuV)) {
            return Component.translatable("pollution.magic.failure.astral_lens");
        }
        return super.getMagicRequirementFailure(recipe);
    }

    private static com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate casing(GTRecipeType type) {
        return MagicStructureElements.magicCasing(BlocksAS.MARBLE_BRICKS.get(), type);
    }

    private static com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate lens() {
        return Predicates.abilities(POMultiblockAbility.ASTRAL_HATCH).setExactLimit(1);
    }

    public static BlockPattern infuser(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("  CCCCC  ", "  CCCCC  ", "         ", "         ", "         ", "         ")
                .aisle(" CCCCCCC ", " CARRRAC ", " P     P ", " P     P ", " AAAAAAA ", "         ")
                .aisle("CCCCCCCCC", "CRLLLLLRC", "         ", "         ", " A     A ", " AAAAAAA ")
                .aisle("CCCCCCCCC", "CRLLLLLRC", "         ", "         ", " A     A ", " A     A ")
                .aisle("CCCCCCCCC", "CRLLILLRC", "         ", "         ", " A     A ", " A     A ")
                .aisle("CCCCCCCCC", "CRLLLLLRC", "         ", "         ", " A     A ", " A     A ")
                .aisle("CCCCCCCCC", "CRLLLLLRC", "         ", "         ", " A     A ", " AAAAAAA ")
                .aisle(" CCCCCCC ", " CARRRAC ", " P     P ", " P     P ", " AAAAAAA ", "         ")
                .aisle("  CCSCC  ", "  CCCCC  ", "         ", "         ", "         ", "         ")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('C', casing(AstralRecipeMaps.INDUSTRIAL_STARLIGHT_INFUSER_RECIPES))
                .where('R', Predicates.blocks(BlocksAS.MARBLE_RUNED.get()))
                .where('P', Predicates.blocks(BlocksAS.MARBLE_PILLAR.get()))
                .where('A', Predicates.blocks(BlocksAS.MARBLE_ARCH.get()))
                .where('L', Predicates.blocks(FluidsAS.LIQUID_STARLIGHT_BLOCK.get()))
                .where('I', Predicates.blocks(BlocksAS.INFUSER.get()))
                .where(' ', Predicates.any()).build();
    }

    public static BlockPattern lightwell(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle(" CCCCC ", " CCCCC ", "       ", "       ", "       ", "       ", "       ")
                .aisle("CCCCCCC", "CARRRAC", "P     P", "P     P", "P     P", "AAAAAAA", "       ")
                .aisle("CCCCCCC", "CRRRRRC", "       ", "       ", "       ", "A     A", "AAAAAAA")
                .aisle("CCCCCCC", "CRRRRRC", "R  W  R", "       ", "       ", "A     A", "A     A")
                .aisle("CCCCCCC", "CRRRRRC", "       ", "       ", "       ", "A     A", "AAAAAAA")
                .aisle("CCCCCCC", "CARRRAC", "P     P", "P     P", "P     P", "AAAAAAA", "       ")
                .aisle(" CCSCC ", " CCCCC ", "       ", "       ", "       ", "       ", "       ")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('C', casing(AstralRecipeMaps.INDUSTRIAL_LIGHTWELL_RECIPES))
                .where('R', Predicates.blocks(BlocksAS.MARBLE_RUNED.get()))
                .where('P', Predicates.blocks(BlocksAS.MARBLE_PILLAR.get()))
                .where('A', Predicates.blocks(BlocksAS.MARBLE_ARCH.get()))
                .where('W', Predicates.blocks(BlocksAS.WELL.get()))
                .where(' ', Predicates.any()).build();
    }

    public static BlockPattern observatory(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("#CCSCC#", "##CCC##", "###R###", "###R###", "###C###")
                .aisle("C#####C", "#P###P#", "##R#R##", "##R#R##", "##C#C##")
                .aisle("C#####C", "#######", "#R###R#", "#R###R#", "#C###C#")
                .aisle("C##A##C", "###P###", "R##A##R", "R#####R", "C##L##C")
                .aisle("C#####C", "#######", "#R###R#", "#R###R#", "#C###C#")
                .aisle("C#####C", "#P###P#", "##R#R##", "##R#R##", "##C#C##")
                .aisle("#CCCCC#", "##CCC##", "###R###", "###R###", "###C###")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('C', casing(AstralRecipeMaps.CELESTIAL_OBSERVATION_RECIPES))
                .where('R', Predicates.blocks(BlocksAS.MARBLE_RUNED.get()))
                .where('P', Predicates.blocks(BlocksAS.MARBLE_PILLAR.get()))
                .where('A', Predicates.blocks(BlocksAS.MARBLE_ARCH.get()))
                .where('L', lens()).where('#', Predicates.air()).build();
    }

    public static BlockPattern calibration(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("#CCCSCCC#", "##CCCCC##", "###RRR###", "####R####", "####R####", "####C####", "#########")
                .aisle("C#######C", "#P#####P#", "##R###R##", "###P#P###", "###R#R###", "###C#C###", "#########")
                .aisle("C#######C", "##P###P##", "#R#####R#", "##R###R##", "##R###R##", "##C###C##", "#########")
                .aisle("C#######C", "###AAA###", "R##A#A##R", "###AAA###", "R##A#A##R", "###C#C###", "#########")
                .aisle("C###A###C", "####P####", "R##A#A##R", "###P#P###", "R##A#A##R", "####C####", "####L####")
                .aisle("C#######C", "###AAA###", "R##A#A##R", "###AAA###", "R##A#A##R", "###C#C###", "#########")
                .aisle("C#######C", "##P###P##", "#R#####R#", "##R###R##", "##R###R##", "##C###C##", "#########")
                .aisle("C#######C", "#P#####P#", "##R###R##", "###P#P###", "###R#R###", "###C#C###", "#########")
                .aisle("#CCCCCCC#", "##CCCCC##", "###RRR###", "####R####", "####R####", "####C####", "#########")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('C', casing(AstralRecipeMaps.CELESTIAL_CALIBRATION_RECIPES))
                .where('R', Predicates.blocks(BlocksAS.MARBLE_RUNED.get()))
                .where('P', Predicates.blocks(BlocksAS.MARBLE_PILLAR.get()))
                .where('A', Predicates.blocks(BlocksAS.MARBLE_ARCH.get()))
                .where('L', lens()).where('#', Predicates.air()).build();
    }

    public static BlockPattern growth(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("##CCC##", "##CRC##", "##C#C##", "##C#C##", "##C#C##", "##CGC##", "##CCC##")
                .aisle("#CCCCC#", "#C###C#", "#C#G#C#", "#C###C#", "#C#G#C#", "#C###C#", "#CCCCC#")
                .aisle("CCCCCCC", "C#####C", "C#R#R#C", "C##T##C", "C#R#R#C", "C#####C", "CCCCCCC")
                .aisle("CCCCCCC", "C#R#R#C", "C##T##C", "C##T##C", "C##T##C", "C#R#R#C", "CCCLCCC")
                .aisle("CCCCCCC", "C#####C", "C#R#R#C", "C##T##C", "C#R#R#C", "C#####C", "CCCCCCC")
                .aisle("#CCCCC#", "#C###C#", "#C#G#C#", "#C###C#", "#C#G#C#", "#C###C#", "#CCCCC#")
                .aisle("##CCC##", "##CRC##", "##C#C##", "##CSC##", "##C#C##", "##CGC##", "##CCC##")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('C', casing(AstralRecipeMaps.CELESTIAL_CRYSTAL_GROWTH_RECIPES))
                .where('R', Predicates.blocks(BlocksAS.MARBLE_RUNED.get()))
                .where('G', Predicates.blocks(PollutionMagicBlocks.LAMINATED_GLASS.get()))
                .where('T', Predicates.blocks(BlocksAS.TRANSLUCENT_BLOCK.get()))
                .where('L', lens()).where('#', Predicates.air()).build();
    }
}
