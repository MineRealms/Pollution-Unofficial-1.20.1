package meowmel.pollution.common.machine.multiblock.botania;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import vazkii.botania.common.block.BotaniaBlocks;

/**
 * Structure of the mega mana turbine, converted 1:1 from the 1.12.2 aisles of
 * {@code MetaTileEntityMegaManaTurbine}.
 *
 * <p>Deviations: the GTQT tiered coil casings map to GregTech heating coils
 * ({@link Predicates#heatingCoils()}), the front casing uses the port's
 * {@code MANA_5} mana plate and the fusion casings/glass use the modern GTCEu
 * blocks.</p>
 */
final class MegaManaTurbinePatterns {

    static BlockPattern create(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("AAAAAAA", "ABBBBBA", "ABCCCBA", "ABCJCBA", "ABCCCBA", "ABBBBBA", "AAAAAAA")
                .aisle("ABBBBBA", "BEEEEEB", "BEFGFEB", "BEGDGEB", "BEFGFEB", "BEEEEEB", "ABBBBBA")
                .aisle("ABBBBBA", "BEEEEEB", "BEFGFEB", "BEGDGEB", "BEFGFEB", "BEEEEEB", "ABBBBBA")
                .aisle("ABBBBBA", "BEEEEEB", "BEFGFEB", "BEGDGEB", "BEFGFEB", "BEEEEEB", "ABBBBBA")
                .aisle("ABBBBBA", "BEEEEEB", "BEFGFEB", "BEGDGEB", "BEFGFEB", "BEEEEEB", "ABBBBBA")
                .aisle("ABBBBBA", "BEEEEEB", "BEFGFEB", "BEGDGEB", "BEFGFEB", "BEEEEEB", "ABBBBBA")
                .aisle("ABBBBBA", "BEEEEEB", "BEFGFEB", "BEGDGEB", "BEFGFEB", "BEEEEEB", "ABBBBBA")
                .aisle("ABBBBBA", "BEEEEEB", "BEFGFEB", "BEGDGEB", "BEFGFEB", "BEEEEEB", "ABBBBBA")
                .aisle("ABBBBBA", "BEEEEEB", "BEFGFEB", "BEGDGEB", "BEFGFEB", "BEEEEEB", "ABBBBBA")
                .aisle("ABBBBBA", "BEEEEEB", "BEFGFEB", "BEGDGEB", "BEFGFEB", "BEEEEEB", "ABBBBBA")
                .aisle("ABBBBBA", "BEEEEEB", "BEFGFEB", "BEGDGEB", "BEFGFEB", "BEEEEEB", "ABBBBBA")
                .aisle("ABBBBBA", "BEEEEEB", "BEFGFEB", "BEGDGEB", "BEFGFEB", "BEEEEEB", "ABBBBBA")
                .aisle("ABBBBBA", "BEEEEEB", "BEFGFEB", "BEGDGEB", "BEFGFEB", "BEEEEEB", "ABBBBBA")
                .aisle("AAAAAAA", "ABBBBBA", "ABHHHBA", "ABHIHBA", "ABHHHBA", "ABBBBBA", "AAAAAAA")
                .where('I', Predicates.controller(Predicates.blocks(definition.get())))
                .where('A', Predicates.blocks(PollutionMagicBlocks.MANA_3.get()))
                .where('B', Predicates.blocks(GTBlocks.FUSION_GLASS.get()))
                .where('C', Predicates.blocks(GTBlocks.FUSION_CASING_MK2.get()))
                .where('D', Predicates.blocks(GTBlocks.FUSION_COIL.get()))
                .where('E', Predicates.blocks(BotaniaBlocks.bifrostPerm))
                .where('F', Predicates.blocks(BotaniaBlocks.dreamwoodGlimmering))
                .where('G', Predicates.heatingCoils())
                .where('H', Predicates.blocks(PollutionMagicBlocks.MANA_5.get())
                        .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                        .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(4))
                        .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(4))
                        .or(Predicates.abilities(PartAbility.MUFFLER).setMaxGlobalLimited(1)))
                .where('J', Predicates.blocks(GTBlocks.FUSION_COIL.get())
                        .or(Predicates.abilities(PartAbility.OUTPUT_ENERGY).setMaxGlobalLimited(1))
                        .or(Predicates.abilities(PartAbility.OUTPUT_LASER).setMaxGlobalLimited(1)))
                .build();
    }

    private MegaManaTurbinePatterns() {}
}
