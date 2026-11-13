$ErrorActionPreference = 'Stop'
$deliveryRoot = Split-Path -Parent $PSScriptRoot
$qaJavaHome = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
$qaGradle = Get-ChildItem -LiteralPath 'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin' -Filter gradle.bat -Recurse | Select-Object -First 1 -ExpandProperty FullName
$qaRunDirectory = 'runs/run-' + (Get-Date -Format 'yyyyMMdd-HHmmss-fff')
$oldJavaHome = $env:JAVA_HOME
$oldPath = $env:PATH
try {
    $env:JAVA_HOME = $qaJavaHome
    $env:PATH = "$qaJavaHome\bin;$oldPath"
    Push-Location -LiteralPath (Join-Path $deliveryRoot 'native-qa')
    try {
        & $qaGradle runGameTestServer --console=plain --offline "-PqaRunDir=$qaRunDirectory" '-Dnet.minecraftforge.gradle.check.certs=false' *> (Join-Path $deliveryRoot 'qa\forge-gametest.log')
        $qaExit = $LASTEXITCODE
    } finally { Pop-Location }
} finally { $env:JAVA_HOME = $oldJavaHome; $env:PATH = $oldPath }
Get-Content -LiteralPath (Join-Path $deliveryRoot 'qa\forge-gametest.log') -Tail 45
exit $qaExit

