$ErrorActionPreference = 'Stop'
$deliveryRoot = Split-Path -Parent $PSScriptRoot
$gradlePath = Get-ChildItem -LiteralPath 'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin' -Filter gradle.bat -Recurse | Select-Object -First 1 -ExpandProperty FullName
$taskJava = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
if (-not $gradlePath -or -not (Test-Path -LiteralPath "$taskJava\bin\java.exe")) { throw 'Java 17 and Gradle 8.8 required' }
$previousJava = $env:JAVA_HOME
$previousPath = $env:PATH
try {
    $env:JAVA_HOME = $taskJava
    $env:PATH = "$taskJava\bin;$previousPath"
    Push-Location -LiteralPath (Join-Path $deliveryRoot 'native')
    try {
        & $gradlePath runGameTestServer --offline --console=plain '-Dnet.minecraftforge.gradle.check.certs=false' *> (Join-Path $deliveryRoot 'qa/forge-gametest.log')
        $result = $LASTEXITCODE
    } finally { Pop-Location }
} finally {
    $env:JAVA_HOME = $previousJava
    $env:PATH = $previousPath
}
Get-Content -LiteralPath (Join-Path $deliveryRoot 'qa/forge-gametest.log') -Tail 45
exit $result

