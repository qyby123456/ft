$ErrorActionPreference = 'Stop'
$skill = 'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill'
$previousIndex = $env:S5_ROOM_INDEX
$previousSpec = $env:S5_SPEC_PATH
try {
    for ($index = 0; $index -lt 9; $index++) {
        $target = Join-Path $PSScriptRoot ('rooms\NAT-S5-{0:d2}' -f ($index + 1))
        New-Item -ItemType Directory -Force -Path $target | Out-Null
        $env:S5_ROOM_INDEX = [string]$index
        $env:S5_SPEC_PATH = Join-Path $target 'room.json'
        & node (Join-Path $skill 'scripts\run-build.mjs') (Join-Path $PSScriptRoot 'recipe.mjs') --out-dir $target --name room --json
        if ($LASTEXITCODE -ne 0) { throw "NBT build failed: $target" }
    }
} finally {
    $env:S5_ROOM_INDEX = $previousIndex
    $env:S5_SPEC_PATH = $previousSpec
}
