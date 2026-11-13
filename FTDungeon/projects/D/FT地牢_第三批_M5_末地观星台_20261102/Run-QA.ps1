param([switch]$Rebuild)

$ErrorActionPreference = 'Stop'
$python = 'C:\Users\YOUR_USERNAME\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe'
$javaHome = Join-Path $env:APPDATA '.minecraft\runtime\java-runtime-gamma-snapshot'
if ($env:JAVA_HOME_17) { $javaHome = $env:JAVA_HOME_17 }
$previousJavaHome = $env:JAVA_HOME
$previousPath = $env:PATH
$previousEncoding = $env:PYTHONIOENCODING
try {
    $env:JAVA_HOME = $javaHome
    $env:PATH = (Join-Path $javaHome 'bin') + ';' + $previousPath
    $env:PYTHONIOENCODING = 'utf-8'
    if ($Rebuild) {
        & $python (Join-Path $PSScriptRoot 'tools\build_m5.py')
        if ($LASTEXITCODE -ne 0) { throw 'M5 generation failed' }
    }
    & $python (Join-Path $PSScriptRoot 'tools\static_qa.py')
    if ($LASTEXITCODE -ne 0) { throw 'M5 static QA failed' }
    Push-Location (Join-Path $PSScriptRoot 'native-test')
    try {
        & (Join-Path $javaHome 'bin\java.exe') '-classpath' 'gradle\wrapper\gradle-wrapper.jar' 'org.gradle.wrapper.GradleWrapperMain' 'runGameTestServer' '--offline' '-Dnet.minecraftforge.gradle.check.certs=false' '--console=plain' 2>&1 | Tee-Object -FilePath (Join-Path $PSScriptRoot 'evidence\forge-gametest-rerun.log')
        if ($LASTEXITCODE -ne 0) { throw 'M5 native Forge QA failed' }
    } finally {
        Pop-Location
    }
    & $python (Join-Path $PSScriptRoot 'tools\render_preview.py')
    if ($LASTEXITCODE -ne 0) { throw 'M5 preview failed' }
    & $python (Join-Path $PSScriptRoot 'tools\finalize_delivery.py') '--native-log' 'evidence/forge-gametest-rerun.log'
    if ($LASTEXITCODE -ne 0) { throw 'M5 packaging failed' }
} finally {
    $env:JAVA_HOME = $previousJavaHome
    $env:PATH = $previousPath
    $env:PYTHONIOENCODING = $previousEncoding
}
