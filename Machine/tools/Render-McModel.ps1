# Renders Minecraft block/item model JSON(s) as a 32x32 inventory-style PNG.
param(
	[Parameter(Mandatory = $true)][string[]]$ModelPath,
	[Parameter(Mandatory = $true)][string]$AssetsBlockDir,
	[Parameter(Mandatory = $true)][string]$OutPath,
	[int]$Size = 32,
	[double]$RotX = 30,
	[double]$RotY = 225,
	[double]$RotZ = 0,
	[double]$Scale = 0.48,
	[double]$TransY = -0.5
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$typeName = 'McModel_' + [guid]::NewGuid().ToString('N').Substring(0, 8)
$cs = @"
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Web.Script.Serialization;

public static class $typeName {
	public struct V3 { public double X,Y,Z; public V3(double x,double y,double z){X=x;Y=y;Z=z;} }

	static readonly Dictionary<string,double> FaceShade = new Dictionary<string,double> {
		{ "down", 0.5 }, { "up", 1.0 },
		{ "north", 0.8 }, { "south", 0.8 },
		{ "west", 0.6 }, { "east", 0.6 }
	};

	static Color Shade(Color c, double f) {
		if (c.A < 8) return Color.FromArgb(0,0,0,0);
		return Color.FromArgb(c.A,
			Math.Min(255, (int)Math.Round(c.R * f)),
			Math.Min(255, (int)Math.Round(c.G * f)),
			Math.Min(255, (int)Math.Round(c.B * f)));
	}

	static Color[,] LoadTex(string path) {
		using (var bmp = new Bitmap(path)) {
			var a = new Color[bmp.Width, bmp.Height];
			for (int y = 0; y < bmp.Height; y++)
				for (int x = 0; x < bmp.Width; x++)
					a[x, y] = bmp.GetPixel(x, y);
			return a;
		}
	}

	static Color Sample(Color[,] tex, double u, double v) {
		int w = tex.GetLength(0), h = tex.GetLength(1);
		u = Math.Max(0, Math.Min(0.999999, u));
		v = Math.Max(0, Math.Min(0.999999, v));
		return tex[(int)Math.Floor(u * w), (int)Math.Floor(v * h)];
	}

	static V3 Rotate(V3 p, double rx, double ry, double rz) {
		double cx = Math.Cos(rx * Math.PI / 180.0), sx = Math.Sin(rx * Math.PI / 180.0);
		double cy = Math.Cos(ry * Math.PI / 180.0), sy = Math.Sin(ry * Math.PI / 180.0);
		double cz = Math.Cos(rz * Math.PI / 180.0), sz = Math.Sin(rz * Math.PI / 180.0);
		double y1 = p.Y * cx - p.Z * sx;
		double z1 = p.Y * sx + p.Z * cx;
		double x1 = p.X;
		double x2 = x1 * cy + z1 * sy;
		double z2 = -x1 * sy + z1 * cy;
		double y2 = y1;
		double x3 = x2 * cz - y2 * sz;
		double y3 = x2 * sz + y2 * cz;
		return new V3(x3, y3, z2);
	}

	static V3 Transform(V3 p, double scale, double transY, double rx, double ry, double rz) {
		// Model 0..16 → block-local -0.5..0.5 (ItemRenderer space)
		p = new V3((p.X - 8.0) / 16.0, (p.Y - 8.0) / 16.0, (p.Z - 8.0) / 16.0);
		// ItemTransform.apply: translate → rotateXYZ → scale
		p = new V3(p.X, p.Y + transY, p.Z);
		p = Rotate(p, rx, ry, rz);
		return new V3(p.X * scale, p.Y * scale, p.Z * scale);
	}

	class FaceJob {
		public Color[,] Tex;
		public double Shade;
		public V3 P00, P10, P01, P11;
		public double U0, V0, U1, V1;
	}

	static void AddFace(List<FaceJob> jobs, Color[,] tex, double shade,
		V3 c00, V3 c10, V3 c01, double u0, double v0, double u1, double v1,
		double scale, double transY, double rx, double ry, double rz) {
		V3 p00 = Transform(c00, scale, transY, rx, ry, rz);
		V3 p10 = Transform(c10, scale, transY, rx, ry, rz);
		V3 p01 = Transform(c01, scale, transY, rx, ry, rz);
		V3 c11 = new V3(c10.X + (c01.X - c00.X), c10.Y + (c01.Y - c00.Y), c10.Z + (c01.Z - c00.Z));
		V3 p11 = Transform(c11, scale, transY, rx, ry, rz);
		jobs.Add(new FaceJob {
			Tex = tex, Shade = shade,
			P00 = p00, P10 = p10, P01 = p01, P11 = p11,
			U0 = u0 / 16.0, V0 = v0 / 16.0, U1 = u1 / 16.0, V1 = v1 / 16.0
		});
	}

	static void Raster(List<FaceJob> jobs, Color[,] outPx, double[,] zbuf, int size,
		double minX, double maxX, double minY, double maxY) {
		foreach (var job in jobs) {
			double q00x = (job.P00.X - minX) / (maxX - minX) * (size - 1);
			double q00y = ((-job.P00.Y) - minY) / (maxY - minY) * (size - 1);
			double q10x = (job.P10.X - minX) / (maxX - minX) * (size - 1);
			double q10y = ((-job.P10.Y) - minY) / (maxY - minY) * (size - 1);
			double q01x = (job.P01.X - minX) / (maxX - minX) * (size - 1);
			double q01y = ((-job.P01.Y) - minY) / (maxY - minY) * (size - 1);
			double q11x = (job.P11.X - minX) / (maxX - minX) * (size - 1);
			double q11y = ((-job.P11.Y) - minY) / (maxY - minY) * (size - 1);

			double ax = q10x - q00x, ay = q10y - q00y;
			double bx = q01x - q00x, by = q01y - q00y;
			double det = ax * by - ay * bx;
			// Cull back-faces (camera looks toward +depth / screen)
			if (det <= 1e-12) continue;

			int pixMinX = Math.Max(0, (int)Math.Floor(Math.Min(Math.Min(q00x, q10x), Math.Min(q01x, q11x))) - 1);
			int pixMaxX = Math.Min(size - 1, (int)Math.Ceiling(Math.Max(Math.Max(q00x, q10x), Math.Max(q01x, q11x))) + 1);
			int pixMinY = Math.Max(0, (int)Math.Floor(Math.Min(Math.Min(q00y, q10y), Math.Min(q01y, q11y))) - 1);
			int pixMaxY = Math.Min(size - 1, (int)Math.Ceiling(Math.Max(Math.Max(q00y, q10y), Math.Max(q01y, q11y))) + 1);

			for (int iy = pixMinY; iy <= pixMaxY; iy++) {
				for (int ix = pixMinX; ix <= pixMaxX; ix++) {
					double dx = ix + 0.5 - q00x;
					double dy = iy + 0.5 - q00y;
					double s = (dx * by - dy * bx) / det;
					double t = (ax * dy - ay * dx) / det;
					if (s < -0.001 || t < -0.001 || s > 1.001 || t > 1.001) continue;
					s = Math.Max(0, Math.Min(1, s));
					t = Math.Max(0, Math.Min(1, t));
					double u = job.U0 + (job.U1 - job.U0) * s;
					double v = job.V0 + (job.V1 - job.V0) * t;
					double depth = job.P00.Z + s * (job.P10.Z - job.P00.Z) + t * (job.P01.Z - job.P00.Z);
					if (depth < zbuf[ix, iy]) continue;
					Color col = Shade(Sample(job.Tex, u, v), job.Shade);
					if (col.A < 8) continue;
					zbuf[ix, iy] = depth;
					outPx[ix, iy] = col;
				}
			}
		}
	}

	static string ResolveTex(Dictionary<string,string> textures, string key, string assetsDir) {
		string cur = key;
		for (int i = 0; i < 8; i++) {
			if (!cur.StartsWith("#")) break;
			string k = cur.Substring(1);
			if (!textures.ContainsKey(k)) throw new Exception("Missing texture #" + k);
			cur = textures[k];
		}
		int slash = cur.LastIndexOf('/');
		string file = (slash >= 0 ? cur.Substring(slash + 1) : cur) + ".png";
		string path = Path.Combine(assetsDir, file);
		if (!File.Exists(path)) throw new Exception("Texture not found: " + path);
		return path;
	}

	static void Collect(string jsonPath, string assetsDir, Dictionary<string,Color[,]> cache, List<FaceJob> jobs,
		double scale, double transY, double rx, double ry, double rz) {
		var ser = new JavaScriptSerializer();
		var root = (Dictionary<string,object>)ser.DeserializeObject(File.ReadAllText(jsonPath));
		var textures = new Dictionary<string,string>();
		if (root.ContainsKey("textures")) {
			foreach (var kv in (Dictionary<string,object>)root["textures"])
				textures[kv.Key] = Convert.ToString(kv.Value);
		}
		if (!root.ContainsKey("elements")) return;
		foreach (Dictionary<string,object> el in (object[])root["elements"]) {
			var from = (object[])el["from"];
			var to = (object[])el["to"];
			double x0 = Convert.ToDouble(from[0]), y0 = Convert.ToDouble(from[1]), z0 = Convert.ToDouble(from[2]);
			double x1 = Convert.ToDouble(to[0]), y1 = Convert.ToDouble(to[1]), z1 = Convert.ToDouble(to[2]);
			foreach (var fkv in (Dictionary<string,object>)el["faces"]) {
				var fd = (Dictionary<string,object>)fkv.Value;
				string dir = fkv.Key;
				string path = ResolveTex(textures, Convert.ToString(fd["texture"]), assetsDir);
				if (!cache.ContainsKey(path)) cache[path] = LoadTex(path);
				var uv = (object[])fd["uv"];
				double u0 = Convert.ToDouble(uv[0]), v0 = Convert.ToDouble(uv[1]);
				double u1 = Convert.ToDouble(uv[2]), v1 = Convert.ToDouble(uv[3]);
				double shade = FaceShade.ContainsKey(dir) ? FaceShade[dir] : 0.8;
				V3 c00, c10, c01;
				switch (dir) {
					case "north": c00 = new V3(x1,y1,z0); c10 = new V3(x0,y1,z0); c01 = new V3(x1,y0,z0); break;
					case "south": c00 = new V3(x0,y1,z1); c10 = new V3(x1,y1,z1); c01 = new V3(x0,y0,z1); break;
					case "west":  c00 = new V3(x0,y1,z0); c10 = new V3(x0,y1,z1); c01 = new V3(x0,y0,z0); break;
					case "east":  c00 = new V3(x1,y1,z1); c10 = new V3(x1,y1,z0); c01 = new V3(x1,y0,z1); break;
					case "up":    c00 = new V3(x0,y1,z0); c10 = new V3(x1,y1,z0); c01 = new V3(x0,y1,z1); break;
					case "down":  c00 = new V3(x0,y0,z1); c10 = new V3(x1,y0,z1); c01 = new V3(x0,y0,z0); break;
					default: continue;
				}
				AddFace(jobs, cache[path], shade, c00, c10, c01, u0, v0, u1, v1, scale, transY, rx, ry, rz);
			}
		}
	}

	public static void Render(string modelPathsCsv, string assetsDir, string outPath, int size,
		double rx, double ry, double rz, double scale, double transY) {
		var cache = new Dictionary<string,Color[,]>();
		var jobs = new List<FaceJob>();
		foreach (string mp in modelPathsCsv.Split(new char[]{'|'}, StringSplitOptions.RemoveEmptyEntries))
			Collect(mp.Trim(), assetsDir, cache, jobs, scale, transY, rx, ry, rz);

		double minX = double.PositiveInfinity, maxX = double.NegativeInfinity;
		double minY = double.PositiveInfinity, maxY = double.NegativeInfinity;
		foreach (var j in jobs) {
			V3[] pts = new V3[] { j.P00, j.P10, j.P01, j.P11 };
			foreach (var p in pts) {
				double sx = p.X, sy = -p.Y;
				if (sx < minX) minX = sx; if (sx > maxX) maxX = sx;
				if (sy < minY) minY = sy; if (sy > maxY) maxY = sy;
			}
		}
		double pad = 0.06 * Math.Max(maxX - minX, maxY - minY);
		double cx = (minX + maxX) * 0.5, cy = (minY + maxY) * 0.5;
		double half = Math.Max(maxX - minX, maxY - minY) * 0.5 + pad;
		minX = cx - half; maxX = cx + half; minY = cy - half; maxY = cy + half;

		var outPx = new Color[size, size];
		var zbuf = new double[size, size];
		for (int y = 0; y < size; y++)
			for (int x = 0; x < size; x++) {
				outPx[x, y] = Color.FromArgb(0,0,0,0);
				zbuf[x, y] = double.NegativeInfinity;
			}

		Raster(jobs, outPx, zbuf, size, minX, maxX, minY, maxY);

		using (var bmp = new Bitmap(size, size, PixelFormat.Format32bppArgb)) {
			for (int y = 0; y < size; y++)
				for (int x = 0; x < size; x++)
					bmp.SetPixel(x, y, outPx[x, y]);
			bmp.Save(outPath, ImageFormat.Png);
		}
	}
}
"@

$added = Add-Type -TypeDefinition $cs -ReferencedAssemblies @('System.Drawing','System.Web.Extensions') -PassThru
$cls = @($added) | Where-Object { $_.IsClass -and $_.IsPublic -and $_.Name -like 'McModel_*' } | Select-Object -First 1
if (-not $cls) { throw "Compiled type not found. Types: $((@($added) | ForEach-Object Name) -join ', ')" }
$paths = @($ModelPath)
if ($paths.Count -eq 1 -and $paths[0] -match ',') {
	$paths = @($paths[0].Split(',') | ForEach-Object { $_.Trim() })
}
$joined = ($paths -join '|')
Write-Host "Type=$($cls.FullName)"
Write-Host "models=$joined"
$code = @"
[$($cls.FullName)]::Render(
	`$joined,
	`$AssetsBlockDir,
	`$OutPath,
	`$Size,
	`$RotX, `$RotY, `$RotZ,
	`$Scale, `$TransY
)
"@
Invoke-Expression $code
Write-Host "Wrote $OutPath"
