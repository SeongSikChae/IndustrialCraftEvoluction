package com.industrialcraft.material.worldgen;

import com.industrialcraft.material.block.ModBlocks;
import com.mojang.serialization.Codec;
import java.util.BitSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

/**
 * Coal-vein placement that rolls the Material rank weights per block
 * (peat 50%, lignite 10%, sub-bituminous 17.5%, bituminous 20%, anthracite 2.5%).
 */
public class MixedCoalOreFeature extends Feature<MixedCoalOreConfiguration> {
	private static final int WEIGHT_SUM = 1000;

	public MixedCoalOreFeature(Codec<MixedCoalOreConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<MixedCoalOreConfiguration> context) {
		RandomSource random = context.random();
		BlockPos origin = context.origin();
		WorldGenLevel level = context.level();
		MixedCoalOreConfiguration config = context.config();

		float angle = random.nextFloat() * (float) Math.PI;
		float stretch = (float) config.size() / 8.0F;
		int padding = Mth.ceil((config.size() / 16.0F * 2.0F + 1.0F) / 2.0F);
		double startX = origin.getX() + Math.sin(angle) * stretch;
		double endX = origin.getX() - Math.sin(angle) * stretch;
		double startZ = origin.getZ() + Math.cos(angle) * stretch;
		double endZ = origin.getZ() - Math.cos(angle) * stretch;
		double startY = origin.getY() + random.nextInt(3) - 2;
		double endY = origin.getY() + random.nextInt(3) - 2;
		int minX = origin.getX() - Mth.ceil(stretch) - padding;
		int minY = origin.getY() - 2 - padding;
		int minZ = origin.getZ() - Mth.ceil(stretch) - padding;
		int width = 2 * (Mth.ceil(stretch) + padding);
		int height = 2 * (2 + padding);

		for (int x = minX; x <= minX + width; x++) {
			for (int z = minZ; z <= minZ + width; z++) {
				if (minY <= level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z)) {
					return doPlace(level, random, config, startX, endX, startZ, endZ, startY, endY, minX, minY, minZ, width, height);
				}
			}
		}
		return false;
	}

	private boolean doPlace(
		WorldGenLevel level,
		RandomSource random,
		MixedCoalOreConfiguration config,
		double startX,
		double endX,
		double startZ,
		double endZ,
		double startY,
		double endY,
		int originX,
		int originY,
		int originZ,
		int width,
		int height
	) {
		int placed = 0;
		BitSet visited = new BitSet(width * height * width);
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		int size = config.size();
		double[] sphere = new double[size * 4];

		for (int i = 0; i < size; i++) {
			float t = (float) i / (float) size;
			double cx = Mth.lerp(t, startX, endX);
			double cy = Mth.lerp(t, startY, endY);
			double cz = Mth.lerp(t, startZ, endZ);
			double radiusNoise = random.nextDouble() * size / 16.0D;
			double radius = ((Mth.sin((float) Math.PI * t) + 1.0F) * radiusNoise + 1.0D) / 2.0D;
			sphere[i * 4] = cx;
			sphere[i * 4 + 1] = cy;
			sphere[i * 4 + 2] = cz;
			sphere[i * 4 + 3] = radius;
		}

		for (int i = 0; i < size - 1; i++) {
			if (sphere[i * 4 + 3] <= 0.0D) {
				continue;
			}
			for (int j = i + 1; j < size; j++) {
				if (sphere[j * 4 + 3] <= 0.0D) {
					continue;
				}
				double dx = sphere[i * 4] - sphere[j * 4];
				double dy = sphere[i * 4 + 1] - sphere[j * 4 + 1];
				double dz = sphere[i * 4 + 2] - sphere[j * 4 + 2];
				double dr = sphere[i * 4 + 3] - sphere[j * 4 + 3];
				if (dr * dr > dx * dx + dy * dy + dz * dz) {
					if (dr > 0.0D) {
						sphere[j * 4 + 3] = -1.0D;
					} else {
						sphere[i * 4 + 3] = -1.0D;
					}
				}
			}
		}

		for (int i = 0; i < size; i++) {
			double radius = sphere[i * 4 + 3];
			if (radius < 0.0D) {
				continue;
			}
			double cx = sphere[i * 4];
			double cy = sphere[i * 4 + 1];
			double cz = sphere[i * 4 + 2];
			int x0 = Math.max(Mth.floor(cx - radius), originX);
			int y0 = Math.max(Mth.floor(cy - radius), originY);
			int z0 = Math.max(Mth.floor(cz - radius), originZ);
			int x1 = Math.max(Mth.floor(cx + radius), x0);
			int y1 = Math.max(Mth.floor(cy + radius), y0);
			int z1 = Math.max(Mth.floor(cz + radius), z0);

			for (int x = x0; x <= x1; x++) {
				double nx = ((double) x + 0.5D - cx) / radius;
				if (nx * nx >= 1.0D) {
					continue;
				}
				for (int y = y0; y <= y1; y++) {
					double ny = ((double) y + 0.5D - cy) / radius;
					if (nx * nx + ny * ny >= 1.0D) {
						continue;
					}
					for (int z = z0; z <= z1; z++) {
						double nz = ((double) z + 0.5D - cz) / radius;
						if (nx * nx + ny * ny + nz * nz >= 1.0D) {
							continue;
						}
						int bit = x - originX + (y - originY) * width + (z - originZ) * width * height;
						if (visited.get(bit)) {
							continue;
						}
						visited.set(bit);
						cursor.set(x, y, z);
						if (tryPlaceOre(level, random, config, cursor)) {
							placed++;
						}
					}
				}
			}
		}
		return placed > 0;
	}

	private boolean tryPlaceOre(WorldGenLevel level, RandomSource random, MixedCoalOreConfiguration config, BlockPos pos) {
		BlockState current = level.getBlockState(pos);
		boolean deepslate = current.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
		boolean stone = current.is(BlockTags.STONE_ORE_REPLACEABLES);
		if (!deepslate && !stone) {
			return false;
		}
		if (config.discardChanceOnAirExposure() > 0.0F && shouldDiscardOnAirExposure(level, pos, random, config.discardChanceOnAirExposure())) {
			return false;
		}
		BlockState ore = pickOre(random, deepslate);
		return level.setBlock(pos, ore, 2);
	}

	private static boolean shouldDiscardOnAirExposure(WorldGenLevel level, BlockPos pos, RandomSource random, float chance) {
		for (Direction direction : Direction.values()) {
			BlockState neighbor = level.getBlockState(pos.relative(direction));
			if (neighbor.isAir() || !neighbor.getFluidState().isEmpty()) {
				return random.nextFloat() < chance;
			}
		}
		return false;
	}

	public static BlockState pickOre(RandomSource random, boolean deepslate) {
		int roll = random.nextInt(WEIGHT_SUM);
		if (roll < 500) {
			return deepslate ? ModBlocks.DEEPSLATE_PEAT_ORE.defaultBlockState() : ModBlocks.PEAT_ORE.defaultBlockState();
		}
		if (roll < 600) {
			return deepslate ? ModBlocks.DEEPSLATE_LIGNITE_ORE.defaultBlockState() : ModBlocks.LIGNITE_ORE.defaultBlockState();
		}
		if (roll < 775) {
			return deepslate
				? ModBlocks.DEEPSLATE_SUB_BITUMINOUS_ORE.defaultBlockState()
				: ModBlocks.SUB_BITUMINOUS_ORE.defaultBlockState();
		}
		if (roll < 975) {
			return deepslate ? Blocks.DEEPSLATE_COAL_ORE.defaultBlockState() : Blocks.COAL_ORE.defaultBlockState();
		}
		return deepslate ? ModBlocks.DEEPSLATE_ANTHRACITE_ORE.defaultBlockState() : ModBlocks.ANTHRACITE_ORE.defaultBlockState();
	}
}
