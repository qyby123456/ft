$ErrorActionPreference = 'Stop'
$taskRoot = $PSScriptRoot
$skillRunner = 'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill\scripts\run-build.mjs'
$taskPython = 'C:\Users\YOUR_USERNAME\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe'
foreach ($number in 11..20) {
    $identifier = "s3-$number"
    node $skillRunner (Join-Path $taskRoot "room$number.mjs") --out-dir (Join-Path $taskRoot "rooms\$identifier")
    if ($LASTEXITCODE -ne 0) { throw "$identifier build failed" }
    & $taskPython (Join-Path $taskRoot 'qa.py') $identifier
    if ($LASTEXITCODE -ne 0) { throw "$identifier native/routes failed" }
    & $taskPython (Join-Path $taskRoot 'support.py') $identifier
    if ($LASTEXITCODE -ne 0) { throw "$identifier support failed" }
}
node $skillRunner (Join-Path $taskRoot 'assemble.mjs') --out-dir (Join-Path $taskRoot 'rooms\s3-merged') --no-block-state-updates
if ($LASTEXITCODE -ne 0) { throw 'Merged build failed' }
& $taskPython (Join-Path $taskRoot 'qa.py') s3-merged
if ($LASTEXITCODE -ne 0) { throw 'Merged native/routes failed' }
& $taskPython (Join-Path $taskRoot 'support.py') s3-merged
if ($LASTEXITCODE -ne 0) { throw 'Merged support failed' }
node (Join-Path $taskRoot 'split-verify.mjs')
if ($LASTEXITCODE -ne 0) { throw 'Split/source verification failed' }
