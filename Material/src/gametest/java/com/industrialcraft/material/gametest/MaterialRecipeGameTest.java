package com.industrialcraft.material.gametest;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.industrialcraft.material.MaterialMod;
import com.industrialcraft.material.block.ModBlocks;
import com.industrialcraft.material.item.ModItems;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Block;

public class MaterialRecipeGameTest {
	private static final String[] COMPRESSION_RECIPES = {
		"peat_block", "peat_from_block",
		"lignite_block", "lignite_from_block",
		"sub_bituminous_block", "sub_bituminous_from_block",
		"anthracite_block", "anthracite_from_block"
	};

	private static final OreSmeltCase[] ORE_SMELT_CASES = {
		new OreSmeltCase("peat", ModBlocks.PEAT_ORE, ModBlocks.DEEPSLATE_PEAT_ORE, ModItems.PEAT),
		new OreSmeltCase("lignite", ModBlocks.LIGNITE_ORE, ModBlocks.DEEPSLATE_LIGNITE_ORE, ModItems.LIGNITE),
		new OreSmeltCase("sub_bituminous", ModBlocks.SUB_BITUMINOUS_ORE, ModBlocks.DEEPSLATE_SUB_BITUMINOUS_ORE, ModItems.SUB_BITUMINOUS),
		new OreSmeltCase("anthracite", ModBlocks.ANTHRACITE_ORE, ModBlocks.DEEPSLATE_ANTHRACITE_ORE, ModItems.ANTHRACITE)
	};

	@GameTest
	public void allRanksCompressToBlocks(GameTestHelper helper) {
		assertCraftsTo(helper, "peat_block", gridOf(ModItems.PEAT, 9), ModBlocks.PEAT_BLOCK.asItem(), 1);
		assertCraftsTo(helper, "lignite_block", gridOf(ModItems.LIGNITE, 9), ModBlocks.LIGNITE_BLOCK.asItem(), 1);
		assertCraftsTo(helper, "sub_bituminous_block", gridOf(ModItems.SUB_BITUMINOUS, 9), ModBlocks.SUB_BITUMINOUS_BLOCK.asItem(), 1);
		assertCraftsTo(helper, "anthracite_block", gridOf(ModItems.ANTHRACITE, 9), ModBlocks.ANTHRACITE_BLOCK.asItem(), 1);
		helper.succeed();
	}

	@GameTest
	public void allRanksDecompressFromBlocks(GameTestHelper helper) {
		assertCraftsTo(helper, "peat_from_block", List.of(new ItemStack(ModBlocks.PEAT_BLOCK)), ModItems.PEAT, 9);
		assertCraftsTo(helper, "lignite_from_block", List.of(new ItemStack(ModBlocks.LIGNITE_BLOCK)), ModItems.LIGNITE, 9);
		assertCraftsTo(helper, "sub_bituminous_from_block", List.of(new ItemStack(ModBlocks.SUB_BITUMINOUS_BLOCK)), ModItems.SUB_BITUMINOUS, 9);
		assertCraftsTo(helper, "anthracite_from_block", List.of(new ItemStack(ModBlocks.ANTHRACITE_BLOCK)), ModItems.ANTHRACITE, 9);
		helper.succeed();
	}

	@GameTest
	public void allCompressionRecipeAdvancementsExist(GameTestHelper helper) {
		for (String recipe : COMPRESSION_RECIPES) {
			assertRecipeAdvancement(helper, "recipes/building/" + recipe, recipe);
		}
		helper.succeed();
	}

	@GameTest
	public void allOreSmeltingAndBlastingRecipesMatch(GameTestHelper helper) {
		for (OreSmeltCase oreCase : ORE_SMELT_CASES) {
			assertCooksTo(helper, RecipeType.SMELTING, oreCase.rank + "_from_smelting_" + oreCase.rank + "_ore", oreCase.ore, oreCase.result);
			assertCooksTo(helper, RecipeType.SMELTING, oreCase.rank + "_from_smelting_deepslate_" + oreCase.rank + "_ore", oreCase.deepslateOre, oreCase.result);
			assertCooksTo(helper, RecipeType.BLASTING, oreCase.rank + "_from_blasting_" + oreCase.rank + "_ore", oreCase.ore, oreCase.result);
			assertCooksTo(helper, RecipeType.BLASTING, oreCase.rank + "_from_blasting_deepslate_" + oreCase.rank + "_ore", oreCase.deepslateOre, oreCase.result);
		}
		helper.succeed();
	}

	@GameTest
	public void allOreSmeltingRecipeAdvancementsExist(GameTestHelper helper) {
		for (OreSmeltCase oreCase : ORE_SMELT_CASES) {
			for (String kind : new String[] {"smelting", "blasting"}) {
				String normal = oreCase.rank + "_from_" + kind + "_" + oreCase.rank + "_ore";
				String deepslate = oreCase.rank + "_from_" + kind + "_deepslate_" + oreCase.rank + "_ore";
				assertRecipeAdvancement(helper, "recipes/misc/" + normal, normal);
				assertRecipeAdvancement(helper, "recipes/misc/" + deepslate, deepslate);
			}
		}
		helper.succeed();
	}

	private static List<ItemStack> gridOf(Item item, int count) {
		List<ItemStack> grid = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			grid.add(new ItemStack(item));
		}
		return grid;
	}

	private static void assertCraftsTo(
		GameTestHelper helper,
		String recipePath,
		List<ItemStack> stacks,
		Item expectedResult,
		int expectedCount
	) {
		Identifier recipeId = MaterialMod.id(recipePath);
		ResourceKey<Recipe<?>> recipeKey = ResourceKey.create(Registries.RECIPE, recipeId);
		Optional<RecipeHolder<?>> byKey = helper.getLevel().getServer().getRecipeManager().byKey(recipeKey);
		helper.assertTrue(byKey.isPresent(), "recipe registered: " + recipeId);

		int width = stacks.size() == 9 ? 3 : 1;
		int height = stacks.size() == 9 ? 3 : 1;
		CraftingInput input = CraftingInput.of(width, height, stacks);
		Optional<RecipeHolder<CraftingRecipe>> matched = helper.getLevel().recipeAccess()
			.getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
		helper.assertTrue(matched.isPresent(), "crafting match for " + recipePath);
		helper.assertTrue(matched.get().id().equals(recipeKey), "matched unexpected recipe " + matched.get().id());

		ItemStack result = matched.get().value().assemble(input);
		helper.assertTrue(result.is(expectedResult), recipePath + " craft result item");
		helper.assertTrue(result.getCount() == expectedCount, recipePath + " craft result count");
	}

	private static <T extends Recipe<SingleRecipeInput>> void assertCooksTo(
		GameTestHelper helper,
		RecipeType<T> recipeType,
		String recipePath,
		Block ore,
		Item expectedResult
	) {
		Identifier recipeId = MaterialMod.id(recipePath);
		ResourceKey<Recipe<?>> recipeKey = ResourceKey.create(Registries.RECIPE, recipeId);
		Optional<RecipeHolder<?>> byKey = helper.getLevel().getServer().getRecipeManager().byKey(recipeKey);
		helper.assertTrue(byKey.isPresent(), "recipe registered: " + recipeId);

		SingleRecipeInput input = new SingleRecipeInput(new ItemStack(ore));
		Optional<RecipeHolder<T>> matched = helper.getLevel().recipeAccess()
			.getRecipeFor(recipeType, input, helper.getLevel());
		helper.assertTrue(matched.isPresent(), "cooking match for " + recipePath);
		helper.assertTrue(matched.get().id().equals(recipeKey), "matched unexpected recipe " + matched.get().id());

		ItemStack result = matched.get().value().assemble(input);
		helper.assertTrue(result.is(expectedResult), recipePath + " cook result item");
		helper.assertTrue(result.getCount() == 1, recipePath + " cook result count");
	}

	private static void assertRecipeAdvancement(GameTestHelper helper, String advancementPath, String recipe) {
		Identifier advId = MaterialMod.id(advancementPath);
		AdvancementHolder advancement = helper.getLevel().getServer().getAdvancements().get(advId);
		helper.assertTrue(advancement != null, "missing advancement " + advId);

		JsonObject json = readDataJson("data/material/advancement/" + advancementPath + ".json");
		JsonArray rewarded = json.getAsJsonObject("rewards").getAsJsonArray("recipes");
		boolean found = false;
		for (int i = 0; i < rewarded.size(); i++) {
			if (("material:" + recipe).equals(rewarded.get(i).getAsString())) {
				found = true;
				break;
			}
		}
		helper.assertTrue(found, "advancement " + advId + " should reward recipe material:" + recipe);
	}

	private static JsonObject readDataJson(String path) {
		InputStream stream = MaterialRecipeGameTest.class.getClassLoader().getResourceAsStream(path);
		if (stream == null) {
			throw new AssertionError("missing datapack resource: " + path);
		}
		try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
			return JsonParser.parseReader(reader).getAsJsonObject();
		} catch (Exception e) {
			throw new AssertionError("failed reading " + path + ": " + e.getMessage(), e);
		}
	}

	private record OreSmeltCase(String rank, Block ore, Block deepslateOre, Item result) {
	}
}
