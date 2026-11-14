param([Parameter(Mandatory=$true)][string]$OutputRoot)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$root = (Resolve-Path -LiteralPath $OutputRoot).Path
$source = [System.Drawing.Image]::FromFile((Join-Path $root 'xuanlan-poses-raw.png'))
$image = [System.Drawing.Bitmap]::new($source)
$source.Dispose()
$graphics = [System.Drawing.Graphics]::FromImage($image)
$graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit
$font = [System.Drawing.Font]::new('Segoe UI', 24, [System.Drawing.FontStyle]::Bold)
$medium = [System.Drawing.Font]::new('Segoe UI', 15, [System.Drawing.FontStyle]::Bold)
$small = [System.Drawing.Font]::new('Segoe UI', 12)
$bright = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(222, 247, 245))
$mint = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(104, 222, 190))
$muted = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(141, 174, 184))
$graphics.DrawString('XUANLAN / LUNAR MECHANISM', $font, $bright, 28, 15)
$graphics.DrawString('Original geometry + articulated bones / charge, recoil and catch', $small, $muted, 31, 60)
$labels = @('01 / REST', '02 / FULL CHARGE', '03 / FIRING RECOIL', '04 / READY', '05 / BLADE IN FLIGHT', '06 / CATCH & LOCK')
$notes = @('Closed rails / suspended energy chamber', 'Split rails / forward core / muzzle focus', 'Rearward slide / spring return', 'Twin crescents locked into the hub', 'Held blades fold while projectile is away', 'Blades unfold / lock ring overshoots then settles')
for ($index = 0; $index -lt 6; $index++) {
    $left = ($index % 3) * 480 + 32
    $top = [Math]::Floor($index / 3) * 365 + 112
    $graphics.DrawString($labels[$index], $medium, $mint, $left, $top)
    $graphics.DrawString($notes[$index], $small, $muted, $left, $top + 298)
}
$graphics.DrawString('OFFLINE POSE PREVIEW / sampled from the actual Java animation rules; not an in-game capture', $small, $muted, 30, 821)
$image.Save((Join-Path $root 'xuanlan-poses.png'), [System.Drawing.Imaging.ImageFormat]::Png)
foreach ($resource in @($graphics,$image,$font,$medium,$small,$bright,$mint,$muted)) { $resource.Dispose() }
