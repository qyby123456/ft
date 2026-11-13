$ErrorActionPreference = 'Stop'
$root = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$gradle = 'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\bin\gradle.bat'
$javaHome = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
$runFolder = Join-Path $root ('qa\forge-run-' + (Get-Date -Format 'yyyyMMdd-HHmmssfff'))
if (-not $runFolder.StartsWith($root + '\', [StringComparison]::OrdinalIgnoreCase)) { throw 'Run folder outside S01 workspace' }
New-Item -ItemType Directory -Path $runFolder -Force | Out-Null
$previousJavaHome = $env:JAVA_HOME
$result = 1
Push-Location -LiteralPath $root
try {
    $env:JAVA_HOME = $javaHome
    & $gradle --offline --no-daemon '-Dnet.minecraftforge.gradle.check.certs=false' "-PverificationRunDirectory=$runFolder" runGameTestServer 2>&1 | Tee-Object -FilePath (Join-Path $root 'qa\gametest.log')
    $result = $LASTEXITCODE
} finally {
    $env:JAVA_HOME = $previousJavaHome
    Pop-Location
}
exit $result
