$ErrorActionPreference = 'Stop'
$deliveryRoot = Split-Path -Parent $PSScriptRoot
$javaRuntime = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
$gradleExecutable = Get-ChildItem -LiteralPath 'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin' -Filter gradle.bat -Recurse | Select-Object -First 1 -ExpandProperty FullName
$previousJavaHome = $env:JAVA_HOME
$previousPath = $env:PATH
try {
    $env:JAVA_HOME = $javaRuntime
    $env:PATH = "$javaRuntime\bin;$previousPath"
    Push-Location -LiteralPath (Join-Path $deliveryRoot 'native-qa')
    try {
        & $gradleExecutable runGameTestServer --console=plain --offline '-Dnet.minecraftforge.gradle.check.certs=false' *> (Join-Path $deliveryRoot 'qa\forge-gametest.log')
        $nativeExit = $LASTEXITCODE
    } finally { Pop-Location }
} finally {
    $env:JAVA_HOME = $previousJavaHome
    $env:PATH = $previousPath
}
Get-Content -LiteralPath (Join-Path $deliveryRoot 'qa\forge-gametest.log') -Tail 45
exit $nativeExit
