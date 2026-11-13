$ErrorActionPreference = 'Stop'
$root = (Resolve-Path -LiteralPath (Split-Path $PSScriptRoot -Parent)).Path
if ((Split-Path $root -Leaf) -ne 'corridor-M08') { throw 'Unexpected QA project root' }
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
        $run = Join-Path $root "qa\forge-runs\$stamp"
        New-Item -ItemType Directory -Path $run -Force | Out-Null
        $properties = @(
            'level-type=minecraft\:flat',
            'generator-settings={"biome":"minecraft:the_void","layers":[{"block":"minecraft:bedrock","height":1}]}',
            'generate-structures=false',
            'view-distance=2',
            'simulation-distance=2',
            'max-tick-time=-1'
        )
        Set-Content -LiteralPath (Join-Path $run 'server.properties') -Value $properties -Encoding utf8
        & $gradle '--offline' '--no-daemon' '--console=plain' '-Dnet.minecraftforge.gradle.check.certs=false' "-Pm08RunDir=qa/forge-runs/$stamp" 'runGameTestServer' 2>&1 | Tee-Object -FilePath (Join-Path $root "qa\gametest-$stamp.log")
        if ($LASTEXITCODE -ne 0) { throw "M08 isolated Forge QA failed: $LASTEXITCODE" }
    } finally { Pop-Location }
} finally {
    $env:JAVA_HOME = $oldJavaHome
    $env:PATH = $oldPath
}
