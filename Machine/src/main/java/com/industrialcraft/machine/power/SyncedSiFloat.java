package com.industrialcraft.machine.power;

/**
 * Packs a SI {@code float} into two ContainerData shorts (IEEE-754 bit-exact).
 * Minecraft set-data packets still carry each slot as a signed short; reconstruct with {@code & 0xFFFF}.
 */
public final class SyncedSiFloat {
	private SyncedSiFloat() {
	}

	public static int loBits(float value) {
		return Float.floatToIntBits(value) & 0xFFFF;
	}

	public static int hiBits(float value) {
		return (Float.floatToIntBits(value) >>> 16) & 0xFFFF;
	}

	public static float fromData(int lo, int hi) {
		int bits = ((hi & 0xFFFF) << 16) | (lo & 0xFFFF);
		return Float.intBitsToFloat(bits);
	}

	public static float fromSi(double si) {
		if (!Double.isFinite(si)) {
			return 0.0F;
		}
		return (float) si;
	}
}
