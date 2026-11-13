$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
$javaRuntime = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
$gradle = Get-ChildItem -LiteralPath "$env:USERPROFILE\.gradle\wrapper\dists\gradle-8.8-bin" -Filter gradle.bat -Recurse -File | Select-Object -First 1 -ExpandProperty FullName
if (-not $gradle -or -not (Test-Path -LiteralPath "$javaRuntime\bin\java.exe")) { throw 'Java 17 and Gradle 8.8 required.' }
$oldJava = $env:JAVA_HOME
$oldPath = $env:PATH
$started = [DateTimeOffset]::Now.ToString('o')
$stamp = [DateTimeOffset]::Now.ToString('yyyyMMdd-HHmmss')
$fixtureWorld = Join-Path $root 'qa\forge-run\world'
if (Test-Path -LiteralPath $fixtureWorld) {
    $resolvedRoot = [IO.Path]::GetFullPath($root).TrimEnd('\') + '\'
    $resolvedWorld = (Resolve-Path -LiteralPath $fixtureWorld).Path
    $archiveDirectory = Join-Path $root 'qa\fixture-history'
    $archiveWorld = [IO.Path]::GetFullPath((Join-Path $archiveDirectory "$stamp-world"))
    if (-not $resolvedWorld.StartsWith($resolvedRoot, [StringComparison]::OrdinalIgnoreCase) -or -not $archiveWorld.StartsWith($resolvedRoot, [StringComparison]::OrdinalIgnoreCase)) { throw 'Fixture archive path escaped zone-R02.' }
    New-Item -ItemType Directory -Path $archiveDirectory -Force | Out-Null
    Move-Item -LiteralPath $resolvedWorld -Destination $archiveWorld
}
foreach ($filename in @('forge-run.log','forge-native.json','forge-process.json')) {
    $previous = Join-Path $root "qa\$filename"
    if (Test-Path -LiteralPath $previous) { Copy-Item -LiteralPath $previous -Destination (Join-Path $root "qa\previous-$stamp-$filename") }
}
@{ status = 'STARTING'; started_at = $started; scope = 'Isolated R02 fixture only' } | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $root 'qa\forge-native.json') -Encoding utf8
try {
    $env:JAVA_HOME = $javaRuntime
    $env:PATH = "$javaRuntime\bin;$env:PATH"
    Push-Location -LiteralPath (Join-Path $root 'native')
    try {
        & $gradle runGameTestServer --offline --console=plain '-Dnet.minecraftforge.gradle.check.certs=false' *> (Join-Path $root 'qa\forge-run.log')
        $result = $LASTEXITCODE
        @{ started_at = $started; completed_at = [DateTimeOffset]::Now.ToString('o'); exit_code = $result; scope = 'Isolated headless Forge GameTest only' } | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $root 'qa\forge-process.json') -Encoding utf8
        Get-Content -LiteralPath (Join-Path $root 'qa\forge-run.log') -Tail 45
        if ($result -ne 0) { throw "Forge isolated validation failed: $result" }
        $report = Get-Content -LiteralPath (Join-Path $root 'qa\forge-native.json') -Raw | ConvertFrom-Json
        $fingerprint = (Get-FileHash -Algorithm SHA256 -LiteralPath (Join-Path $root 'room.nbt')).Hash.ToLowerInvariant()
        if ($report.status -ne 'PASS' -or $report.structure_sha256 -ne $fingerprint) { throw 'A current matching native PASS is required in addition to process success.' }
    } finally { Pop-Location }
} finally {
    $env:JAVA_HOME = $oldJava
    $env:PATH = $oldPath
}
