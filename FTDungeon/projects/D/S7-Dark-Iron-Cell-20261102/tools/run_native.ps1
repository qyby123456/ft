$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$javaHome = Join-Path $env:APPDATA '.minecraft\runtime\java-runtime-gamma-snapshot'
$gradle = 'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.12-bin\cetblhg4pflnnks72fxwobvgv\gradle-8.12\bin\gradle.bat'
$previousJavaHome = $env:JAVA_HOME
$previousPath = $env:PATH
try {
    $env:JAVA_HOME = $javaHome
    $env:PATH = (Join-Path $javaHome 'bin') + ';' + $previousPath
    foreach ($view in @('north', 'east', 'south', 'west')) {
        $report = Join-Path $root ('qa\native-' + $view + '.json')
        if (Test-Path -LiteralPath $report) { Remove-Item -LiteralPath $report }
    }
    Push-Location -LiteralPath $root
    try {
        & $gradle 'runGameTestServer' '--offline' '-Dnet.minecraftforge.gradle.check.certs=false' '--console=plain' 2>&1 | Tee-Object -FilePath (Join-Path $root 'qa\forge-gametest.log')
        if ($LASTEXITCODE -ne 0) { throw "Isolated Forge GameTest failed: $LASTEXITCODE" }
        foreach ($view in @('north', 'east', 'south', 'west')) {
            if (-not (Test-Path -LiteralPath (Join-Path $root ('qa\native-' + $view + '.json')))) { throw "Native report missing: $view" }
        }
    } finally {
        Pop-Location
    }
} finally {
    $env:JAVA_HOME = $previousJavaHome
    $env:PATH = $previousPath
}
