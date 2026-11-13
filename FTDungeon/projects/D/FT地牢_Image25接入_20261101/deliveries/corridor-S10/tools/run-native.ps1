$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$javaHome = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
$gradle = 'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\bin\gradle.bat'
$oldJava = $env:JAVA_HOME
$oldPath = $env:PATH
$originalLocation = Get-Location
$result = 1
try {
    Set-Location -LiteralPath $root
    $env:JAVA_HOME = $javaHome
    $env:PATH = "$javaHome\bin;$oldPath"
    $stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
    foreach ($relative in @('qa\native.json', 'qa\forge-gametest.log')) {
        if (Test-Path -LiteralPath $relative) {
            Copy-Item -LiteralPath $relative -Destination ($relative + '.previous-' + $stamp)
        }
    }
    & $gradle runGameTestServer --offline --console=plain '-Dnet.minecraftforge.gradle.check.certs=false' *> 'qa\forge-gametest.log'
    $result = $LASTEXITCODE
    Get-Content -LiteralPath 'qa\forge-gametest.log' -Tail 60
} finally {
    $env:JAVA_HOME = $oldJava
    $env:PATH = $oldPath
    Set-Location -LiteralPath $originalLocation
}
exit $result
