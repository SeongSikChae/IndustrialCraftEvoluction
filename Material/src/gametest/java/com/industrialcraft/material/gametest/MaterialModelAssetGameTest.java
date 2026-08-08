package com.industrialcraft.material.gametest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * project-common §3 asset shape checks. Compressed fuel blocks must be real cube-face
 * textures (opaque, square), not reused lump item sprites.
 */
public class MaterialModelAssetGameTest {
	private static final String[] FLAT_ITEMS = {"peat", "lignite", "sub_bituminous", "anthracite"};
	private static final String[] BLOCK_IDS = {
		"peat_ore", "deepslate_peat_ore",
		"lignite_ore", "deepslate_lignite_ore",
		"sub_bituminous_ore", "deepslate_sub_bituminous_ore",
		"anthracite_ore", "deepslate_anthracite_ore",
		"peat_block", "lignite_block", "sub_bituminous_block", "anthracite_block"
	};
	private static final String[] FUEL_BLOCKS = {
		"peat_block", "lignite_block", "sub_bituminous_block", "anthracite_block"
	};

	@GameTest
	public void flatFuelItemsUseGeneratedModels(GameTestHelper helper) {
		for (String id : FLAT_ITEMS) {
			JsonObject itemModel = readJson("assets/material/models/item/" + id + ".json");
			helper.assertTrue(
				"minecraft:item/generated".equals(itemModel.get("parent").getAsString()),
				id + " item model parent must be generated (2D)"
			);
			String layer0 = itemModel.getAsJsonObject("textures").get("layer0").getAsString();
			helper.assertTrue(("material:item/" + id).equals(layer0), id + " layer0 texture");
			assertResourceExists(helper, "assets/material/textures/item/" + id + ".png");

			JsonObject itemDef = readJson("assets/material/items/" + id + ".json");
			String model = itemDef.getAsJsonObject("model").get("model").getAsString();
			helper.assertTrue(("material:item/" + id).equals(model), id + " items/*.json must point to item model");
		}
		helper.succeed();
	}

	@GameTest
	public void blockItemsUseBlockModels(GameTestHelper helper) {
		for (String id : BLOCK_IDS) {
			JsonObject blockModel = readJson("assets/material/models/block/" + id + ".json");
			helper.assertTrue(blockModel.has("parent"), id + " block model needs parent");
			helper.assertTrue(
				blockModel.get("parent").getAsString().startsWith("minecraft:block/"),
				id + " block model should be 3D block parent"
			);
			assertResourceExists(helper, "assets/material/textures/block/" + id + ".png");
			assertResourceExists(helper, "assets/material/blockstates/" + id + ".json");

			JsonObject itemDef = readJson("assets/material/items/" + id + ".json");
			String model = itemDef.getAsJsonObject("model").get("model").getAsString();
			helper.assertTrue(
				("material:block/" + id).equals(model),
				id + " block item must use 3D block model (not flat item model)"
			);
		}
		helper.succeed();
	}

	@GameTest
	public void compressedFuelBlockTexturesAreOpaqueCubeFaces(GameTestHelper helper) throws Exception {
		for (String blockId : FUEL_BLOCKS) {
			String rank = blockId.substring(0, blockId.length() - "_block".length());
			BufferedImage blockTex = readPng("assets/material/textures/block/" + blockId + ".png");
			BufferedImage itemTex = readPng("assets/material/textures/item/" + rank + ".png");

			helper.assertTrue(blockTex.getWidth() == blockTex.getHeight(), blockId + " texture must be square");
			helper.assertTrue(blockTex.getWidth() >= 16, blockId + " texture must be at least 16x16");

			int transparent = countTransparent(blockTex);
			helper.assertTrue(
				transparent == 0,
				blockId + " has " + transparent + " transparent pixels — item sprites must not be used as cube faces"
			);
			helper.assertTrue(
				!samePixels(blockTex, itemTex),
				blockId + " must not be a copy of textures/item/" + rank + ".png"
			);
		}
		helper.succeed();
	}

	private static int countTransparent(BufferedImage image) {
		int transparent = 0;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int alpha = (image.getRGB(x, y) >>> 24) & 0xFF;
				if (alpha < 255) {
					transparent++;
				}
			}
		}
		return transparent;
	}

	private static boolean samePixels(BufferedImage a, BufferedImage b) {
		if (a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight()) {
			return false;
		}
		for (int y = 0; y < a.getHeight(); y++) {
			for (int x = 0; x < a.getWidth(); x++) {
				if (a.getRGB(x, y) != b.getRGB(x, y)) {
					return false;
				}
			}
		}
		return true;
	}

	private static BufferedImage readPng(String path) throws Exception {
		InputStream stream = MaterialModelAssetGameTest.class.getClassLoader().getResourceAsStream(path);
		if (stream == null) {
			throw new AssertionError("missing classpath resource: " + path);
		}
		try (stream) {
			BufferedImage image = ImageIO.read(stream);
			if (image == null) {
				throw new AssertionError("failed decoding PNG: " + path);
			}
			return image;
		}
	}

	private static JsonObject readJson(String path) {
		InputStream stream = MaterialModelAssetGameTest.class.getClassLoader().getResourceAsStream(path);
		if (stream == null) {
			throw new AssertionError("missing classpath resource: " + path);
		}
		try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
			return JsonParser.parseReader(reader).getAsJsonObject();
		} catch (Exception e) {
			throw new AssertionError("failed reading " + path + ": " + e.getMessage(), e);
		}
	}

	private static void assertResourceExists(GameTestHelper helper, String path) {
		helper.assertTrue(
			MaterialModelAssetGameTest.class.getClassLoader().getResource(path) != null,
			"missing asset " + path
		);
	}
}
