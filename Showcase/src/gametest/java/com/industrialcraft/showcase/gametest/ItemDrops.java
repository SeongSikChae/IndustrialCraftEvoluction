package com.industrialcraft.showcase.gametest;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Helpers for dropping showcase items onto the floor (natural bob/spin render).
 */
public final class ItemDrops {
	private ItemDrops() {
	}

	/**
	 * Spawns a never-expiring, never-picked-up item entity with gravity (natural spin/bob).
	 *
	 * @return world position to aim the camera at (slightly above the floor)
	 */
	public static Vec3 dropOnFloor(ServerLevel level, ItemStack stack, Vec3 pos) {
		ItemEntity entity = new ItemEntity(level, pos.x, pos.y, pos.z, stack);
		entity.setDeltaMovement(Vec3.ZERO);
		entity.setNeverPickUp();
		entity.setUnlimitedLifetime();
		level.addFreshEntity(entity);
		return pos;
	}
}
