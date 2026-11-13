Add-Type -AssemblyName System.Drawing
$sheetSets = @(
    @{ Name = 'eight-view-sheet.png'; Files = @('interior-01.png', 'interior-02.png', 'interior-03.png', 'interior-04.png', 'exterior-01.png', 'exterior-02.png', 'exterior-03.png', 'exterior-04.png') },
    @{ Name = 'full-source-interiors-sheet.png'; Files = @('nave-full-nbt.png', 'treasury-full-nbt.png', 'pool-full-nbt.png', 'court-full-nbt.png', 'bone-room-full-nbt.png', 'entrance-full-nbt.png', 'west-turn-full-nbt.png', 'east-turn-full-nbt.png') }
)
foreach ($sheetSet in $sheetSets) {
    $bitmap = New-Object System.Drawing.Bitmap 2880,1160
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $font = New-Object System.Drawing.Font 'Arial',15
    try {
        $graphics.Clear([System.Drawing.Color]::FromArgb(30,35,42))
        for ($index = 0; $index -lt 8; $index++) {
            $column = $index % 4
            $row = [math]::Floor($index / 4)
            $image = [System.Drawing.Image]::FromFile((Join-Path $PSScriptRoot $sheetSet.Files[$index]))
            try {
                $graphics.DrawImage($image, ($column * 720), ($row * 580 + 30), 720, 550)
                $graphics.DrawString($sheetSet.Files[$index], $font, [System.Drawing.Brushes]::White, ($column * 720 + 5), ($row * 580 + 5))
            } finally {
                $image.Dispose()
            }
        }
        $bitmap.Save((Join-Path $PSScriptRoot $sheetSet.Name), [System.Drawing.Imaging.ImageFormat]::Png)
    } finally {
        $font.Dispose()
        $graphics.Dispose()
        $bitmap.Dispose()
    }
}
