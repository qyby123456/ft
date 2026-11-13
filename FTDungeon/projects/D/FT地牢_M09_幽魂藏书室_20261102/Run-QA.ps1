param([switch]$Rebuild)

$ErrorActionPreference = 'Stop'
$python = 'C:\Users\YOUR_USERNAME\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe'
$javaHome = Join-Path $env:APPDATA '.minecraft\runtime\java-runtime-gamma-snapshot'
if ($env:JAVA_HOME_17) { $javaHome = $env:JAVA_HOME_17 }
$previousJavaHome = $env:JAVA_HOME
$previousPath = $env:PATH
$previousEncoding = $env:PYTHONIOENCODING
$runId = Get-Date -Format 'yyyyMMdd-HHmmss-fff'
$logPath = Join-Path $PSScriptRoot "evidence\forge-gametest-$runId.log"
try {
    $env:JAVA_HOME = $javaHome
    $env:PATH = (Join-Path $javaHome 'bin') + ';' + $previousPath
    $env:PYTHONIOENCODING = 'utf-8'
    if ($Rebuild) {
        & $python (Join-Path $PSScriptRoot 'tools\build_m09.py')
        if ($LASTEXITCODE -ne 0) { throw 'M09 generation failed' }
    }
    & $python (Join-Path $PSScriptRoot 'tools\static_qa.py')
    if ($LASTEXITCODE -ne 0) { throw 'M09 static QA failed' }
    Push-Location (Join-Path $PSScriptRoot 'native-test')
    try {
        & (Join-Path $javaHome 'bin\java.exe') '-classpath' 'gradle\wrapper\gradle-wrapper.jar' 'org.gradle.wrapper.GradleWrapperMain' 'runGameTestServer' '--offline' "-Pm09RunId=$runId" '-Dnet.minecraftforge.gradle.check.certs=false' '--console=plain' 2>&1 | Tee-Object -FilePath $logPath
        if ($LASTEXITCODE -ne 0) { throw 'M09 native Forge QA failed; keep the failure log' }
    } finally {
        Pop-Location
    }
    & $python (Join-Path $PSScriptRoot 'tools\render_preview.py')
    if ($LASTEXITCODE -ne 0) { throw 'M09 preview failed' }
    & $python (Join-Path $PSScriptRoot 'tools\finalize_delivery.py') '--native-log' $logPath
    if ($LASTEXITCODE -ne 0) { throw 'M09 final QA/packaging failed' }
} finally {
    $env:JAVA_HOME = $previousJavaHome
    $env:PATH = $previousPath
    $env:PYTHONIOENCODING = $previousEncoding
}
