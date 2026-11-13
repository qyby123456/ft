param([switch]$Cutaway)
$ErrorActionPreference = 'Stop'
$skill = 'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill'
$file = if ($Cutaway) { Join-Path $PSScriptRoot 'lodestone\inspection-cutaway.nbt' } else { Join-Path $PSScriptRoot 'room.nbt' }
$port = if ($Cutaway) { 5372 } else { 5371 }
& node (Join-Path $skill 'scripts\preview.mjs') --nbt $file --host 127.0.0.1 --port $port --no-open
