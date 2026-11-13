param([switch]$Rebuild)

$ErrorActionPreference = 'Stop'
$python = 'C:\Users\YOUR_USERNAME\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe'
$skill = 'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill'
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
        Set-Location -LiteralPath $skill
        & node 'scripts/run-build.mjs' (Join-Path $PSScriptRoot 'tools\corridor-s03.mjs') '--out-dir' $PSScriptRoot '--name' 'room' '--data-version' '3465' '--json'
        if ($LASTEXITCODE -ne 0) { throw 'S03 structure generation failed' }
    }
    & $python (Join-Path $PSScriptRoot 'tools\qa_static.py')
    if ($LASTEXITCODE -ne 0) { throw 'S03 static QA failed' }
    Set-Location -LiteralPath (Join-Path $PSScriptRoot 'native-test')
    & (Join-Path $javaHome 'bin\java.exe') '-classpath' 'gradle\wrapper\gradle-wrapper.jar' 'org.gradle.wrapper.GradleWrapperMain' 'runGameTestServer' '--offline' '-Dnet.minecraftforge.gradle.check.certs=false' '--console=plain' 2>&1 | Tee-Object -FilePath (Join-Path $PSScriptRoot 'qa\forge-gametest.log')
    if ($LASTEXITCODE -ne 0) { throw 'S03 Forge native QA failed' }
    Write-Output 'Static and isolated native QA passed. Use the installed Builder preview and tools/render_views.cjs to refresh images after any NBT change, then visually review before packaging.'
} finally {
    Set-Location -LiteralPath $previousLocation.Path
    $env:JAVA_HOME = $previousJavaHome
    $env:PATH = $previousPath
    $env:PYTHONIOENCODING = $previousEncoding
}
