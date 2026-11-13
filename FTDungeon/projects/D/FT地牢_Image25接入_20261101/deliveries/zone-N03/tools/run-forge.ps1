$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$javaHome = 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/runtime/java-runtime-gamma-snapshot'
$gradle = Get-ChildItem -LiteralPath 'C:/Users/YOUR_USERNAME/.gradle/wrapper/dists/gradle-8.8-bin' -Filter gradle.bat -Recurse | Select-Object -First 1 -ExpandProperty FullName
if (-not $gradle -or -not (Test-Path -LiteralPath "$javaHome/bin/java.exe")) { throw 'Java 17 and Gradle 8.8 required.' }
$previousJava = $env:JAVA_HOME
$previousPath = $env:PATH
try {
    $env:JAVA_HOME = $javaHome
    $env:PATH = "$javaHome/bin;$previousPath"
    Push-Location -LiteralPath (Join-Path $root 'forge')
    try {
        & $gradle runGameTestServer --offline --console=plain '-Dnet.minecraftforge.gradle.check.certs=false' *> (Join-Path $root 'qa/forge-gametest.log')
        $result = $LASTEXITCODE
    } finally { Pop-Location }
} finally {
    $env:JAVA_HOME = $previousJava
    $env:PATH = $previousPath
}
Get-Content -LiteralPath (Join-Path $root 'qa/forge-gametest.log') -Tail 70
exit $result
