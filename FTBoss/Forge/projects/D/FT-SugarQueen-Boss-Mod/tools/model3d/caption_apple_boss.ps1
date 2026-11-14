param([Parameter(Mandatory=$true)][string]$AssetRoot)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$root = (Resolve-Path -LiteralPath $AssetRoot).Path
$image = [System.Drawing.Bitmap]::new((Join-Path $root 'apple_boss_preview.png'))
$graphics = [System.Drawing.Graphics]::FromImage($image)
$font = [System.Drawing.Font]::new('Microsoft YaHei', 17, [System.Drawing.FontStyle]::Bold)
$small = [System.Drawing.Font]::new('Microsoft YaHei', 10)
$bright = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(232, 220, 184))
$muted = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(139, 157, 158))
try {
    $graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit
    $graphics.DrawString('01  苹果爵士 · 蜜脆庄园', $font, $bright, 32, 26)
    $graphics.DrawString('02  正面 · 礼帽与金边墨镜', $font, $bright, 592, 26)
    $graphics.DrawString('03  背面 · 三节叶脉披风', $font, $bright, 32, 636)
    $graphics.DrawString('04  二阶段 · 腐果与虫蛀形态', $font, $bright, 592, 636)
    $graphics.DrawString('141 部件 / 37 骨骼 / 逐面 UV / 256 × 256 双阶段贴图', $small, $muted, 32, 1188)
    $graphics.DrawString('实际模型离线渲染 · 非游戏截图', $small, $muted, 825, 1188)
    $image.Save((Join-Path $root 'apple_boss_preview_labeled.png'), [System.Drawing.Imaging.ImageFormat]::Png)
} finally {
    $graphics.Dispose(); $image.Dispose(); $font.Dispose(); $small.Dispose(); $bright.Dispose(); $muted.Dispose()
}
