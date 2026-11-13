$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path -Parent $PSScriptRoot
$taskJava = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
$taskGradle = Get-ChildItem -LiteralPath 'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin' -Filter gradle.bat -Recurse | Select-Object -First 1 -ExpandProperty FullName
$savedJavaHome = $env:JAVA_HOME
$savedPath = $env:PATH
try {
    $env:JAVA_HOME = $taskJava
    $env:PATH = "$taskJava\bin;$savedPath"
    Push-Location -LiteralPath (Join-Path $taskRoot 'native-qa')
    try {
        $taskRunDirectory = 'run-isolated-h01-' + (Get-Date -Format 'yyyyMMdd-HHmmss-fff')
        & $taskGradle runGameTestServer --console=plain --offline '-Dnet.minecraftforge.gradle.check.certs=false' "-Ph01QaRunDir=$taskRunDirectory" *> (Join-Path $taskRoot 'qa\forge-gametest.log')
        $taskExit = $LASTEXITCODE
    } finally { Pop-Location }
} finally {
    $env:JAVA_HOME = $savedJavaHome
    $env:PATH = $savedPath
}
Get-Content -LiteralPath (Join-Path $taskRoot 'qa\forge-gametest.log') -Tail 45
exit $taskExit
