$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
if ((Split-Path $root -Leaf) -ne 'zone-H02') { throw 'Unexpected QA project root' }
$javaHome = Join-Path $env:APPDATA '.minecraft\runtime\java-runtime-gamma-snapshot'
$gradle = 'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\bin\gradle.bat'
$oldJavaHome = $env:JAVA_HOME
$oldPath = $env:PATH
try {
    $env:JAVA_HOME = $javaHome
    $env:PATH = (Join-Path $javaHome 'bin') + ';' + $oldPath
    Push-Location $root
    try {
        $stamp = Get-Date -Format 'yyyyMMdd-HHmmss-fff'
        $runDirectory = Join-Path $root "qa\forge-runs\$stamp"
        New-Item -ItemType Directory -Path $runDirectory -Force | Out-Null
        @('level-type=minecraft:flat', 'generate-structures=false', 'spawn-animals=false', 'spawn-monsters=false', 'spawn-npcs=false', 'view-distance=2', 'simulation-distance=2', 'online-mode=false', 'max-tick-time=60000') | Set-Content -LiteralPath (Join-Path $runDirectory 'server.properties') -Encoding ascii
        & $gradle '--offline' '--no-daemon' '--max-workers=1' '--console=plain' '-Dnet.minecraftforge.gradle.check.certs=false' "-PqaRun=$stamp" 'runGameTestServer' 2>&1 | Tee-Object -FilePath (Join-Path $root "qa\forge-$stamp.log")
        if ($LASTEXITCODE -ne 0) { throw "Isolated H02 Forge QA failed: $LASTEXITCODE" }
    } finally { Pop-Location }
} finally {
    $env:JAVA_HOME = $oldJavaHome
    $env:PATH = $oldPath
}
