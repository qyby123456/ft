param([switch]$Rebuild, [switch]$Capture)

$ErrorActionPreference = 'Stop'
$skill = 'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill'
$javaHome = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
$gradle = 'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\bin\gradle.bat'
$python = 'C:\Users\YOUR_USERNAME\.codex\.venvs\imagegen\Scripts\python.exe'
$previousJavaHome = $env:JAVA_HOME
$previousPath = $env:PATH
try {
    $env:JAVA_HOME = $javaHome
    $env:PATH = (Join-Path $javaHome 'bin') + ';' + $previousPath
    if ($Rebuild) {
        & node (Join-Path $skill 'scripts\run-build.mjs') (Join-Path $PSScriptRoot 'tools\recipe.mjs') '--out-dir' $PSScriptRoot '--name' 'room' '--data-version' '3465' '--json'
        if ($LASTEXITCODE -ne 0) { throw 'M13 build failed' }
    }
    & node (Join-Path $PSScriptRoot 'tools\review.mjs')
    if ($LASTEXITCODE -ne 0) { throw 'M13 static QA failed' }
    Push-Location (Join-Path $PSScriptRoot 'native-test')
    try {
        & $gradle '--offline' '--no-daemon' '-Dnet.minecraftforge.gradle.check.certs=false' '--console=plain' 'runGameTestServer' *> (Join-Path $PSScriptRoot 'qa\forge-gametest-java17.log')
        if ($LASTEXITCODE -ne 0) { throw 'M13 Forge native QA failed; inspect qa/forge-gametest-java17.log' }
    } finally { Pop-Location }
    if ($Capture) {
        & node (Join-Path $PSScriptRoot 'tools\capture.mjs')
        if ($LASTEXITCODE -ne 0) { throw 'M13 capture failed; first start node tools/preview-server.mjs in a separate terminal' }
    }
    & $python (Join-Path $PSScriptRoot 'tools\finalize.py')
    if ($LASTEXITCODE -ne 0) { throw 'M13 finalization failed' }
} finally {
    $env:JAVA_HOME = $previousJavaHome
    $env:PATH = $previousPath
}
