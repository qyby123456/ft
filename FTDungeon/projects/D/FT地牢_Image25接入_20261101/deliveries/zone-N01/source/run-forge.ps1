$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
if ((Split-Path $root -Leaf) -ne 'zone-N01') { throw 'Unexpected N01 QA root' }
$javaHome = Join-Path $env:APPDATA '.minecraft\runtime\java-runtime-gamma-snapshot'
$gradle = 'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\bin\gradle.bat'
$oldJavaHome = $env:JAVA_HOME
$oldPath = $env:PATH
try {
    $env:JAVA_HOME = $javaHome
    $env:PATH = (Join-Path $javaHome 'bin') + ';' + $oldPath
    Push-Location (Join-Path $root 'forge')
    try {
        $stamp = Get-Date -Format 'yyyyMMdd-HHmmss-fff'
        $runDirectory = Join-Path $root "forge\run-$stamp"
        New-Item -ItemType Directory -Path $runDirectory | Out-Null
        @('level-type=minecraft:flat', 'generate-structures=false', 'spawn-animals=false', 'spawn-monsters=false', 'spawn-npcs=false', 'view-distance=2', 'simulation-distance=2', 'online-mode=false', 'max-tick-time=60000') | Set-Content -LiteralPath (Join-Path $runDirectory 'server.properties') -Encoding ascii
        $log = Join-Path $root "qa\forge-$stamp.log"
        & $gradle '--offline' '--no-daemon' '--max-workers=1' '--console=plain' '-Dnet.minecraftforge.gradle.check.certs=false' "-Pn01RunDir=run-$stamp" 'runGameTestServer' 2>&1 | Tee-Object -FilePath $log
        $exitCode = $LASTEXITCODE
        Copy-Item -LiteralPath $log -Destination (Join-Path $root 'qa\forge-run.log')
        if ($exitCode -ne 0) { throw "N01 isolated Forge QA failed: $exitCode" }
    } finally { Pop-Location }
} finally {
    $env:JAVA_HOME = $oldJavaHome
    $env:PATH = $oldPath
}
