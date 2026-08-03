package com.industrialcraft.machine.block;

import com.industrialcraft.machine.menu.MachineCraftingTableMenu;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class MachineCraftingTableBlock extends Block {
	public static final MapCodec<MachineCraftingTableBlock> CODEC = simpleCodec(MachineCraftingTableBlock::new);
	private static final Component CONTAINER_TITLE = Component.translatable("container.machine.machine_crafting_table");

	public MachineCraftingTableBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends Block> codec() {
		return CODEC;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!level.isClientSide()) {
			player.openMenu(state.getMenuProvider(level, pos));
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
		return new SimpleMenuProvider(
			(containerId, inventory, player) -> new MachineCraftingTableMenu(containerId, inventory, ContainerLevelAccess.create(level, pos)),
			CONTAINER_TITLE
		);
	}
}
