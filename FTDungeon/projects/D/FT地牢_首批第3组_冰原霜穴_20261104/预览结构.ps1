param([ValidatePattern('^FTN3-0[1-9]$')][string]$Id = 'FTN3-01')
$nbt = Join-Path $PSScriptRoot "incoming\$Id\room.nbt"
& node 'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill\scripts\preview.mjs' --nbt $nbt --port 0 --no-open
