$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$gradle = 'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\bin\gradle.bat'
$previousJava = $env:JAVA_HOME
try {
    $env:JAVA_HOME = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
    Push-Location -LiteralPath (Join-Path $root 'forge')
    try {
        & $gradle --offline --no-daemon --max-workers=2 '-Dnet.minecraftforge.gradle.check.certs=false' runGameTestServer 2>&1 | Tee-Object -FilePath (Join-Path $root 'qa\forge-run.log')
        if ($LASTEXITCODE -ne 0) { throw "Forge GameTest failed: $LASTEXITCODE" }
    } finally {
        Pop-Location
    }
} finally {
    $env:JAVA_HOME = $previousJava
}
