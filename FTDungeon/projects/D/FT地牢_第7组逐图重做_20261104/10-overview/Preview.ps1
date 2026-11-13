$ErrorActionPreference = 'Stop'
$overviewPreview = Join-Path $PSScriptRoot 'incoming/S7R2-10/room.nbt'
& node 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/preview.mjs' --nbt $overviewPreview --port 5386 --no-open
