$ErrorActionPreference = 'Stop'
$treasurePreview = Join-Path $PSScriptRoot 'incoming/S7R2-09/room.nbt'
& node 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/preview.mjs' --nbt $treasurePreview --port 5385 --no-open
