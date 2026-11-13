param([switch]$Rebuild)

$ErrorActionPreference = 'Stop'
$python = 'C:\Users\YOUR_USERNAME\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe'
$javaHome = Join-Path $env:APPDATA '.minecraft\runtime\java-runtime-gamma-snapshot'
if ($env:JAVA_HOME_17) { $javaHome = $env:JAVA_HOME_17 }
$previousJavaHome = $env:JAVA_HOME
$previousPath = $env:PATH
$previousEncoding = $env:PYTHONIOENCODING
$previousLocation = Get-Location
try {
    $env:JAVA_HOME = $javaHome
    $env:PATH = (Join-Path $javaHome 'bin') + ';' + $previousPath
    $env:PYTHONIOENCODING = 'utf-8'
    if ($Rebuild) {
        & $python (Join-Path $PSScriptRoot 'tools\build_m08.py')
        if ($LASTEXITCODE -ne 0) { throw 'M08 generation failed' }
    }
    & $python (Join-Path $PSScriptRoot 'tools\static_qa.py')
    if ($LASTEXITCODE -ne 0) { throw 'M08 static QA failed' }
    Set-Location -LiteralPath (Join-Path $PSScriptRoot 'native-test')
    $nativeLog = Join-Path $PSScriptRoot ('evidence\forge-gametest-rerun-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '.log')
    & (Join-Path $javaHome 'bin\java.exe') '-classpath' 'gradle\wrapper\gradle-wrapper.jar' 'org.gradle.wrapper.GradleWrapperMain' 'runGameTestServer' '--offline' '-Dnet.minecraftforge.gradle.check.certs=false' '--console=plain' 2>&1 | Tee-Object -FilePath $nativeLog
    if ($LASTEXITCODE -ne 0) { throw 'M08 native Forge QA failed' }
    Set-Location -LiteralPath $PSScriptRoot
    & $python (Join-Path $PSScriptRoot 'tools\render_preview.py')
    if ($LASTEXITCODE -ne 0) { throw 'M08 preview failed' }
    & $python (Join-Path $PSScriptRoot 'tools\finalize_delivery.py') '--native-log' $nativeLog
    if ($LASTEXITCODE -ne 0) { throw 'M08 packaging failed' }
} finally {
    Set-Location -LiteralPath $previousLocation.Path
    $env:JAVA_HOME = $previousJavaHome
    $env:PATH = $previousPath
    $env:PYTHONIOENCODING = $previousEncoding
}
