param([string]$Task = 'build', [switch]$Offline)
$ErrorActionPreference = 'Stop'
$previousJavaHome = $env:JAVA_HOME
$previousPath = $env:PATH
try {
    $javaHome = Join-Path $env:APPDATA '.minecraft\runtime\java-runtime-gamma-snapshot'
    if ($env:JAVA_HOME_17) { $javaHome = $env:JAVA_HOME_17 }
    if (-not (Test-Path -LiteralPath (Join-Path $javaHome 'bin\javac.exe')) -and
        -not (Test-Path -LiteralPath (Join-Path $javaHome 'lib\modules'))) {
        throw 'Set JAVA_HOME_17 to a Java 17 runtime containing the compiler module.'
    }
    $env:JAVA_HOME = $javaHome
    $env:PATH = (Join-Path $javaHome 'bin') + ';' + $previousPath
    Push-Location $PSScriptRoot
    try {
        $arguments = @($Task, '--console=plain')
        if ($Offline) { $arguments += @('--offline', '-Dnet.minecraftforge.gradle.check.certs=false') }
        & (Join-Path $javaHome 'bin\java.exe') '-classpath' 'gradle\wrapper\gradle-wrapper.jar' 'org.gradle.wrapper.GradleWrapperMain' @arguments
        if ($LASTEXITCODE -ne 0) { throw "Gradle failed: $LASTEXITCODE" }
    } finally { Pop-Location }
} finally {
    $env:JAVA_HOME = $previousJavaHome
    $env:PATH = $previousPath
}
