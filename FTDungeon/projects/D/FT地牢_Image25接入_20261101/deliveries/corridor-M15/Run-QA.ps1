param([switch]$Rebuild)

$ErrorActionPreference = 'Stop'
$python = 'C:\Users\YOUR_USERNAME\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe'
$skill = 'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill'
$javaHome = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
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
        Set-Location -LiteralPath $skill
        & node 'scripts/run-build.mjs' (Join-Path $PSScriptRoot 'tools/build-m15.mjs') '--out-dir' $PSScriptRoot '--name' 'room' '--data-version' '3465' '--json' | Tee-Object -FilePath (Join-Path $PSScriptRoot 'evidence/builder-run.log')
        if ($LASTEXITCODE -ne 0) { throw 'M15 builder failed' }
    }
    & $python (Join-Path $PSScriptRoot 'tools/static-qa.py')
    if ($LASTEXITCODE -ne 0) { throw 'M15 static QA failed' }
    Set-Location -LiteralPath (Join-Path $PSScriptRoot 'native-test')
    $nativeLog = Join-Path $PSScriptRoot 'evidence/forge-gametest.log'
    if (Test-Path -LiteralPath $nativeLog) {
        Copy-Item -LiteralPath $nativeLog -Destination (Join-Path $PSScriptRoot ('evidence/forge-gametest-before-rerun-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '.log'))
    }
    & (Join-Path $javaHome 'bin/java.exe') '-XX:ActiveProcessorCount=2' '-Xmx256m' '-classpath' 'gradle/wrapper/gradle-wrapper.jar' 'org.gradle.wrapper.GradleWrapperMain' 'runGameTestServer' '--offline' '-Dnet.minecraftforge.gradle.check.certs=false' '--console=plain' '--no-daemon' '--max-workers=2' 2>&1 | Tee-Object -FilePath $nativeLog
    if ($LASTEXITCODE -ne 0) { throw 'M15 native Forge QA failed' }
    Set-Location -LiteralPath $PSScriptRoot
    & node (Join-Path $PSScriptRoot 'tools/render-views.mjs')
    if ($LASTEXITCODE -ne 0) { throw 'M15 Lodestone views failed' }
    & $python (Join-Path $PSScriptRoot 'tools/finalize.py')
    if ($LASTEXITCODE -ne 0) { throw 'M15 finalization failed' }
} finally {
    Set-Location -LiteralPath $previousLocation.Path
    $env:JAVA_HOME = $previousJavaHome
    $env:PATH = $previousPath
    $env:PYTHONIOENCODING = $previousEncoding
}
