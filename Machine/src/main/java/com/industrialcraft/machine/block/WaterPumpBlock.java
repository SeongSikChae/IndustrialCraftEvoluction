package com.industrialcraft.machine.block;

import com.industrialcraft.machine.block.entity.ModBlockEntities;
import com.industrialcraft.machine.block.entity.WaterPumpBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Rotatable pump: {@link #FACING} = power input (place looking at the shaft source).
 * Fluid intake on {@link Direction#DOWN}; sticky output on UP or any horizontal except FACING.
 */
public class WaterPumpBlock extends BaseEntityBlock {
	public static final MapCodec<WaterPumpBlock> CODEC = simpleCodec(WaterPumpBlock::new);
	/** Power input face; authored motor is on south. */
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

	private static final VoxelShape PLINTH = Block.box(3.0, 0.0, 3.0, 13.0, 2.0, 13.0);
	private static final VoxelShape TANK = Shapes.or(
		Block.box(5.0, 2.0, 4.0, 11.0, 13.0, 12.0),
		Block.box(4.0, 2.0, 5.0, 12.0, 13.0, 11.0)
	);
	private static final VoxelShape LID = Block.box(4.5, 13.0, 4.5, 11.5, 14.0, 11.5);
	private static final VoxelShape INTAKE = Block.box(6.0, -1.0, 6.0, 10.0, 2.0, 10.0);
	private static final VoxelShape PORT_UP = Block.box(6.0, 14.0, 6.0, 10.0, 17.0, 10.0);
	/** Authored for FACING=south (motor on +Z). */
	private static final VoxelShape SHAPE_SOUTH = Shapes.or(
		PLINTH,
		TANK,
		LID,
		INTAKE,
		PORT_UP,
		Block.box(4.5, 4.5, 11.0, 11.5, 11.5, 15.35),
		Block.box(6.0, 6.0, -1.0, 10.0, 10.0, 4.0),
		Block.box(12.0, 6.0, 6.0, 17.0, 10.0, 10.0),
		Block.box(-1.0, 6.0, 6.0, 4.0, 10.0, 10.0)
	);
	private static final VoxelShape SHAPE_WEST = rotateYClockWise(SHAPE_SOUTH);
	private static final VoxelShape SHAPE_NORTH = rotateYClockWise(SHAPE_WEST);
	private static final VoxelShape SHAPE_EAST = rotateYClockWise(SHAPE_NORTH);

	public WaterPumpBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH));
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
		return switch (state.getValue(FACING)) {
			case WEST -> SHAPE_WEST;
			case NORTH -> SHAPE_NORTH;
			case EAST -> SHAPE_EAST;
			default -> SHAPE_SOUTH;
		};
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new WaterPumpBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return createTickerHelper(type, ModBlockEntities.WATER_PUMP, WaterPumpBlockEntity::tick);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		// Look toward power source → FACING = input.
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof WaterPumpBlockEntity pump) {
			player.openMenu(pump);
		}
		return InteractionResult.SUCCESS;
	}

	public static Direction getInputFace(BlockState state) {
		return state.getValue(FACING);
	}

	/** UP plus horizontals other than the power input. */
	public static boolean isFluidOutputFace(BlockState state, Direction face) {
		if (face == Direction.DOWN) {
			return false;
		}
		if (face == Direction.UP) {
			return true;
		}
		return face.getAxis().isHorizontal() && face != getInputFace(state);
	}

	/**
	 * Whether a pipe on {@code pumpFace} of this pump may attach (visual + sticky lock).
	 */
	public static boolean allowsPipeConnection(BlockGetter level, BlockPos pumpPos, Direction pumpFace) {
		return WaterPumpBlockEntity.allowsPipeConnection(level, pumpPos, pumpFace);
	}

	/** Clockwise 90° around Y in block space (0..1 boxes). */
	private static VoxelShape rotateYClockWise(VoxelShape shape) {
		VoxelShape[] result = {Shapes.empty()};
		shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
			result[0] = Shapes.or(
				result[0],
				Shapes.box(1.0 - maxZ, minY, minX, 1.0 - minZ, maxY, maxX)
			);
		});
		return result[0];
	}
}
