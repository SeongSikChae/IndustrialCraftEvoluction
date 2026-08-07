package com.industrialcraft.showcase.gametest;

import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.inventory.FurnaceScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * One ≤60s clip: each fuel grade (1×) + iron ore (64×) in a vanilla <strong>furnace</strong>,
 * recorded at 5× with the furnace GUI open.
 *
 * <p>Expected iron per fuel at furnace cook time (200 ticks): burnTicks / 200.
 */
@SuppressWarnings("UnstableApiUsage")
public final class FurnaceFuelShowcaseClip implements ShowcaseClip {
	public static final String ID = "furnace_fuels";

	/** Vanilla furnace cook duration for most ores. */
	public static final int FURNACE_COOK_TICKS = 200;
	public static final int IRON_ORE_COUNT = 64;

	/**
	 * Capture every N game ticks. At encode 10 fps this is N/2 × realtime
	 * (N=10 → 5×). Total burn ticks across grades = 6000 → 600 frames → 60s.
	 */
	public static final int TIMELAPSE_TICKS_PER_FRAME = 10;

	public static final List<FuelGradeSpec> GRADES = List.of(
		FuelGradeSpecs.PEAT,
		FuelGradeSpecs.LIGNITE,
		FuelGradeSpecs.SUB_BITUMINOUS,
		FuelGradeSpecs.BITUMINOUS,
		FuelGradeSpecs.ANTHRACITE
	);

	private static final BlockPos ORIGIN = new BlockPos(8, -60, 8);
	private static final BlockPos FURNACE_POS = ORIGIN;

	/** Mojang SLOT_INPUT / SLOT_FUEL / SLOT_RESULT (package-protected on BE). */
	private static final int SLOT_INPUT = 0;
	private static final int SLOT_FUEL = 1;
	private static final int SLOT_RESULT = 2;

	@Override
	public String id() {
		return ID;
	}

	@Override
	public void setup(ClientGameTestContext context, TestSingleplayerContext world) {
		ShowcaseWorlds.prepareCreativeFlying(world);
		ShowcaseWorlds.clearPlatform(world, ORIGIN, 8, 4, Blocks.SMOOTH_STONE);

		world.getServer().runOnServer(server -> {
			ServerLevel level = world.getConnection().getServerLevel();
			ServerPlayer player = world.getConnection().getServerPlayer();
			player.getInventory().clearContent();

			BlockState furnace = Blocks.FURNACE.defaultBlockState()
				.setValue(AbstractFurnaceBlock.FACING, Direction.SOUTH);
			level.setBlock(FURNACE_POS, furnace, 3);

			player.teleportTo(
				level,
				FURNACE_POS.getX() + 0.5,
				FURNACE_POS.getY(),
				FURNACE_POS.getZ() + 2.5,
				Set.of(),
				180.0F,
				15.0F,
				false
			);
		});
		world.getConnection().waitForClientboundPackets();
		world.getConnection().waitForChunksDownload();
		world.getConnection().waitForChunksRender();
		context.waitTicks(20);

		Captions.hideHud(context);
		openFurnaceGui(context, world);
	}

	@Override
	public void record(ClientGameTestContext context, TestSingleplayerContext world, FrameCapture frames) {
		ensureFurnaceGui(context, world);

		for (FuelGradeSpec grade : GRADES) {
			loadCharge(world, grade);
			context.waitTicks(2);
			world.getConnection().waitForClientboundPackets();
			ensureFurnaceGui(context, world);

			int targetFrames = grade.burnTicks() / TIMELAPSE_TICKS_PER_FRAME;
			for (int i = 0; i < targetFrames; i++) {
				ensureFurnaceGui(context, world);
				Captions.hideHud(context);
				System.out.println("Saved frame: " + frames.capture().toAbsolutePath()
					+ " [" + grade.clipId() + " " + (i + 1) + "/" + targetFrames + "]");
			}

			int produced = world.getServer().computeOnServer(server -> readResultCount(world));
			System.out.println(grade.clipId() + " produced iron ingots: " + produced
				+ " (expected " + expectedIngots(grade) + ")");
		}
	}

	private static void openFurnaceGui(ClientGameTestContext context, TestSingleplayerContext world) {
		world.getServer().runOnServer(server -> {
			ServerLevel level = world.getConnection().getServerLevel();
			ServerPlayer player = world.getConnection().getServerPlayer();
			if (!(level.getBlockEntity(FURNACE_POS) instanceof AbstractFurnaceBlockEntity furnace)) {
				throw new IllegalStateException("Furnace missing at " + FURNACE_POS);
			}
			player.openMenu(furnace);
		});
		world.getConnection().waitForClientboundPackets();
		context.waitForScreen(FurnaceScreen.class);
		context.waitTicks(5);
	}

	private static void ensureFurnaceGui(ClientGameTestContext context, TestSingleplayerContext world) {
		boolean open = context.computeOnClient(client -> client.gui.screen() instanceof FurnaceScreen);
		if (!open) {
			openFurnaceGui(context, world);
		}
	}

	private static void loadCharge(TestSingleplayerContext world, FuelGradeSpec grade) {
		world.getServer().runOnServer(server -> {
			ServerLevel level = world.getConnection().getServerLevel();
			if (!(level.getBlockEntity(FURNACE_POS) instanceof AbstractFurnaceBlockEntity furnace)) {
				throw new IllegalStateException("Furnace missing at " + FURNACE_POS);
			}
			furnace.setItem(SLOT_INPUT, new ItemStack(Items.IRON_ORE, IRON_ORE_COUNT));
			furnace.setItem(SLOT_FUEL, new ItemStack(grade.item(), 1));
			furnace.setItem(SLOT_RESULT, ItemStack.EMPTY);
			furnace.setChanged();
			BlockState state = level.getBlockState(FURNACE_POS);
			level.sendBlockUpdated(FURNACE_POS, state, state, 3);
		});
	}

	private static int readResultCount(TestSingleplayerContext world) {
		ServerLevel level = world.getConnection().getServerLevel();
		if (!(level.getBlockEntity(FURNACE_POS) instanceof AbstractFurnaceBlockEntity furnace)) {
			return 0;
		}
		return furnace.getItem(SLOT_RESULT).getCount();
	}

	public static int expectedIngots(FuelGradeSpec grade) {
		return grade.burnTicks() / FURNACE_COOK_TICKS;
	}
}
