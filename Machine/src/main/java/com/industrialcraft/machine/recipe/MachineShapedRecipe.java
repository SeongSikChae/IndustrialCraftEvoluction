package com.industrialcraft.machine.recipe;

import com.industrialcraft.machine.MachineMod;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

public class MachineShapedRecipe implements Recipe<CraftingInput> {
	public static final MapCodec<MachineShapedRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
			ShapedRecipePattern.MAP_CODEC.forGetter(MachineShapedRecipe::pattern),
			ItemStackTemplate.CODEC.fieldOf("result").forGetter(MachineShapedRecipe::result)
		).apply(instance, MachineShapedRecipe::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, MachineShapedRecipe> STREAM_CODEC = StreamCodec.composite(
		ShapedRecipePattern.STREAM_CODEC,
		MachineShapedRecipe::pattern,
		ItemStackTemplate.STREAM_CODEC,
		MachineShapedRecipe::result,
		MachineShapedRecipe::new
	);

	private final ShapedRecipePattern pattern;
	private final ItemStackTemplate result;
	private PlacementInfo placementInfo;

	public MachineShapedRecipe(ShapedRecipePattern pattern, ItemStackTemplate result) {
		this.pattern = pattern;
		this.result = result;
	}

	public ShapedRecipePattern pattern() {
		return this.pattern;
	}

	public ItemStackTemplate result() {
		return this.result;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		return this.pattern.matches(input);
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		return this.result.create();
	}

	@Override
	public boolean showNotification() {
		return true;
	}

	@Override
	public String group() {
		return "";
	}

	@Override
	public RecipeSerializer<? extends Recipe<CraftingInput>> getSerializer() {
		return ModRecipes.MACHINE_CRAFTING_SERIALIZER;
	}

	@Override
	public RecipeType<? extends Recipe<CraftingInput>> getType() {
		return ModRecipes.MACHINE_CRAFTING;
	}

	@Override
	public PlacementInfo placementInfo() {
		if (this.placementInfo == null) {
			this.placementInfo = PlacementInfo.createFromOptionals(this.pattern.ingredients());
		}
		return this.placementInfo;
	}

	@Override
	public RecipeBookCategory recipeBookCategory() {
		return ModRecipeBookCategories.MACHINE;
	}

	@Override
	public List<RecipeDisplay> display() {
		Item station = BuiltInRegistries.ITEM.getValue(MachineMod.id("machine_crafting_table"));
		return List.of(
			new ShapedCraftingRecipeDisplay(
				this.pattern.width(),
				this.pattern.height(),
				this.pattern.ingredients().stream().map(Ingredient::optionalIngredientToDisplay).toList(),
				new SlotDisplay.ItemStackSlotDisplay(this.result),
				new SlotDisplay.ItemSlotDisplay(station)
			)
		);
	}
}
