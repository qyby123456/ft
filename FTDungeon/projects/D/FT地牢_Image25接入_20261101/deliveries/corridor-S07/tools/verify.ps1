$ErrorActionPreference = 'Stop'
$deliveryRoot = Split-Path -Parent $PSScriptRoot
$skillRoot = 'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill'
$pythonExecutable = 'C:\Users\YOUR_USERNAME\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe'
$gradleExecutable = 'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\bin\gradle.bat'
$previousJava = $env:JAVA_HOME

try {
    Push-Location -LiteralPath $skillRoot
    try {
        & node 'scripts/run-build.mjs' (Join-Path $deliveryRoot 'source/corridor-s07.mjs') --out-dir $deliveryRoot --name room --data-version 3465 --no-block-state-updates --json
        if ($LASTEXITCODE -ne 0) { throw 'Minecraft Builder export failed' }
    } finally {
        Pop-Location
    }
    & $pythonExecutable (Join-Path $deliveryRoot 'tools/static_qa.py')
    if ($LASTEXITCODE -ne 0) { throw 'Static QA failed' }
    & $pythonExecutable (Join-Path $deliveryRoot 'tools/render_views.py')
    if ($LASTEXITCODE -ne 0) { throw 'Four-view rendering failed' }
    $env:JAVA_HOME = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
    Push-Location -LiteralPath (Join-Path $deliveryRoot 'forge')
    try {
        & $gradleExecutable --offline --console=plain runGameTestServer *> (Join-Path $deliveryRoot 'qa/forge-run.log')
        if ($LASTEXITCODE -ne 0) { throw 'Forge GameTest failed; inspect qa/forge-run.log' }
    } finally {
        Pop-Location
    }
    & $pythonExecutable (Join-Path $deliveryRoot 'tools/finalize.py')
    if ($LASTEXITCODE -ne 0) { throw 'Final packaging failed or review evidence no longer matches the NBT' }
} finally {
    $env:JAVA_HOME = $previousJava
}
