package com.industrialcraft.material.gametest;

import com.industrialcraft.material.block.ModBlocks;
import com.industrialcraft.material.item.ModItems;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

public class MaterialFurnaceGameTest {
	private static final int SLOT_INPUT = 0;
	private static final int SLOT_FUEL = 1;
	private static final int SLOT_RESULT = 2;

	@GameTest(maxTicks = 100)
	public void peatFuelLightsFurnace(GameTestHelper helper) {
		assertFuelLightsFurnace(helper, new ItemStack(ModItems.PEAT));
	}

	@GameTest(maxTicks = 100)
	public void anthraciteFuelLightsFurnace(GameTestHelper helper) {
		assertFuelLightsFurnace(helper, new ItemStack(ModItems.ANTHRACITE));
	}

	@GameTest(maxTicks = 100)
	public void peatBlockFuelLightsFurnace(GameTestHelper helper) {
		assertFuelLightsFurnace(helper, new ItemStack(ModBlocks.PEAT_BLOCK));
	}

	@GameTest(maxTicks = 250)
	public void peatOreSmeltsToPeat(GameTestHelper helper) {
		assertOreSmeltsInFurnace(helper, ModBlocks.PEAT_ORE, ModItems.PEAT);
	}

	@GameTest(maxTicks = 250)
	public void deepslateAnthraciteOreSmeltsToAnthracite(GameTestHelper helper) {
		assertOreSmeltsInFurnace(helper, ModBlocks.DEEPSLATE_ANTHRACITE_ORE, ModItems.ANTHRACITE);
	}

	@GameTest(maxTicks = 150)
	public void ligniteOreBlastsToLignite(GameTestHelper helper) {
		assertOreSmeltsInBlock(helper, Blocks.BLAST_FURNACE, ModBlocks.LIGNITE_ORE, ModItems.LIGNITE);
	}

	private static void assertFuelLightsFurnace(GameTestHelper helper, ItemStack fuel) {
		BlockPos pos = BlockPos.ZERO;
		helper.setBlock(pos, Blocks.FURNACE);
		AbstractFurnaceBlockEntity furnace = helper.getBlockEntity(pos, AbstractFurnaceBlockEntity.class);
		furnace.setItem(SLOT_INPUT, new ItemStack(Items.IRON_ORE, 8));
		furnace.setItem(SLOT_FUEL, fuel.copy());
		furnace.setChanged();

		helper.succeedWhen(() -> {
			helper.assertTrue(
				helper.getBlockState(pos).getValue(AbstractFurnaceBlock.LIT),
				"furnace should be lit with " + fuel.getItem()
			);
			helper.assertTrue(furnace.getItem(SLOT_FUEL).isEmpty(), "fuel item consumed when lit");
		});
	}

	private static void assertOreSmeltsInFurnace(GameTestHelper helper, Block ore, Item result) {
		assertOreSmeltsInBlock(helper, Blocks.FURNACE, ore, result);
	}

	private static void assertOreSmeltsInBlock(GameTestHelper helper, Block furnaceBlock, Block ore, Item result) {
		BlockPos pos = BlockPos.ZERO;
		helper.setBlock(pos, furnaceBlock);
		AbstractFurnaceBlockEntity furnace = helper.getBlockEntity(pos, AbstractFurnaceBlockEntity.class);
		furnace.setItem(SLOT_INPUT, new ItemStack(ore));
		furnace.setItem(SLOT_FUEL, new ItemStack(Items.COAL));
		furnace.setChanged();

		helper.succeedWhen(() -> {
			ItemStack output = furnace.getItem(SLOT_RESULT);
			helper.assertTrue(output.is(result), "smelted " + ore + " should produce " + result);
			helper.assertTrue(output.getCount() == 1, "smelt result count");
			helper.assertTrue(furnace.getItem(SLOT_INPUT).isEmpty(), "input ore consumed");
		});
	}
}
