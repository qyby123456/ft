$ErrorActionPreference = 'Stop'
$deliveryRoot = Split-Path $PSScriptRoot -Parent
$runtimePath = Join-Path $env:APPDATA '.minecraft\runtime\java-runtime-gamma-snapshot'
$launcherPath = Join-Path $env:USERPROFILE '.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\lib\gradle-launcher-8.8.jar'
$previousJavaHome = $env:JAVA_HOME
$previousPath = $env:PATH
try {
    $env:JAVA_HOME = $runtimePath
    $env:PATH = (Join-Path $runtimePath 'bin') + ';' + $previousPath
    Push-Location (Join-Path $deliveryRoot 'forge')
    try {
        & (Join-Path $runtimePath 'bin\java.exe') '-classpath' $launcherPath 'org.gradle.launcher.GradleMain' 'runGameTestServer' '--offline' '--no-daemon' '--max-workers=2' '--console=plain' '-Dnet.minecraftforge.gradle.check.certs=false' 2>&1 | Tee-Object -FilePath (Join-Path $deliveryRoot 'qa\forge-run.log')
        if ($LASTEXITCODE -ne 0) { throw "C04 isolated Forge QA failed: $LASTEXITCODE" }
    } finally { Pop-Location }
} finally {
    $env:JAVA_HOME = $previousJavaHome
    $env:PATH = $previousPath
}
