$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$javaHome = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
$gradle = 'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\bin\gradle.bat'
$previousJavaHome = $env:JAVA_HOME
$previousPath = $env:PATH
try {
    $env:JAVA_HOME = $javaHome
    $env:PATH = (Join-Path $javaHome 'bin') + ';' + $previousPath
    Push-Location -LiteralPath (Join-Path $root 'forge')
    try {
        & $gradle runGameTestServer --offline --console=plain '-Dnet.minecraftforge.gradle.check.certs=false' 2>&1 | Tee-Object -FilePath (Join-Path $root 'qa\forge-gametest.log')
        if ($LASTEXITCODE -ne 0) { throw "Forge verification exited $LASTEXITCODE" }
    } finally { Pop-Location }
} finally {
    $env:JAVA_HOME = $previousJavaHome
    $env:PATH = $previousPath
}
