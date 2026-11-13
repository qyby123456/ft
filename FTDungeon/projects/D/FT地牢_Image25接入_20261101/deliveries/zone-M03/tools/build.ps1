$ErrorActionPreference = 'Stop'
$zoneRoot = Split-Path -Parent $PSScriptRoot
$skillRoot = 'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill'
Push-Location -LiteralPath $skillRoot
try {
    & node scripts/run-build.mjs (Join-Path $PSScriptRoot 'recipe.mjs') --out-dir (Join-Path $zoneRoot 'builder') --name room --data-version 3465 --json
    if ($LASTEXITCODE -ne 0) { throw 'Minecraft Builder failed' }
    Copy-Item -LiteralPath (Join-Path $zoneRoot 'builder/room.nbt') -Destination (Join-Path $zoneRoot 'room.nbt')
} finally { Pop-Location }
