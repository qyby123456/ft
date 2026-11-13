$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$javaHome = Join-Path $env:APPDATA '.minecraft\runtime\java-runtime-gamma-snapshot'
$launcher = Join-Path $env:USERPROFILE '.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\lib\gradle-launcher-8.8.jar'
$oldJavaHome = $env:JAVA_HOME
$oldPath = $env:PATH
try {
    $env:JAVA_HOME = $javaHome
    $env:PATH = (Join-Path $javaHome 'bin') + ';' + $oldPath
    Push-Location (Join-Path $root 'forge')
    try {
        & (Join-Path $javaHome 'bin\java.exe') '-classpath' $launcher 'org.gradle.launcher.GradleMain' 'runGameTestServer' '--offline' '--no-daemon' '--console=plain' '-Dnet.minecraftforge.gradle.check.certs=false' 2>&1 | Tee-Object -FilePath (Join-Path $root 'qa\forge-run.log')
        if ($LASTEXITCODE -ne 0) { throw "Isolated S09 Forge QA failed: $LASTEXITCODE" }
    } finally { Pop-Location }
} finally {
    $env:JAVA_HOME = $oldJavaHome
    $env:PATH = $oldPath
}
