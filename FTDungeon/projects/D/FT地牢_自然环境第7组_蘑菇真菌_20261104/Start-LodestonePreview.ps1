$ErrorActionPreference = 'Stop'
$skill = 'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill'
$template = Join-Path $PSScriptRoot 'incoming\NAT7-01\room.nbt'
& node (Join-Path $skill 'scripts\preview.mjs') --nbt $template --port 5277 --no-open
