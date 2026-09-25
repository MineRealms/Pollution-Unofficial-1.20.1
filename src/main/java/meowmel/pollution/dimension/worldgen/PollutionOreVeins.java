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
 * Source ore veins and bedrock fluids registered through GTCEu Modern's reload events.
 * <p>Ten underground veins, three Alfheim veins, six shared aspect veins and four fluid
 * deposits retain their upstream materials, weights, heights and yields. Underground
 * host rocks use the stone-ore tag; Alfheim also accepts Botania livingrock. Explicit
 * ore states support host blocks without a GT ore-prefix mapping.</p>
 * <p>Stone sphere palettes are registered separately by StoneSphereFeature. Modern XEI
 * pages use dimension registry ids instead of the removed 1.12 orb naming API.</p>
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
        registerVein("cryolite_vein", UNDERGROUND_LAYER, 40, 0.50f, 80, 160, 8, 20,
                PollutionMaterials.Cryolite, PollutionMaterials.Cryolite, PollutionMaterials.Cryolite,
                PollutionMaterials.Cryolite, GTMaterials.Bauxite);
        registerVein("octine_vein", UNDERGROUND_LAYER, 60, 0.40f, 40, 120, 12, 16,
                PollutionMaterials.Octine, PollutionMaterials.Octine, PollutionMaterials.Octine,
                PollutionMaterials.Octine, PollutionMaterials.MeltGold);
        registerVein("syrmorite_vein", UNDERGROUND_LAYER, 60, 0.40f, 40, 120, 12, 16,
                PollutionMaterials.Syrmorite, PollutionMaterials.Syrmorite, PollutionMaterials.Syrmorite,
                PollutionMaterials.Syrmorite, PollutionMaterials.MeltGold);
        registerVein("valonite_vein", UNDERGROUND_LAYER, 20, 0.25f, 40, 160, 14, 18,
                PollutionMaterials.Valonite, PollutionMaterials.DumbTin, PollutionMaterials.DumbTin,
                PollutionMaterials.Valonite, PollutionMaterials.Valonite);

        // ---- Alfheim: Botania livingrock-hosted veins (upstream `registerAlfheimVein`) ----
        // pollution.vein.dragonstone
        registerVein("dragonstone_vein", ALFHEIM_LAYER, 12, 0.2f, 20, 70, 16, 24,
                PollutionMaterials.Dragonstone, PollutionMaterials.Dragonstone, PollutionMaterials.Dragonstone,
                PollutionMaterials.Dragonstone, PollutionMaterials.Dragonstone);
        // pollution.vein.pixie_quartz
        registerVein("pixie_quartz_vein", ALFHEIM_LAYER, 18, 0.25f, 30, 100, 18, 26,
                PollutionMaterials.ElvenQuartz, PollutionMaterials.PixieDust, PollutionMaterials.ElvenQuartz,
                PollutionMaterials.ElvenQuartz, PollutionMaterials.PixieDust);
        registerVein("elementium_vein", ALFHEIM_LAYER, 8, 0.15f, 8, 40, 16, 24,
                PollutionMaterials.ElvenElementium, PollutionMaterials.ElvenElementium, PollutionMaterials.ElvenElementium,
                PollutionMaterials.ElvenElementium, PollutionMaterials.ElvenElementium);

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
        // pollution.veins.fluid.mana - pure mana, also produced by the Life Activation Garden.
        registerFluidDeposit("mana_deposit", 10, 5, 25, 1, 100, 40,
                PollutionMaterials.Mana.getFluid(), ALFHEIM_DIMENSION);
        registerFluidDeposit("pure_tar_deposit", 20, 100, 200, 1, 100, 20,
                PollutionMaterials.PureTar.getFluid(), UNDERGROUND_DIMENSION);
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
            return state.is(BlockTags.STONE_ORE_REPLACEABLES) || state.is(BotaniaBlocks.livingrock);
        }

        @Override
        protected RuleTestType<?> getType() {
            return RuleTestType.ALWAYS_TRUE_TEST;
        }
    }
}
