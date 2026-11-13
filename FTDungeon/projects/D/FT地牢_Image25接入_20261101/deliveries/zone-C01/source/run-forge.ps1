$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$javaHome = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
$launcher = 'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\lib\gradle-launcher-8.8.jar'
$previousJavaHome = $env:JAVA_HOME
$previousPath = $env:PATH
try {
    $env:JAVA_HOME = $javaHome
    $env:PATH = (Join-Path $javaHome 'bin') + ';' + $previousPath
    Push-Location -LiteralPath (Join-Path $root 'native')
    try {
        & (Join-Path $javaHome 'bin/java.exe') '-classpath' $launcher 'org.gradle.launcher.GradleMain' 'runGameTestServer' '--offline' '--no-daemon' '--max-workers=2' '--console=plain' '-Dorg.gradle.jvmargs=-Xmx768m' '-Dnet.minecraftforge.gradle.check.certs=false' *> (Join-Path $root 'qa/forge-run.log')
        $result = $LASTEXITCODE
    } finally { Pop-Location }
    Get-Content -LiteralPath (Join-Path $root 'qa/forge-run.log') -Tail 70
    exit $result
} finally {
    $env:JAVA_HOME = $previousJavaHome
    $env:PATH = $previousPath
}
