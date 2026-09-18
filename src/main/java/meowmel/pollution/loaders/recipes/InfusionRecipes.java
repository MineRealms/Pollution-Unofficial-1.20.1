package meowmel.pollution.loaders.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import dev.arbor.gtnn.data.GTNNMaterials;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fluids.FluidStack;
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
 * <p><b>Ported (24 + 58 new)</b>: the {@code InfusedExchange} converter, the 20
 * magic single-machine controllers (alloy blast, distillery, brewery, chemical
 * reactor, autoclave, extruder, bender, solidifier, wiremill, sifter, cutter,
 * centrifuge, blast furnace, chemical bath, macerator, electrolyzer, mixer,
 * battery, essence collector, assembler), the essence smelter, the endoflame
 * array and the coking catalyst core. The newly ported batch adds the 18 solar
 * plate upgrades, the five catalyst-core infusions, the artifact infusions
 * (valonite/scabyst/star/stone-1/goggles/wings/soap), the five beam cores, the
 * eight wire coils, the five vis hatches, the four reactor frames, the four
 * reactor compose blocks, the industrial infusion controller, the node
 * producer, the magic fusion reactor, the large node generator and the four
 * Botania-style machine infusions.</p>
 *
 * <p><b>New substitutions</b> (// 上游: X -&gt; 本移植版: Y):</p>
 * <ul>
 *   <li>ItemsTC.visResonator -&gt; TC4R {@code TCItems.ESSENTIA_RESONATOR}</li>
 *   <li>ItemsTC.morphicResonator -&gt; TC4R {@code TCBlocks.NODE_TRANSDUCER}</li>
 *   <li>ItemsTC.causalityCollapser -&gt; TC4R {@code TCItems.PRIMORDIAL_PEARL}</li>
 *   <li>BlocksTC.visBattery -&gt; TC4R {@code TCBlocks.VIS_CHARGE_RELAY}</li>
 *   <li>BlocksTC.infusionMatrix / matrixCost / matrixSpeed -&gt; TC4R
 *       {@code TCBlocks.RUNIC_MATRIX}</li>
 *   <li>ItemsTC.voidSeed -&gt; TC4R {@code TCItems.ELDRITCH_OBJECT}</li>
 *   <li>ItemsTC.creativeFluxSponge -&gt; TC4R {@code TCItems.SANITY_SOAP}</li>
 *   <li>PollutionMetaBlocks.FUSION_REACTOR FRAME_* -&gt; GTCEu
 *       {@code GTBlocks.FUSION_CASING}, COMPOSE_* -&gt;
 *       {@code GTBlocks.FUSION_COIL}</li>
 *   <li>plateMansussteel / frameGtMansussteel -&gt; HSSG plate / frame</li>
 *   <li>plateManasteel -&gt; GTNN ManaSteel ingot (GTNN only carries
 *       ingot/fluid)</li>
 *   <li>plateIgnissteel -&gt; IgnisSteel ingot (the port alloy carries
 *       ingot/fluid)</li>
 *   <li>plateOctine -&gt; Octine dust (the port material carries dust/fluid)</li>
 *   <li>dustSunnarium -&gt; Titanium dust (existing substitution)</li>
 *   <li>blockTerrasteel -&gt; 9x GTNN TerraSteel ingot, blockValonite / Substrate
 *       -&gt; 9x gem/dust (the port materials carry no block form)</li>
 *   <li>blockKeqinggold -&gt; TungstenSteel block, blockHyperdimensionalSilver
 *       -&gt; NaquadahAlloy block, blockUranium235 -&gt; Uranium235 block</li>
 * </ul>
 *
 * <p><b>Still skipped</b>:</p>
 * <ul>
 *   <li>// 跳过: 整合包无 GTFO（{@code magic_greenhouse} 主方块注魔；
 *       温室配方本身已在 {@code MagicGCYMRecipes} 移植）。</li>
 *   <li>// 跳过: 星辉网络方块未移植（{@code starstream_obelisk_core}；
 *       {@code constellation_anchor} 已用 order 水晶簇替代 ritual crystal
 *       并移植）。</li>
 *   <li>The arcane-crafting half of the upstream batch (turbines, glass, pipes,
 *       gearboxes, battery casing, filters) is ported as GT assembler recipes
 *       in {@code MagicGCYMRecipes}.</li>
 * </ul>
 */
public final class InfusionRecipes {

    private static final String RESEARCH = "INFUSION";

    private InfusionRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        magicMachineControllers(provider);
        magicInfrastructure(provider);
        chemicalInfusions(provider);
        solarPlates(provider);
        catalystCoreInfusions(provider);
        artifactInfusions(provider);
        structureInfusions(provider);
        circuitBoardInfusions(provider);
        starstreamInfusions(provider);
        integrationComponents(provider);
    }

    // ////////////////////////////////////
    // ***** integration components *****//
    // ////////////////////////////////////

    /**
     * 球中球、节点稳定框架、魔导控制组件。// 上游: ItemsTC.visResonator ->
     * ESSENTIA_RESONATOR，ItemsTC.morphicResonator / ItemsAS.skyResonator ->
     * NODE_TRANSDUCER，frameGtMansussteel -> HSSG frame，FILTER_MKIII ->
     * 本移植版 FILTER_III。
     */
    private static void integrationComponents(Consumer<FinishedRecipe> provider) {
        ItemStack coreOfIdea = item("core_of_idea");
        if (!coreOfIdea.isEmpty()) {
            infusion(provider, "ball_in_itself_circuit", item("ball_in_itself"), 6,
                    coreOfIdea.copy(),
                    aspects("cognitio", 32, "ordo", 24, "praecantatio", 24),
                    ing(circuit(GTValues.EV)),
                    ing(new ItemStack(TCItems.ESSENTIA_RESONATOR.get())),
                    ing(new ItemStack(TCItems.ESSENTIA_RESONATOR.get())),
                    ing(new ItemStack(TCBlocks.NODE_TRANSDUCER.get())),
                    ing(new ItemStack(TCBlocks.NODE_TRANSDUCER.get())),
                    ing(GTItems.FIELD_GENERATOR_EV.asStack()),
                    ing(GTItems.FIELD_GENERATOR_EV.asStack()));

            infusion(provider, "ball_in_itself_board", item("ball_in_itself"), 6,
                    coreOfIdea.copy(),
                    aspects("cognitio", 32, "ordo", 24, "praecantatio", 24),
                    ing(PollutionItems.MAGIC_CIRCUIT_BOARD_EV.asStack()),
                    ing(new ItemStack(TCItems.ESSENTIA_RESONATOR.get())),
                    ing(new ItemStack(TCItems.ESSENTIA_RESONATOR.get())),
                    ing(new ItemStack(TCBlocks.NODE_TRANSDUCER.get())),
                    ing(new ItemStack(TCBlocks.NODE_TRANSDUCER.get())),
                    ing(GTItems.FIELD_GENERATOR_EV.asStack()),
                    ing(GTItems.FIELD_GENERATOR_EV.asStack()));
        }

        // 节点稳定框架
        infusion(provider, "node_stabilization_frame", PollutionItems.NODE_STABILIZATION_FRAME.asStack(), 8,
                PollutionItems.PACKAGED_AURA_NODE.asStack(),
                aspects("auram", 64, "praecantatio", 32, "ordo", 32),
                ing(PollutionItems.WHITE_RUNE.asStack()),
                ing(item("ball_in_itself")),
                ing(PollutionItems.FILTER_III.asStack()),
                ing(frame(GTMaterials.HSSG, 1)), ing(frame(GTMaterials.HSSG, 1)),
                ing(frame(GTMaterials.HSSG, 1)), ing(frame(GTMaterials.HSSG, 1)));

        // 魔导控制组件（电路与电路板两套）
        controlAssembly(provider, "circuit", circuit(GTValues.MV));
        controlAssembly(provider, "board", PollutionItems.MAGIC_CIRCUIT_BOARD_MV.asStack());
    }

    private static void controlAssembly(Consumer<FinishedRecipe> provider, String route, ItemStack circuit) {
        infusion(provider, "magic_control_assembly_" + route, PollutionItems.MAGIC_CONTROL_ASSEMBLY.asStack(), 4,
                circuit,
                aspects("machina", 32, "praecantatio", 24, "auram", 16),
                ing(PollutionItems.MANA_RESONANCE_COIL.asStack()),
                ing(PollutionItems.BLOOD_PORT.asStack()),
                ing(new ItemStack(TCItems.ESSENTIA_RESONATOR.get())),
                ing(new ItemStack(TCBlocks.NODE_TRANSDUCER.get())),
                ing(GTItems.ROBOT_ARM_MV.asStack()),
                ing(GTItems.SENSOR_MV.asStack()));
    }

    // ////////////////////////////////////
    // ***** starstream *****//
    // ////////////////////////////////////

    /**
     * 星轨星座锚点。// 上游: ritual crystal（POConstellationCrystal）未移植 ->
     * 本移植版: TC4R order 水晶簇；liquid starlight -> InfusedAura。
     * 方尖碑核心（OBELISK_CORE）与星轨主机未移植，保持跳过
     * （// 跳过: 星辉网络方块/机器未移植）。
     */
    private static void starstreamInfusions(Consumer<FinishedRecipe> provider) {
        if (PollutionMagicBlocks.CONSTELLATION_ANCHOR == null) {
            return;
        }
        FluidStack aura = fluid(PollutionMaterials.InfusedAura, 4000);
        if (aura == null) {
            return;
        }
        infusion(provider, "constellation_anchor", PollutionMagicBlocks.CONSTELLATION_ANCHOR.asStack(), 16,
                PollutionMagicBlocks.STARSTREAM_RUNED_CASING.asStack(),
                aspects("praecantatio", 512, "auram", 512, "ordo", 256, "potentia", 256, "lux", 256),
                ing(PollutionItems.STARRY_RUNE.asStack()),
                ing(crystal("order")),
                ing(PollutionItems.ASTRAL_RESONANCE_COIL.asStack()),
                ing(PollutionItems.MAGIC_CIRCUIT_BOARD_UHV.asStack()),
                ing(PollutionItems.CELESTIAL_CALIBRATION_CORE.asStack()),
                ing(crystal("order")),
                ing(GTItems.FIELD_GENERATOR_UHV.asStack()),
                ing(PollutionItems.STARRY_RUNE.asStack()),
                ing(crystal("order")),
                ing(PollutionItems.ASTRAL_RESONANCE_COIL.asStack()),
                ing(PollutionItems.MAGIC_CIRCUIT_BOARD_UHV.asStack()),
                ing(PollutionItems.CELESTIAL_CALIBRATION_CORE.asStack()),
                ing(crystal("order")),
                ing(GTItems.FIELD_GENERATOR_UHV.asStack()));
    }

    // ////////////////////////////////////
    // ***** EV/IV circuit boards *****//
    // ////////////////////////////////////

    /** EV/IV 魔法电路板注魔（上游 MagicIntegrationRecipes）。 */
    private static void circuitBoardInfusions(Consumer<FinishedRecipe> provider) {
        infusion(provider, "magic_circuit_board_ev", PollutionItems.MAGIC_CIRCUIT_BOARD_EV.asStack(), 4,
                PollutionItems.MAGIC_CIRCUIT_BOARD_HV.asStack(),
                aspects("fabrico", 32, "machina", 32, "praecantatio", 24),
                ing(PollutionItems.PRECISION_RUNE_BLANK.asStack()),
                ing(PollutionItems.PRECISION_RUNE_BLANK.asStack()),
                ing(PollutionItems.NATURAL_INFUSED_COIL.asStack()),
                ing(GTItems.SMD_CAPACITOR.asStack(4)), ing(GTItems.SMD_CAPACITOR.asStack(4)),
                ing(GTItems.SMD_TRANSISTOR.asStack(4)), ing(GTItems.SMD_TRANSISTOR.asStack(4)),
                ing(GTItems.SMD_DIODE.asStack(4)), ing(GTItems.SMD_DIODE.asStack(4)));

        infusion(provider, "magic_circuit_board_iv", PollutionItems.MAGIC_CIRCUIT_BOARD_IV.asStack(), 6,
                PollutionItems.MAGIC_CIRCUIT_BOARD_EV.asStack(),
                aspects("praecantatio", 64, "machina", 64, "ordo", 32, "auram", 32),
                ing(PollutionItems.WHITE_RUNE.asStack()),
                ing(PollutionItems.BLACK_RUNE.asStack()),
                ing(PollutionItems.STARRY_RUNE.asStack()),
                ing(PollutionItems.NODE_STABILIZATION_FRAME.asStack()),
                ing(GTItems.SMD_CAPACITOR.asStack(8)), ing(GTItems.SMD_CAPACITOR.asStack(8)),
                ing(GTItems.SMD_TRANSISTOR.asStack(8)), ing(GTItems.SMD_TRANSISTOR.asStack(8)),
                ing(GTItems.SMD_DIODE.asStack(8)), ing(GTItems.SMD_DIODE.asStack(8)));
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
    // ***** solar plates *****//
    // ////////////////////////////////////

    /** 18 solar plate upgrades: kind 1..6 (air, entropy, earth, fire, order, water) x MK1..3. */
    private static void solarPlates(Consumer<FinishedRecipe> provider) {
        String[] kinds = { "air", "entropy", "earth", "fire", "order", "water" };
        String[] crystals = { "aer", "perditio", "terra", "ignis", "ordo", "aqua" };
        int[] aspects = { 16, 64, 250 };
        int[] instability = { 2, 4, 6 };
        ItemStack[][] covers = {
                { GTItems.COVER_SOLAR_PANEL_LV.asStack(), GTItems.COVER_SOLAR_PANEL_MV.asStack(),
                        GTItems.COVER_SOLAR_PANEL_HV.asStack() },
                { GTItems.SENSOR_LV.asStack(), GTItems.SENSOR_MV.asStack(), GTItems.SENSOR_HV.asStack() },
        };
        ItemStack[] circuits = { circuit(GTValues.LV), circuit(GTValues.MV), circuit(GTValues.HV) };
        Material[] cables = { GTMaterials.Tin, GTMaterials.Copper, GTMaterials.Gold };

        for (int kind = 1; kind <= kinds.length; kind++) {
            for (int tierIndex = 0; tierIndex < 3; tierIndex++) {
                MachineDefinition[] plates = PollutionMachines.SOLAR_PLATE[kind];
                if (plates == null || plates.length <= tierIndex || plates[tierIndex] == null) {
                    continue;
                }
                int dustCount = 1 << tierIndex;
                ItemStack central = tierIndex == 0 ? frame(GTMaterials.HSSG, 1)
                        : machine(plates[tierIndex - 1]);
                infusion(provider, "solar/" + kinds[kind - 1] + "_" + (tierIndex + 1),
                        plates[tierIndex].asStack(), instability[tierIndex], central,
                        aspects("potentia", tierIndex == 0 ? 16 : (tierIndex == 1 ? 32 : 64),
                                crystals[kind - 1], aspects[tierIndex]),
                        // 上游: dustSunnarium -> 本移植版: Titanium dust
                        ing(dust(GTMaterials.Titanium, dustCount)),
                        ing(covers[0][tierIndex]),
                        ing(circuits[tierIndex]),
                        ing(crystal(kinds[kind - 1])),
                        ing(covers[1][tierIndex]), ing(covers[1][tierIndex]),
                        ing(PollutionMagicBlocks.AAMINATED_GLASS.asStack()),
                        ing(PollutionMagicBlocks.LAMINATED_GLASS.asStack()),
                        ing(ChemicalHelper.get(TagPrefix.cableGtSingle, cables[tierIndex], 1)));
            }
        }
    }

    // ////////////////////////////////////
    // ***** catalyst cores *****//
    // ////////////////////////////////////

    /** 空白/炽热/极寒/凝聚/分离催化核心（上游 ThaumcraftRecipes.catalyst 注魔版）。 */
    private static void catalystCoreInfusions(Consumer<FinishedRecipe> provider) {
        infusion(provider, "blank_catalyst_core", PollutionItems.BLANK_CATALYST_CORE.asStack(), 6,
                gem(PollutionMaterials.Valonite, 1),
                aspects("permutatio", 64, "motus", 32, "praecantatio", 32, "potentia", 16),
                // 上游: blockSubstrate -> 本移植版: Substrate dust x9（本移植版无方块形态）
                ing(dust(PollutionMaterials.Substrate, 9)),
                ing(dust(PollutionMaterials.Substrate, 9)),
                ing(gem(GTMaterials.Amethyst, 1)),
                ing(gem(GTMaterials.Opal, 1)),
                ing(new ItemStack(TCItems.ESSENTIA_RESONATOR.get())),
                ing(new ItemStack(TCBlocks.NODE_TRANSDUCER.get())),
                ing(new ItemStack(TCItems.PRIMORDIAL_PEARL.get())),
                ing(new ItemStack(TCBlocks.VIS_CHARGE_RELAY.get())));

        infusion(provider, "hot_catalyst_core", PollutionItems.HOT_CATALYST_CORE.asStack(), 3,
                PollutionItems.BLANK_CATALYST_CORE.asStack(),
                aspects("ignis", 128, "auram", 32),
                ing(gem(GTMaterials.Ruby, 1)), ing(gem(GTMaterials.Ruby, 1)),
                ing(crystal("fire")), ing(crystal("fire")),
                ing(Items.BLAZE_POWDER), ing(Items.BLAZE_POWDER),
                // 上游: plateOctine -> 本移植版: Octine dust（本移植版无板形态）
                ing(dust(PollutionMaterials.Octine, 2)), ing(dust(PollutionMaterials.Octine, 2)));

        infusion(provider, "cold_catalyst_core", PollutionItems.COLD_CATALYST_CORE.asStack(), 3,
                PollutionItems.BLANK_CATALYST_CORE.asStack(),
                aspects("gelum", 128, "auram", 32),
                ing(gem(GTMaterials.Sapphire, 1)), ing(gem(GTMaterials.Sapphire, 1)),
                ing(crystal("water")), ing(crystal("water")),
                ing(Blocks.SNOW), ing(Blocks.SNOW), ing(Blocks.ICE), ing(Blocks.ICE));

        infusion(provider, "integration_catalyst_core", PollutionItems.INTEGRATION_CATALYST_CORE.asStack(), 5,
                PollutionItems.BLANK_CATALYST_CORE.asStack(),
                aspects("ordo", 128, "auram", 64),
                ing(gem(GTMaterials.Diamond, 1)), ing(gem(GTMaterials.Diamond, 1)),
                ing(crystal("order")), ing(crystal("order")),
                ing(new ItemStack(TCBlocks.SILVERWOOD_LOG.get())),
                ing(new ItemStack(TCBlocks.SILVERWOOD_LOG.get())),
                ing(dust(GTMaterials.Lead, 2)), ing(dust(GTMaterials.Lead, 2)));

        infusion(provider, "segregation_catalyst_core", PollutionItems.SEGREGATION_CATALYST_CORE.asStack(), 5,
                PollutionItems.BLANK_CATALYST_CORE.asStack(),
                aspects("perditio", 128, "auram", 64),
                ing(Items.ENDER_EYE), ing(Items.ENDER_EYE),
                ing(crystal("entropy")), ing(crystal("entropy")),
                // 上游: ItemsTC.voidSeed -> 本移植版: TC4R ELDRITCH_OBJECT
                ing(new ItemStack(TCItems.ELDRITCH_OBJECT.get())),
                ing(new ItemStack(TCItems.ELDRITCH_OBJECT.get())),
                ing(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Gunpowder, 2)),
                ing(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Gunpowder, 2)));
    }

    // ////////////////////////////////////
    // ***** artifacts (ThaumcraftRecipes.misc) *****//
    // ////////////////////////////////////

    /** 人工法罗钠/痂壳晶/下界之星、黑土贤者之石、护目镜/翅膀、创造肥皂。 */
    private static void artifactInfusions(Consumer<FinishedRecipe> provider) {
        // 人工制作法罗钠
        infusion(provider, "artificial_valonite", gem(PollutionMaterials.Valonite, 1), 4,
                gem(GTMaterials.Diamond, 1),
                aspects("vitium", 16, "auram", 16, "humanus", 16),
                ing(new ItemStack(TCBlocks.SILVERWOOD_LOG.get())),
                ing(Items.DIAMOND),
                // 上游: BlocksTC.crystalTaint -> 本移植版: TC4R FLUX_GOO
                ing(new ItemStack(TCBlocks.FLUX_GOO.get())));

        // 人工制造痂壳晶
        infusion(provider, "artificial_scabyst", gem(PollutionMaterials.Scabyst, 1), 2,
                gem(GTMaterials.Amethyst, 1),
                aspects("instrumentum", 25),
                ing(new ItemStack(TCBlocks.GREATWOOD_LOG.get())),
                ing(gem(GTMaterials.Amethyst, 1)),
                ing(crystal("earth")));

        // 人工下界之星
        infusion(provider, "artificial_star", new ItemStack(Items.NETHER_STAR), 6,
                gem(PollutionMaterials.Valonite, 1),
                aspects("mortuus", 128, "exanimis", 128, "praecantatio", 32, "auram", 16),
                ing(Blocks.DIAMOND_BLOCK), ing(Blocks.EMERALD_BLOCK),
                ing(Blocks.NETHERRACK), ing(Blocks.SOUL_SAND),
                ing(gem(GTMaterials.Amethyst, 1)), ing(gem(GTMaterials.Opal, 1)),
                ing(new ItemStack(TCItems.ELDRITCH_OBJECT.get())),
                ing(new ItemStack(TCItems.ELDRITCH_OBJECT.get())),
                ing(crystal("entropy")));

        // 黑土贤者之石
        ItemStack stone1 = PollutionItems.get("stone_of_philosopher_1") == null ? ItemStack.EMPTY
                : PollutionItems.get("stone_of_philosopher_1").asStack();
        if (!stone1.isEmpty()) {
            infusion(provider, "stone_of_philosopher_1", stone1, 12,
                    PollutionItems.BLANK_CATALYST_CORE.asStack(),
                    aspects("permutatio", 250, "fabrico", 250, "praecantatio", 250, "auram", 64),
                    ing(GTItems.QUANTUM_STAR.asStack()), ing(GTItems.QUANTUM_EYE.asStack()),
                    ing(new ItemStack(TCItems.ELDRITCH_OBJECT.get())),
                    ing(new ItemStack(TCItems.PRIMORDIAL_PEARL.get())),
                    ing(GTItems.FIELD_GENERATOR_EV.asStack()), ing(GTItems.FIELD_GENERATOR_EV.asStack()),
                    ing(GTItems.FIELD_GENERATOR_EV.asStack()), ing(GTItems.FIELD_GENERATOR_EV.asStack()),
                    ing(gem(PollutionMaterials.Valonite, 1)),
                    // 上游: blockSubstrate -> 本移植版: Substrate dust x9
                    ing(dust(PollutionMaterials.Substrate, 9)),
                    // 上游: blockTerrasteel -> 本移植版: GTNN TerraSteel ingot x9
                    ing(ingot(GTNNMaterials.TerraSteel, 9)),
                    ing(machine(PollutionMachines.ESSENCE_COLLECTOR)));
        }

        // 纳米护目镜 / 纳米翅膀
        infusion(provider, "nano_goggles", PollutionItems.NANO_GOGGLES.asStack(), 6,
                GTItems.NANO_HELMET.asStack(),
                aspects("praecantatio", 128, "instrumentum", 128, "terra", 128, "sensus", 64),
                ing(circuit(GTValues.HV)), ing(circuit(GTValues.HV)),
                ing(GTItems.QUANTUM_EYE.asStack()),
                ing(new ItemStack(TCItems.PRIMORDIAL_PEARL.get())),
                ing(GTItems.ELECTRIC_MOTOR_HV.asStack()), ing(GTItems.ELECTRIC_MOTOR_HV.asStack()),
                ing(GTItems.FIELD_GENERATOR_HV.asStack()), ing(GTItems.FIELD_GENERATOR_HV.asStack()),
                ing(gem(PollutionMaterials.Valonite, 1)));

        infusion(provider, "wing_nano", PollutionItems.WING_NANO.asStack(), 6,
                GTItems.NANO_CHESTPLATE.asStack(),
                aspects("praecantatio", 128, "instrumentum", 128, "terra", 128, "sensus", 64),
                ing(circuit(GTValues.HV)), ing(circuit(GTValues.HV)),
                ing(GTItems.QUANTUM_EYE.asStack()),
                ing(new ItemStack(TCItems.PRIMORDIAL_PEARL.get())),
                ing(GTItems.ELECTRIC_PISTON_HV.asStack()), ing(GTItems.ELECTRIC_PISTON_HV.asStack()),
                ing(GTItems.FIELD_GENERATOR_HV.asStack()), ing(GTItems.FIELD_GENERATOR_HV.asStack()),
                ing(gem(PollutionMaterials.Valonite, 1)));

        infusion(provider, "quantum_goggles", PollutionItems.QUANTUM_GOGGLES.asStack(), 8,
                PollutionItems.NANO_GOGGLES.asStack(),
                aspects("praecantatio", 250, "instrumentum", 250, "terra", 250, "sensus", 128),
                ing(circuit(GTValues.IV)), ing(circuit(GTValues.IV)),
                ing(GTItems.QUANTUM_HELMET.asStack()),
                ing(PollutionItems.get("core_of_idea") == null ? ItemStack.EMPTY
                        : PollutionItems.get("core_of_idea").asStack()),
                ing(PollutionItems.get("core_of_idea") == null ? ItemStack.EMPTY
                        : PollutionItems.get("core_of_idea").asStack()),
                ing(GTItems.ELECTRIC_MOTOR_IV.asStack()), ing(GTItems.ELECTRIC_MOTOR_IV.asStack()),
                ing(GTItems.FIELD_GENERATOR_IV.asStack()), ing(GTItems.FIELD_GENERATOR_IV.asStack()),
                ing(gem(PollutionMaterials.Valonite, 1)));

        infusion(provider, "wing_quantum", PollutionItems.WING_QUANTUM.asStack(), 8,
                PollutionItems.WING_NANO.asStack(),
                aspects("praecantatio", 250, "instrumentum", 250, "terra", 250, "sensus", 128),
                ing(circuit(GTValues.IV)), ing(circuit(GTValues.IV)),
                ing(GTItems.QUANTUM_CHESTPLATE.asStack()),
                ing(PollutionItems.get("core_of_idea") == null ? ItemStack.EMPTY
                        : PollutionItems.get("core_of_idea").asStack()),
                ing(PollutionItems.get("core_of_idea") == null ? ItemStack.EMPTY
                        : PollutionItems.get("core_of_idea").asStack()),
                ing(GTItems.ELECTRIC_PISTON_IV.asStack()), ing(GTItems.ELECTRIC_PISTON_IV.asStack()),
                ing(GTItems.FIELD_GENERATOR_IV.asStack()), ing(GTItems.FIELD_GENERATOR_IV.asStack()),
                ing(gem(PollutionMaterials.Valonite, 1)));

        // 创造肥皂（上游: ItemsTC.creativeFluxSponge -> 本移植版: TC4R SANITY_SOAP）
        ItemStack stone3 = PollutionItems.get("stone_of_philosopher_3") == null ? ItemStack.EMPTY
                : PollutionItems.get("stone_of_philosopher_3").asStack();
        if (!stone3.isEmpty()) {
            infusion(provider, "flux_soap", new ItemStack(TCItems.SANITY_SOAP.get()), 12, stone3,
                    aspects("praecantatio", 250, "auram", 250, "ordo", 250, "aqua", 250, "vacuos", 250,
                            "victus", 250, "vitium", 250, "tutamen", 250),
                    ing(circuit(GTValues.ZPM)), ing(circuit(GTValues.ZPM)),
                    ing(PollutionItems.get("core_of_idea") == null ? ItemStack.EMPTY
                            : PollutionItems.get("core_of_idea").asStack()),
                    ing(PollutionItems.get("core_of_idea") == null ? ItemStack.EMPTY
                            : PollutionItems.get("core_of_idea").asStack()),
                    ing(PollutionItems.get("auto_elenchus_device") == null ? ItemStack.EMPTY
                            : PollutionItems.get("auto_elenchus_device").asStack()),
                    ing(PollutionItems.get("bottle_of_phlogistonic_oneness") == null ? ItemStack.EMPTY
                            : PollutionItems.get("bottle_of_phlogistonic_oneness").asStack()),
                    ing(GTItems.FIELD_GENERATOR_LuV.asStack()), ing(GTItems.FIELD_GENERATOR_LuV.asStack()),
                    ing(PollutionItems.BLACK_RUNE.asStack()), ing(PollutionItems.WHITE_RUNE.asStack()),
                    ing(PollutionItems.STARRY_RUNE.asStack()),
                    ing(new ItemStack(TCItems.PRIMORDIAL_PEARL.get())),
                    ing(Items.NETHER_STAR),
                    ing(gem(PollutionMaterials.Valonite, 1)));
        }
    }

    // ////////////////////////////////////
    // ***** structures: cores, coils, hatches, reactor *****//
    // ////////////////////////////////////

    private static void structureInfusions(Consumer<FinishedRecipe> provider) {
        beamCores(provider);
        wireCoils(provider);
        visHatches(provider);
        reactorParts(provider);
        controllers(provider);
        botaniaMachines(provider);
    }

    /** 五个阵法核心。 */
    private static void beamCores(Consumer<FinishedRecipe> provider) {
        ItemStack[] centres = {
                ChemicalHelper.get(TagPrefix.block, GTMaterials.SteelMagnetic, 1),
                new ItemStack(Items.TNT),
                new ItemStack(Items.SOUL_SAND),
                crystal("water"),
                new ItemStack(TCBlocks.VIS_CHARGE_RELAY.get()),
        };
        String[][] aspects = {
                { "machina", "16", "praecantatio", "8", "metallum", "16" },
                { "machina", "16", "praecantatio", "8", "perditio", "16" },
                { "machina", "16", "praecantatio", "8", "sensus", "16" },
                { "machina", "16", "praecantatio", "8", "aqua", "16" },
                { "machina", "16", "praecantatio", "8", "potentia", "16" },
        };
        for (int index = 0; index < 5; index++) {
            String name = "beam_core_" + index;
            if (centres[index].isEmpty()) {
                Pollution.LOGGER.warn("Skipping infusion/{}: central item is missing", name);
                continue;
            }
            infusion(provider, name, beamCore(index), 6, centres[index],
                    aspects(aspects[index][0], 16, aspects[index][2], 8, aspects[index][4], 16),
                    ing(plate(GTMaterials.HSSG, 6)),
                    ing(frame(GTMaterials.HSSG, 1)),
                    ing(new ItemStack(TCItems.ESSENTIA_RESONATOR.get())),
                    ing(new ItemStack(TCBlocks.NODE_TRANSDUCER.get())));
        }
    }

    /** 八个魔力线圈（上游 infusion magic_wirecoil-1..8）。 */
    private static void wireCoils(Consumer<FinishedRecipe> provider) {
        var coils = new Object[][] {
                { "cupronickel", GTBlocks.COIL_CUPRONICKEL.asStack(), 4, 4, 1 },
                { "kanthal", GTBlocks.COIL_KANTHAL.asStack(), 4, 4, 1 },
                { "nichrome", GTBlocks.COIL_NICHROME.asStack(), 8, 8, 2 },
                { "rtm_alloy", GTBlocks.COIL_RTMALLOY.asStack(), 8, 8, 2 },
                { "hssg", GTBlocks.COIL_HSSG.asStack(), 16, 16, 3 },
                { "naquadah", GTBlocks.COIL_NAQUADAH.asStack(), 16, 16, 3 },
                { "trinium", GTBlocks.COIL_TRINIUM.asStack(), 32, 32, 4 },
                { "tritanium", GTBlocks.COIL_TRITANIUM.asStack(), 32, 32, 4 },
        };
        ItemStack[] outputs = {
                PollutionMagicBlocks.WIRE_COIL_CUPRONICKEL.asStack(), PollutionMagicBlocks.WIRE_COIL_KANTHAL.asStack(),
                PollutionMagicBlocks.WIRE_COIL_NICHROME.asStack(), PollutionMagicBlocks.WIRE_COIL_RTM_ALLOY.asStack(),
                PollutionMagicBlocks.WIRE_COIL_HSSG.asStack(), PollutionMagicBlocks.WIRE_COIL_NAQUADAH.asStack(),
                PollutionMagicBlocks.WIRE_COIL_TRINIUM.asStack(), PollutionMagicBlocks.WIRE_COIL_TRITANIUM.asStack(),
        };
        for (int index = 0; index < coils.length; index++) {
            ItemStack central = (ItemStack) coils[index][1];
            int fire = (Integer) coils[index][2];
            int magic = (Integer) coils[index][3];
            int tier = (Integer) coils[index][4];
            java.util.List<Ingredient> components = new java.util.ArrayList<>();
            components.add(ing(plate(GTMaterials.HSSG, 2)));
            // 上游: plateIgnissteel -> 本移植版: IgnisSteel ingot
            components.add(ing(ingot(PollutionMaterials.IgnisSteel, 2)));
            if (tier >= 3) {
                components.add(ing(dust(PollutionMaterials.Substrate, 9)));
            }
            if (tier >= 4) {
                components.add(ing(new ItemStack(TCBlocks.VIS_CHARGE_RELAY.get())));
            }
            components.add(ing(new ItemStack(TCItems.ESSENTIA_RESONATOR.get())));
            components.add(ing(new ItemStack(TCBlocks.NODE_TRANSDUCER.get())));
            infusion(provider, "wirecoil/" + coils[index][0], outputs[index], 4, central,
                    aspects("ignis", fire, "praecantatio", magic), components.toArray(new Ingredient[0]));
        }
    }

    /** 五个灵气仓（上游 ULV..EV -> 本移植版 LV..IV）。 */
    private static void visHatches(Consumer<FinishedRecipe> provider) {
        int[] tiers = { GTValues.LV, GTValues.MV, GTValues.HV, GTValues.EV, GTValues.IV };
        var generators = new ItemStack[] {
                GTItems.FIELD_GENERATOR_LV.asStack(), GTItems.FIELD_GENERATOR_MV.asStack(),
                GTItems.FIELD_GENERATOR_HV.asStack(), GTItems.FIELD_GENERATOR_EV.asStack(),
                GTItems.FIELD_GENERATOR_IV.asStack(),
        };
        for (int index = 0; index < tiers.length; index++) {
            int tier = tiers[index];
            if (PollutionMachines.VIS_HATCH == null || PollutionMachines.VIS_HATCH.length <= tier
                    || PollutionMachines.VIS_HATCH[tier] == null
                    || GTMachines.FLUID_IMPORT_HATCH.length <= tier
                    || GTMachines.FLUID_IMPORT_HATCH[tier] == null) {
                continue;
            }
            infusion(provider, "vis_hatch_" + index, PollutionMachines.VIS_HATCH[tier].asStack(), 6,
                    GTMachines.FLUID_IMPORT_HATCH[tier].asStack(),
                    aspects("auram", 128, "praecantatio", 32, "lucrum", 64),
                    ing(plate(GTMaterials.HSSG, 8)),
                    ing(generators[index]),
                    ing(new ItemStack(TCItems.ESSENTIA_RESONATOR.get())),
                    ing(new ItemStack(TCBlocks.NODE_TRANSDUCER.get())));
        }
    }

    /** 约束器框架 II..V 与裂变核心 I..IV（上游 FUSION_REACTOR -> GT 聚变外壳/线圈）。 */
    private static void reactorParts(Consumer<FinishedRecipe> provider) {
        ItemStack frameCasing = GTBlocks.FUSION_CASING.asStack();
        ItemStack composeCoil = GTBlocks.FUSION_COIL.asStack();

        ItemStack[] frameCentrals = { frameCasing, frameCasing, frameCasing, frameCasing };
        int[] frameInstability = { 2, 4, 6, 8 };
        int[][] frameAspects = { { 8, 8 }, { 16, 16 }, { 32, 32 }, { 64, 64 } };
        ItemStack[] frameCrystals = { circuit(GTValues.LV), circuit(GTValues.MV), circuit(GTValues.HV),
                circuit(GTValues.EV) };
        for (int index = 0; index < 4; index++) {
            java.util.List<Ingredient> components = new java.util.ArrayList<>();
            components.add(ing(gem(PollutionMaterials.Scabyst, 1)));
            if (index >= 1) {
                components.add(ing(gem(PollutionMaterials.Valonite, 1)));
            }
            components.add(ing(frameCrystals[index]));
            components.add(ing(dust(index < 2 ? GTMaterials.Thorium : GTMaterials.Uranium238, 1)));
            components.add(ing(new ItemStack(TCItems.ESSENTIA_RESONATOR.get())));
            if (index >= 1) {
                components.add(ing(new ItemStack(TCBlocks.NODE_TRANSDUCER.get())));
            }
            if (index >= 3) {
                components.add(ing(new ItemStack(TCBlocks.VIS_CHARGE_RELAY.get())));
            }
            infusion(provider, "reactor_frame_" + (index + 2), frameCasing.copyWithCount(1), frameInstability[index],
                    frameCentrals[index], aspects("ordo", frameAspects[index][0], "tutamen", frameAspects[index][1]),
                    components.toArray(new Ingredient[0]));
        }

        int[] composeInstability = { 2, 4, 6, 8 };
        int[][] composeAspects = { { 8, 8, 8 }, { 16, 16, 16 }, { 32, 32, 32 }, { 64, 64, 64 } };
        ItemStack[] composeCircuits = { circuit(GTValues.LV), circuit(GTValues.MV), circuit(GTValues.HV),
                circuit(GTValues.EV) };
        for (int index = 0; index < 4; index++) {
            java.util.List<Ingredient> components = new java.util.ArrayList<>();
            components.add(ing(gem(GTMaterials.Opal, 1)));
            if (index >= 2) {
                components.add(ing(gem(GTMaterials.Amethyst, 1)));
            }
            if (index >= 3) {
                components.add(ing(new ItemStack(TCBlocks.VIS_CHARGE_RELAY.get())));
            }
            components.add(ing(crystal("entropy")));
            components.add(ing(composeCircuits[index]));
            components.add(ing(new ItemStack(TCItems.ESSENTIA_RESONATOR.get())));
            if (index >= 1) {
                components.add(ing(new ItemStack(TCBlocks.NODE_TRANSDUCER.get())));
            }
            infusion(provider, "reactor_compose_" + (index + 1), composeCoil.copyWithCount(1),
                    composeInstability[index], composeCoil,
                    aspects("perditio", composeAspects[index][0], "potentia", composeAspects[index][1],
                            "vacuos", composeAspects[index][2]),
                    components.toArray(new Ingredient[0]));
        }
    }

    /** 工业注魔、节点生产机、魔法聚变反应堆、大型节点发电机、合金高炉外壳。 */
    private static void controllers(Consumer<FinishedRecipe> provider) {
        // 蕴魔炼炉扩散要素凝聚外壳（上游: plateOctine -> Octine dust）
        infusion(provider, "alloy_blast_casing", PollutionMagicBlocks.ALLOY_BLAST_CASING.asStack(), 4,
                PollutionMagicBlocks.SPELL_PRISM_HOT.asStack(),
                aspects("ignis", 16, "metallum", 64),
                ing(dust(PollutionMaterials.Octine, 2)),
                ing(dust(PollutionMaterials.Octine, 2)),
                ing(plate(GTMaterials.HSSG, 2)),
                ing(plate(GTMaterials.HSSG, 2)),
                ing(PollutionItems.HOT_CATALYST_CORE.asStack()),
                ing(PollutionItems.SEGREGATION_CATALYST_CORE.asStack()));

        // 工业注魔主方块（上游: BlocksTC.infusionMatrix -> TC4R RUNIC_MATRIX；
        // matrixCost/matrixSpeed -> RUNIC_MATRIX；causalityCollapser -> PRIMORDIAL_PEARL）
        infusion(provider, "industrial_infusion", PollutionMachines.INDUSTRIAL_INFUSION.asStack(), 12,
                new ItemStack(TCBlocks.RUNIC_MATRIX.get()),
                aspects("praecantatio", 250, "auram", 250, "vacuos", 64, "cognitio", 128, "fabrico", 128),
                ing(gem(PollutionMaterials.Valonite, 9)),
                ing(new ItemStack(TCBlocks.RUNIC_MATRIX.get())),
                ing(new ItemStack(TCBlocks.RUNIC_MATRIX.get())),
                ing(PollutionItems.INTEGRATION_CATALYST_CORE.asStack()),
                ing(PollutionItems.SEGREGATION_CATALYST_CORE.asStack()),
                ing(GTItems.FIELD_GENERATOR_HV.asStack()), ing(GTItems.FIELD_GENERATOR_HV.asStack()),
                ing(GTItems.FIELD_GENERATOR_HV.asStack()), ing(GTItems.FIELD_GENERATOR_HV.asStack()),
                ing(new ItemStack(TCItems.PRIMORDIAL_PEARL.get())),
                ing(GTBlocks.FUSION_CASING.asStack()),
                ing(GTBlocks.FUSION_COIL.asStack()));

        // 节点生产机
        infusion(provider, "node_producer", PollutionMachines.NODE_PRODUCER.asStack(), 8,
                GTBlocks.FUSION_CASING.asStack(),
                aspects("praecantatio", 128, "machina", 250, "auram", 250, "vacuos", 250),
                ing(gem(PollutionMaterials.Valonite, 1)),
                ing(PollutionItems.INTEGRATION_CATALYST_CORE.asStack()),
                ing(PollutionItems.SEGREGATION_CATALYST_CORE.asStack()),
                ing(new ItemStack(TCBlocks.SILVERWOOD_LOG.get())),
                ing(crystal("air")), ing(crystal("fire")), ing(crystal("earth")),
                ing(crystal("water")), ing(crystal("order")), ing(crystal("entropy")),
                ing(frame(GTMaterials.HSSG, 1)),
                ing(GTItems.FIELD_GENERATOR_HV.asStack()),
                ing(PollutionMagicBlocks.VOID_PRISM.asStack()));

        // 魔法聚变反应堆（上游 blockVoid -> 本移植版: TC4R void ingot x9）
        infusion(provider, "magic_fusion_reactor", PollutionMachines.MAGIC_FUSION_REACTOR.asStack(), 8,
                GTBlocks.FUSION_CASING.asStack(),
                aspects("potentia", 250, "perditio", 250, "praecantatio", 128, "vacuos", 64),
                ing(gem(PollutionMaterials.Valonite, 1)),
                ing(crystal("air")), ing(crystal("entropy")),
                ing(new ItemStack(TCItems.PRIMORDIAL_PEARL.get())),
                ing(new ItemStack(TCItems.VOID_INGOT.get(), 9)),
                ing(GTItems.FIELD_GENERATOR_EV.asStack()),
                ing(PollutionItems.SEGREGATION_CATALYST_CORE.asStack()),
                ing(frame(GTMaterials.HSSG, 1)),
                ing(ChemicalHelper.get(TagPrefix.block, GTMaterials.Uranium235, 1)),
                ing(PollutionMagicBlocks.VOID_PRISM.asStack()));

        // 大型节点发电机（上游 AURA_GENERATORS[4] -> 本移植版 VIS_GENERATOR[4]）
        if (PollutionMachines.VIS_GENERATOR != null && PollutionMachines.VIS_GENERATOR.length > 4
                && PollutionMachines.VIS_GENERATOR[4] != null) {
            infusion(provider, "large_node_generator", PollutionMachines.LARGE_NODE_GENERATOR.asStack(), 8,
                    PollutionMachines.VIS_GENERATOR[4].asStack(),
                    aspects("praecantatio", 128, "machina", 250, "auram", 250, "motus", 64),
                    ing(gem(PollutionMaterials.Valonite, 1)),
                    ing(gem(PollutionMaterials.Scabyst, 1)),
                    ing(GTBlocks.FUSION_CASING.asStack()),
                    ing(new ItemStack(TCBlocks.COMPOUND_RECHARGE_FOCUS.get())),
                    ing(new ItemStack(TCBlocks.WAND_RECHARGE_PEDESTAL.get())),
                    ing(new ItemStack(TCBlocks.VIS_CHARGE_RELAY.get())),
                    ing(PollutionItems.INTEGRATION_CATALYST_CORE.asStack()),
                    ing(PollutionItems.SEGREGATION_CATALYST_CORE.asStack()),
                    ing(frame(GTMaterials.HSSG, 1)),
                    ing(ChemicalHelper.get(TagPrefix.block, GTMaterials.TungstenSteel, 1)),
                    ing(ChemicalHelper.get(TagPrefix.block, GTMaterials.NaquadahAlloy, 1)),
                    ing(GTItems.FIELD_GENERATOR_IV.asStack()),
                    ing(PollutionMagicBlocks.VOID_PRISM.asStack()));
        }
    }

    /** 四台 Botania 风格机器（上游 plateManasteel -> GTNN ManaSteel ingot）。 */
    private static void botaniaMachines(Consumer<FinishedRecipe> provider) {
        ItemStack[] centres = {
                new ItemStack(BotaniaFlowerBlocks.pureDaisy),
                new ItemStack(BotaniaBlocks.alchemyCatalyst),
                new ItemStack(BotaniaBlocks.runeAltar),
                new ItemStack(BotaniaBlocks.defaultAltar),
        };
        MachineDefinition[] machines = {
                PollutionMachines.INDUSTRIAL_PURE_DAISY,
                PollutionMachines.MANA_INFUSION_REACTOR,
                PollutionMachines.MANA_RUNE_ALTAR,
                PollutionMachines.MANA_PETAL_APOTHECARY,
        };
        String[] names = { "pure_daisy", "mana_infusion", "mana_rune_altar", "mana_petal" };
        String[][] aspectPairs = {
                { "herba", "128", "praecantatio", "32", "machina", "32" },
                { "herba", "128", "praecantatio", "32", "permutatio", "128" },
                { "herba", "128", "praecantatio", "64", "fabrico", "32" },
                { "herba", "128", "praecantatio", "32", "auram", "32" },
        };
        for (int index = 0; index < machines.length; index++) {
            if (machines[index] == null) {
                continue;
            }
            infusion(provider, names[index], machines[index].asStack(), 6, centres[index],
                    aspects(aspectPairs[index][0], 128, aspectPairs[index][2], 32,
                            aspectPairs[index][4], 32),
                    ing(ingot(GTNNMaterials.ManaSteel, 2)),
                    ing(ingot(GTNNMaterials.ManaSteel, 2)),
                    ing(plate(GTMaterials.HSSG, 2)),
                    ing(circuit(GTValues.EV)), ing(circuit(GTValues.EV)),
                    ing(GTItems.FIELD_GENERATOR_EV.asStack()),
                    ing(new ItemStack(TCItems.ESSENTIA_RESONATOR.get())),
                    ing(new ItemStack(TCBlocks.NODE_TRANSDUCER.get())));
        }
    }

    // ////////////////////////////////////
    // ***** helpers *****//
    // ////////////////////////////////////

    private static ItemStack beamCore(int index) {
        return switch (index) {
            case 0 -> PollutionMagicBlocks.BEAM_CORE_0.asStack();
            case 1 -> PollutionMagicBlocks.BEAM_CORE_1.asStack();
            case 2 -> PollutionMagicBlocks.BEAM_CORE_2.asStack();
            case 3 -> PollutionMagicBlocks.BEAM_CORE_3.asStack();
            default -> PollutionMagicBlocks.BEAM_CORE_4.asStack();
        };
    }

    private static ItemStack plate(Material material, int count) {
        return ChemicalHelper.get(TagPrefix.plate, material, count);
    }

    private static ItemStack dust(Material material, int count) {
        return ChemicalHelper.get(TagPrefix.dust, material, count);
    }

    private static ItemStack ingot(Material material, int count) {
        return ChemicalHelper.get(TagPrefix.ingot, material, count);
    }

    private static ItemStack item(String name) {
        com.tterrag.registrate.util.entry.ItemEntry<Item> entry = PollutionItems.get(name);
        return entry == null ? ItemStack.EMPTY : entry.asStack();
    }

    /** @return the fluid stack, or null when the material has no fluid in this GTCEu build */
    private static net.minecraftforge.fluids.FluidStack fluid(Material material, int amount) {
        if (material == null || !material.hasFluid()) {
            return null;
        }
        return material.getFluid(amount);
    }

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

    private static Ingredient ing(Item item) {
        return Ingredient.of(item);
    }

    private static Ingredient ing(Block block) {
        return Ingredient.of(block.asItem());
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
