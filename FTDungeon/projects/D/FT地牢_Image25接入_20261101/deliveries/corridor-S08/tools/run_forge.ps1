$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$gradle = Get-ChildItem -Path 'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\*\gradle-8.8\bin\gradle.bat' | Select-Object -First 1 -ExpandProperty FullName
$previousJavaHome = $env:JAVA_HOME
$previousPath = $env:PATH
$log = Join-Path $root 'qa/forge-run.log'
if (Test-Path -LiteralPath $log) {
    $stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
    Copy-Item -LiteralPath $log -Destination (Join-Path $root "qa/forge-run-$stamp.log")
}
try {
    $env:JAVA_HOME = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
    $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
    Push-Location -LiteralPath (Join-Path $root 'native')
    try {
        & $gradle runGameTestServer --no-daemon --max-workers=2 --console=plain --offline '-Dorg.gradle.jvmargs=-Xmx768m' '-Dnet.minecraftforge.gradle.check.certs=false' *> (Join-Path $root 'qa/forge-run.log')
        $result = $LASTEXITCODE
    } finally {
        Pop-Location
    }
    Get-Content -LiteralPath (Join-Path $root 'qa/forge-run.log') -Tail 65
    exit $result
} finally {
    $env:JAVA_HOME = $previousJavaHome
    $env:PATH = $previousPath
}
