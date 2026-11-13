param([switch]$Interior)
$ErrorActionPreference = 'Stop'
$target = if ($Interior) { Join-Path $PSScriptRoot 'views\PREVIEW_ONLY_cutaway.nbt' } else { Join-Path $PSScriptRoot 'incoming\S7R2-02\room.nbt' }
& node 'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill\scripts\preview.mjs' --nbt $target --port 5378 --no-open
