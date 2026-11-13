$ErrorActionPreference = 'Stop'
$roomPreview = Join-Path $PSScriptRoot 'incoming/S7R2-08/room.nbt'
& node 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/preview.mjs' --nbt $roomPreview --port 5384 --no-open
