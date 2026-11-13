param([switch]$Rebuild, [switch]$Render, [string]$PreviewUrl = 'http://127.0.0.1:5308/')
$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
$skill = 'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill'
if ($Rebuild) {
    Push-Location $skill
    try {
        & node 'scripts\run-build.mjs' (Join-Path $root 'recipe.mjs') --out-dir $root --name room --data-version 3465 --no-block-state-updates --json
        if ($LASTEXITCODE -ne 0) { throw 'Minecraft Builder failed' }
    } finally { Pop-Location }
}
& node (Join-Path $root 'tools\static-qa.mjs')
if ($LASTEXITCODE -ne 0) { throw 'Static QA failed' }
$previousJava = $env:JAVA_HOME
$previousPath = $env:PATH
$stamp = Get-Date -Format 'yyyyMMdd-HHmmss-fff'
$log = Join-Path $root "qa\forge-gametest-$stamp.log"
$gradle = Get-ChildItem -Path 'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\*\gradle-8.8\bin\gradle.bat' | Select-Object -First 1
if (-not $gradle) { throw 'Gradle 8.8 is required' }
Push-Location (Join-Path $root 'native-test')
try {
    $env:JAVA_HOME = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
    $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
    & $gradle.FullName --offline --console plain '-Dnet.minecraftforge.gradle.check.certs=false' "-PqaRun=m06-$stamp" runGameTestServer *> $log
    $result = $LASTEXITCODE
    Copy-Item -LiteralPath $log -Destination (Join-Path $root 'qa\forge-gametest.log')
    Get-Content -LiteralPath $log -Tail 35
    if ($result -ne 0) { throw 'Isolated Forge GameTest failed; inspect the log' }
} finally {
    $env:JAVA_HOME = $previousJava
    $env:PATH = $previousPath
    Pop-Location
}
& node (Join-Path $root 'tools\review-native-snapshots.mjs')
if ($LASTEXITCODE -ne 0) { throw 'Native exported snapshot reread failed' }
if ($Render) {
    & node (Join-Path $root 'tools\capture-preview.mjs') $PreviewUrl
    if ($LASTEXITCODE -ne 0) { throw 'Lodestone rendering failed' }
}
Write-Output 'Inspect previews, then run: node tools/finalize.mjs --visual-reviewed'
