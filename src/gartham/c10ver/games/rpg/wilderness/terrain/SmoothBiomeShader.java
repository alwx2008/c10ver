package gartham.c10ver.games.rpg.wilderness.terrain;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.alixia.javalibrary.JavaTools;

import gartham.c10ver.games.rpg.wilderness.Emoji;
import gartham.c10ver.games.rpg.wilderness.Location;

public class SmoothBiomeShader implements BiomeShader {

	private static final MessageDigest md;

	static {
		MessageDigest md2;
		try {
			md2 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			md2 = null;
			System.err.println("No available MD5 hashing algorithm for Smooth Biome Shader!");
		}

		md = md2;
	}

	private final Emoji[] emojis = Emoji.values();

	private final class Vec {
		private final double x, y;

		public Vec(double x, double y) {
			this.x = x;
			this.y = y;
		}

	}

	private static long calculateLocalSeed(Seed seed, Location location, int index) {

		int x = location.getX() / 2, y = location.getY() / 2;

		byte[] msg = new byte[20];
		long s = seed.getSeed();
		for (byte i = 0; i < 8; i++, s >>= 8)
			msg[i] = (byte) (s & 0xff);

		s = (long) x << 32 | y;
		for (byte i = 0; i < 8; i++, s >>= 8)
			msg[i + 8] = (byte) (s & 0xff);

		for (byte i = 0; i < 4; i++, index >>= 8)
			msg[i + 4] = (byte) (index & 0xff);

		return JavaTools.bytesToLong(md.digest(msg));
	}

	@Override
	public void shade(String[][] tile, Seed seed, Location tileLocation) {
		Vec[][] grads = new Vec[tile.length + 1][tile[0].length + 1];

		// 0th rand is for grads.
		var r = new Random(calculateLocalSeed(seed, tileLocation, 0));

		for (int i = 0; i < grads.length; i++)
			for (int j = 0; j < grads.length; j++) {
				double sqr = r.nextDouble();
				grads[i][j] = new Vec(Math.sqrt(sqr), Math.sqrt(1 - sqr));
			}

		// Instead of correlating variables, we'll have each corner be completely
		// random.
		// To do this, every *other* tile will be "responsible" for generating the
		// values at corners.
		// Tiles that do not generate values themselves will rely on the values
		// generated by their adjacent tiles.

		double tl = new Random(seed.getSeed() + JavaTools.hash(tileLocation.getX(), tileLocation.getY())).nextDouble(),
				tr = new Random(seed.getSeed() + JavaTools.hash(tileLocation.getX() + 1, tileLocation.getY()))
						.nextDouble(),
				bl = new Random(seed.getSeed() + JavaTools.hash(tileLocation.getX(), tileLocation.getY() - 1))
						.nextDouble(),
				br = new Random(seed.getSeed() + JavaTools.hash(tileLocation.getX() + 1, tileLocation.getY() - 1))
						.nextDouble();

		for (int i = 0; i < tile.length; i++) {
			for (int j = 0; j < tile[i].length; j++) {
				double res = bilinearlyInterpolate(bl, br, tl, tr, tile[0].length, 0, 0, tile.length, j, i);
				res *= res * res * (res * (res * 6 - 15) + 10);
				tile[i][j] = emojis[(int) (res * emojis.length) % emojis.length].getValue();
			}
		}
	}

	private static double bilinearlyInterpolate(double bottomLeft, double bottomRight, double topLeft, double topRight,
			double rightXPos, double leftXPos, double bottomYPos, double topYPos, double x, double y) {
		double interpXY1 = (rightXPos - x) / (rightXPos - leftXPos) * bottomLeft
				+ (x - leftXPos) / (rightXPos - leftXPos) * bottomRight;
		double interpXY2 = (rightXPos - x) / (rightXPos - leftXPos) * topLeft
				+ (x - leftXPos) / (rightXPos - leftXPos) * topRight;

		double result = (topYPos - y) / (topYPos - bottomYPos) * interpXY1
				+ (y - bottomYPos) / (topYPos - bottomYPos) * interpXY2;
		return result;
	}

}
