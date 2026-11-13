$ErrorActionPreference = 'Stop'
$zoneRoot = Split-Path -Parent $PSScriptRoot
$javaRoot = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
$gradlePath = 'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\bin\gradle.bat'
$savedJavaHome = $env:JAVA_HOME
$savedPath = $env:PATH
try {
    $env:JAVA_HOME = $javaRoot
    $env:PATH = (Join-Path $javaRoot 'bin') + ';' + $savedPath
    Push-Location -LiteralPath (Join-Path $zoneRoot 'forge')
    try {
        & $gradlePath runGameTestServer --offline --console=plain '-Dnet.minecraftforge.gradle.check.certs=false' 2>&1 | Tee-Object -FilePath (Join-Path $zoneRoot 'qa/forge-gametest.log')
        if ($LASTEXITCODE -ne 0) { throw "Forge verification exited $LASTEXITCODE" }
    } finally { Pop-Location }
} finally {
    $env:JAVA_HOME = $savedJavaHome
    $env:PATH = $savedPath
}
