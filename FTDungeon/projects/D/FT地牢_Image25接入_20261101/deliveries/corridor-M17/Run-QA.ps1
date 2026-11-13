param([switch]$Rebuild)

$ErrorActionPreference = 'Stop'
$python = 'C:\Users\YOUR_USERNAME\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe'
$skill = 'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill'
$javaHome = Join-Path $env:APPDATA '.minecraft\runtime\java-runtime-gamma-snapshot'
$previousJava = $env:JAVA_HOME
$previousPath = $env:PATH
$previousEncoding = $env:PYTHONIOENCODING
$previousLocation = Get-Location
try {
    $env:JAVA_HOME = $javaHome
    $env:PATH = (Join-Path $javaHome 'bin') + ';' + $previousPath
    $env:PYTHONIOENCODING = 'utf-8'
    if ($Rebuild) {
        Set-Location -LiteralPath $skill
        & node (Join-Path $skill 'scripts\run-build.mjs') (Join-Path $PSScriptRoot 'tools\build-m17.mjs') '--out-dir' $PSScriptRoot '--name' 'room' '--data-version' '3465'
        if ($LASTEXITCODE -ne 0) { throw 'M17 NBT generation failed' }
    }
    & $python (Join-Path $PSScriptRoot 'tools\static_qa.py')
    if ($LASTEXITCODE -ne 0) { throw 'M17 static QA failed' }
    Set-Location -LiteralPath (Join-Path $PSScriptRoot 'native-test')
    $nativeLog = Join-Path $PSScriptRoot ('evidence\forge-gametest-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '.log')
    & (Join-Path $javaHome 'bin\java.exe') '-classpath' 'gradle\wrapper\gradle-wrapper.jar' 'org.gradle.wrapper.GradleWrapperMain' 'runGameTestServer' '--offline' '-Dnet.minecraftforge.gradle.check.certs=false' '--console=plain' 2>&1 | Tee-Object -FilePath $nativeLog
    if ($LASTEXITCODE -ne 0) { throw 'M17 native Forge QA failed' }
} finally {
    Set-Location -LiteralPath $previousLocation.Path
    $env:JAVA_HOME = $previousJava
    $env:PATH = $previousPath
    $env:PYTHONIOENCODING = $previousEncoding
}
