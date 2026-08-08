package com.industrialcraft.material.gametest;

import com.industrialcraft.material.MaterialMod;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.GameType;

/**
 * Advancement → recipe-book unlock needs a real player connection (mock GameTest players NPE).
 */
@SuppressWarnings("UnstableApiUsage")
public class MaterialRecipeClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext world = context.worldBuilder().setUseConsistentSettings(true).create()) {
			world.getServer().runOnServer(server -> {
				ServerPlayer player = world.getConnection().getServerPlayer();
				player.setGameMode(GameType.SURVIVAL);
				unlock(player, "peat_block", "recipes/building/peat_block");
				unlock(player, "peat_from_block", "recipes/building/peat_from_block");
				unlock(player, "anthracite_block", "recipes/building/anthracite_block");
			});
			world.getConnection().waitForClientboundPackets();
		}
	}

	private static void unlock(ServerPlayer player, String recipePath, String advancementPath) {
		ResourceKey<Recipe<?>> recipeKey = ResourceKey.create(Registries.RECIPE, MaterialMod.id(recipePath));
		Identifier advancementId = MaterialMod.id(advancementPath);
		if (player.getRecipeBook().contains(recipeKey)) {
			throw new AssertionError(recipePath + " should start locked");
		}
		AdvancementHolder advancement = player.level().getServer().getAdvancements().get(advancementId);
		if (advancement == null) {
			throw new AssertionError("missing advancement " + advancementId);
		}
		AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
		for (String criterion : progress.getRemainingCriteria()) {
			player.getAdvancements().award(advancement, criterion);
		}
		if (!progress.isDone()) {
			throw new AssertionError("advancement not completed: " + advancementId);
		}
		if (!player.getRecipeBook().contains(recipeKey)) {
			throw new AssertionError("recipe not unlocked: " + recipePath);
		}
	}
}
