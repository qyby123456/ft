param([int]$Number)
$ErrorActionPreference = 'Stop'
$taskRoot = $PSScriptRoot
$taskPython = 'C:\Users\YOUR_USERNAME\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe'
$identifier = "s3-$Number"
node 'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill\scripts\run-build.mjs' (Join-Path $taskRoot "room$Number.mjs") --out-dir (Join-Path $taskRoot "rooms\$identifier")
if ($LASTEXITCODE -ne 0) { throw "$identifier build failed" }
& $taskPython (Join-Path $taskRoot 'qa.py') $identifier *> (Join-Path $taskRoot "rooms\$identifier\qa\revision-native.log")
if ($LASTEXITCODE -ne 0) { throw "$identifier native/routes failed" }
& $taskPython (Join-Path $taskRoot 'support.py') $identifier
if ($LASTEXITCODE -ne 0) { throw "$identifier support failed" }
node (Join-Path $taskRoot 'render.cjs') $identifier
if ($LASTEXITCODE -ne 0) { throw "$identifier render failed" }
