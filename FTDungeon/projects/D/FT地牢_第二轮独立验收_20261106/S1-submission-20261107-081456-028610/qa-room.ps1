param([string]$Room)
$ErrorActionPreference='Stop'
$root=$PSScriptRoot
$folder=Join-Path $root $Room
$skill='C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill'
& node "$skill\scripts\run-build.mjs" "$folder\recipe.mjs" --out-dir $folder --data-version 3465
if($LASTEXITCODE){throw 'build failed'}
& 'C:\Users\YOUR_USERNAME\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe' "$root\equip.py" $folder
if($LASTEXITCODE){throw 'equipment payload failed'}
& node "$root\render.cjs" $folder
if($LASTEXITCODE){throw 'render failed'}
