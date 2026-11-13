param([switch]$Rebuild)

$ErrorActionPreference = 'Stop'
$python = 'C:\Users\YOUR_USERNAME\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe'
$javaHome = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
$previousJavaHome = $env:JAVA_HOME
$previousPath = $env:PATH
$previousEncoding = $env:PYTHONIOENCODING
try {
    $env:JAVA_HOME = $javaHome
    $env:PATH = (Join-Path $javaHome 'bin') + ';' + $previousPath
    $env:PYTHONIOENCODING = 'utf-8'
    if ($Rebuild) {
        & $python (Join-Path $PSScriptRoot 'tools\build_r6.py')
        if ($LASTEXITCODE -ne 0) { throw 'R6 generation failed' }
    }
    & $python (Join-Path $PSScriptRoot 'tools\static_qa.py')
    if ($LASTEXITCODE -ne 0) { throw 'R6 static QA failed' }
    Push-Location (Join-Path $PSScriptRoot 'native-test')
    try {
        & (Join-Path $javaHome 'bin\java.exe') '-classpath' 'gradle\wrapper\gradle-wrapper.jar' 'org.gradle.wrapper.GradleWrapperMain' 'runGameTestServer' '--offline' '-Dnet.minecraftforge.gradle.check.certs=false' '--console=plain' 2>&1 | Tee-Object -FilePath (Join-Path $PSScriptRoot 'evidence\forge-native-console.log')
        if ($LASTEXITCODE -ne 0) { throw 'R6 Forge native QA failed' }
    } finally {
        Pop-Location
    }
    Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'native-test\run\r6-isolated-gametest\logs\latest.log') -Destination (Join-Path $PSScriptRoot 'evidence\forge-native-server.log')
    & $python (Join-Path $PSScriptRoot 'tools\render_preview.py')
    if ($LASTEXITCODE -ne 0) { throw 'R6 preview failed' }
    & $python (Join-Path $PSScriptRoot 'tools\finalize.py')
    if ($LASTEXITCODE -ne 0) { throw 'R6 finalization failed' }
} finally {
    $env:JAVA_HOME = $previousJavaHome
    $env:PATH = $previousPath
    $env:PYTHONIOENCODING = $previousEncoding
}
