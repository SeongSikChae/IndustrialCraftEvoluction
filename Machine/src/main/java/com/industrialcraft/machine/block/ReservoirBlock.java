package com.industrialcraft.machine.block;

import com.industrialcraft.machine.block.entity.ModBlockEntities;
import com.industrialcraft.machine.block.entity.ReservoirBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Fluid tank: GUI bucket fill, extract toward {@link net.minecraft.core.Direction#DOWN} only.
 * Side/top pipe insert is reserved for a later update.
 */
public class ReservoirBlock extends BaseEntityBlock {
	public static final MapCodec<ReservoirBlock> CODEC = simpleCodec(ReservoirBlock::new);
	private static final VoxelShape BODY = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);
	private static final VoxelShape SHAPE = Shapes.or(
		BODY,
		Block.box(6.0, -1.0, 6.0, 10.0, 1.5, 10.0),
		Block.box(6.0, 14.5, 6.0, 10.0, 17.0, 10.0),
		Block.box(6.0, 6.0, -1.0, 10.0, 10.0, 1.5),
		Block.box(6.0, 6.0, 14.5, 10.0, 10.0, 17.0),
		Block.box(-1.0, 6.0, 6.0, 1.5, 10.0, 10.0),
		Block.box(14.5, 6.0, 6.0, 17.0, 10.0, 10.0)
	);

	public ReservoirBlock(BlockBehaviour.Properties properties) {
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
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ReservoirBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide()
			? null
			: createTickerHelper(type, ModBlockEntities.RESERVOIR, ReservoirBlockEntity::serverTick);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ReservoirBlockEntity reservoir) {
			player.openMenu(reservoir);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
		Containers.updateNeighboursAfterDestroy(state, level, pos);
	}
}
