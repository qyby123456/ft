param([string]$Task = 'build', [switch]$Offline)
$ErrorActionPreference = 'Stop'
$previousJavaHome = $env:JAVA_HOME
$previousPath = $env:PATH
try {
    $javaHome = Join-Path $env:APPDATA '.minecraft\runtime\java-runtime-gamma-snapshot'
    if ($env:JAVA_HOME_17) { $javaHome = $env:JAVA_HOME_17 }
    if (-not (Test-Path -LiteralPath (Join-Path $javaHome 'bin\javac.exe'))) {
        throw 'JDK 17 not found. Set JAVA_HOME_17 to a JDK 17 directory.'
    }
    $env:JAVA_HOME = $javaHome
    $env:PATH = (Join-Path $javaHome 'bin') + ';' + $previousPath
    Push-Location $PSScriptRoot
    try {
        $gradleArguments = @($Task, '--console=plain')
        if ($Offline) { $gradleArguments += @('--offline', '-Dnet.minecraftforge.gradle.check.certs=false') }
        & (Join-Path $javaHome 'bin\java.exe') '-classpath' 'gradle\wrapper\gradle-wrapper.jar' 'org.gradle.wrapper.GradleWrapperMain' @gradleArguments
        if ($LASTEXITCODE -ne 0) { throw "Gradle failed: $LASTEXITCODE" }
    } finally {
        Pop-Location
    }
} finally {
    $env:JAVA_HOME = $previousJavaHome
    $env:PATH = $previousPath
}
