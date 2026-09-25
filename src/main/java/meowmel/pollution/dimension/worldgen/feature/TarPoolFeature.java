package meowmel.pollution.dimension.worldgen.feature;

import meowmel.pollution.api.unification.PollutionMaterials;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Source pool geometry: 4-7 overlapping ellipsoids, dry upper half and sealed liquid bottom. */
public final class TarPoolFeature extends Feature<NoneFeatureConfiguration> {
    public TarPoolFeature() { super(NoneFeatureConfiguration.CODEC); }
    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        var level = context.level();
        var random = context.random();
        BlockPos origin = context.origin().offset(-8, 0, -8);
        while (origin.getY() > level.getMinBuildHeight() + 5 && level.isEmptyBlock(origin)) origin = origin.below();
        if (origin.getY() <= level.getMinBuildHeight() + 4) return false;
        origin = origin.below(4);
        var fluid = PollutionMaterials.PureTar.getFluid().defaultFluidState().createLegacyBlock();
        if (fluid.isAir()) return false;
        boolean[] cells = new boolean[16 * 16 * 8];
        int blobs = 4 + random.nextInt(4);
        for (int blob = 0; blob < blobs; blob++) {
            double sx = random.nextDouble() * 6 + 3, sy = random.nextDouble() * 4 + 2, sz = random.nextDouble() * 6 + 3;
            double bx = random.nextDouble() * (14 - sx) + 1 + sx / 2;
            double by = random.nextDouble() * (4 - sy) + 2 + sy / 2;
            double bz = random.nextDouble() * (14 - sz) + 1 + sz / 2;
            for (int x = 1; x < 15; x++) for (int z = 1; z < 15; z++) for (int y = 1; y < 7; y++) {
                double dx = (x - bx) / (sx / 2), dy = (y - by) / (sy / 2), dz = (z - bz) / (sz / 2);
                if (dx * dx + dy * dy + dz * dz < 1) cells[index(x, y, z)] = true;
            }
        }
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) for (int y = 0; y < 8; y++) {
            BlockPos pos = origin.offset(x, y, z);
            if (!level.ensureCanWrite(pos) || level.isOutsideBuildHeight(pos)
                    || level.getBlockState(pos).hasBlockEntity() || level.getBlockState(pos).is(Blocks.BEDROCK)) return false;
            boolean boundary = !cells[index(x, y, z)] && (x < 15 && cells[index(x + 1, y, z)] || x > 0 && cells[index(x - 1, y, z)]
                    || z < 15 && cells[index(x, y, z + 1)] || z > 0 && cells[index(x, y, z - 1)]
                    || y < 7 && cells[index(x, y + 1, z)] || y > 0 && cells[index(x, y - 1, z)]);
            if (boundary && (y >= 4 ? !level.getFluidState(pos).isEmpty()
                    : !level.getBlockState(pos).isSolid() && !level.getBlockState(pos).is(fluid.getBlock()))) return false;
        }
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) for (int y = 0; y < 8; y++) {
            if (cells[index(x, y, z)]) level.setBlock(origin.offset(x, y, z), y >= 4 ? Blocks.CAVE_AIR.defaultBlockState() : fluid, 2);
        }
        return true;
    }
    private static int index(int x, int y, int z) { return (x * 16 + z) * 8 + y; }
}
