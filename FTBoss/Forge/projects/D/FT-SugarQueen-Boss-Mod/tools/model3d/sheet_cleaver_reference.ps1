param([string]$PreviousDirectory)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$root = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..'))
$art = Join-Path $root 'art\black_cleaver_reference'
$report = Get-Content -LiteralPath (Join-Path $art 'report.json') -Raw | ConvertFrom-Json

function Draw-Label {
  param($Graphics, [string]$Text, [single]$Left, [single]$Top, [single]$Size = 22, [bool]$Muted = $false)
  $font = [System.Drawing.Font]::new('Microsoft YaHei UI', $Size, [System.Drawing.FontStyle]::Regular, [System.Drawing.GraphicsUnit]::Pixel)
  $color = if ($Muted) { [System.Drawing.Color]::FromArgb(157, 164, 179) } else { [System.Drawing.Color]::FromArgb(230, 233, 240) }
  $brush = [System.Drawing.SolidBrush]::new($color)
  try { $Graphics.DrawString($Text, $font, $brush, $Left, $Top) }
  finally { $brush.Dispose(); $font.Dispose() }
}

function Draw-PanelImage {
  param($Graphics, [string]$File, [single]$Left, [single]$Top, [single]$Width, [single]$Height)
  $image = [System.Drawing.Image]::FromFile($File)
  try {
    $scale = [Math]::Min($Width / $image.Width, $Height / $image.Height)
    $rectangle = [System.Drawing.RectangleF]::new($Left + ($Width - $image.Width * $scale) / 2, $Top + ($Height - $image.Height * $scale) / 2, $image.Width * $scale, $image.Height * $scale)
    $Graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
    $Graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
    $source = [System.Drawing.RectangleF]::new(0, 0, $image.Width, $image.Height)
    $Graphics.DrawImage($image, $rectangle, $source, [System.Drawing.GraphicsUnit]::Pixel)
  }
  finally { $image.Dispose() }
}

function Save-Sheet {
  param([string]$Name, [int]$Width, [int]$Height, [scriptblock]$Content)
  $bitmap = [System.Drawing.Bitmap]::new($Width, $Height)
  $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
  try {
    $graphics.Clear([System.Drawing.Color]::FromArgb(24, 29, 39))
    $graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit
    & $Content $graphics
    $target = Join-Path $art $Name
    $bitmap.Save($target, [System.Drawing.Imaging.ImageFormat]::Png)
    Write-Output $target
  }
  finally { $graphics.Dispose(); $bitmap.Dispose() }
}

Save-Sheet 'comparison.png' 1400 880 {
  param($graphics)
  Draw-Label $graphics '黑切 / 厚刃 · 血怒动画版' 40 28 34
  Draw-Label $graphics '手绘原型 · 阶梯刃带 · 加固刃背 · 分层血怒发光' 42 85 18 $true
  Draw-Label $graphics '你的手绘参考' 218 132 24
  Draw-Label $graphics '优化模型 · 同角度' 908 132 24
  Draw-PanelImage $graphics (Join-Path $art 'reference.png') 60 176 580 580
  Draw-PanelImage $graphics (Join-Path $art 'reference-angle.png') 702 159 650 650
  Draw-Label $graphics "$($report.bones) 骨骼 / $($report.cubes) 方块 · 主刃厚 $($report.blade_thickness.iron) · 握柄宽 $($report.section_sizes.grip_core[0]) 模型单位" 42 810 18 $true
  Draw-Label $graphics '离线模型预览，非游戏截图' 42 841 17 $true
}

Save-Sheet 'model-sheet.png' 1320 1250 {
  param($graphics)
  Draw-Label $graphics '黑切血怒版 / 结构与展开' 40 26 34
  $panels = @(
    @{ Label = '合拢 · 正面'; File = 'front.png' },
    @{ Label = '合拢 · 背面'; File = 'back.png' },
    @{ Label = '侧面 · 实体厚度'; File = 'side.png' },
    @{ Label = '灰模 · 立体结构'; File = 'clay.png' },
    @{ Label = '展开 · 5.5°'; File = 'half-open.png' },
    @{ Label = '血怒 · 11°'; File = 'open.png' }
  )
  for ($index = 0; $index -lt $panels.Count; $index++) {
    $left = 30 + ($index % 3) * 440
    $top = 87 + [Math]::Floor($index / 3) * 551
    Draw-Label $graphics $panels[$index].Label ($left + 10) $top 24
    Draw-PanelImage $graphics (Join-Path $art $panels[$index].File) $left ($top + 39) 410 500
  }
  Draw-Label $graphics '离线预览；展开图发光为模拟效果，非游戏截图' 40 1212 17 $true
}

if (Test-Path -LiteralPath (Join-Path $art 'motion-impact.png')) {
  Save-Sheet 'animation-sheet.png' 1600 1090 {
    param($graphics)
    Draw-Label $graphics '黑切 / 血怒动画关键姿态' 35 24 34
    Draw-Label $graphics '先蓄能、再展开；维持时克制脉动，重劈时快速释放，最后平缓收回' 37 78 20 $true
    $stills = @(
      @{ Name = 'normal'; Label = '01 / 常态闭合' },
      @{ Name = 'unfold'; Label = '02 / 核心蓄能与展开' },
      @{ Name = 'heartbeat'; Label = '03 / 双拍脉动' },
      @{ Name = 'windup'; Label = '04 / 重劈蓄势' },
      @{ Name = 'impact'; Label = '05 / 释放与刃光峰值' },
      @{ Name = 'recover'; Label = '06 / 回震与回稳' },
      @{ Name = 'cooldown'; Label = '07 / 退怒合拢' },
      @{ Name = 'closed'; Label = '08 / 恢复闭合' }
    )
    for ($index = 0; $index -lt $stills.Count; $index++) {
      $left = 20 + ($index % 4) * 395
      $top = 126 + [Math]::Floor($index / 4) * 457
      Draw-Label $graphics $stills[$index].Label ($left + 12) $top 23
      Draw-PanelImage $graphics (Join-Path $art ('motion-' + $stills[$index].Name + '.png')) $left ($top + 37) 380 407
    }
    Draw-Label $graphics '读取导出的骨骼动画生成，固定镜头；发光为独立贴图层的离线模拟，非游戏截图。' 37 1057 18 $true
  }
}

if ($PreviousDirectory) {
  $previous = (Resolve-Path -LiteralPath $PreviousDirectory).Path
  if (-not (Test-Path -LiteralPath (Join-Path $previous 'hero.png'))) { throw 'Previous hero preview is missing' }
  $previousProject = Get-Content -LiteralPath (Join-Path $previous 'black_cleaver.bbmodel') -Raw | ConvertFrom-Json
  $previousGrip = $previousProject.elements | Where-Object name -EQ 'grip_core' | Select-Object -First 1
  $previousPlate = $previousProject.elements | Where-Object name -Like 'main_*_iron_*' | Select-Object -First 1
  if (-not $previousGrip -or -not $previousPlate) { throw 'Previous model sections are missing' }
  $previousWidth = [Math]::Round($previousGrip.to[0] - $previousGrip.from[0], 2)
  $previousThickness = [Math]::Round($previousPlate.to[2] - $previousPlate.from[2], 2)
  Save-Sheet 'mass-comparison.png' 1500 1000 {
    param($graphics)
    Draw-Label $graphics '黑切 / 厚重感修订对比' 40 28 36
    Draw-Label $graphics '同角度模型渲染 · 不是仅放大预览，已修改实体几何截面' 42 84 20 $true
    Draw-Label $graphics '修改前' 318 146 27
    Draw-Label $graphics '加厚加粗后' 1050 146 27
    Draw-PanelImage $graphics (Join-Path $previous 'hero.png') 38 188 685 685
    Draw-PanelImage $graphics (Join-Path $art 'hero.png') 778 188 685 685
    Draw-Label $graphics "主刃厚 $previousThickness / 握柄宽 $previousWidth" 205 890 22
    Draw-Label $graphics "主刃厚 $($report.blade_thickness.iron) / 握柄宽 $($report.section_sizes.grip_core[0])" 953 890 22
    Draw-Label $graphics '尺寸为模型单位；刃口仍有收薄层次，连接座与柄端同步加粗。离线渲染，非游戏截图。' 42 956 18 $true
  }
}
