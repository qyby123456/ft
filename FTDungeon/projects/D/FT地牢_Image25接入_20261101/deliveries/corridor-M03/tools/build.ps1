$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$skill = 'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill'
Push-Location -LiteralPath $skill
try {
    & node scripts/run-build.mjs (Join-Path $PSScriptRoot 'recipe.mjs') --out-dir (Join-Path $root 'builder') --name room --data-version 3465 --json
    if ($LASTEXITCODE -ne 0) { throw 'Minecraft Builder failed' }
    & node (Join-Path $PSScriptRoot 'prepare.mjs')
    if ($LASTEXITCODE -ne 0) { throw 'Metadata preparation failed' }
} finally { Pop-Location }
