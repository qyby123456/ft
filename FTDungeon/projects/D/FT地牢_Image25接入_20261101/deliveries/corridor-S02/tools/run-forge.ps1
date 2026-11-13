param([string]$RunId = (Get-Date -Format 'yyyyMMdd-HHmmss'))
$ErrorActionPreference = 'Stop'
$Root = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
if ($RunId -notmatch '^[a-zA-Z0-9-]+$') { throw 'RunId must not contain a path.' }
$JavaHome = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
$Gradle = (Get-ChildItem -Path 'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\*\gradle-8.8\bin\gradle.bat' | Select-Object -First 1).FullName
if (-not (Test-Path -LiteralPath (Join-Path $JavaHome 'bin\java.exe')) -or -not $Gradle) { throw 'Cached Java 17 and Gradle 8.8 are required.' }
$OldJavaHome = $env:JAVA_HOME
$OldPath = $env:PATH
try {
    $env:JAVA_HOME = $JavaHome
    $env:PATH = (Join-Path $JavaHome 'bin') + ';' + $env:PATH
    Push-Location -LiteralPath $Root
    try {
        & $Gradle runGameTestServer --offline --console=plain '-Dnet.minecraftforge.gradle.check.certs=false' "-PqaRun=$RunId" 2>&1 | Tee-Object -FilePath (Join-Path $Root "qa\forge-$RunId.log")
        if ($LASTEXITCODE -ne 0) { throw "Forge GameTest failed with code $LASTEXITCODE; inspect qa/forge-$RunId.log" }
    } finally {
        Pop-Location
    }
} finally {
    $env:JAVA_HOME = $OldJavaHome
    $env:PATH = $OldPath
}
