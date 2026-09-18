package meowmel.pollution.dimension.worldgen;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTOres;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.unification.PollutionMaterials;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import vazkii.botania.common.block.BotaniaBlocks;

import java.util.Set;

/**
 * 1.20.1 / GTCEu Modern 7.5.3 port of the 1.12 {@code PollutionOreVeins}.
 *
 * <p>1.12 registered veins through {@code OreDepositBuilder} +
 * {@code WorldGenRegistry.addVeinDefinitions}. GTCEu Modern exposes
 * {@link GTOreDefinition} with {@link com.gregtechceu.gtceu.api.data.worldgen.generator.veins.ClassicVeinGenerator}
 * (the modern name for {@code layeredGeneration}/{@code layeredFill}) and
 * registers veins from the {@code GTCEuAPI.RegisterEvent} fired by
 * {@code GTOreLoader} (the datapack loader that clears and rebuilds
 * {@code GTRegistries.ORE_VEINS} on every server start/reload). This class
 * subscribes to that event from {@link #init(FMLJavaModLoadingContext)}.</p>
 *
 * <h2>Dimension layers</h2>
 * <p>Modern GTCEu only ships overworld/nether/end layers, and
 * {@code OreGenerator} only considers layers present in
 * {@code WorldGeneratorUtils.WORLD_GEN_LAYERS}. Three custom layers are
 * therefore registered for the port's datapack dimensions
 * {@code pollution:underground} and {@code pollution:alfheim}.</p>
 *
 * <h2>Host block adaptation</h2>
 * <p>Upstream restricted the Alfheim veins to Botania {@code livingrock}
 * ({@code generationPredicate(state.getBlock() == ModBlocks.livingrock)}).
 * The port's {@code noise_settings/alfheim.json} still uses
 * {@code minecraft:stone} as the default block (upstream replaced the base
 * terrain with livingrock in code), so the Alfheim layer accepts both stone and
 * livingrock. The generators target the explicit {@code gtceu:<material>_ore}
 * state instead of GTCEu's {@code .mat(material)} shorthand, because the
 * shorthand only resolves ores for blocks that already map to an ore prefix
 * (stone) and would place nothing inside livingrock.</p>
 *
 * <h2>Ported veins</h2>
 * <p>All veins whose materials exist in this port: 6 underground veins
 * (flame coal, galena, nickel, pyrargyrite, scabyst, zinc), 2 Alfheim veins
 * (dragonstone, pixie quartz) and the 6 thaumastic aspect veins shared by both
 * dimensions. Bedrock fluid deposits are ported as
 * {@link BedrockFluidDefinition}s (lava/water/mana).</p>
 *
 * <h2>Skipped upstream entries</h2>
 * <ul>
 *   <li>{@code cryolite_vein} - {@code GTQTMaterials.Cryolite} is not ported
 *       and GTCEu 7.5.3 has no {@code GTMaterials.Cryolite}.</li>
 *   <li>{@code octine_vein}, {@code syrmorite_vein}, {@code valonite_vein} -
 *       the materials exist ({@code SubstrateMaterials}) but the port registers
 *       them as dust/gem shapes without the upstream ORE property, so there is
 *       no {@code gtceu:<material>_ore} block to place. Registering the veins
 *       would be a no-op, so they are skipped until the ore shapes land.</li>
 *   <li>{@code elementium_vein} - {@code ElvenElementium} is not ported.</li>
 *   <li>{@code pure_tar_deposit} - the {@code PureTar} material/fluid is not
 *       ported.</li>
 *   <li>{@code registerStoneSpheres} / {@code registerGTQTStoneSpheres} - the
 *       1.12 sphere generation API and the GTQT stone variant blocks have no
 *       GTCEu 7.5.3 equivalent in this port; vanilla/GT stone spheres are not
 *       part of the requested vein port.</li>
 *   <li>{@code registerOrbs} - {@code OrbItems} dimension display mapping and
 *       {@code WorldGenRegistry.addNamedDimension} do not exist in 7.5.3 (the
 *       XEI vein pages derive names from the registry id).</li>
 * </ul>
 *
 * <h2>Substitution table</h2>
 * <p>Of the requested substitutions only {@code GTQT Mana -> InfusedAura}
 * applies here (the Alfheim mana bedrock deposit). {@code FLESH_BLOCK},
 * {@code HyperdimensionalSilver}, {@code KQGold}, {@code Mansussteel} and
 * {@code Thaumium} are not referenced by the upstream vein file, so no
 * substitution was needed for them.</p>
 */
public final class PollutionOreVeins {

    private static final ResourceLocation UNDERGROUND_ID =
            ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, "underground");
    private static final ResourceLocation ALFHEIM_ID =
            ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, "alfheim");

    private static final ResourceKey<Level> UNDERGROUND_DIMENSION =
            ResourceKey.create(Registries.DIMENSION, UNDERGROUND_ID);
    private static final ResourceKey<Level> ALFHEIM_DIMENSION =
            ResourceKey.create(Registries.DIMENSION, ALFHEIM_ID);

    /** Stone-hosted layer for the underground dimension (its default block is stone). */
    private static final IWorldGenLayer UNDERGROUND_LAYER = new SimpleLayer("pollution_underground",
            new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), Set.of(UNDERGROUND_ID));

    /** Alfheim layer, host = stone or Botania livingrock (see class javadoc). */
    private static final IWorldGenLayer ALFHEIM_LAYER = new SimpleLayer("pollution_alfheim",
            new AlfheimHostTest(), Set.of(ALFHEIM_ID));

    /** Layer used by the thaumastic veins, which upstream generated in both dimensions. */
    private static final IWorldGenLayer UNDERGROUND_ALFHEIM_LAYER = new SimpleLayer("pollution_underground_alfheim",
            new AlfheimHostTest(), Set.of(UNDERGROUND_ID, ALFHEIM_ID));

    private PollutionOreVeins() {
    }

    public static void init(FMLJavaModLoadingContext context) {
        registerLayer(UNDERGROUND_LAYER);
        registerLayer(ALFHEIM_LAYER);
        registerLayer(UNDERGROUND_ALFHEIM_LAYER);

        IEventBus modBus = context.getModEventBus();
        modBus.addGenericListener(GTOreDefinition.class, PollutionOreVeins::onOreVeinRegister);
        modBus.addGenericListener(BedrockFluidDefinition.class, PollutionOreVeins::onFluidVeinRegister);
    }

    private static void registerLayer(IWorldGenLayer layer) {
        WorldGeneratorUtils.WORLD_GEN_LAYERS.put(layer.getSerializedName(), layer);
    }

    private static void onOreVeinRegister(GTCEuAPI.RegisterEvent<ResourceLocation, GTOreDefinition> event) {
        registerVeins();
    }

    private static void onFluidVeinRegister(GTCEuAPI.RegisterEvent<ResourceLocation, BedrockFluidDefinition> event) {
        registerFluidDeposits();
    }

    /**
     * Registers all ported ore veins. Called on every server start/datapack
     * reload right after GTCEu's own veins and before the registry freezes.
     */
    public static void registerVeins() {
        // ---- Underground world (upstream `registerVein`, dimension = pollution:underground) ----
        // pollution.veins.ore.flame_coal
        registerVein("flame_coal_vein", UNDERGROUND_LAYER, 60, 0.60f, 60, 180, 18, 32,
                PollutionMaterials.FlameCoal, PollutionMaterials.FlameCoal,
                PollutionMaterials.FlameCoal, PollutionMaterials.FlameCoal, GTMaterials.Diamond);
        // pollution.veins.ore.galena
        registerVein("galena_vein", UNDERGROUND_LAYER, 40, 0.25f, 60, 180, 14, 16,
                GTMaterials.Galena, GTMaterials.Galena, GTMaterials.Galena,
                GTMaterials.Silver, GTMaterials.Lead);
        // pollution.veins.ore.nickel
        registerVein("nickel_vein", UNDERGROUND_LAYER, 40, 0.25f, 120, 240, 14, 16,
                GTMaterials.Nickel, GTMaterials.Garnierite, GTMaterials.Nickel,
                GTMaterials.Cobaltite, GTMaterials.Pentlandite);
        // pollution.veins.ore.pyrargyrite
        registerVein("pyrargyrite_vein", UNDERGROUND_LAYER, 20, 0.25f, 40, 120, 12, 16,
                PollutionMaterials.Pyrargyrite, PollutionMaterials.AuthorityLead, PollutionMaterials.AuthorityLead,
                PollutionMaterials.Pyrargyrite, PollutionMaterials.Pyrargyrite);
        // pollution.veins.ore.scabyst
        registerVein("scabyst_vein", UNDERGROUND_LAYER, 20, 0.25f, 40, 120, 12, 16,
                PollutionMaterials.Scabyst, PollutionMaterials.AuthorityLead, PollutionMaterials.AuthorityLead,
                PollutionMaterials.Scabyst, PollutionMaterials.Scabyst);
        // pollution.veins.ore.pluto_zinc
        registerVein("zinc_vein", UNDERGROUND_LAYER, 20, 0.25f, 120, 240, 12, 16,
                PollutionMaterials.PlutoZinc, GTMaterials.Sulfur, GTMaterials.Sulfur,
                PollutionMaterials.PlutoZinc, PollutionMaterials.PlutoZinc);
        // Skipped: cryolite_vein (GTQT Cryolite not ported)
        // Skipped: octine_vein / syrmorite_vein / valonite_vein (no ore blocks in this port)

        // ---- Alfheim: Botania livingrock-hosted veins (upstream `registerAlfheimVein`) ----
        // pollution.vein.dragonstone
        registerVein("dragonstone_vein", ALFHEIM_LAYER, 12, 0.2f, 20, 70, 16, 24,
                PollutionMaterials.Dragonstone, PollutionMaterials.Dragonstone, PollutionMaterials.Dragonstone,
                PollutionMaterials.Dragonstone, PollutionMaterials.Dragonstone);
        // pollution.vein.pixie_quartz
        registerVein("pixie_quartz_vein", ALFHEIM_LAYER, 18, 0.25f, 30, 100, 18, 26,
                PollutionMaterials.ElvenQuartz, PollutionMaterials.PixieDust, PollutionMaterials.ElvenQuartz,
                PollutionMaterials.ElvenQuartz, PollutionMaterials.PixieDust);
        // Skipped: elementium_vein (ElvenElementium not ported)

        // ---- Thaumastic aspect veins (underground + alfheim) ----
        // pollution.veins.ore.thaumastic.air
        registerThaumasticVein("thaumastic_air_vein", PollutionMaterials.InfusedAir);
        // pollution.veins.ore.thaumastic.earth
        registerThaumasticVein("thaumastic_earth_vein", PollutionMaterials.InfusedEarth);
        // pollution.veins.ore.thaumastic.entropy
        registerThaumasticVein("thaumastic_entropy_vein", PollutionMaterials.InfusedEntropy);
        // pollution.veins.ore.thaumastic.fire
        registerThaumasticVein("thaumastic_fire_vein", PollutionMaterials.InfusedFire);
        // pollution.veins.ore.thaumastic.order
        registerThaumasticVein("thaumastic_order_vein", PollutionMaterials.InfusedOrder);
        // pollution.veins.ore.thaumastic.water
        registerThaumasticVein("thaumastic_water_vein", PollutionMaterials.InfusedWater);
    }

    /** Upstream {@code registerVein}: layered fill, shared by both dimensions' aspect veins. */
    private static void registerThaumasticVein(String name, Material infused) {
        registerVein(name, UNDERGROUND_ALFHEIM_LAYER, 40, 0.50f, 20, 80, 12, 20,
                infused, infused, infused, PollutionMaterials.Amber, GTMaterials.Cinnabar);
    }

    /**
     * Modern equivalent of {@code OreDepositBuilder.layeredGeneration(radiusMin, radiusMax)}
     * + {@code layeredFill(primary, secondary, between, sporadic)} +
     * {@code surfaceRock(surfaceRock)}.
     */
    private static void registerVein(String name, IWorldGenLayer layer, int weight, float density,
                                     int minHeight, int maxHeight, int radiusMin, int radiusMax,
                                     Material surfaceRock, Material primary, Material secondary,
                                     Material between, Material sporadic) {
        BlockState primaryState = oreState(primary);
        BlockState secondaryState = oreState(secondary);
        BlockState betweenState = oreState(between);
        BlockState sporadicState = oreState(sporadic);
        if (primaryState == null || secondaryState == null || betweenState == null || sporadicState == null) {
            Pollution.LOGGER.warn("[PollutionOreVeins] Skipping vein {}: a material has no ore block in this port",
                    name);
            return;
        }

        GTOreDefinition definition = GTOres.blankOreDefinition()
                .clusterSize(UniformInt.of(radiusMin, radiusMax))
                .density(density)
                .weight(weight)
                .layer(layer)
                .heightRangeUniform(minHeight, maxHeight)
                .classicVeinGenerator(generator -> generator
                        .primary(builder -> builder.state(primaryState))
                        .secondary(builder -> builder.state(secondaryState))
                        .between(builder -> builder.state(betweenState))
                        .sporadic(builder -> builder.state(sporadicState)));

        if (GTMaterialBlocks.SURFACE_ROCK_BLOCKS.get(surfaceRock) != null) {
            definition.surfaceIndicatorGenerator(indicator -> indicator.surfaceRock(surfaceRock));
        }

        definition.register(ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, name));
    }

    /** Resolves the {@code gtceu:<material>_ore} state, or null when the material has no ore block. */
    private static BlockState oreState(Material material) {
        Block block = ChemicalHelper.getBlock(TagPrefix.ore, material);
        return block == null ? null : block.defaultBlockState();
    }

    /** Registers all ported bedrock fluid deposits (upstream {@code registerFluidDeposit}). */
    public static void registerFluidDeposits() {
        // pollution.veins.fluid.lava
        registerFluidDeposit("lava_deposit", 65, 125, 250, 1, 100, 30,
                GTMaterials.Lava.getFluid(), UNDERGROUND_DIMENSION);
        // pollution.veins.fluid.water
        registerFluidDeposit("water_deposit", 20, 50, 100, 1, 100, 60,
                GTMaterials.Water.getFluid(), UNDERGROUND_DIMENSION, ALFHEIM_DIMENSION);
        // pollution.veins.fluid.mana - upstream used GTQT Mana, substituted with InfusedAura
        registerFluidDeposit("mana_deposit", 10, 5, 25, 1, 100, 40,
                PollutionMaterials.InfusedAura.getFluid(), ALFHEIM_DIMENSION);
        // Skipped: pure_tar_deposit (PureTar not ported)
    }

    @SafeVarargs
    private static void registerFluidDeposit(String name, int weight, int minYield, int maxYield,
                                             int depletionAmount, int depletionChance, int depletedYield,
                                             Fluid fluid, ResourceKey<Level>... dimensions) {
        BedrockFluidDefinition.builder(ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, name))
                .weight(weight)
                .yield(minYield, maxYield)
                .depletionAmount(depletionAmount)
                .depletionChance(depletionChance)
                .depletedYield(depletedYield)
                .fluid(() -> fluid)
                .dimensions(Set.of(dimensions))
                .register();
    }

    /** Simple immutable {@link IWorldGenLayer} for the port's custom dimensions. */
    private static final class SimpleLayer implements IWorldGenLayer {

        private final String name;
        private final RuleTest target;
        private final Set<ResourceLocation> levels;

        private SimpleLayer(String name, RuleTest target, Set<ResourceLocation> levels) {
            this.name = name;
            this.target = target;
            this.levels = levels;
        }

        @Override
        public boolean isApplicableForLevel(ResourceLocation level) {
            return levels.contains(level);
        }

        @Override
        public Set<ResourceLocation> getLevels() {
            return levels;
        }

        @Override
        public RuleTest getTarget() {
            return target;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    /**
     * Host test for the Alfheim veins: stone (the port's current default block)
     * or Botania livingrock (the upstream host).
     *
     * <p>This rule test is only used in memory by GTCEu's {@code OrePlacer};
     * {@code IWorldGenLayer.CODEC} serializes the layer by name and resolves the
     * code-registered instance locally, so the test is never written to disk or
     * the network and does not need its own {@code RuleTestType}.</p>
     */
    private static final class AlfheimHostTest extends RuleTest {

        @Override
        public boolean test(BlockState state, RandomSource random) {
            return state.is(Blocks.STONE) || state.is(BotaniaBlocks.livingrock);
        }

        @Override
        protected RuleTestType<?> getType() {
            return RuleTestType.ALWAYS_TRUE_TEST;
        }
    }
}
