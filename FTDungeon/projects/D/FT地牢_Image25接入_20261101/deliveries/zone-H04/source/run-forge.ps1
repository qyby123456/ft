$ErrorActionPreference = 'Stop'
$deliveryRoot = Split-Path $PSScriptRoot -Parent
if ((Split-Path $deliveryRoot -Leaf) -ne 'zone-H04') { throw 'Unexpected H04 QA root' }
$javaRuntime = Join-Path $env:APPDATA '.minecraft\runtime\java-runtime-gamma-snapshot'
$gradleRuntime = 'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\bin\gradle.bat'
$previousJava = $env:JAVA_HOME
$previousPath = $env:PATH
try {
    $env:JAVA_HOME = $javaRuntime
    $env:PATH = (Join-Path $javaRuntime 'bin') + ';' + $previousPath
    $stamp = Get-Date -Format 'yyyyMMdd-HHmmss-fff'
    $fixtureRoot = Join-Path $deliveryRoot ('forge\run-isolated-' + $stamp)
    New-Item -ItemType Directory -Path $fixtureRoot | Out-Null
    @('level-type=minecraft:flat', 'generate-structures=false', 'spawn-animals=false', 'spawn-monsters=false', 'spawn-npcs=false', 'view-distance=2', 'simulation-distance=2', 'online-mode=false', 'max-tick-time=120000') | Set-Content -LiteralPath (Join-Path $fixtureRoot 'server.properties') -Encoding ascii
    Push-Location (Join-Path $deliveryRoot 'forge')
    try {
        & $gradleRuntime '--offline' '--no-daemon' '--max-workers=1' '--console=plain' '-Dnet.minecraftforge.gradle.check.certs=false' ('-Ph04RunDir=run-isolated-' + $stamp) 'runGameTestServer' 2>&1 | Tee-Object -FilePath (Join-Path $deliveryRoot ('qa\forge-' + $stamp + '.log'))
        $result = $LASTEXITCODE
        if ($result -ne 0) { throw ('H04 Forge verification failed: ' + $result) }
    } finally { Pop-Location }
} finally {
    $env:JAVA_HOME = $previousJava
    $env:PATH = $previousPath
}
