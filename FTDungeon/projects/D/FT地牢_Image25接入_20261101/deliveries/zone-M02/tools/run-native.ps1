param([string]$Run = (Get-Date -Format 'yyyyMMdd-HHmmss'))
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$javaHome = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
$gradle = 'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\bin\gradle.bat'
$oldJava = $env:JAVA_HOME
$oldPath = $env:PATH
try {
    $env:JAVA_HOME = $javaHome
    $env:PATH = "$javaHome\bin;$oldPath"
    Push-Location -LiteralPath (Join-Path $root 'native-test')
    & $gradle runGameTestServer --console=plain --offline '-Dnet.minecraftforge.gradle.check.certs=false' "-PqaRun=$Run" *> (Join-Path $root "qa\forge-$Run.log")
    $result = $LASTEXITCODE
    Get-Content -LiteralPath (Join-Path $root "qa\forge-$Run.log") -Tail 35
    if ($result -ne 0) { throw "Forge GameTest failed: $result" }
} finally {
    Pop-Location
    $env:JAVA_HOME = $oldJava
    $env:PATH = $oldPath
}
