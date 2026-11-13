$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$javaHome = Join-Path $env:APPDATA '.minecraft\runtime\java-runtime-gamma-snapshot'
$gradle = 'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\bin\gradle.bat'
$oldJavaHome = $env:JAVA_HOME
$oldPath = $env:PATH
try {
    $env:JAVA_HOME = $javaHome
    $env:PATH = (Join-Path $javaHome 'bin') + ';' + $oldPath
    Push-Location $root
    try {
        $stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
        & $gradle '--offline' '--no-daemon' '--console=plain' '-Dnet.minecraftforge.gradle.check.certs=false' "-Pm10RunDir=qa/forge-runs/$stamp" 'runGameTestServer' 2>&1 | Tee-Object -FilePath (Join-Path $root "qa\gametest-$stamp.log")
        if ($LASTEXITCODE -ne 0) { throw "M10 isolated Forge QA failed: $LASTEXITCODE" }
    } finally { Pop-Location }
} finally {
    $env:JAVA_HOME = $oldJavaHome
    $env:PATH = $oldPath
}
