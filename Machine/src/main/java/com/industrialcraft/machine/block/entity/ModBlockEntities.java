package com.industrialcraft.machine.block.entity;

import com.industrialcraft.machine.MachineMod;
import com.industrialcraft.machine.block.ModBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
	public static final BlockEntityType<FurnaceEngineBlockEntity> FURNACE_ENGINE = register(
		"furnace_engine",
		FurnaceEngineBlockEntity::new,
		ModBlocks.FURNACE_ENGINE
	);
	public static final BlockEntityType<DynamoBlockEntity> DYNAMO = register(
		"dynamo",
		DynamoBlockEntity::new,
		ModBlocks.DYNAMO
	);
	public static final BlockEntityType<ReservoirBlockEntity> RESERVOIR = register(
		"reservoir",
		ReservoirBlockEntity::new,
		ModBlocks.RESERVOIR
	);
	public static final BlockEntityType<FluidPipeBlockEntity> FLUID_PIPE = register(
		"fluid_pipe",
		FluidPipeBlockEntity::new,
		ModBlocks.FLUID_PIPE
	);
	public static final BlockEntityType<RainCollectorBlockEntity> RAIN_COLLECTOR = register(
		"rain_collector",
		RainCollectorBlockEntity::new,
		ModBlocks.RAIN_COLLECTOR
	);

	private ModBlockEntities() {
	}

	private static <T extends BlockEntity> BlockEntityType<T> register(
		String name,
		FabricBlockEntityTypeBuilder.Factory<? extends T> factory,
		Block... blocks
	) {
		return Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			MachineMod.id(name),
			FabricBlockEntityTypeBuilder.<T>create(factory, blocks).build()
		);
	}

	public static void initialize() {
		// Static field registration.
	}
}
