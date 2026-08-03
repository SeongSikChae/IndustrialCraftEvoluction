package com.industrialcraft.material.mixin;

import com.industrialcraft.material.power.FuelDurations;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.vehicle.minecart.MinecartFurnace;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla furnace minecarts always add 3600 fuel ticks per tagged item.
 * Scale minecart fuel from {@code FuelValues} so materials differ
 * (coal 1600 → 3600, matching vanilla).
 */
@Mixin(MinecartFurnace.class)
public abstract class MinecartFurnaceMixin {
	@Shadow
	private int fuel;

	@Shadow
	private Vec3 push;

	@Inject(method = "addFuel", at = @At("HEAD"), cancellable = true)
	private void material$addFuelByBurnTime(Vec3 interactingPos, ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
		if (!itemStack.is(ItemTags.FURNACE_MINECART_FUEL)) {
			cir.setReturnValue(false);
			return;
		}

		MinecartFurnace self = (MinecartFurnace) (Object) this;
		Level level = self.level();
		int minecartFuel = FuelDurations.minecartStyleFuelTicks(level, itemStack);

		if (!FuelDurations.canAcceptFuel(this.fuel, minecartFuel)) {
			cir.setReturnValue(false);
			return;
		}

		this.fuel += minecartFuel;
		if (this.fuel > 0) {
			this.push = self.position().subtract(interactingPos).horizontal();
		}

		cir.setReturnValue(true);
	}
}
