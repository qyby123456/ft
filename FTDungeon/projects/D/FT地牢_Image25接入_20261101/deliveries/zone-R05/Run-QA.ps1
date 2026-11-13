param([switch]$Rebuild)
$ErrorActionPreference = 'Stop'
$pythonPath = 'C:\Users\YOUR_USERNAME\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe'
$skillPath = 'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill'
$javaPath = Join-Path $env:APPDATA '.minecraft\runtime\java-runtime-gamma-snapshot'
$previousJava = $env:JAVA_HOME
$previousPath = $env:PATH
$previousEncoding = $env:PYTHONIOENCODING
$previousLocation = Get-Location
try {
    $env:JAVA_HOME = $javaPath
    $env:PATH = (Join-Path $javaPath 'bin') + ';' + $previousPath
    $env:PYTHONIOENCODING = 'utf-8'
    if ($Rebuild) {
        Set-Location -LiteralPath $skillPath
        & node (Join-Path $skillPath 'scripts\run-build.mjs') (Join-Path $PSScriptRoot 'source\build-r05.mjs') '--out-dir' $PSScriptRoot '--name' 'room' '--data-version' '3465'
        if ($LASTEXITCODE -ne 0) { throw 'R05 build failed' }
    }
    & $pythonPath (Join-Path $PSScriptRoot 'source\static_qa.py')
    if ($LASTEXITCODE -ne 0) { throw 'R05 static QA failed' }
    Set-Location -LiteralPath (Join-Path $PSScriptRoot 'native-test')
    $nativeLog = Join-Path $PSScriptRoot ('qa\forge-gametest-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '.log')
    & (Join-Path $javaPath 'bin\java.exe') '-classpath' 'gradle\wrapper\gradle-wrapper.jar' 'org.gradle.wrapper.GradleWrapperMain' 'runGameTestServer' '--offline' '-Dnet.minecraftforge.gradle.check.certs=false' '--console=plain' 2>&1 | Tee-Object -FilePath $nativeLog
    if ($LASTEXITCODE -ne 0) { throw 'R05 native Forge QA failed' }
} finally {
    Set-Location -LiteralPath $previousLocation.Path
    $env:JAVA_HOME = $previousJava
    $env:PATH = $previousPath
    $env:PYTHONIOENCODING = $previousEncoding
}
