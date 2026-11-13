param([ValidateRange(1,9)][int]$Room = 1)
$ErrorActionPreference = 'Stop'
$skill = 'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill'
$roomId = 'NAT6-{0:00}' -f $Room
$nbt = Join-Path $PSScriptRoot "incoming\$roomId\room.nbt"
& node (Join-Path $skill 'scripts\preview.mjs') --nbt $nbt --port (5235 + $Room) --no-open
