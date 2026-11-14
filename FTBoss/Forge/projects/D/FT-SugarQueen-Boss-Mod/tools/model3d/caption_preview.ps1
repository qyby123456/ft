param([Parameter(Mandatory=$true)][string]$AssetRoot)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$root = (Resolve-Path -LiteralPath $AssetRoot).Path
$report = Get-Content -LiteralPath (Join-Path $root 'apple_stem_report.json') -Raw -Encoding UTF8 | ConvertFrom-Json
$inputPath = Join-Path $root 'apple_stem_views.png'
$source = [System.Drawing.Image]::FromFile($inputPath)
$image = [System.Drawing.Bitmap]::new($source)
$source.Dispose()
$graphics = [System.Drawing.Graphics]::FromImage($image)
$graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit
$font = [System.Drawing.Font]::new('Microsoft YaHei', 26, [System.Drawing.FontStyle]::Bold)
$small = [System.Drawing.Font]::new('Microsoft YaHei', 12)
$medium = [System.Drawing.Font]::new('Microsoft YaHei', 14)
$bright = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(229,234,204))
$muted = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(147,171,144))
$graphics.DrawString('苹果爵士的果梗 · 蜜脆青枝', $font, $bright, 24, 16)
$graphics.DrawString('红苹果护心 / 青叶弯刃 / 缠带枝柄', $small, $muted, 28, 61)
$graphics.DrawString('正面', $medium, $bright, 181, 725)
$graphics.DrawString('立体斜视', $medium, $bright, 552, 725)
$graphics.DrawString('背面', $medium, $bright, 963, 725)
$graphics.DrawString("$($report.elements) 立体部件 · $($report.textureSize)×$($report.textureSize) 图集 · 离线贴图预览，非游戏截图", $small, $muted, 28, 759)
$image.Save((Join-Path $root 'apple_stem_preview.png'), [System.Drawing.Imaging.ImageFormat]::Png)
$graphics.Dispose()
$image.Dispose()
$font.Dispose()
$small.Dispose()
$medium.Dispose()
$bright.Dispose()
$muted.Dispose()
