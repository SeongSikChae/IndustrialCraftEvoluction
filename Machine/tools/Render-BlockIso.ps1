# Invicon-style 2:1 dimetric cube (Minecraft inventory look).
param(
	[Parameter(Mandatory = $true)][string]$TopPath,
	[Parameter(Mandatory = $true)][string]$LeftPath,
	[Parameter(Mandatory = $true)][string]$RightPath,
	[Parameter(Mandatory = $true)][string]$OutPath
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$typeName = 'McInvicon_' + [guid]::NewGuid().ToString('N').Substring(0, 8)
$cs = @"
using System;
using System.Drawing;
using System.Drawing.Imaging;

public static class $typeName {
	static Color[,] Load(string path) {
		using (var bmp = new Bitmap(path)) {
			int w = bmp.Width, h = bmp.Height;
			var a = new Color[w, h];
			for (int y = 0; y < h; y++)
				for (int x = 0; x < w; x++)
					a[x, y] = bmp.GetPixel(x, y);
			return a;
		}
	}

	static Color Shade(Color c, double f) {
		if (c.A < 8) return Color.FromArgb(0, 0, 0, 0);
		return Color.FromArgb(c.A,
			Clamp((int)Math.Round(c.R * f)),
			Clamp((int)Math.Round(c.G * f)),
			Clamp((int)Math.Round(c.B * f)));
	}

	static int Clamp(int v) {
		if (v < 0) return 0;
		if (v > 255) return 255;
		return v;
	}

	static void Put(Color[,] dst, int x, int y, Color c) {
		if (c.A < 8) return;
		if (x < 0 || y < 0 || x >= dst.GetLength(0) || y >= dst.GetLength(1)) return;
		dst[x, y] = c;
	}

	public static void Render(string topPath, string leftPath, string rightPath, string outPath) {
		var top = Load(topPath);
		var left = Load(leftPath);
		var right = Load(rightPath);
		int N = top.GetLength(0);
		if (N != top.GetLength(1) || left.GetLength(0) != N || right.GetLength(0) != N)
			throw new Exception("Textures must be square and same size");

		// Classic inventory footprint: (2N) x (2N)
		int W = N * 2, H = N * 2;
		var dst = new Color[W, H];
		for (int y = 0; y < H; y++)
			for (int x = 0; x < W; x++)
				dst[x, y] = Color.FromArgb(0, 0, 0, 0);

		// Left face (darker)
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				Color c = Shade(left[i, j], 0.6);
				int x = i;
				int y = j + (i / 2) + (N / 2);
				Put(dst, x, y, c);
			}
		}

		// Right face
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				Color c = Shade(right[i, j], 0.8);
				int x = i + N;
				int y = j + ((N - 1 - i) / 2) + (N / 2);
				Put(dst, x, y, c);
			}
		}

		// Top face — 2:1 diamond. Paint both pixels of each cell without smearing UV.
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				Color c = top[i, j];
				int x = (N - 1) + i - j;
				int y0 = (i + j) / 2;
				Put(dst, x, y0, c);
				// Fill companion pixel of the dimetric diamond cell
				if (((i + j) & 1) == 0)
					Put(dst, x, y0 + 1, c);
				else
					Put(dst, x - 1, y0, c);
			}
		}

		using (var bmp = new Bitmap(W, H, PixelFormat.Format32bppArgb)) {
			for (int y = 0; y < H; y++)
				for (int x = 0; x < W; x++)
					bmp.SetPixel(x, y, dst[x, y]);
			bmp.Save(outPath, ImageFormat.Png);
		}
	}
}
"@

$added = Add-Type -TypeDefinition $cs -ReferencedAssemblies System.Drawing -PassThru
$cls = @($added) | Where-Object { $_.IsClass -and $_.Name -like 'McInvicon_*' } | Select-Object -First 1
$cls.GetMethod('Render').Invoke($null, @($TopPath, $LeftPath, $RightPath, $OutPath))
Write-Host "Wrote $OutPath"
