using System;
using System.Drawing;
using System.Drawing.Imaging;

class Program {
	static void Main(string[] args) {
		string srcPath = args[0];
		string outPath = args[1];
		double hueShift = double.Parse(args[2]);
		double satMul = double.Parse(args[3]);
		double valMul = double.Parse(args[4]);
		int rBias = int.Parse(args[5]);
		int gBias = int.Parse(args[6]);
		int bBias = int.Parse(args[7]);
		using (Bitmap src = new Bitmap(srcPath))
		using (Bitmap dst = new Bitmap(src.Width, src.Height, PixelFormat.Format32bppArgb)) {
			for (int y = 0; y < src.Height; y++) {
				for (int x = 0; x < src.Width; x++) {
					Color c = src.GetPixel(x, y);
					double rf = c.R / 255.0, gf = c.G / 255.0, bf = c.B / 255.0;
					double max = Math.Max(rf, Math.Max(gf, bf));
					double min = Math.Min(rf, Math.Min(gf, bf));
					double d = max - min;
					double h = 0;
					if (d > 1e-6) {
						if (max == rf) h = 60.0 * Mod6((gf - bf) / d);
						else if (max == gf) h = 60.0 * (((bf - rf) / d) + 2.0);
						else h = 60.0 * (((rf - gf) / d) + 4.0);
					}
					if (h < 0) h += 360.0;
					double s = max <= 1e-6 ? 0.0 : d / max;
					double v = max;
					h = Mod360(h + hueShift);
					s = Clamp01(s * satMul);
					v = Clamp01(v * valMul);
					double C = v * s;
					double X = C * (1.0 - Math.Abs(Mod2(h / 60.0) - 1.0));
					double m = v - C;
					double rp = 0, gp = 0, bp = 0;
					if (h < 60) { rp = C; gp = X; }
					else if (h < 120) { rp = X; gp = C; }
					else if (h < 180) { gp = C; bp = X; }
					else if (h < 240) { gp = X; bp = C; }
					else if (h < 300) { rp = X; bp = C; }
					else { rp = C; bp = X; }
					int rr = ClampByte((int)Math.Round((rp + m) * 255.0) + rBias);
					int gg = ClampByte((int)Math.Round((gp + m) * 255.0) + gBias);
					int bb = ClampByte((int)Math.Round((bp + m) * 255.0) + bBias);
					dst.SetPixel(x, y, Color.FromArgb(255, rr, gg, bb));
				}
			}
			dst.Save(outPath, ImageFormat.Png);
			Console.WriteLine("Wrote " + outPath);
		}
	}

	static double Mod6(double x) {
		double r = x % 6.0;
		if (r < 0) r += 6.0;
		return r;
	}

	static double Mod2(double x) {
		double r = x % 2.0;
		if (r < 0) r += 2.0;
		return r;
	}

	static double Mod360(double x) {
		double r = x % 360.0;
		if (r < 0) r += 360.0;
		return r;
	}

	static double Clamp01(double v) {
		if (v < 0) return 0;
		if (v > 1) return 1;
		return v;
	}

	static int ClampByte(int v) {
		if (v < 0) return 0;
		if (v > 255) return 255;
		return v;
	}
}
