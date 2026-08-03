package com.industrialcraft.machine.block;

import com.industrialcraft.machine.block.entity.FurnaceEngineBlockEntity;
import com.industrialcraft.machine.block.entity.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class FurnaceEngineBlock extends BaseEntityBlock {
	public static final MapCodec<FurnaceEngineBlock> CODEC = simpleCodec(FurnaceEngineBlock::new);
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty LIT = BlockStateProperties.LIT;

	private static final VoxelShape BASE = Block.box(-1.0, 0.0, -1.0, 17.0, 1.0, 17.0);
	private static final VoxelShape CORE = Block.box(1.0, 1.0, 0.0, 15.0, 15.5, 15.0);
	private static final VoxelShape EXHAUST_NORTH = Block.box(5.5, 15.5, 10.0, 10.5, 24.0, 14.5);
	private static final VoxelShape EXHAUST_SOUTH = Block.box(5.5, 15.5, 1.5, 10.5, 24.0, 6.0);
	private static final VoxelShape EXHAUST_WEST = Block.box(10.0, 15.5, 5.5, 14.5, 24.0, 10.5);
	private static final VoxelShape EXHAUST_EAST = Block.box(1.5, 15.5, 5.5, 6.0, 24.0, 10.5);
	private static final VoxelShape SPROCKET_EAST = Block.box(15.0, 5.0, 5.0, 18.5, 11.0, 11.0);
	private static final VoxelShape SPROCKET_WEST = Block.box(-2.5, 5.0, 5.0, 1.0, 11.0, 11.0);
	private static final VoxelShape SPROCKET_SOUTH = Block.box(5.0, 5.0, 15.0, 11.0, 11.0, 18.5);
	private static final VoxelShape SPROCKET_NORTH = Block.box(5.0, 5.0, -2.5, 11.0, 11.0, 1.0);
	private static final VoxelShape SHAPE_NORTH = Shapes.or(BASE, CORE, EXHAUST_NORTH, SPROCKET_EAST);
	private static final VoxelShape SHAPE_SOUTH = Shapes.or(BASE, CORE, EXHAUST_SOUTH, SPROCKET_WEST);
	private static final VoxelShape SHAPE_WEST = Shapes.or(BASE, CORE, EXHAUST_WEST, SPROCKET_NORTH);
	private static final VoxelShape SHAPE_EAST = Shapes.or(BASE, CORE, EXHAUST_EAST, SPROCKET_SOUTH);

	public FurnaceEngineBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
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
			case SOUTH -> SHAPE_SOUTH;
			case WEST -> SHAPE_WEST;
			case EAST -> SHAPE_EAST;
			default -> SHAPE_NORTH;
		};
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new FurnaceEngineBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide()
			? createTickerHelper(type, ModBlockEntities.FURNACE_ENGINE, FurnaceEngineBlockEntity::clientTick)
			: createTickerHelper(type, ModBlockEntities.FURNACE_ENGINE, FurnaceEngineBlockEntity::serverTick);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (!state.getValue(LIT)) {
			return;
		}

		double x = pos.getX() + 0.5;
		double y = pos.getY();
		double z = pos.getZ() + 0.5;
		if (random.nextDouble() < 0.1) {
			level.playLocalSound(x, y, z, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
		}

		spawnExhaustSmoke(level, pos, state, random, true);
	}

	public static void spawnExhaustSmoke(Level level, BlockPos pos, BlockState state, RandomSource random, boolean forceVisible) {
		Direction rear = state.getValue(FACING).getOpposite();
		double x = pos.getX() + 0.5 + rear.getStepX() * 0.22 + (random.nextDouble() - 0.5) * 0.1;
		double y = pos.getY() + 1.5 + random.nextDouble() * 0.08;
		double z = pos.getZ() + 0.5 + rear.getStepZ() * 0.22 + (random.nextDouble() - 0.5) * 0.1;

		if (forceVisible) {
			level.addAlwaysVisibleParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, true, x, y, z, 0.0, 0.07, 0.0);
		} else {
			level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z, 0.0, 0.07, 0.0);
		}
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof FurnaceEngineBlockEntity engine) {
			player.openMenu(engine);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		// Shaft/sprocket faces the direction the player is looking.
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getClockWise());
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
		Containers.updateNeighboursAfterDestroy(state, level, pos);
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
		builder.add(FACING, LIT);
	}
}
