$ErrorActionPreference = 'Stop'
$preview = 'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill\scripts\preview.mjs'
$nbt = Join-Path $PSScriptRoot 'views\PREVIEW_ONLY_cutaway.nbt'
node $preview --nbt $nbt --port 5379 --no-open
