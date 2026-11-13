$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
$javaHome = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
$gradle = Get-ChildItem -LiteralPath "$env:USERPROFILE\.gradle\wrapper\dists\gradle-8.8-bin" -Filter gradle.bat -Recurse -File | Select-Object -First 1 -ExpandProperty FullName
if (-not $gradle -or -not (Test-Path -LiteralPath "$javaHome\bin\java.exe")) { throw 'Java 17 and Gradle 8.8 are required.' }
$oldJava = $env:JAVA_HOME
$oldPath = $env:PATH
$stamp = [DateTimeOffset]::Now.ToString('yyyyMMdd-HHmmss')
foreach ($filename in @('forge-run.log','forge-native.json','forge-process.json')) {
    $previous = Join-Path $root "qa\$filename"
    if (Test-Path -LiteralPath $previous) { Copy-Item -LiteralPath $previous -Destination (Join-Path $root "qa\previous-$stamp-$filename") }
}
$started = [DateTimeOffset]::Now.ToString('o')
@{ status = 'STARTING'; started_at = $started; scope = 'isolated Forge GameTest only' } | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $root 'qa\forge-native.json') -Encoding utf8
try {
    $env:JAVA_HOME = $javaHome
    $env:PATH = "$javaHome\bin;$env:PATH"
    Push-Location -LiteralPath (Join-Path $root 'native')
    try {
        & $gradle runGameTestServer --offline --console=plain '-Dnet.minecraftforge.gradle.check.certs=false' *> (Join-Path $root 'qa\forge-run.log')
        $result = $LASTEXITCODE
        @{ started_at = $started; completed_at = [DateTimeOffset]::Now.ToString('o'); exit_code = $result; scope = 'isolated Forge GameTest only' } | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $root 'qa\forge-process.json') -Encoding utf8
        Get-Content -LiteralPath (Join-Path $root 'qa\forge-run.log') -Tail 65
        if ($result -ne 0) { throw "Forge isolated validation failed: $result" }
        $nativeReport = Get-Content -LiteralPath (Join-Path $root 'qa\forge-native.json') -Raw | ConvertFrom-Json
        $fingerprint = (Get-FileHash -Algorithm SHA256 -LiteralPath (Join-Path $root 'room.nbt')).Hash.ToLowerInvariant()
        if ($nativeReport.status -ne 'PASS' -or $nativeReport.structure_sha256 -ne $fingerprint) { throw 'Process exit zero without a current matching native PASS is not successful verification.' }
    } finally { Pop-Location }
} finally {
    $env:JAVA_HOME = $oldJava
    $env:PATH = $oldPath
}
