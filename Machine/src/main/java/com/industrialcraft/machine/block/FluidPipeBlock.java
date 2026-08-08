package com.industrialcraft.machine.block;

import com.industrialcraft.machine.block.entity.FluidPipeBlockEntity;
import com.industrialcraft.machine.block.entity.ModBlockEntities;
import com.industrialcraft.machine.block.entity.ReservoirBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * 1 B fluid conduit with visual connections to compatible pipes, reservoirs, and water pumps.
 */
public class FluidPipeBlock extends BaseEntityBlock {
	public static final MapCodec<FluidPipeBlock> CODEC = simpleCodec(FluidPipeBlock::new);
	public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
	public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
	public static final BooleanProperty EAST = BlockStateProperties.EAST;
	public static final BooleanProperty WEST = BlockStateProperties.WEST;
	public static final BooleanProperty UP = BlockStateProperties.UP;
	public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

	private static final VoxelShape CORE = Block.box(6.0, 6.0, 6.0, 10.0, 10.0, 10.0);
	/** Arms cross 1 unit into the neighbor so inset tanks still look joined. */
	private static final VoxelShape ARM_NORTH = Block.box(6.0, 6.0, -1.0, 10.0, 10.0, 6.0);
	private static final VoxelShape ARM_SOUTH = Block.box(6.0, 6.0, 10.0, 10.0, 10.0, 17.0);
	private static final VoxelShape ARM_WEST = Block.box(-1.0, 6.0, 6.0, 6.0, 10.0, 10.0);
	private static final VoxelShape ARM_EAST = Block.box(10.0, 6.0, 6.0, 17.0, 10.0, 10.0);
	private static final VoxelShape ARM_DOWN = Block.box(6.0, -1.0, 6.0, 10.0, 6.0, 10.0);
	private static final VoxelShape ARM_UP = Block.box(6.0, 10.0, 6.0, 10.0, 17.0, 10.0);

	public FluidPipeBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition.any()
				.setValue(NORTH, false)
				.setValue(SOUTH, false)
				.setValue(EAST, false)
				.setValue(WEST, false)
				.setValue(UP, false)
				.setValue(DOWN, false)
		);
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
		VoxelShape shape = CORE;
		if (state.getValue(NORTH)) {
			shape = Shapes.or(shape, ARM_NORTH);
		}
		if (state.getValue(SOUTH)) {
			shape = Shapes.or(shape, ARM_SOUTH);
		}
		if (state.getValue(WEST)) {
			shape = Shapes.or(shape, ARM_WEST);
		}
		if (state.getValue(EAST)) {
			shape = Shapes.or(shape, ARM_EAST);
		}
		if (state.getValue(DOWN)) {
			shape = Shapes.or(shape, ARM_DOWN);
		}
		if (state.getValue(UP)) {
			shape = Shapes.or(shape, ARM_UP);
		}
		return shape;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new FluidPipeBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide()
			? null
			: createTickerHelper(type, ModBlockEntities.FLUID_PIPE, FluidPipeBlockEntity::serverTick);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return withConnections(this.defaultBlockState(), context.getLevel(), context.getClickedPos());
	}

	@Override
	protected BlockState updateShape(
		BlockState state,
		net.minecraft.world.level.LevelReader level,
		net.minecraft.world.level.ScheduledTickAccess ticks,
		BlockPos pos,
		Direction direction,
		BlockPos neighborPos,
		BlockState neighborState,
		net.minecraft.util.RandomSource random
	) {
		return withConnections(state, level, pos);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
	}

	public static BooleanProperty property(Direction direction) {
		return switch (direction) {
			case NORTH -> NORTH;
			case SOUTH -> SOUTH;
			case EAST -> EAST;
			case WEST -> WEST;
			case UP -> UP;
			case DOWN -> DOWN;
		};
	}

	public static boolean connects(BlockGetter level, BlockPos pos, Direction face) {
		BlockPos neighborPos = pos.relative(face);
		BlockState neighbor = level.getBlockState(neighborPos);
		Block block = neighbor.getBlock();
		if (block instanceof FluidPipeBlock) {
			return fluidsCompatible(level, pos, neighborPos);
		}
		if (block instanceof ReservoirBlock) {
			return ReservoirBlockEntity.allowsPipeConnection(level, neighborPos, face.getOpposite());
		}
		if (block instanceof WaterPumpBlock) {
			return WaterPumpBlock.allowsPipeConnection(level, neighborPos, face.getOpposite());
		}
		return false;
	}

	/** Empty↔empty or same fluid; different fluids do not connect. */
	private static boolean fluidsCompatible(BlockGetter level, BlockPos a, BlockPos b) {
		Fluid fa = pipeFluid(level, a);
		Fluid fb = pipeFluid(level, b);
		if (fa == Fluids.EMPTY || fb == Fluids.EMPTY) {
			return true;
		}
		return fa.isSame(fb);
	}

	private static Fluid pipeFluid(BlockGetter level, BlockPos pos) {
		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof FluidPipeBlockEntity pipe) {
			return pipe.getFluid();
		}
		return Fluids.EMPTY;
	}

	public static BlockState withConnections(BlockState state, BlockGetter level, BlockPos pos) {
		BlockState result = state;
		for (Direction direction : Direction.values()) {
			result = result.setValue(property(direction), connects(level, pos, direction));
		}
		return result;
	}

	public static boolean isConnected(BlockState state, Direction direction) {
		return state.getValue(property(direction));
	}
}
