package com.industrialcraft.machine.block;

import com.industrialcraft.machine.block.entity.ModBlockEntities;
import com.industrialcraft.machine.block.entity.RainCollectorBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Optional funnel placed on a {@link ReservoirBlock}. Collects rainwater into the tank below.
 */
public class RainCollectorBlock extends BaseEntityBlock {
	public static final MapCodec<RainCollectorBlock> CODEC = simpleCodec(RainCollectorBlock::new);

	private static final VoxelShape SHAPE = Shapes.or(
		Block.box(2.0, 0.0, 2.0, 14.0, 2.0, 14.0),
		Block.box(1.0, 2.0, 1.0, 15.0, 4.0, 15.0),
		Block.box(0.0, 4.0, 0.0, 16.0, 6.0, 2.0),
		Block.box(0.0, 4.0, 14.0, 16.0, 6.0, 16.0),
		Block.box(0.0, 4.0, 2.0, 2.0, 6.0, 14.0),
		Block.box(14.0, 4.0, 2.0, 16.0, 6.0, 14.0)
	);

	public RainCollectorBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return level.getBlockState(pos.below()).is(ModBlocks.RESERVOIR);
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState state = this.defaultBlockState();
		return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
	}

	@Override
	protected BlockState updateShape(
		BlockState state,
		LevelReader level,
		ScheduledTickAccess ticks,
		BlockPos pos,
		Direction direction,
		BlockPos neighborPos,
		BlockState neighborState,
		RandomSource random
	) {
		if (direction == Direction.DOWN && !state.canSurvive(level, pos)) {
			ticks.scheduleTick(pos, this, 1);
		}
		return state;
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!state.canSurvive(level, pos)) {
			level.destroyBlock(pos, true);
		}
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new RainCollectorBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide()
			? null
			: createTickerHelper(type, ModBlockEntities.RAIN_COLLECTOR, RainCollectorBlockEntity::serverTick);
	}
}
