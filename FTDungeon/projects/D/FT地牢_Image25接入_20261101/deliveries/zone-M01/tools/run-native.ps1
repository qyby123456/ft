$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$javaHome = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
$gradle = Get-ChildItem -LiteralPath 'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin' -Filter gradle.bat -Recurse | Select-Object -First 1 -ExpandProperty FullName
$oldJavaHome = $env:JAVA_HOME
$oldPath = $env:PATH
try {
    $env:JAVA_HOME = $javaHome
    $env:PATH = "$javaHome\bin;$oldPath"
    Push-Location -LiteralPath (Join-Path $root 'native-qa')
    try {
        & $gradle runGameTestServer --console=plain --offline '-Dnet.minecraftforge.gradle.check.certs=false' *> (Join-Path $root 'qa\forge-gametest.log')
        $nativeExit = $LASTEXITCODE
    } finally {
        Pop-Location
    }
} finally {
    $env:JAVA_HOME = $oldJavaHome
    $env:PATH = $oldPath
}
Get-Content -LiteralPath (Join-Path $root 'qa\forge-gametest.log') -Tail 55
exit $nativeExit
