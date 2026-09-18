package meowmel.pollution.loaders.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import dev.tc4port.thaumcraft.api.ThaumcraftContent;
import dev.tc4port.thaumcraft.registry.TCBlocks;
import dev.tc4port.thaumcraft.registry.TCItems;
import dev.tc4port.thaumcraft.registry.TCRecipes;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import meowmel.pollution.common.item.PollutionItems;
import meowmel.pollution.common.machine.PollutionMachines;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.BotaniaFlowerBlocks;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Thaumcraft infusion-altar recipes, port of the portable subset of the skipped
 * 1.12 {@code ThaumcraftApi.addInfusionCraftingRecipe} calls in
 * {@code MagicGCYMRecipes} (machine controllers) and
 * {@code MagicChemicalRecipes} (coking core).
 *
 * <p><b>Why this is possible now.</b> TC4R has no Java "add infusion recipe"
 * method, but it registers the vanilla-style recipe serializer
 * {@code thaumcraft:infusion} ({@link TCRecipes#INFUSION_SERIALIZER}) and loads
 * infusion recipes from datapack JSON. The port's GregTech addon recipe
 * provider feeds a built-in dynamic data pack
 * ({@code GTDynamicDataPack.addRecipe(FinishedRecipe)}), so this class emits the
 * exact JSON schema the serializer expects and lets the normal
 * {@code RecipeManager} load it. See {@code docs/TC4R_INFUSION_API.md} for the
 * full API audit.</p>
 *
 * <p><b>Substitutions</b> (task table plus the port's established mappings):</p>
 * <ul>
 *   <li>HyperdimensionalSilver -&gt; NaquadahAlloy, KQGold / Terrasteel -&gt;
 *       TungstenSteel, Mansussteel -&gt; HSSG, Thaumium -&gt; StainlessSteel,
 *       Manasteel -&gt; {@code GTNNMaterials.ManaSteel}</li>
 *   <li>{@code circuitLv..Iv} -&gt; the port's {@code MAGIC_CIRCUIT_*} line
 *       (GTCEu 7.5.3 only ships integrated circuits up to HV)</li>
 *   <li>{@code batteryMv} -&gt; {@link PollutionItems#MAGIC_BATTERY_MV}</li>
 *   <li>{@code oreCrystalAir/Water/Fire/Order/Earth/Entropy} -&gt; the TC4R
 *       crystal cluster blocks ({@code ThaumcraftContent.block("*_crystal_cluster")})</li>
 *   <li>{@code BlocksTC.smelterThaumium} -&gt; {@code alchemical_furnace},
 *       {@code BlocksTC.metalAlchemical} -&gt; {@code alchemical_construct},
 *       {@code BlocksTC.arcaneWorkbench} -&gt; {@code arcane_worktable},
 *       {@code BlocksTC.crystal*} -&gt; the cluster blocks,
 *       {@code ItemsTC.alumentum} -&gt; {@code alumentum}</li>
 *   <li>{@code ModBlocks.pylon} (meta 1) -&gt; Botania natura pylon,
 *       {@code ItemBlockSpecialFlower.ofType("endoflame")} -&gt; the Botania
 *       endoflame block item</li>
 *   <li>upstream {@code Aspect.*} -&gt; the 48 TC4R core aspect ids
 *       ({@code ALCHEMY}/{@code EXCHANGE} -&gt; {@code permutatio},
 *       {@code SOUL} -&gt; {@code spiritus}, ...)</li>
 *   <li>research: upstream {@code INFUSION@2} -&gt; TC4R {@code INFUSION}</li>
 * </ul>
 *
 * <p><b>Ported (24)</b>: the {@code InfusedExchange} converter, the 20 magic
 * single-machine controllers (alloy blast, distillery, brewery, chemical
 * reactor, autoclave, extruder, bender, solidifier, wiremill, sifter, cutter,
 * centrifuge, blast furnace, chemical bath, macerator, electrolyzer, mixer,
 * battery, essence collector, assembler), the essence smelter, the endoflame
 * array and the coking catalyst core.</p>
 *
 * <p><b>Still skipped</b> (all documented in {@code docs/TC4R_INFUSION_API.md}):</p>
 * <ul>
 *   <li>Beam cores, wire coils, vis hatches and the four Botania-style machine
 *       infusions: every one needs {@code ItemsTC.visResonator} /
 *       {@code ItemsTC.morphicResonator}, which TC4R did not port.</li>
 *   <li>{@code industrial_infusion}, {@code node_producer},
 *       {@code magic_fusion_reactor}, {@code large_node_generator},
 *       {@code reactor-frame-*}, {@code reactor-compose-*}: the upstream
 *       {@code PollutionMetaBlocks.FUSION_REACTOR} FRAME/COMPOSE blocks are
 *       unported, and {@code industrial_infusion} also needs
 *       {@code BlocksTC.matrixCost/matrixSpeed} and
 *       {@code ItemsTC.causalityCollapser}.</li>
 *   <li>{@code alloy_blast_casing}: needs {@code plateOctine} (the port's
 *       Octine is dust/fluid only).</li>
 *   <li>{@code magic_greenhouse}: central {@code GTFOTileEntities.GREENHOUSE}
 *       is unported.</li>
 *   <li>Both starstream infusions: the ritual/constellation crystal block and
 *       {@code OBELISK_CORE} are unported.</li>
 *   <li>The arcane-crafting half of the same upstream batch (turbines, glass,
 *       pipes, gearboxes, battery casing, filters): out of scope here; TC4R
 *       supports them the same way via {@code thaumcraft:arcane_shaped}.</li>
 * </ul>
 */
public final class InfusionRecipes {

    private static final String RESEARCH = "INFUSION";

    private InfusionRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        magicMachineControllers(provider);
        magicInfrastructure(provider);
        chemicalInfusions(provider);
    }

    // ////////////////////////////////////
    // ***** magic machine controllers *****//
    // ////////////////////////////////////

    private static void magicMachineControllers(Consumer<FinishedRecipe> provider) {
        // 转换矩阵 InfusedExchange
        infusion(provider, "infused_exchange", PollutionMachines.INFUSED_EXCHANGE.asStack(), 2,
                machine(GTMachines.EXTRACTOR[GTValues.MV]),
                aspects("permutatio", 16, "motus", 16, "aqua", 16),
                ing(gem(PollutionMaterials.Valonite, 1)),
                ing(circuit(GTValues.MV)), ing(circuit(GTValues.MV)),
                ing(crystal("air")), ing(crystal("water")),
                ing(PollutionMagicBlocks.VOID_PRISM.asStack()));

        // 魔法合金高炉 magic_alloy_blast
        infusion(provider, "magic_alloy_blast", PollutionMachines.MAGIC_ALLOY_BLAST.asStack(), 4,
                PollutionItems.HOT_CATALYST_CORE.asStack(),
                aspects("praecantatio", 64, "machina", 128, "ignis", 64),
                ing(PollutionItems.MAGIC_CONTROL_ASSEMBLY.asStack()),
                ing(gem(PollutionMaterials.Valonite, 1)),
                ing(circuit(GTValues.MV)), ing(circuit(GTValues.MV)),
                ing(circuit(GTValues.MV)), ing(circuit(GTValues.MV)),
                ing(crystal("fire")), ing(crystal("order")), ing(crystal("earth")),
                ing(machine(GTMachines.ALLOY_SMELTER[GTValues.MV])),
                ing(PollutionMagicBlocks.SPELL_PRISM_HOT.asStack()));

        // 聚灵阵 essence_collector
        infusion(provider, "essence_collector", PollutionMachines.ESSENCE_COLLECTOR.asStack(), 7,
                PollutionItems.INTEGRATION_CATALYST_CORE.asStack(),
                aspects("praecantatio", 250, "machina", 250, "auram", 250),
                ing(gem(PollutionMaterials.Valonite, 1)),
                ing(crystal("air")), ing(crystal("fire")), ing(crystal("earth")),
                ing(crystal("water")), ing(crystal("order")), ing(crystal("entropy")),
                ing(frame(GTMaterials.HSSG, 1)),
                ing(GTItems.FIELD_GENERATOR_HV.asStack()),
                ing(PollutionMagicBlocks.SPELL_PRISM.asStack()),
                ing(PollutionMagicBlocks.BEAM_CORE_4.asStack()));

        // 魔导电池 magic_battery
        infusion(provider, "magic_battery", PollutionMachines.MAGIC_BATTERY.asStack(), 7,
                PollutionMagicBlocks.FILTER_1.asStack(),
                aspects("potentia", 250, "praecantatio", 128, "machina", 128),
                ing(gem(PollutionMaterials.Valonite, 1)),
                ing(PollutionItems.MAGIC_BATTERY_MV.asStack()),
                ing(PollutionItems.MAGIC_BATTERY_MV.asStack()),
                ing(PollutionItems.MAGIC_BATTERY_MV.asStack()),
                ing(GTItems.FIELD_GENERATOR_MV.asStack()),
                ing(PollutionMagicBlocks.SPELL_PRISM_VOID.asStack()),
                ing(PollutionMagicBlocks.BEAM_CORE_4.asStack()));

        // 蒸馏二合一 magic_distillery
        infusion(provider, "magic_distillery", PollutionMachines.MAGIC_DISTILLERY.asStack(), 5,
                PollutionItems.SEGREGATION_CATALYST_CORE.asStack(),
                aspects("aer", 125, "aqua", 125, "praecantatio", 64, "machina", 128),
                ing(PollutionItems.MAGIC_CONTROL_ASSEMBLY.asStack()),
                ing(gem(PollutionMaterials.Valonite, 1)),
                ing(circuit(GTValues.MV)), ing(circuit(GTValues.MV)),
                ing(circuit(GTValues.MV)), ing(circuit(GTValues.MV)),
                ing(crystal("air")), ing(crystal("water")),
                ing(machine(GTMultiMachines.DISTILLATION_TOWER)),
                ing(PollutionMagicBlocks.SPELL_PRISM_COLD.asStack()));

        // 酿造三合一 magic_brewery
        infusion(provider, "magic_brewery", PollutionMachines.MAGIC_BREWERY.asStack(), 5,
                PollutionItems.INTEGRATION_CATALYST_CORE.asStack(),
                aspects("aer", 125, "aqua", 125, "praecantatio", 64, "machina", 128),
                ing(PollutionItems.MAGIC_CONTROL_ASSEMBLY.asStack()),
                ing(gem(PollutionMaterials.Valonite, 1)),
                ing(circuit(GTValues.MV)), ing(circuit(GTValues.MV)),
                ing(circuit(GTValues.MV)), ing(circuit(GTValues.MV)),
                ing(crystal("air")), ing(crystal("water")),
                ing(machine(GTMachines.BREWERY[GTValues.MV])),
                ing(machine(GTMachines.FLUID_HEATER[GTValues.MV])),
                ing(machine(GTMachines.FERMENTER[GTValues.MV])),
                ing(PollutionMagicBlocks.SPELL_PRISM_COLD.asStack()));

        // 化反 magic_chemical_reactor
        infusion(provider, "magic_chemical_reactor", PollutionMachines.MAGIC_CHEMICAL_REACTOR.asStack(), 5,
                machine(GTMultiMachines.LARGE_CHEMICAL_REACTOR),
                aspects("permutatio", 250, "praecantatio", 64, "machina", 128),
                ing(PollutionItems.MAGIC_CONTROL_ASSEMBLY.asStack()),
                ing(gem(PollutionMaterials.Valonite, 1)),
                ing(PollutionItems.HOT_CATALYST_CORE.asStack()),
                ing(PollutionItems.COLD_CATALYST_CORE.asStack()),
                ing(PollutionItems.INTEGRATION_CATALYST_CORE.asStack()),
                ing(PollutionItems.SEGREGATION_CATALYST_CORE.asStack()),
                ing(crystal("water")),
                ing(PollutionMagicBlocks.POLYTETRAFLUOROETHYLENE_PIPE.asStack()),
                ing(PollutionMagicBlocks.SPELL_PRISM_WATER.asStack()));

        // 高压釜 magic_autoclave
        infusion(provider, "magic_autoclave", PollutionMachines.MAGIC_AUTOCLAVE.asStack(), 5,
                PollutionItems.INTEGRATION_CATALYST_CORE.asStack(),
                aspects("aer", 250, "praecantatio", 64, "machina", 128),
                ing(PollutionItems.MAGIC_CONTROL_ASSEMBLY.asStack()),
                ing(gem(PollutionMaterials.Valonite, 1)),
                ing(circuit(GTValues.MV)), ing(circuit(GTValues.MV)),
                ing(circuit(GTValues.MV)), ing(circuit(GTValues.MV)),
                ing(crystal("air")), ing(crystal("water")),
                ing(machine(GTMachines.AUTOCLAVE[GTValues.MV])),
                ing(PollutionMagicBlocks.SPELL_PRISM_WATER.asStack()),
                ing(PollutionMagicBlocks.SPELL_PRISM_AIR.asStack()));

        // 压模 magic_extruder
        infusion(provider, "magic_extruder", PollutionMachines.MAGIC_EXTRUDER.asStack(), 5,
                PollutionItems.SEGREGATION_CATALYST_CORE.asStack(),
                aspects("instrumentum", 250, "praecantatio", 64, "machina", 128),
                ing(PollutionItems.MAGIC_CONTROL_ASSEMBLY.asStack()),
                ing(gem(PollutionMaterials.Valonite, 1)),
                ing(circuit(GTValues.MV)), ing(circuit(GTValues.MV)),
                ing(circuit(GTValues.MV)), ing(circuit(GTValues.MV)),
                ing(crystal("order")),
                ing(machine(GTMachines.EXTRUDER[GTValues.MV])),
                ing(PollutionMagicBlocks.SPELL_PRISM_ORDER.asStack()));

        // 卷板 magic_bender
        infusion(provider, "magic_bender", PollutionMachines.MAGIC_BENDER.asStack(), 5,
                PollutionItems.INTEGRATION_CATALYST_CORE.asStack(),
                aspects("metallum", 250, "praecantatio", 64, "machina", 128),
                ing(PollutionItems.MAGIC_CONTROL_ASSEMBLY.asStack()),
                ing(gem(PollutionMaterials.Valonite, 1)),
                ing(circuit(GTValues.MV)), ing(circuit(GTValues.MV)),
                ing(circuit(GTValues.MV)), ing(circuit(GTValues.MV)),
                ing(crystal("order")), ing(crystal("earth")),
                ing(machine(GTMachines.BENDER[GTValues.MV])),
                ing(machine(GTMachines.COMPRESSOR[GTValues.MV])),
                ing(machine(GTMachines.FORMING_PRESS[GTValues.MV])),
                ing(machine(GTMachines.FORGE_HAMMER[GTValues.MV])),
                ing(PollutionMagicBlocks.SPELL_PRISM_ORDER.asStack()));

        // 固化三合一 magic_solidifier
        infusion(provider, "magic_solidifier", PollutionMachines.MAGIC_SOLIDIFIER.asStack(), 5,
                PollutionItems.COLD_CATALYST_CORE.asStack(),
                aspects("permutatio", 250, "praecantatio", 64, "machina", 128),
                ing(PollutionItems.MAGIC_CONTROL_ASSEMBLY.asStack()),
                ing(PollutionItems.SEGREGATION_CATALYST_CORE.asStack()),
                ing(PollutionItems.INTEGRATION_CATALYST_CORE.asStack()),
                ing(gem(PollutionMaterials.Valonite, 1)),
                ing(circuit(GTValues.MV)), ing(circuit(GTValues.MV)),
                ing(circuit(GTValues.MV)), ing(circuit(GTValues.MV)),
                ing(crystal("order")), ing(crystal("entropy")),
                ing(machine(GTMachines.FLUID_SOLIDIFIER[GTValues.MV])),
                ing(machine(GTMachines.CANNER[GTValues.MV])),
                ing(machine(GTMachines.EXTRACTOR[GTValues.MV])),
                ing(PollutionMagicBlocks.SPELL_PRISM_VOID.asStack()),
                ing(PollutionMagicBlocks.SPELL_PRISM_ORDER.asStack()));

        // 轧线 magic_wiremill
        infusion(provider, "magic_wiremill", PollutionMachines.MAGIC_WIRE_MILL.asStack(), 5,
                PollutionItems.SEGREGATION_CATALYST_CORE.asStack(),
                aspects("instrumentum", 250, "praecantatio", 64, "machina", 128),
                ing(PollutionItems.MAGIC_CONTROL_ASSEMBLY.asStack()),
                ing(gem(PollutionMaterials.Valonite, 1)),
                ing(circuit(GTValues.MV)), ing(circuit(GTValues.MV)),
                ing(circuit(GTValues.MV)), ing(circuit(GTValues.MV)),
                ing(crystal("order")), ing(crystal("earth")), ing(crystal("fire")),
                ing(machine(GTMachines.WIREMILL[GTValues.MV])),
                ing(PollutionMagicBlocks.SPELL_PRISM_ORDER.asStack()));

        // 筛选 magic_sifter
        infusion(provider, "magic_sifter", PollutionMachines.MAGIC_SIFTER.asStack(), 5,
                PollutionItems.INTEGRATION_CATALYST_CORE.asStack(),
                aspects("vitreus", 250, "praecantatio", 64, "machina", 128),
                ing(PollutionItems.MAGIC_CONTROL_ASSEMBLY.asStack()),
                ing(gem(PollutionMaterials.Valonite, 1)),
                ing(circuit(GTValues.MV)), ing(circuit(GTValues.MV)),
                ing(circuit(GTValues.MV)), ing(circuit(GTValues.MV)),
                ing(crystal("earth")), ing(crystal("air")),
                ing(machine(GTMachines.SIFTER[GTValues.MV])),
                ing(PollutionMagicBlocks.SPELL_PRISM_EARTH.asStack()),
                ing(PollutionMagicBlocks.SPELL_PRISM_AIR.asStack()));

        // 切割 magic_cutter
        infusion(provider, "magic_cutter", PollutionMachines.MAGIC_CUTTER.asStack(), 5,
                PollutionItems.HOT_CATALYST_CORE.asStack(),
                aspects("spiritus", 125, "perditio", 125, "praecantatio", 64, "machina", 128),
                ing(PollutionItems.MAGIC_CONTROL_ASSEMBLY.asStack()),
                ing(gem(PollutionMaterials.Valonite, 1)),
                ing(circuit(GTValues.MV)), ing(circuit(GTValues.MV)),
                ing(circuit(GTValues.MV)), ing(circuit(GTValues.MV)),
                ing(crystal("entropy")), ing(crystal("earth")), ing(crystal("water")),
                ing(machine(GTMachines.CUTTER[GTValues.MV])),
                ing(PollutionMagicBlocks.SPELL_PRISM_HOT.asStack()));

        // 离心 magic_centrifuge
        infusion(provider, "magic_centrifuge", PollutionMachines.MAGIC_CENTRIFUGE.asStack(), 5,
                PollutionItems.SEGREGATION_CATALYST_CORE.asStack(),
                aspects("aer", 250, "praecantatio", 64, "machina", 128),
                ing(PollutionItems.MAGIC_CONTROL_ASSEMBLY.asStack()),
                ing(gem(PollutionMaterials.Valonite, 1)),
                ing(circuit(GTValues.MV)), ing(circuit(GTValues.MV)),
                ing(circuit(GTValues.MV)), ing(circuit(GTValues.MV)),
                ing(crystal("air")),
                ing(machine(GTMachines.CENTRIFUGE[GTValues.MV])),
                ing(machine(GTMachines.THERMAL_CENTRIFUGE[GTValues.MV])),
                ing(PollutionMagicBlocks.SPELL_PRISM_AIR.asStack()));

        // 高炉 magic_blast
        infusion(provider, "magic_blast", PollutionMachines.MAGIC_ELECTRIC_BLAST_FURNACE.asStack(), 5,
                PollutionItems.HOT_CATALYST_CORE.asStack(),
                aspects("ignis", 250, "praecantatio", 64, "machina", 128),
                ing(PollutionItems.MAGIC_CONTROL_ASSEMBLY.asStack()),
                ing(gem(PollutionMaterials.Valonite, 1)),
                ing(circuit(GTValues.MV)), ing(circuit(GTValues.MV)),
                ing(circuit(GTValues.MV)), ing(circuit(GTValues.MV)),
                ing(crystal("fire")),
                ing(machine(GTMachines.ELECTRIC_FURNACE[GTValues.MV])),
                ing(machine(GTMachines.ALLOY_SMELTER[GTValues.MV])),
                ing(PollutionMagicBlocks.SPELL_PRISM_HOT.asStack()));

        // 洗矿 magic_chemical_bath
        infusion(provider, "magic_chemical_bath", PollutionMachines.MAGIC_CHEMICAL_BATH.asStack(), 5,
                PollutionItems.COLD_CATALYST_CORE.asStack(),
                aspects("aqua", 250, "praecantatio", 64, "machina", 128),
                ing(PollutionItems.MAGIC_CONTROL_ASSEMBLY.asStack()),
                ing(gem(PollutionMaterials.Valonite, 1)),
                ing(circuit(GTValues.MV)), ing(circuit(GTValues.MV)),
                ing(circuit(GTValues.MV)), ing(circuit(GTValues.MV)),
                ing(crystal("water")),
                ing(machine(GTMachines.CHEMICAL_BATH[GTValues.MV])),
                ing(machine(GTMachines.ORE_WASHER[GTValues.MV])),
                ing(PollutionMagicBlocks.SPELL_PRISM_WATER.asStack()));

        // 研磨 magic_macerator
        infusion(provider, "magic_macerator", PollutionMachines.MAGIC_MACERATOR.asStack(), 5,
                PollutionItems.SEGREGATION_CATALYST_CORE.asStack(),
                aspects("terra", 250, "praecantatio", 64, "machina", 128),
                ing(PollutionItems.MAGIC_CONTROL_ASSEMBLY.asStack()),
                ing(gem(PollutionMaterials.Valonite, 1)),
                ing(circuit(GTValues.MV)), ing(circuit(GTValues.MV)),
                ing(circuit(GTValues.MV)), ing(circuit(GTValues.MV)),
                ing(crystal("earth")),
                ing(machine(GTMachines.MACERATOR[GTValues.MV])),
                ing(PollutionMagicBlocks.SPELL_PRISM_EARTH.asStack()));

        // 电解 magic_electrolyzer
        infusion(provider, "magic_electrolyzer", PollutionMachines.MAGIC_ELECTROLYZER.asStack(), 5,
                PollutionItems.SEGREGATION_CATALYST_CORE.asStack(),
                aspects("ordo", 250, "praecantatio", 64, "machina", 128),
                ing(PollutionItems.MAGIC_CONTROL_ASSEMBLY.asStack()),
                ing(gem(PollutionMaterials.Valonite, 1)),
                ing(circuit(GTValues.MV)), ing(circuit(GTValues.MV)),
                ing(circuit(GTValues.MV)), ing(circuit(GTValues.MV)),
                ing(crystal("order")),
                ing(machine(GTMachines.ELECTROLYZER[GTValues.MV])),
                ing(PollutionMagicBlocks.SPELL_PRISM_ORDER.asStack()));

        // 搅拌 magic_mixer
        infusion(provider, "magic_mixer", PollutionMachines.MAGIC_MIXER.asStack(), 5,
                PollutionItems.INTEGRATION_CATALYST_CORE.asStack(),
                aspects("perditio", 250, "praecantatio", 64, "machina", 128),
                ing(PollutionItems.MAGIC_CONTROL_ASSEMBLY.asStack()),
                ing(gem(PollutionMaterials.Valonite, 1)),
                ing(circuit(GTValues.MV)), ing(circuit(GTValues.MV)),
                ing(circuit(GTValues.MV)), ing(circuit(GTValues.MV)),
                ing(crystal("entropy")),
                ing(machine(GTMachines.MIXER[GTValues.MV])),
                ing(PollutionMagicBlocks.VOID_PRISM.asStack()));
    }

    // ////////////////////////////////////
    // ***** infrastructure machines *****//
    // ////////////////////////////////////

    private static void magicInfrastructure(Consumer<FinishedRecipe> provider) {
        // 魔法组装机 magic_assembler
        infusion(provider, "magic_assembler", PollutionMachines.MAGIC_ASSEMBLER.asStack(), 10,
                PollutionMagicBlocks.FILTER_5.asStack(),
                aspects("fabrico", 250, "ordo", 128, "praecantatio", 64, "motus", 64),
                ing(PollutionItems.MAGIC_CONTROL_ASSEMBLY.asStack()),
                ing(block(PollutionMaterials.Valonite, 1)),
                ing(frame(GTMaterials.TungstenSteel, 1)),
                ing(frame(GTMaterials.NaquadahAlloy, 1)),
                ing(frame(GTMaterials.TungstenSteel, 1)),
                ing(new ItemStack(TCBlocks.ARCANE_WORKTABLE.get())),
                ing(PollutionItems.INTEGRATION_CATALYST_CORE.asStack()),
                ing(PollutionItems.SEGREGATION_CATALYST_CORE.asStack()),
                ing(GTItems.FIELD_GENERATOR_IV.asStack()),
                ing(GTItems.FIELD_GENERATOR_IV.asStack()),
                ing(GTItems.FIELD_GENERATOR_IV.asStack()),
                ing(GTItems.FIELD_GENERATOR_IV.asStack()),
                ing(machine(GTMachines.ASSEMBLER[GTValues.IV])));

        // 炼金枢纽 essence_smelter
        infusion(provider, "essence_smelter", PollutionMachines.ESSENCE_SMELTER.asStack(), 4,
                new ItemStack(TCBlocks.ALCHEMICAL_FURNACE.get()),
                aspects("permutatio", 128, "ignis", 64, "praecantatio", 32, "machina", 16),
                ing(gem(PollutionMaterials.Valonite, 1)),
                ing(frame(GTMaterials.StainlessSteel, 1)),
                ing(frame(GTMaterials.HSSG, 1)),
                ing(new ItemStack(TCBlocks.ALCHEMICAL_CONSTRUCT.get())),
                ing(new ItemStack(TCBlocks.ALCHEMICAL_CONSTRUCT.get())),
                ing(PollutionItems.INTEGRATION_CATALYST_CORE.asStack()),
                ing(PollutionItems.SEGREGATION_CATALYST_CORE.asStack()),
                ing(GTItems.FIELD_GENERATOR_MV.asStack()),
                ing(GTItems.FIELD_GENERATOR_MV.asStack()),
                ing(GTItems.FIELD_GENERATOR_MV.asStack()),
                ing(GTItems.FIELD_GENERATOR_MV.asStack()),
                ing(machine(GTMachines.CHEMICAL_REACTOR[GTValues.MV])));

        // 火红莲阵列 endoflame_array
        infusion(provider, "endoflame_array", PollutionMachines.ENDOFLAME_ARRAY.asStack(), 6,
                PollutionItems.HOT_CATALYST_CORE.asStack(),
                aspects("herba", 250, "ignis", 128, "praecantatio", 32, "machina", 16),
                ing(frame(GTMaterials.TungstenSteel, 1)),
                ing(new ItemStack(BotaniaBlocks.naturaPylon)),
                ing(new ItemStack(BotaniaFlowerBlocks.endoflame)),
                ing(PollutionMagicBlocks.TERRA_4_CASING.asStack()),
                ing(gem(PollutionMaterials.Valonite, 1)),
                ing(circuit(GTValues.IV)), ing(circuit(GTValues.IV)),
                ing(GTItems.FIELD_GENERATOR_IV.asStack()),
                ing(GTItems.FIELD_GENERATOR_IV.asStack()),
                ing(machine(GTMultiMachines.LARGE_GAS_TURBINE)));
    }

    // ////////////////////////////////////
    // ***** chemical infusions *****//
    // ////////////////////////////////////

    private static void chemicalInfusions(Consumer<FinishedRecipe> provider) {
        // 焦化催化剂核心 coking_core（上游注册在 thaumcraft 命名空间下）
        infusion(provider, "coking_core", PollutionItems.COKING_CATALYST_CORE.asStack(), 3,
                PollutionItems.BLANK_CATALYST_CORE.asStack(),
                aspects("potentia", 128, "auram", 32),
                ing(gem(GTMaterials.Coke, 1)),
                ing(gem(GTMaterials.Coke, 1)),
                ing(gem(GTMaterials.Coal, 1)),
                ing(gem(GTMaterials.Coal, 1)),
                ing(new ItemStack(TCItems.ALUMENTUM.get())),
                ing(new ItemStack(TCItems.ALUMENTUM.get())),
                ing(crystal("entropy")),
                ing(crystal("entropy")));
    }

    // ////////////////////////////////////
    // ***** helpers *****//
    // ////////////////////////////////////

    private static void infusion(Consumer<FinishedRecipe> provider, String name, ItemStack result, int instability,
                                 ItemStack central, Map<String, Integer> aspects, Ingredient... components) {
        if (result.isEmpty()) {
            Pollution.LOGGER.warn("Skipping infusion/{}: result item is missing", name);
            return;
        }
        if (central.isEmpty()) {
            Pollution.LOGGER.warn("Skipping infusion/{}: central item is missing", name);
            return;
        }
        for (Ingredient component : components) {
            if (component.isEmpty()) {
                Pollution.LOGGER.warn("Skipping infusion/{}: a component item is missing", name);
                return;
            }
        }
        provider.accept(new InfusionFinishedRecipe(
                ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, "infusion/" + name),
                Ingredient.of(central), List.of(components), result, instability, aspects));
    }

    private static Ingredient ing(ItemStack... stacks) {
        return Ingredient.of(stacks);
    }

    private static Map<String, Integer> aspects(Object... pairs) {
        Map<String, Integer> aspects = new LinkedHashMap<>();
        for (int index = 0; index + 1 < pairs.length; index += 2) {
            aspects.put((String) pairs[index], (Integer) pairs[index + 1]);
        }
        return aspects;
    }

    private static ItemStack gem(Material material, int count) {
        return ChemicalHelper.get(TagPrefix.gem, material, count);
    }

    private static ItemStack block(Material material, int count) {
        return ChemicalHelper.get(TagPrefix.block, material, count);
    }

    private static ItemStack frame(Material material, int count) {
        return ChemicalHelper.get(TagPrefix.frameGt, material, count);
    }

    private static ItemStack machine(MachineDefinition definition) {
        return definition == null ? ItemStack.EMPTY : definition.asStack();
    }

    private static ItemStack circuit(int tier) {
        if (tier == GTValues.LV) return PollutionItems.MAGIC_CIRCUIT_LV.asStack();
        if (tier == GTValues.MV) return PollutionItems.MAGIC_CIRCUIT_MV.asStack();
        if (tier == GTValues.HV) return PollutionItems.MAGIC_CIRCUIT_HV.asStack();
        if (tier == GTValues.EV) return PollutionItems.MAGIC_CIRCUIT_EV.asStack();
        if (tier == GTValues.IV) return PollutionItems.MAGIC_CIRCUIT_IV.asStack();
        return ItemStack.EMPTY;
    }

    private static ItemStack crystal(String aspect) {
        return new ItemStack(ThaumcraftContent.block(aspect + "_crystal_cluster").asItem());
    }

    private static JsonObject stackJson(ItemStack stack) {
        JsonObject json = new JsonObject();
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        json.addProperty("item", itemId == null ? "minecraft:air" : itemId.toString());
        if (stack.getCount() > 1) {
            json.addProperty("count", stack.getCount());
        }
        return json;
    }

    /**
     * Emits the {@code thaumcraft:infusion} datapack schema (see
     * {@code docs/TC4R_INFUSION_API.md}); GregTech's dynamic data pack writes it
     * into the server's virtual {@code data/pollution/recipes/infusion/} folder
     * and TC4R's {@code InfusionRecipe.Serializer} loads it.
     */
    private static final class InfusionFinishedRecipe implements FinishedRecipe {

        private final ResourceLocation id;
        private final Ingredient central;
        private final List<Ingredient> components;
        private final ItemStack result;
        private final int instability;
        private final Map<String, Integer> aspects;

        private InfusionFinishedRecipe(ResourceLocation id, Ingredient central, List<Ingredient> components,
                                       ItemStack result, int instability, Map<String, Integer> aspects) {
            this.id = id;
            this.central = central;
            this.components = components;
            this.result = result;
            this.instability = instability;
            this.aspects = aspects;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.addProperty("research", RESEARCH);
            json.add("central", central.toJson());
            JsonArray componentArray = new JsonArray();
            for (Ingredient component : components) {
                componentArray.add(component.toJson());
            }
            json.add("components", componentArray);
            json.add("result", stackJson(result));
            json.addProperty("instability", instability);
            JsonObject aspectJson = new JsonObject();
            aspects.forEach(aspectJson::addProperty);
            json.add("aspects", aspectJson);
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return TCRecipes.INFUSION_SERIALIZER.get();
        }

        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }
}
