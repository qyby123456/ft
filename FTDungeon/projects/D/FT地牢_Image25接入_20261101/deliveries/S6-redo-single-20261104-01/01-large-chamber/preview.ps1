$ErrorActionPreference = 'Stop'
$skill = 'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill'
& node (Join-Path $skill 'scripts\preview.mjs') --nbt (Join-Path $PSScriptRoot 'room.nbt') --host 127.0.0.1 --port 5361 --no-open
