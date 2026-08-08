# Crop a hotbar HUD screenshot into a clean 32x32 item icon.
# Geometry only uses the slot interior — whatever the in-game slot rim already clips stays clipped.
param(
	[Parameter(Mandatory = $true)][string]$SourcePath,
	[Parameter(Mandatory = $true)][string]$OutPath,
	[int]$SlotIndex = 0,
	[int]$SlotLeft = 244,
	[int]$SlotTop = 440,
	[int]$SlotPitch = 40,
	# ~6px strips wood/selection bevel on each side of a 40px hotbar cell.
	[int]$Inset = 6,
	[int]$Pad = 2
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing

function Test-Discard([System.Drawing.Color]$c) {
	if ($c.A -lt 8) { return $true }
	$r = [int]$c.R; $g = [int]$c.G; $b = [int]$c.B
	$max = [Math]::Max($r, [Math]::Max($g, $b))
	$min = [Math]::Min($r, [Math]::Min($g, $b))
	$sat = $max - $min

	# Empty slot hole / black concrete
	if ($max -le 24) { return $true }

	# Bright selection highlight
	if ($max -ge 155 -and $sat -le 40) { return $true }

	# Green-tinted / beige hotbar chrome (selection rim averages ~158,174,154)
	if ($g -ge ($r + 5) -and $g -ge ($b + 5) -and $max -ge 80 -and $max -le 200) { return $true }
	if ($r -gt ($b + 18) -and $g -gt ($b + 10) -and $b -lt 95 -and $max -lt 200) { return $true }

	return $false
}

$src = [System.Drawing.Bitmap]::FromFile((Resolve-Path $SourcePath))
try {
	$x0 = $SlotLeft + ($SlotIndex * $SlotPitch) + $Inset
	$y0 = $SlotTop + $Inset
	$size = $SlotPitch - (2 * $Inset)
	if ($size -lt 8) { throw "Slot crop size too small: $size (check SlotPitch/Inset)" }

	$work = New-Object System.Drawing.Bitmap $size, $size, ([System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
	$g = [System.Drawing.Graphics]::FromImage($work)
	$g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
	$g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
	$g.DrawImage(
		$src,
		(New-Object System.Drawing.Rectangle 0, 0, $size, $size),
		(New-Object System.Drawing.Rectangle $x0, $y0, $size, $size),
		[System.Drawing.GraphicsUnit]::Pixel)
	$g.Dispose()

	for ($y = 0; $y -lt $size; $y++) {
		for ($x = 0; $x -lt $size; $x++) {
			if (Test-Discard ($work.GetPixel($x, $y))) {
				$work.SetPixel($x, $y, [System.Drawing.Color]::Transparent)
			}
		}
	}

	$minX = $size; $minY = $size; $maxX = -1; $maxY = -1
	for ($y = 0; $y -lt $size; $y++) {
		for ($x = 0; $x -lt $size; $x++) {
			if ($work.GetPixel($x, $y).A -gt 8) {
				if ($x -lt $minX) { $minX = $x }
				if ($y -lt $minY) { $minY = $y }
				if ($x -gt $maxX) { $maxX = $x }
				if ($y -gt $maxY) { $maxY = $y }
			}
		}
	}
	if ($maxX -lt 0) { throw "No opaque pixels after cleanup: $OutPath" }

	$bw = $maxX - $minX + 1
	$bh = $maxY - $minY + 1
	$inner = 32 - (2 * $Pad)
	if ($inner -lt 8) { throw "Pad too large for 32x32" }

	$final = New-Object System.Drawing.Bitmap 32, 32, ([System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
	$fg = [System.Drawing.Graphics]::FromImage($final)
	$fg.Clear([System.Drawing.Color]::Transparent)
	$fg.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
	$fg.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half

	$scale = [Math]::Min([double]$inner / $bw, [double]$inner / $bh)
	$dw = [Math]::Max(1, [int][Math]::Round($bw * $scale))
	$dh = [Math]::Max(1, [int][Math]::Round($bh * $scale))
	$ox = [int]((32 - $dw) / 2)
	$oy = [int]((32 - $dh) / 2)
	$fg.DrawImage(
		$work,
		(New-Object System.Drawing.Rectangle $ox, $oy, $dw, $dh),
		(New-Object System.Drawing.Rectangle $minX, $minY, $bw, $bh),
		[System.Drawing.GraphicsUnit]::Pixel)
	$fg.Dispose()
	$work.Dispose()

	$full = [IO.Path]::GetFullPath($OutPath)
	$dir = [IO.Path]::GetDirectoryName($full)
	if ($dir -and -not (Test-Path $dir)) { New-Item -ItemType Directory -Force -Path $dir | Out-Null }
	$final.Save($full, [System.Drawing.Imaging.ImageFormat]::Png)
	$final.Dispose()
	Write-Host "Wrote $full (${bw}x${bh} -> ${dw}x${dh} in 32x32, pad=$Pad, inset=$Inset)"
}
finally {
	$src.Dispose()
}
