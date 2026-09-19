package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import dev.arbor.gtnn.data.GTNNMaterials;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.recipes.PORecipeMaps;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.item.PollutionItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import static meowmel.pollution.loaders.recipes.InfusionRecipes.aspects;
import static meowmel.pollution.loaders.recipes.InfusionRecipes.ing;
import static meowmel.pollution.loaders.recipes.InfusionRecipes.infusion;

/**
 * 蕴魔电路（{@code magic_circuit.*}）与魔法电路板（{@code magic_circuit_board.*}）
 * 的完整合成链。
 *
 * <p><b>Why.</b> The port registered the 15 magic circuits and the 15 boards but
 * only carried the board recipes up to LuV (whose legacy recipe in
 * {@link MagicIntegrationRecipes} now uses the obtainable culture-chain
 * substitutes); the circuits had no recipe at all. This class completes the
 * line following the GT circuit-progression pattern, magic-flavoured and
 * tier-scaled.</p>
 *
 * <p><b>Chain shape.</b></p>
 * <ul>
 *   <li><b>Boards ZPM..MAX</b> (8 infusions, plus an alternative LuV route in
 *       addition to the legacy recipe in {@link MagicIntegrationRecipes}):
 *       each board is the previous board
 *       infused with tier metal foil/plate, an advanced SMD part, Salisundus
 *       dust, Botania runes/gems and Thaumcraft pearls. ULV..IV boards keep
 *       their existing recipes (assembler / magic assembler / infusion).</li>
 *   <li><b>Circuits ULV..LuV</b> (7 magic-assembler recipes): previous circuit
 *       + matching board + tier foil/ingot + advanced SMD parts + magic runes,
 *       with 1..3 infused fluids. ULV starts from a vacuum tube instead of a
 *       previous circuit.</li>
 *   <li><b>Circuits ZPM..MAX</b> (8 infusion recipes): the previous circuit is
 *       the central item; pedestals carry the matching board, tier plate/foil,
 *       advanced SMD parts, a Botania rune, a Thaumcraft pearl/eldritch object,
 *       the Pollution runes (white/black/starry) from UHV on and Infinity dust
 *       from UXV on. Aspect cost, pedestal count and instability grow with the
 *       rank, so the top tiers consume the full altar.</li>
 * </ul>
 *
 * <p><b>Budget.</b> Magic-assembler recipes stay at or below the recipe type's
 * 9 item + 3 fluid input slots ({@code setMaxIOSize(9,1,3,0)}); infusion
 * recipes stay at or below the TC4R 8-pedestal convention mirrored by
 * {@link InfusionRecipes#infusion}. The helper methods enforce both caps and
 * only log a warning if a recipe would exceed them.</p>
 *
 * <p><b>Skip list respected.</b> No Astral Sorcery or Blood Magic item is
 * referenced anywhere in this chain; the legacy LuV board recipe in
 * {@link MagicIntegrationRecipes} now takes the culture-chain
 * {@code ips_human_brain} and {@code cultivated_crystal} in place of the gated
 * {@code blood_circuit_advanced} / {@code astral_lens_advanced} inputs. Every
 * stack goes through {@link SafeItems}; a missing item only skips the affected
 * recipe.</p>
 */
public final class MagicCircuitRecipes {

    /** Magic assembler input budget: {@code PORecipeMaps.MAGIC_ASSEMBLER_RECIPES}. */
    private static final int MAX_ASSEMBLER_ITEMS = 9;
    private static final int MAX_ASSEMBLER_FLUIDS = 3;
    /** TC4R infusion pedestal budget mirrored by {@link InfusionRecipes}. */
    private static final int MAX_PEDESTALS = 8;

    /** GT advanced SMD part used by each high tier, cycled per rank 0..7. */
    private static final String[] SMD_TYPES = {
            "advanced_smd_capacitor", "advanced_smd_transistor", "advanced_smd_diode",
            "advanced_smd_inductor", "advanced_smd_resistor", "advanced_smd_capacitor",
            "advanced_smd_transistor", "advanced_smd_diode",
    };

    private MagicCircuitRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        int boards = boardInfusions(provider);
        int assembled = assembledCircuits(provider);
        int infused = infusedCircuits(provider);
        Pollution.LOGGER.info("[circuit] registered {} magic circuit board infusions "
                + "(LuV alternative + ZPM..MAX)", boards);
        Pollution.LOGGER.info("[circuit] registered {} magic circuit recipes in the magic assembler "
                + "(ULV..LuV)", assembled);
        Pollution.LOGGER.info("[circuit] registered {} magic circuit infusions (ZPM..MAX)", infused);
    }

    // ////////////////////////////////////
    // ***** circuit boards: LuV alternative + ZPM..MAX *****//
    // ////////////////////////////////////

    private static int boardInfusions(Consumer<FinishedRecipe> provider) {
        int added = 0;
        if (luvBoardAlternative(provider)) {
            added++;
        }
        for (int tier = GTValues.ZPM; tier <= GTValues.MAX; tier++) {
            if (highBoardInfusion(provider, tier)) {
                added++;
            }
        }
        return added;
    }

    /**
     * Obtainable LuV board alternative. The legacy recipe in
     * {@link MagicIntegrationRecipes} also builds this board from the culture
     * chain ({@code living_magic_biofilm}, {@code ips_human_brain}) and
     * {@code cultivated_crystal}; this infusion adds a second, purely magical
     * route from NaquadahAlloy, advanced SMDs and Botania mana parts so the
     * ZPM+ chain below stays self-consistent either way.
     */
    private static boolean luvBoardAlternative(Consumer<FinishedRecipe> provider) {
        ItemStack result = board(GTValues.LuV);
        ItemStack central = board(GTValues.IV);
        ItemStack foil = ChemicalHelper.get(TagPrefix.foil, GTMaterials.NaquadahAlloy, 8);
        ItemStack salis = dust(PollutionMaterials.Salisundus, 8);
        ItemStack smdCapacitor = SafeItems.gt("advanced_smd_capacitor", 4);
        ItemStack smdTransistor = SafeItems.gt("advanced_smd_transistor", 4);
        if (anyEmpty(result, central, foil, salis, smdCapacitor, smdTransistor)) {
            Pollution.LOGGER.warn("[circuit] skipping the LuV magic circuit board alternative: "
                    + "a required item is missing");
            return false;
        }
        return infusion(provider, "magic_circuit_board_luv_alternative", result, 6, central,
                aspects("fabrico", 48, "praecantatio", 64, "machina", 64, "ordo", 32, "auram", 32),
                components(salis, foil, smdCapacitor, smdTransistor,
                        botania("mana_diamond", 2), botania("mana_pearl", 2),
                        botania("gaia_ingot", 1), botania("rune_spring", 2)));
    }

    /** ZPM..MAX: previous board + tier metal + SMD + Salisundus + magic core. */
    private static boolean highBoardInfusion(Consumer<FinishedRecipe> provider, int tier) {
        int rank = tier - GTValues.ZPM;
        ItemStack result = board(tier);
        ItemStack central = board(tier - 1);
        ItemStack foil = part(TagPrefix.foil, 8 + 4 * (rank / 4), tierMetals(tier));
        ItemStack plate = part(TagPrefix.plate, 4 + 2 * (rank / 4), tierMetals(tier));
        ItemStack smd = SafeItems.gt(SMD_TYPES[rank], 8);
        ItemStack salis = dust(PollutionMaterials.Salisundus, 8 + 8 * rank);
        ItemStack core = magicCore(rank);
        ItemStack rune = botaniaRune(rank);
        if (anyEmpty(result, central, foil, plate, smd, salis, core, rune)) {
            Pollution.LOGGER.warn("[circuit] skipping the {} magic circuit board infusion: "
                    + "a required item is missing", tierName(tier));
            return false;
        }
        ItemStack thaumium = rank <= 2 ? SafeItems.byId("thaumcraft", "thaumium_ingot", 2 + rank)
                : ItemStack.EMPTY;
        ItemStack voidIngot = rank >= 3 && rank <= 4
                ? SafeItems.byId("thaumcraft", "void_ingot", 1 + rank / 2) : ItemStack.EMPTY;
        ItemStack infinity = rank >= 5 ? infinityDust(1 << (rank - 5)) : ItemStack.EMPTY;
        return infusion(provider, "magic_circuit_board_" + tierName(tier), result, 5 + 2 * rank, central,
                boardAspects(rank),
                components(salis, foil, plate, smd, core, pollutionRune(rank), rune, infinity,
                        thaumium, voidIngot));
    }

    // ////////////////////////////////////
    // ***** circuits ULV..LuV: magic assembler *****//
    // ////////////////////////////////////

    private static int assembledCircuits(Consumer<FinishedRecipe> provider) {
        int added = 0;
        for (int tier = GTValues.ULV; tier <= GTValues.LuV; tier++) {
            if (assembledCircuit(provider, tier)) {
                added++;
            }
        }
        return added;
    }

    /** Previous circuit + board + tier materials, 1..3 infused fluids. */
    private static boolean assembledCircuit(Consumer<FinishedRecipe> provider, int tier) {
        ItemStack result = circuit(tier);
        ItemStack board = board(tier);
        ItemStack previous = tier == GTValues.ULV ? ItemStack.EMPTY : circuit(tier - 1);
        List<ItemStack> required = new ArrayList<>();
        List<ItemStack> optional = new ArrayList<>();
        List<FluidStack> fluids = new ArrayList<>();
        int duration = 100;
        switch (tier) {
            case GTValues.ULV -> {
                required.add(SafeItems.gt("vacuum_tube", 2));
                required.add(dust(PollutionMaterials.Salisundus, 2));
                required.add(part(TagPrefix.foil, 4, GTMaterials.Tin));
                optional.add(botania("mana_powder", 4));
                addFluid(fluids, fluid(GTMaterials.Glue, 100));
                duration = 100;
            }
            case GTValues.LV -> {
                required.add(part(TagPrefix.foil, 4, GTMaterials.Copper));
                required.add(ingot(GTNNMaterials.ManaSteel, 1));
                required.add(dust(PollutionMaterials.Salisundus, 2));
                optional.add(botania("mana_powder", 4));
                optional.add(botania("rune_mana", 1));
                addFluid(fluids, fluid(PollutionMaterials.InfusedAura, 100));
                addFluid(fluids, fluid(GTMaterials.Glue, 100));
                duration = 120;
            }
            case GTValues.MV -> {
                required.add(part(TagPrefix.foil, 4, GTMaterials.Silver));
                required.add(ingot(GTNNMaterials.ManaSteel, 2));
                required.add(dust(PollutionMaterials.Salisundus, 2));
                optional.add(botania("mana_diamond", 1));
                optional.add(botania("rune_air", 1));
                addFluid(fluids, fluid(PollutionMaterials.InfusedAura, 200));
                addFluid(fluids, fluid(PollutionMaterials.InfusedLife, 100));
                duration = 160;
            }
            case GTValues.HV -> {
                required.add(part(TagPrefix.foil, 4, GTMaterials.Gold));
                required.add(ingot(GTNNMaterials.Elementium, 2));
                required.add(ChemicalHelper.get(TagPrefix.gem, GTMaterials.CertusQuartz, 2));
                optional.add(botania("mana_pearl", 1));
                optional.add(botania("rune_earth", 1));
                addFluid(fluids, fluid(PollutionMaterials.InfusedAura, 300));
                addFluid(fluids, fluid(PollutionMaterials.InfusedMagic, 100));
                addFluid(fluids, fluid(PollutionMaterials.InfusedLight, 100));
                duration = 200;
            }
            case GTValues.EV -> {
                required.add(part(TagPrefix.foil, 8, GTMaterials.Aluminium));
                required.add(ingot(GTNNMaterials.TerraSteel, 2));
                required.add(SafeItems.gt("advanced_smd_capacitor", 2));
                required.add(SafeItems.gt("advanced_smd_transistor", 2));
                optional.add(botania("pixie_dust", 4));
                optional.add(botania("rune_water", 1));
                addFluid(fluids, fluid(PollutionMaterials.InfusedAura, 500));
                addFluid(fluids, fluid(PollutionMaterials.InfusedMagic, 200));
                addFluid(fluids, fluid(PollutionMaterials.InfusedLight, 200));
                duration = 240;
            }
            case GTValues.IV -> {
                required.add(part(TagPrefix.foil, 8, GTMaterials.TungstenSteel));
                required.add(dust(PollutionMaterials.Salisundus, 4));
                required.add(SafeItems.gt("advanced_smd_diode", 2));
                required.add(SafeItems.gt("advanced_smd_inductor", 2));
                required.add(botania("gaia_ingot", 1));
                required.add(botania("terrasteel_ingot", 2));
                optional.add(botania("rune_fire", 1));
                addFluid(fluids, fluid(PollutionMaterials.InfusedAura, 1000));
                addFluid(fluids, fluid(PollutionMaterials.InfusedOrder, 500));
                addFluid(fluids, fluid(PollutionMaterials.InfusedMagic, 500));
                duration = 300;
            }
            default -> {
                required.add(part(TagPrefix.foil, 8, GTMaterials.NaquadahAlloy));
                required.add(SafeItems.byId("thaumcraft", "void_ingot", 2));
                required.add(SafeItems.gt("advanced_smd_resistor", 4));
                required.add(SafeItems.gt("advanced_smd_capacitor", 4));
                required.add(botania("gaia_ingot", 2));
                optional.add(botania("dragonstone", 2));
                optional.add(botania("rune_spring", 1));
                addFluid(fluids, fluid(PollutionMaterials.InfusedAura, 2000));
                addFluid(fluids, fluid(PollutionMaterials.InfusedLife, 1000));
                addFluid(fluids, fluid(PollutionMaterials.InfusedLight, 1000));
                duration = 400;
            }
        }
        List<ItemStack> inputs = new ArrayList<>();
        if (!previous.isEmpty()) {
            inputs.add(previous);
        }
        inputs.add(board);
        inputs.addAll(required);
        inputs.addAll(optional);
        if (result.isEmpty() || board.isEmpty() || (tier != GTValues.ULV && previous.isEmpty())
                || anyEmpty(required.toArray(new ItemStack[0])) || fluids.isEmpty()) {
            Pollution.LOGGER.warn("[circuit] skipping the {} magic circuit: a required item or fluid is missing",
                    tierName(tier));
            return false;
        }
        if (inputs.size() > MAX_ASSEMBLER_ITEMS) {
            Pollution.LOGGER.warn("[circuit] skipping the {} magic circuit: {} item inputs exceed the "
                    + "magic assembler budget of {}", tierName(tier), inputs.size(), MAX_ASSEMBLER_ITEMS);
            return false;
        }
        GTRecipeBuilder builder = GTRecipeBuilder.of(id("magic_circuit_" + tierName(tier)),
                PORecipeMaps.MAGIC_ASSEMBLER_RECIPES);
        for (ItemStack stack : inputs) {
            builder.inputItems(stack);
        }
        for (FluidStack stack : fluids) {
            builder.inputFluids(stack);
        }
        builder.outputItems(result)
                .duration(duration)
                .EUt(GTValues.VA[tier])
                .save(provider);
        return true;
    }

    // ////////////////////////////////////
    // ***** circuits ZPM..MAX: TC4R infusion *****//
    // ////////////////////////////////////

    private static int infusedCircuits(Consumer<FinishedRecipe> provider) {
        int added = 0;
        for (int tier = GTValues.ZPM; tier <= GTValues.MAX; tier++) {
            if (infusedCircuit(provider, tier)) {
                added++;
            }
        }
        return added;
    }

    /** Previous circuit central + board + tier metal + SMD + magic core pedestals. */
    private static boolean infusedCircuit(Consumer<FinishedRecipe> provider, int tier) {
        int rank = tier - GTValues.ZPM;
        ItemStack result = circuit(tier);
        ItemStack central = circuit(tier - 1);
        ItemStack board = board(tier);
        ItemStack plate = part(TagPrefix.plate, 4 + 2 * (rank / 4), tierMetals(tier));
        ItemStack foil = part(TagPrefix.foil, 8 + 4 * (rank / 4), tierMetals(tier));
        ItemStack smd = SafeItems.gt(SMD_TYPES[rank], 4 + 4 * (rank / 4));
        ItemStack core = magicCore(rank);
        ItemStack rune = botaniaRune(rank);
        if (anyEmpty(result, central, board, plate, foil, smd, core, rune)) {
            Pollution.LOGGER.warn("[circuit] skipping the {} magic circuit infusion: "
                    + "a required item is missing", tierName(tier));
            return false;
        }
        ItemStack thaumium = rank <= 2 ? SafeItems.byId("thaumcraft", "thaumium_ingot", 2 + rank)
                : ItemStack.EMPTY;
        ItemStack infinity = rank >= 5 ? infinityDust(1 << (rank - 5)) : ItemStack.EMPTY;
        return infusion(provider, "magic_circuit_" + tierName(tier), result, 6 + 2 * rank, central,
                circuitAspects(rank),
                components(board, plate, foil, smd, rune, core, pollutionRune(rank), infinity, thaumium));
    }

    // ////////////////////////////////////
    // ***** aspect tables *****//
    // ////////////////////////////////////

    private static Map<String, Integer> boardAspects(int rank) {
        Map<String, Integer> map = aspects("fabrico", 64 + 64 * rank, "praecantatio", 64 + 64 * rank,
                "machina", 48 + 48 * rank, "ordo", 32 + 16 * rank, "auram", 32 + 32 * rank);
        if (rank >= 2) {
            map.put("cognitio", 32 + 16 * rank);
        }
        if (rank >= 4) {
            map.put("alienis", 16 + 16 * rank);
        }
        return map;
    }

    private static Map<String, Integer> circuitAspects(int rank) {
        Map<String, Integer> map = aspects("praecantatio", 128 + 96 * rank, "machina", 64 + 64 * rank,
                "fabrico", 64 + 64 * rank, "auram", 64 + 48 * rank, "ordo", 32 + 32 * rank);
        if (rank >= 2) {
            map.put("cognitio", 32 + 32 * rank);
        }
        if (rank >= 3) {
            map.put("alienis", 32 + 32 * rank);
        }
        return map;
    }

    // ////////////////////////////////////
    // ***** tier tables *****//
    // ////////////////////////////////////

    private static ItemStack circuit(int tier) {
        return switch (tier) {
            case GTValues.ULV -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_ULV);
            case GTValues.LV -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_LV);
            case GTValues.MV -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_MV);
            case GTValues.HV -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_HV);
            case GTValues.EV -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_EV);
            case GTValues.IV -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_IV);
            case GTValues.LuV -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_LUV);
            case GTValues.ZPM -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_ZPM);
            case GTValues.UV -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_UV);
            case GTValues.UHV -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_UHV);
            case GTValues.UEV -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_UEV);
            case GTValues.UIV -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_UIV);
            case GTValues.UXV -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_UXV);
            case GTValues.OpV -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_OPV);
            case GTValues.MAX -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_MAX);
            default -> ItemStack.EMPTY;
        };
    }

    private static ItemStack board(int tier) {
        return switch (tier) {
            case GTValues.ULV -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_BOARD_ULV);
            case GTValues.LV -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_BOARD_LV);
            case GTValues.MV -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_BOARD_MV);
            case GTValues.HV -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_BOARD_HV);
            case GTValues.EV -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_BOARD_EV);
            case GTValues.IV -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_BOARD_IV);
            case GTValues.LuV -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_BOARD_LUV);
            case GTValues.ZPM -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_BOARD_ZPM);
            case GTValues.UV -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_BOARD_UV);
            case GTValues.UHV -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_BOARD_UHV);
            case GTValues.UEV -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_BOARD_UEV);
            case GTValues.UIV -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_BOARD_UIV);
            case GTValues.UXV -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_BOARD_UXV);
            case GTValues.OpV -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_BOARD_OPV);
            case GTValues.MAX -> SafeItems.of(PollutionItems.MAGIC_CIRCUIT_BOARD_MAX);
            default -> ItemStack.EMPTY;
        };
    }

    /** ZPM NaquadahAlloy -> Tritanium, UV Neutronium, UHV Naquadria, ... MAX Neutronium. */
    private static Material[] tierMetals(int tier) {
        return switch (tier) {
            case GTValues.LuV -> new Material[] { GTMaterials.NaquadahAlloy, GTMaterials.TungstenSteel };
            case GTValues.ZPM -> new Material[] { GTMaterials.Tritanium, GTMaterials.NaquadahAlloy };
            case GTValues.UV -> new Material[] { GTMaterials.Neutronium, GTMaterials.Tritanium };
            case GTValues.UHV -> new Material[] { GTMaterials.Naquadria, GTMaterials.Neutronium };
            case GTValues.UEV -> new Material[] { GTMaterials.Duranium, GTMaterials.Neutronium };
            case GTValues.UIV -> new Material[] { GTMaterials.Trinium, GTMaterials.Duranium };
            case GTValues.UXV -> new Material[] { GTMaterials.Neutronium, GTMaterials.Trinium };
            case GTValues.OpV -> new Material[] { GTMaterials.Neutronium, GTMaterials.Naquadria };
            default -> new Material[] { GTMaterials.Neutronium, GTMaterials.Tritanium };
        };
    }

    /** Botania seasonal rune accent, cycled per rank. */
    private static ItemStack botaniaRune(int rank) {
        return switch (rank % 4) {
            case 0 -> botania("rune_summer", 2);
            case 1 -> botania("rune_autumn", 2);
            case 2 -> botania("rune_winter", 2);
            default -> botania("rune_spring", 2);
        };
    }

    /** Pollution runes from UHV on: white -> black -> starry. */
    private static ItemStack pollutionRune(int rank) {
        if (rank >= 5) {
            return SafeItems.of(PollutionItems.STARRY_RUNE, 2);
        }
        if (rank >= 4) {
            return SafeItems.of(PollutionItems.BLACK_RUNE, 1);
        }
        if (rank >= 2) {
            return SafeItems.of(PollutionItems.WHITE_RUNE, 1);
        }
        return ItemStack.EMPTY;
    }

    /** Thaumcraft/Botania core accent of the rank: pearl -> gaia -> eldritch object. */
    private static ItemStack magicCore(int rank) {
        return switch (rank) {
            case 0 -> botania("mana_pearl", 2);
            case 1 -> botania("gaia_ingot", 1);
            case 2 -> SafeItems.byId("thaumcraft", "primordial_pearl", 1);
            case 3 -> SafeItems.byId("thaumcraft", "eldritch_object", 2);
            case 4 -> SafeItems.byId("thaumcraft", "primordial_pearl", 2);
            case 5 -> SafeItems.byId("thaumcraft", "eldritch_object", 4);
            case 6 -> SafeItems.byId("thaumcraft", "primordial_pearl", 4);
            default -> SafeItems.byId("thaumcraft", "eldritch_object", 8);
        };
    }

    private static ItemStack infinityDust(int count) {
        if (count <= 0 || GTNNMaterials.Infinity == null) {
            return ItemStack.EMPTY;
        }
        return ChemicalHelper.get(TagPrefix.dust, GTNNMaterials.Infinity, count);
    }

    // ////////////////////////////////////
    // ***** helpers *****//
    // ////////////////////////////////////

    /** First non-empty part of the candidate materials; EMPTY when none carries the prefix. */
    private static ItemStack part(TagPrefix prefix, int count, Material... materials) {
        for (Material material : materials) {
            if (material == null) {
                continue;
            }
            ItemStack stack = ChemicalHelper.get(prefix, material, count);
            if (!stack.isEmpty()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    /** Builds the pedestal array, dropping missing stacks and capping at 8 components. */
    private static Ingredient[] components(ItemStack... stacks) {
        List<Ingredient> components = new ArrayList<>();
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            if (components.size() >= MAX_PEDESTALS) {
                Pollution.LOGGER.warn("[circuit] more than {} pedestal components, dropping the extras",
                        MAX_PEDESTALS);
                break;
            }
            components.add(ing(stack));
        }
        return components.toArray(new Ingredient[0]);
    }

    private static void addFluid(List<FluidStack> fluids, FluidStack stack) {
        if (!stack.isEmpty()) {
            fluids.add(stack);
        }
    }

    private static boolean anyEmpty(ItemStack... stacks) {
        for (ItemStack stack : stacks) {
            if (stack == null || stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static ItemStack dust(Material material, int count) {
        return ChemicalHelper.get(TagPrefix.dust, material, count);
    }

    private static ItemStack ingot(Material material, int count) {
        return ChemicalHelper.get(TagPrefix.ingot, material, count);
    }

    private static ItemStack botania(String path, int count) {
        return SafeItems.byId("botania", path, count);
    }

    /** @return the fluid stack, or an empty stack when the material has no fluid in this GTCEu build */
    private static FluidStack fluid(Material material, int amount) {
        if (material == null || !material.hasFluid()) {
            return FluidStack.EMPTY;
        }
        return material.getFluid(amount);
    }

    private static String tierName(int tier) {
        return GTValues.VN[tier].toLowerCase(Locale.ROOT);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, "circuit/" + path);
    }
}
