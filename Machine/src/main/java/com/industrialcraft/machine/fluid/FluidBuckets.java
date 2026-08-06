package com.industrialcraft.machine.fluid;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

/**
 * Bucket helpers for Reservoir GUI fill.
 */
public final class FluidBuckets {
	private FluidBuckets() {
	}

	public static boolean isFilledBucket(ItemStack stack) {
		return getFluid(stack) != Fluids.EMPTY;
	}

	public static Fluid getFluid(ItemStack stack) {
		if (stack.getItem() instanceof BucketItem bucket) {
			return FluidBuffer.normalize(bucket.getContent());
		}
		return Fluids.EMPTY;
	}

	public static @Nullable ItemStack emptiedBucket(ItemStack filled) {
		if (!isFilledBucket(filled)) {
			return null;
		}
		return new ItemStack(Items.BUCKET);
	}
}
