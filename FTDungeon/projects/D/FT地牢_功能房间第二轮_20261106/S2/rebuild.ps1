param([Parameter(Mandatory=$true)][string]$Room)
$ErrorActionPreference='Stop'
$python='C:\Users\YOUR_USERNAME\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe'
& $python "$PSScriptRoot\revise-record.py" $Room
if($LASTEXITCODE -ne 0){throw 'design update failed'}
node 'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill\scripts\run-build.mjs' "$PSScriptRoot\$Room\recipe.mjs" --out-dir "$PSScriptRoot\$Room" --json
if($LASTEXITCODE -ne 0){throw 'build failed'}
& $python "$PSScriptRoot\check.py" $Room
if($LASTEXITCODE -ne 0){throw 'QA failed'}
node "$PSScriptRoot\render.cjs" "$PSScriptRoot\$Room"
if($LASTEXITCODE -ne 0){throw 'render failed'}
