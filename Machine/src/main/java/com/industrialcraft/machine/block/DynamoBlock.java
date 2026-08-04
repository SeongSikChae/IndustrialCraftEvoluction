package com.industrialcraft.machine.block;

import com.industrialcraft.machine.block.entity.DynamoBlockEntity;
import com.industrialcraft.machine.block.entity.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Through-shaft coupler: input→output 1:1, live metrics on the four non-I/O faces.
 * {@link #FACING} = output; input = opposite. Place looking at a source shaft (look = input).
 * <p>Locked rules: {@code docs/shaft-power-design.md} §3.
 */
public class DynamoBlock extends BaseEntityBlock {
	public static final MapCodec<DynamoBlock> CODEC = simpleCodec(DynamoBlock::new);
	/** Output shaft direction; input is the opposite horizontal face. */
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

	private static final VoxelShape BODY = Block.box(2.0, 2.0, 2.0, 14.0, 14.0, 14.0);
	/**
	 * Output axle bridges the body inset gap into the next Dynamo's input recess
	 * (bodies are inset to 2..14, so adjacent Dynamos need ~2.5 units past the face).
	 */
	private static final VoxelShape PORT_OUT_NORTH = Block.box(6.5, 6.5, -2.5, 9.5, 9.5, 2.0);
	private static final VoxelShape PORT_OUT_SOUTH = Block.box(6.5, 6.5, 14.0, 9.5, 9.5, 18.5);
	private static final VoxelShape PORT_OUT_WEST = Block.box(-2.5, 6.5, 6.5, 2.0, 9.5, 9.5);
	private static final VoxelShape PORT_OUT_EAST = Block.box(14.0, 6.5, 6.5, 18.5, 9.5, 9.5);
	private static final VoxelShape PORT_IN_NORTH = Block.box(5.5, 5.5, 2.0, 10.5, 10.5, 2.5);
	private static final VoxelShape PORT_IN_SOUTH = Block.box(5.5, 5.5, 13.5, 10.5, 10.5, 14.0);
	private static final VoxelShape PORT_IN_WEST = Block.box(2.0, 5.5, 5.5, 2.5, 10.5, 10.5);
	private static final VoxelShape PORT_IN_EAST = Block.box(13.5, 5.5, 5.5, 14.0, 10.5, 10.5);
	private static final VoxelShape SHAPE_NORTH_SOUTH = Shapes.or(BODY, PORT_OUT_NORTH, PORT_IN_SOUTH);
	private static final VoxelShape SHAPE_SOUTH_NORTH = Shapes.or(BODY, PORT_OUT_SOUTH, PORT_IN_NORTH);
	private static final VoxelShape SHAPE_EAST_WEST = Shapes.or(BODY, PORT_OUT_EAST, PORT_IN_WEST);
	private static final VoxelShape SHAPE_WEST_EAST = Shapes.or(BODY, PORT_OUT_WEST, PORT_IN_EAST);

	public DynamoBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
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
			case SOUTH -> SHAPE_SOUTH_NORTH;
			case EAST -> SHAPE_EAST_WEST;
			case WEST -> SHAPE_WEST_EAST;
			default -> SHAPE_NORTH_SOUTH;
		};
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new DynamoBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return createTickerHelper(type, ModBlockEntities.DYNAMO, DynamoBlockEntity::tick);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		// Design §3: look toward source shaft → look = input, FACING (output) = behind player.
		Direction look = context.getHorizontalDirection();
		return this.defaultBlockState().setValue(FACING, look.getOpposite());
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

	public static Direction getOutputFace(BlockState state) {
		return state.getValue(FACING);
	}

	public static Direction getInputFace(BlockState state) {
		return state.getValue(FACING).getOpposite();
	}
}
