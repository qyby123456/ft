$ErrorActionPreference = 'Stop'
$taskRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$gradle = 'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\bin\gradle.bat'
$taskJava = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
$runFolder = Join-Path $taskRoot ('qa\forge-run-' + (Get-Date -Format 'yyyyMMdd-HHmmssfff'))
if (-not $runFolder.StartsWith($taskRoot + '\', [StringComparison]::OrdinalIgnoreCase)) { throw 'Fixture outside A02 workspace' }
New-Item -ItemType Directory -Path $runFolder -Force | Out-Null
[IO.File]::WriteAllText((Join-Path $runFolder "server.properties"), "level-type=minecraft:flat`ngenerate-structures=false`nview-distance=2`nsimulation-distance=2`nspawn-protection=0`n")
$previousJavaHome = $env:JAVA_HOME
$result = 1
Push-Location -LiteralPath (Join-Path $taskRoot 'native')
try {
    $env:JAVA_HOME = $taskJava
    & $gradle --offline --no-daemon '-Dnet.minecraftforge.gradle.check.certs=false' "-PverificationRunDirectory=$runFolder" runGameTestServer 2>&1 | Tee-Object -FilePath (Join-Path $taskRoot ('qa\gametest-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '.log'))
    $result = $LASTEXITCODE
} finally {
    $env:JAVA_HOME = $previousJavaHome
    Pop-Location
}
exit $result

